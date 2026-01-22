/**
 * The User Registry acts as the central data management layer for the system.
 * It stores all {@link Customer} and {@link Freelancer} entities, maintains indexing
 * maps for fast lookup, and organizes freelancers into specialized {@link AVLTree} structures
 * based on their service type for efficient ranking and Top K retrieval.
 */
public class UserRegistry {
    public CuckooHashTable typeMap;
    public CuckooHashTable indexMap;
    public Customer[] customers;
    public Freelancer[] freelancers;
    public int customerCount, freelancerCount;
    public AVLTree[] serviceTypeFreelancers;

    /**
     * Initializes the registry, allocating storage arrays and initializing hash tables and AVL Trees.
     * Creates one AVL Tree for every predefined service type.
     * @param maxCustomers The maximum number of customers the system can hold.
     * @param maxFreelancers The maximum number of freelancers the system can hold.
     */
    public UserRegistry(int maxCustomers, int maxFreelancers) {
        typeMap = new CuckooHashTable();
        indexMap = new CuckooHashTable();
        customers = new Customer[maxCustomers];
        freelancers = new Freelancer[maxFreelancers];
        customerCount = 0;
        freelancerCount = 0;

        // Initialize AVL Trees for all service types
        serviceTypeFreelancers = new AVLTree[ServiceTypeManager.serviceNames.length];
        for(int i = 0; i < ServiceTypeManager.serviceNames.length; ++i) {
            serviceTypeFreelancers[i] = new AVLTree();
        }
    }

    /**
     * Retrieves the type of entity associated with the given ID (0 for Customer, 1 for Freelancer).
     * Looks up the ID in the {@code typeMap} hash table.
     * @param id The ID of the entity.
     * @return 0 if Customer, 1 if Freelancer, -1 if ID is not found.
     */
    public int getType(String id) {
        if (!typeMap.containsKey(id)) return -1;
        int v = typeMap.get(id);
        return v == 0 ? 0 : (v == 1 ? 1 : -1);
    }

    /**
     * Adds a new {@link Customer} to the registry.
     * Registers the ID and type in hash tables, stores the array index,
     * and adds the customer object to the {@code customers} array.
     * @param c The Customer object to add.
     * @return True if addition was successful (ID was new), false otherwise.
     */
    public boolean addCustomer(Customer c) {
        if (typeMap.containsKey(c.id)) return false;
        typeMap.put(c.id, 0);
        indexMap.put(c.id, customerCount);
        customers[customerCount++] = c;
        return true;
    }

    /**
     * Adds a new {@link Freelancer} to the registry and its corresponding AVL Tree.
     * Registers the freelancer ID, stores the object, calculates the
     * composite score, caches it in the object, and inserts a {@link FreelancerScoreEntry}
     * into the service-specific AVL Tree.
     * @param f The Freelancer object to add.
     * @return True if addition was successful, false otherwise.
     */
    public boolean addFreelancer(Freelancer f) {
        // 1. Check existence
        if (typeMap.containsKey(f.id)) return false;

        // 2. Register to the hastables
        typeMap.put(f.id, 1);
        indexMap.put(f.id, freelancerCount);

        // 3. Store object and set registry index
        freelancers[freelancerCount] = f;
        f.registryIndex = freelancerCount;

        // 4. Calculate score and cache
        int serviceIdx = ServiceTypeManager.getServiceIndex(f.serviceType);

        if(serviceIdx != -1) {
            int[] requiredSkills = ServiceTypeManager.getSkillProfile(f.serviceType);
            f.cachedCompositeScore = calculateCompositeScore(f, requiredSkills);

            // 5. Insert into AVL Tree
            FreelancerScoreEntry entry = new FreelancerScoreEntry(
                    f.registryIndex,
                    f.cachedCompositeScore,
                    f.id
            );
            serviceTypeFreelancers[serviceIdx].insert(entry);
        }

        // 6. Increment counter
        freelancerCount++;
        return true;
    }

    /**
     * Retrieves a {@link Customer} object using their ID.
     * @param id The ID of the customer.
     * @return The Customer object, or null if not found or if the ID belongs to a Freelancer.
     */
    public Customer getCustomer(String id) {
        if (getType(id) != 0) return null;
        int idx = indexMap.get(id);
        return customers[idx];
    }

    /**
     * Retrieves a {@link Freelancer} object using their ID.
     * @param id The ID of the freelancer.
     * @return The Freelancer object, or null if not found or if the ID belongs to a Customer.
     */
    public Freelancer getFreelancer(String id) {
        if (getType(id) != 1) return null;
        int idx = indexMap.get(id);
        return freelancers[idx];
    }

