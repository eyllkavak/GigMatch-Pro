/**
 * The System Manager class, responsible for handling all high-level business
 * logic, command processing, and interactions between core entities (Freelancer, Customer)
 * and data structures (AVL Tree, Registry).
 */
import java.io.PrintWriter;
import java.util.LinkedList;

public class SystemManager {
    private UserRegistry registry;
    private PrintWriter out;

    /**
     * Constructs the SystemManager, initializing it with the necessary data access
     * layer (UserRegistry) and output handler (PrintWriter).
     * @param registry The central repository for all user data and data structures.
     * @param out The output stream for printing results and messages.
     */
    public SystemManager(UserRegistry registry, PrintWriter out) {
        this.registry = registry;
        this.out = out;
    }

    /**
     * Removes a freelancer from the system's active ranking structure (AVL Tree)
     * and flags them as platform blacklisted.
     * **Mechanism:** The freelancer's entry is removed from the AVL Tree corresponding
     * @param f The freelancer to be banned.
     */
    public  void banFreelancerPlatformWide(Freelancer f) {
        // Retrieve freelancer's index using ID map (O(1))
        int fIdx = registry.indexMap.get(f.id);

        // Create the entry key required for AVL removal (O(1))
        FreelancerScoreEntry entryToRemove = new FreelancerScoreEntry(
                fIdx,
                f.cachedCompositeScore,
                f.id
        );

        // Remove the entry from the service-specific AVL Tree (O(log N))
        int currentServiceIdx = ServiceTypeManager.getServiceIndex(f.serviceType);
        if (currentServiceIdx != -1) {
            registry.serviceTypeFreelancers[currentServiceIdx].remove(entryToRemove);
        }
    }
    /**
     * Calculates the final payment amount after applying the customer's loyalty discount.
     * Applies a fixed tiered subsidy (5%, 10%, 15%) based on the customer's current {@code loyaltyTier}.
     * @param c The customer receiving the service.
     * @param price The base price of the service.
     * @return The discounted price (integer, floored).
     */
    public int calculatePayment(Customer c, int price) {
        // Determine subsidy based on loyalty tier (O(1))
        double subsidy = 0.0;
        if (c.loyaltyTier == 1) subsidy = 0.05;
        else if (c.loyaltyTier == 2) subsidy = 0.10;
        else if (c.loyaltyTier == 3) subsidy = 0.15;
        // Calculate and floor the final discounted price (O(1))
        return (int) Math.floor(price * (1.0 - subsidy));
    }

    /**
     * Registers a new customer into the system registry.
     * @param p Command parameters, where p[1] is the customer ID.
     */
    public  void registerCustomer(String[] p) {
        if (p.length != 2 || registry.typeMap.containsKey(p[1])) {
            out.println("Some error occurred in register customer."); return;
        }
        Customer c = new Customer(p[1]);
        registry.addCustomer(c);
        out.println("registered customer " + c.id);
    }

    /**
     * Registers a new freelancer, validates parameters, and stores their
     * initial skills profile.
     * @param p Command parameters including ID, service type, price, and 5 skill scores.
     */
    public  void registerFreelancer(String[] p) {
        if (p.length != 9 || registry.typeMap.containsKey(p[1])) {
            out.println("Some error occurred in register_freelancer.");
            return;
        }
        String id = p[1], service = p[2];
        int serviceIdx = ServiceTypeManager.getServiceIndex(service);
        if (serviceIdx == -1) {
            out.println("Some error occurred in register_freelancer.");
            return;
        }
        int price = Integer.parseInt(p[3]);
        if (price <= 0) {
            out.println("Some error occurred in register_freelancer.");
            return;
        }
        int[] skills = new int[5];
        for (int i = 0; i < 5; i++) {
            int v = Integer.parseInt(p[4 + i]);
            if (v < 0 || v > 100) {
                out.println("Some error occurred in register freelancer.");
                return;
            }
            skills[i] = v;
        }
        Freelancer f = new Freelancer(id, service, price, skills);
        registry.addFreelancer(f);
        out.println("registered freelancer " + f.id);
    }

