/**
 * Represents a customer entity within the system, tracking their transactional
 * history, spending metrics, employment records, and loyalty status.
 * It also maintains a simple blacklist of freelancer indices (fIdx) that this
 * customer wishes to avoid. The blacklist uses a standard ArrayList, favoring
 * simplicity and guaranteed accuracy over absolute O(1) performance while updating loyalty, as list
 * sizes are expected to be small, so checking,adding and removing has O(N) performence but N is so small.
 */
public class Customer {
    public String id;
    public int totalSpent;
    int cancelledJobsTotal = 0;
    public int discountedSpent;
    public int totalEmployments;
    public int loyaltyTier;
    private java.util.ArrayList<Integer> blacklist;
    public SimpleDoublyLinkedList<Employment> employments;

    /**
     * Constructs a new Customer instance and initializes all metrics to zero.
     * @param id The unique identifier for the customer.
     */
    public Customer(String id) {
        this.id = id;
        this.totalSpent = 0;
        this.discountedSpent = 0;
        this.totalEmployments = 0;
        this.loyaltyTier = 0;
        this.blacklist = new java.util.ArrayList<>();
        this.employments = new SimpleDoublyLinkedList<>();
    }
    //Blacklist Management
    /**
     * Checks if a freelancer (identified by fIdx) is on the customer's blacklist.
     * @param fIdx The index of the freelancer.
     * @return True if the freelancer is blacklisted, false otherwise.
     */
    public boolean isBlacklisted(int fIdx) {
        return blacklist.contains(fIdx);
    }

    /**
     * Adds a freelancer index to the blacklist, ensuring no duplicates are added.
     * @param fIdx The index of the freelancer to blacklist.
     */
    public void addBlacklist(int fIdx) {
        if (!blacklist.contains(fIdx)) {
            blacklist.add(fIdx);
        }
    }

    /**
     * Removes a freelancer index from the blacklist.
     * @param fIdx The index of the freelancer to remove.
     */
    public void removeBlacklist(int fIdx) {
        blacklist.remove(Integer.valueOf(fIdx));
    }

    /**
     * Returns the current number of freelancers on the blacklist.
     * @return The size of the blacklist.
     */
    public int getBlacklistCount() {
        return blacklist.size();
    }


    /**
     * Updates the customer's loyalty tier based on their effective spending.
     * Mechanism: Calculates effective spending by subtracting a penalty
     * (250 units per cancelled job) from total spent.
     * Then, assigns a tier based on fixed thresholds (500, 2000, 5000).
     */
    public void updateLoyalty() {
        // Calculate penalty (250 units per cancelled job)
        long penalty = (long) this.cancelledJobsTotal * 250L;

        // Calculate effective spending (min 0)
        long effectiveLoyaltySpending = this.totalSpent - penalty;
        if (effectiveLoyaltySpending < 0) effectiveLoyaltySpending = 0;

        // Assign tier based on thresholds
        if (effectiveLoyaltySpending >= 5000) this.loyaltyTier = 3;
        else if (effectiveLoyaltySpending >= 2000) this.loyaltyTier = 2;
        else if (effectiveLoyaltySpending >= 500) this.loyaltyTier = 1;
        else this.loyaltyTier = 0;
    }
}