    /**
     * Updates a freelancer's service type, moving their ranking entry between two AVL Trees.
     * 1. Calculates old/new service indices.
     * 2. Removes the old entry from the old service's AVL Tree (O(log N)).
     * 3. Recalculates the composite score based on the new service's skill profile (O(1)).
     * 4. Inserts the new entry into the new service's AVL Tree (O(log N)).
     * @param f The freelancer whose service is being changed.
     * @param oldSrv The previous service type string.
     * @param newSrv The new service type string.
     */
    public void updateFreelancerServiceType(Freelancer f, String oldSrv, String newSrv) {
        int oldIdx = ServiceTypeManager.getServiceIndex(oldSrv);
        int newIdx = ServiceTypeManager.getServiceIndex(newSrv);
        if(oldIdx != -1 && newIdx != -1) {
            int fIdx = indexMap.get(f.id);

            // Remove old entry from old service AVL
            FreelancerScoreEntry oldEntry = new FreelancerScoreEntry(
                    fIdx, f.cachedCompositeScore, f.id
            );
            serviceTypeFreelancers[oldIdx].remove(oldEntry);

            // Calculate new score based on new service profile
            int[] newSkills = ServiceTypeManager.getSkillProfile(newSrv);
            f.cachedCompositeScore = calculateCompositeScore(f, newSkills);

            // Insert new entry into new service
            FreelancerScoreEntry newEntry = new FreelancerScoreEntry(
                    fIdx, f.cachedCompositeScore, f.id
            );
            serviceTypeFreelancers[newIdx].insert(newEntry);
        }
    }

    /**
     * Calculates the composite score for a freelancer based on their skills, rating,
     * reliability, and burnout status, using weighted averages and fixed penalties.
     * @param f The freelancer object.
     * @param requiredSkills The skill profile array for the freelancer's current service.
     * @return The calculated composite score, scaled by 10000 and floored to a long.
     */
    private long calculateCompositeScore(Freelancer f, int[] requiredSkills) {
        double ws = 0.55, wr = 0.25, wl = 0.20;
        int[] fs = f.skills;
        int dot = 0, sum = 0;
        // Calculate skill dot product and sum of required skills
        for (int i = 0; i < 5; i++) {
            dot += fs[i] * requiredSkills[i];
            sum += requiredSkills[i];
        }
        double skillScore = sum == 0 ? 0.0 : ((double) dot) / (100.0 * sum);

        double ratingScore = (f.ratingCount > 0) ? (f.averageRating / 5.0) : 0.0;
        // Calculate reliability based on cancellations
        int total = f.completedJobs + f.cancelledJobs;
        double reliabilityScore = (total == 0) ? 1.0 : 1.0 - ((double) f.cancelledJobs / total);
        // Apply burnout penalty
        double burnoutPenalty = f.burnout ? 0.45 : 0.0;
        // Final weighted score calculation
        double compositeScore = ws * skillScore + wr * ratingScore + wl * reliabilityScore - burnoutPenalty;
        // Scale and return the score
        return (long) Math.floor(10000.0 * compositeScore);
    }

    /**
     * Recalculates a freelancer's composite score and updates their position in the AVL Tree.
     * This is required after changes to skills, ratings, or burnout status.
     * 1. Removes the old score entry from the service AVL Tree (O(log N)).
     * 2. Recalculates the new composite score (O(1)).
     * 3. Inserts the new score entry back into the AVL Tree (O(log N)).
     * @param f The freelancer whose score needs updating.
     */
    public void updateFreelancerScore(Freelancer f) {
        int serviceIdx = ServiceTypeManager.getServiceIndex(f.serviceType);
        if (serviceIdx == -1) return;

        int fIdx = indexMap.get(f.id);

        // Remove old entry
        FreelancerScoreEntry oldEntry = new FreelancerScoreEntry(
                fIdx, f.cachedCompositeScore, f.id
        );
        serviceTypeFreelancers[serviceIdx].remove(oldEntry);

        // Calculate new score
        int[] requiredSkills = ServiceTypeManager.getSkillProfile(f.serviceType);
        f.cachedCompositeScore = calculateCompositeScore(f, requiredSkills);

        // Insert new entry
        FreelancerScoreEntry newEntry = new FreelancerScoreEntry(
                fIdx, f.cachedCompositeScore, f.id
        );
        serviceTypeFreelancers[serviceIdx].insert(newEntry);
    }
}