    /**
     * Finds and displays the Top K eligible freelancers for a specific service type.
     * The top-ranked freelancer is automatically employed if found.
     * Mechanism:Uses the service-specific AVL Tree's Descending Iterator for lazy, ranked retrieval.
     * Filters out unavailable or blacklisted freelancers. The highest-ranked eligible freelancer is auto-employed.
     * Time Complexity is O(K * T_blacklist + log N). (T_blacklist is O(B) for blacklist check).
     * @param p Command parameters including customer ID, service type, and Top K limit.
     */
    public  void requestJob(String[] p) {
        if (p.length != 4) {
            out.println("Some error occurred in request_job.");
            return;
        }

        String cid = p[1], srv = p[2];
        int topK;
        try {
            topK = Integer.parseInt(p[3]);
            if (topK <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            out.println("Some error occurred in request_job.");
            return;
        }

        Customer c = registry.getCustomer(cid);
        int[] requiredSkills = ServiceTypeManager.getSkillProfile(srv);

        if (c == null || requiredSkills == null) {
            out.println("Some error occurred in request_job.");
            return;
        }

        int serviceIdx = ServiceTypeManager.getServiceIndex(srv);
        AVLTree serviceFreelancersTree = registry.serviceTypeFreelancers[serviceIdx];

        // Start Reverse Inorder Traversal (O(log N) setup)
        java.util.Iterator<FreelancerScoreEntry> iterator = serviceFreelancersTree.descendingIterator();

        LinkedList<FreelancerCandidate> finalResults = new LinkedList<>();
        boolean foundEligible = false;

        // Iterate and collect up to K eligible candidates
        while (iterator.hasNext() && finalResults.size() < topK) {
            FreelancerScoreEntry entry = iterator.next();
            Freelancer f = registry.freelancers[entry.freelancerIndex];

            // Check availability and blacklists
            if (!f.available || f.platformBlacklisted) continue;
            if (c.isBlacklisted(entry.freelancerIndex)) continue;

            foundEligible = true;

            // Add candidate to results
            FreelancerCandidate candidate = new FreelancerCandidate(f, entry.compositeScore);
            finalResults.add(candidate);
        }


        if (!foundEligible) {
            out.println("no freelancers available");
            return;
        }

        // Output results and perform auto-employment
        out.println("available freelancers for " + srv + " (top " + finalResults.size() + "):");

        Freelancer autoEmploy = null;
        for (int i = 0; i < finalResults.size(); i++) {
            FreelancerCandidate pair = finalResults.get(i);
            Freelancer f = pair.freelancer;
            long score = pair.compositeScore;

            double rawRating = f.getAverageRating();
            String ratingStr = String.format("%.1f", rawRating);

            out.printf("%s - composite: %d, price: %d, rating: %s%n",
                    f.id, score, f.price, ratingStr);

            if (i == 0) {
                autoEmploy = f;
            }
        }

        // Auto-employ the best candidate
        if (autoEmploy != null) {
            Employment e = new Employment(c, autoEmploy);
            e.listNode = c.employments.add(e);
            autoEmploy.available = false;
            autoEmploy.currentEmployment = e;
            c.totalEmployments++;
            out.println("auto-employed best freelancer: " + autoEmploy.id + " for customer " + c.id);
        }
    }

    /**
     * Manually creates an employment contract between a customer and a specific freelancer.
     * Performs availability and blacklist checks.
     * Mechanism: Creates an {@link Employment} object, adds it to the customer's
     * doubly linked list in , and sets the freelancer's status to unavailable.
     * @param p Command parameters including customer ID and freelancer ID.
     */
    public  void employFreelancer(String[] p) {
        if (p.length != 3) {
            out.println("Some error occurred in employ_freelancer.");
            return;
        }
        Customer c = registry.getCustomer(p[1]);
        Freelancer f = registry.getFreelancer(p[2]);

        if (c == null || f == null) {
            out.println("Some error occurred in employ_freelancer.");
            return;
        }
        int fIdx = registry.indexMap.get(f.id);
        // Availability and blacklist check
        if (!f.available || c.isBlacklisted(fIdx) || f.platformBlacklisted) {
            out.println("Some error occurred in employ_freelancer.");
            return;
        }
        // Create employment and link.
        Employment e = new Employment(c, f);
        e.listNode = c.employments.add(e);
        f.available = false;
        f.currentEmployment = e;
        c.totalEmployments++;

        out.println(c.id + " employed " + f.id + " for " + f.serviceType);
    }

    /**
     * Processes a job completion event, updating scores, payment, loyalty, and skills.
     * Updates {@code totalSpent} and {@code averageRating}.
     * Removes the job from the customer's list in  via the node reference.
     * Grants skill bonuses for ratings higher than 4 to the top 3 relevant skills. Triggers an AVL score update.
     * @param p Command parameters including freelancer ID and the customer rating (0-5).
     */
    public void completeAndRate(String[] p) {
        if (p.length != 3) { out.println("Some error occurred in complete_and_rate."); return; }
        Freelancer f = registry.getFreelancer(p[1]);
        int rating = Integer.parseInt(p[2]);
        if (f == null || f.currentEmployment == null || rating < 0 || rating > 5) {
            out.println("Some error occurred in complete_and_rate."); return;
        }
        Employment e = f.currentEmployment;
        Customer c = f.currentEmployment.customer;
        // Calculate payment and update customer spent
        int payment = calculatePayment(c, f.price);
        c.totalSpent += payment;
        // Update average rating using running average formula
        double newAverageRating;
        int oldCount = f.ratingCount;
        if (oldCount == 0) {
            newAverageRating = (double) rating;
        } else {
            double totalSum = (f.averageRating * oldCount) + rating;
            newAverageRating = totalSum / (oldCount + 1);
        }
        // Update job counters and free freelancer
        f.averageRating = newAverageRating; //
        f.ratingCount++;

        f.completedJobs++;
        f.jobsThisMonth++;
        f.available = true;
        c.employments.remove(e.listNode);
        f.currentEmployment = null;
        out.println(f.id + " completed job for " + c.id + " with rating " + rating);
        // Apply skill bonuses if rating is higher than four.
        if (rating >= 4) {
            int[] serviceSkills = ServiceTypeManager.getSkillProfile(f.serviceType);

            // Find top 3 skills using selection sort
            int[] indices = {0, 1, 2, 3, 4};

            // Selection sort: only need to sort first 3 positions
            for (int i = 0; i < 3; i++) {
                // Find the best skill for position i
                for (int j = i + 1; j < 5; j++) {
                    // Compare skill values
                    if (serviceSkills[indices[j]] > serviceSkills[indices[i]]) {
                        // Swap if j-th skill is higher
                        int temp = indices[i];
                        indices[i] = indices[j];
                        indices[j] = temp;
                    } else if (serviceSkills[indices[j]] == serviceSkills[indices[i]]) {
                        // Tie-breaker: prefer smaller index (T=0, C=1, R=2, E=3, A=4)
                        if (indices[j] < indices[i]) {
                            int temp = indices[i];
                            indices[i] = indices[j];
                            indices[j] = temp;
                        }
                    }
                }
            }
            // Extract primary and secondary skills
            int primary = indices[0];      // Highest skill requirement
            int secondary1 = indices[1];   // Second highest
            int secondary2 = indices[2];   // Third highest

            // Apply skill gains (capped at 100)
            f.skills[primary] = Math.min(100, f.skills[primary] + 2);
            f.skills[secondary1] = Math.min(100, f.skills[secondary1] + 1);
            f.skills[secondary2] = Math.min(100, f.skills[secondary2] + 1);
        }
        registry.updateFreelancerScore(f);
    }

    /**
     * Processes a job cancellation initiated by the freelancer. This incurs penalties.
     * Increments cancellation counters. Applies a 0-star rating and reduces all skills.
     * Removes the job in O(1). Triggers a score update (O(log N)). Triggers platform ban if monthly cancels 5 and more customers.
     * @param p Command parameters including freelancer ID.
     */
    public  void cancelByFreelancer(String[] p) {
        if (p.length != 2) { out.println("Some error occurred in cancel_by_freelancer."); return; }
        Freelancer f = registry.getFreelancer(p[1]);
        if (f == null || f.currentEmployment == null) { out.println("Some error occurred in cancel_by_freelancer."); return; }

        Customer c = f.currentEmployment.customer;
        Employment e = f.currentEmployment;
        // Update counters and free freelancer
        f.cancelledJobs++;
        f.cancelsThisMonth++;
        f.available = true;
        c.employments.remove(e.listNode);
        // Apply 0-star rating penalty
        int rating = 0;
        int oldCount = f.ratingCount;
        double totalSum = (f.averageRating * oldCount) + rating;
        f.averageRating = totalSum / (oldCount + 1);
        f.ratingCount++;
        // Reduce all skills by 3
        for (int i = 0; i < 5; i++) f.skills[i] = Math.max(0, f.skills[i] - 3);
        registry.updateFreelancerScore(f);
        f.currentEmployment = null;

        out.println("cancelled by freelancer: " + f.id + " cancelled " + c.id);
        // Check for platform ban threshold
        if (f.cancelsThisMonth >= 5 && !f.platformBlacklisted) {
            banFreelancerPlatformWide(f);
            f.platformBlacklisted = true;
            out.println("platform banned freelancer: " + f.id);
        }
    }
    /**
     * Processes a job cancellation initiated by the customer.
     * The customer incurs a penalty by having their {@code cancelledJobsTotal} count increased.
     * Removes the job from the customer's linked list and frees the freelancer.
     * Increments customer's cancellation counter for loyalty penalty.
     * @param p Command parameters including customer ID and freelancer ID.
     */
    public  void cancelByCustomer(String[] p) {
        if (p.length != 3) { out.println("Some error occurred in cancel_by_customer."); return; }
        Customer c = registry.getCustomer(p[1]);
        Freelancer f = registry.getFreelancer(p[2]);
        if (c == null || f == null || f.currentEmployment == null || f.currentEmployment.customer != c) {
            out.println("Some error occurred in cancel_by_customer."); return;
        }
        // Remove employment link and free freelancer
        Employment e = f.currentEmployment;
        c.employments.remove(e.listNode);
        f.available = true;
        f.currentEmployment = null;

        c.cancelledJobsTotal++;  // Loyalty penalty increment

        out.println("cancelled by customer: " + c.id + " cancelled " + f.id);
    }

    /**
     * Adds a specific freelancer to a customer's personal blacklist.
     * @param p Command parameters including customer ID and freelancer ID.
     */
    public void blacklist(String[] p) {
        if (p.length != 3) {
            out.println("Some error occurred in blacklist.");
            return;
        }
        Customer c = registry.getCustomer(p[1]);
        Freelancer f = registry.getFreelancer(p[2]);
        if (c == null || f == null) {
            out.println("Some error occurred in blacklist.");
            return;
        }
        int fIdx = registry.indexMap.get(f.id);
        // Check if already blacklisted
        if (c.isBlacklisted(fIdx)) {
            out.println("Some error occurred in blacklist.");
            return;
        }
        c.addBlacklist(fIdx);
        out.println(c.id + " blacklisted " + f.id);
    }
    /**
     * Removes a specific freelancer from a customer's personal blacklist.
     * @param p Command parameters including customer ID and freelancer ID.
     */
    public  void unblacklist(String[] p) {
        if (p.length != 3) {
            out.println("Some error occurred in unblacklist.");
            return;
        }
        Customer c = registry.getCustomer(p[1]);
        Freelancer f = registry.getFreelancer(p[2]);
        if (c == null || f == null) {
            out.println("Some error occurred in unblacklist.");
            return;
        }
        int fIdx = registry.indexMap.get(f.id);
        // Check if NOT blacklisted
        if (!c.isBlacklisted(fIdx)) {
            out.println("Some error occurred in unblacklist.");
            return;
        }
        c.removeBlacklist(fIdx);
        out.println(c.id + " unblacklisted " + f.id);
    }

    /**
     * Queues a service type and price change request for a freelancer.
     * The change only takes effect at the beginning of the next month (during {@code simulateMonth}).
     * @param p Command parameters including freelancer ID, new service type, and new price.
     */
    public  void changeService(String[] p) {
        if (p.length != 4) { out.println("Some error occurred in change_service."); return; }
        Freelancer f = registry.getFreelancer(p[1]);
        int idx = ServiceTypeManager.getServiceIndex(p[2]);
        int price = Integer.parseInt(p[3]);
        if (f == null || idx == -1 || price <= 0) {
            out.println("Some error occurred in change_service."); return;
        }
        // Queue the changes
        f.queuedNewServiceType = p[2];
        f.queuedNewPrice = price;
        out.println("service change for " + f.id + " queued from " + f.serviceType + " to " + p[2]);
    }

    /**
     * Executes end-of-month system maintenance.
     * 1.Customer Loyalty:Updates all customer loyalty tiers (O(N_c)).
     * 2.Freelancer Updates: Processes queued service changes (O(log N) per change),
     * updates burnout status, and resets monthly counters. AVL updates occur if service or burnout status changes.
     */
    public void simulateMonth() {
        // 1. Customer loyalty updates
        for (int i = 0; i < registry.customerCount; i++) {
            Customer c = registry.customers[i];
            c.updateLoyalty();
        }

        // 2. Freelancer updates
        for (int i = 0; i < registry.freelancerCount; i++) {
            Freelancer f = registry.freelancers[i];

            // Process queued service change
            if (f.queuedNewServiceType != null) {
                registry.updateFreelancerServiceType(f, f.serviceType, f.queuedNewServiceType);
                f.serviceType = f.queuedNewServiceType;
                f.price = f.queuedNewPrice;
                f.queuedNewServiceType = null;
                f.queuedNewPrice = -1;
            }
            boolean oldBurnout = f.burnout;
            // Update burnout status
            if (f.burnout) {
                if (f.jobsThisMonth <= 2) f.burnout = false;
            } else {
                if (f.jobsThisMonth >= 5) f.burnout = true;
            }
            // Update AVL only if burnout status changed
            if (oldBurnout != f.burnout) {
                registry.updateFreelancerScore(f);
            }
            // Reset monthly counters
            f.jobsThisMonth = 0;
            f.cancelsThisMonth = 0;
        }
        out.println("month complete");
    }

    /**
     * Retrieves and prints detailed status information for a specific freelancer.
     * @param p Command parameters including freelancer ID.
     */
    public  void queryFreelancer(String[] p) {
        if (p.length != 2) { out.println("Some error occurred in query_freelancer."); return; }
        Freelancer f = registry.getFreelancer(p[1]);
        if (f == null) { out.println("Some error occurred in query_freelancer."); return; }
        // Output formatting
        double rawRating = f.getAverageRating();
        double roundedRating = Math.round(rawRating * 10.0) / 10.0;
        String ratingStr = String.format("%.1f", roundedRating);
        out.printf("%s: %s, price: %d, rating: %s, completed: %d, cancelled: %d, skills: (%d,%d,%d,%d,%d), available: %s, burnout: %s%n",
                f.id, f.serviceType, f.price, ratingStr,
                f.completedJobs, f.cancelledJobs, f.skills[0], f.skills[1],
                f.skills[2], f.skills[3], f.skills[4], f.available ? "yes" : "no", f.burnout ? "yes" : "no");
    }
    /**
     * Retrieves and prints detailed status information for a specific customer,
     * including their loyalty tier and blacklist count.
     * @param p Command parameters including customer ID.
     */
    public void queryCustomer(String[] p) {
        if (p.length != 2) {
            out.println("Some error occurred in query_customer.");
            return;
        }
        Customer c = registry.getCustomer(p[1]);
        if (c == null) {
            out.println("Some error occurred in query_customer.");
            return;
        }
        // Loyalty tier string conversion
        String tierStr = c.loyaltyTier == 0 ? "BRONZE" :
                (c.loyaltyTier == 1 ? "SILVER" :
                        (c.loyaltyTier == 2 ? "GOLD" : "PLATINUM"));
        // Output formatting
        out.printf("%s: total spent: $%d, loyalty tier: %s, blacklisted freelancer count: %d, total employment count: %d%n",
                c.id, c.totalSpent, tierStr,  c.getBlacklistCount(), c.totalEmployments);
    }

    /**
     * Immediately updates a freelancer's five skill scores and triggers a score recalculation.
     * Updates the skills array directly and calls {@code registry.updateFreelancerScore} to update the AVL ranking.
     * @param p Command parameters including freelancer ID and the five new skill scores.
     */
    public void updateSkill(String[] p) {
        if (p.length != 7) { out.println("Some error occurred in update_skill."); return; }
        Freelancer f = registry.getFreelancer(p[1]);
        if (f == null) { out.println("Some error occurred in update_skill."); return; }
        // Update skills array
        for (int i = 0; i < 5; i++) {
            int v = Integer.parseInt(p[i + 2]);
            if (v < 0 || v > 100) {
                out.println("Some error occurred in update_skill.");
                return;
            }
            f.skills[i] = v;
        }
        registry.updateFreelancerScore(f); // AVL update
        out.println("updated skills of " + f.id + " for " + f.serviceType);
    }
}
