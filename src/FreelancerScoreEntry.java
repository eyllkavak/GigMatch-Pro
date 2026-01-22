/**
 * Represents an entry for a freelancer used specifically for ranking and storage
 * within the {@link AVLTree}.
 * The class implements {@code Comparable} to define the specialized ordering
 * needed for the AVL Tree to support efficient Reverse Inorder Traversal
 * (descending order retrieval of the best freelancers)
 * **Comparison Logic (AVL Tree Ordering):**
 * 1. **Primary Key:** {@code compositeScore} (Ascending).
 * 2. **Tie-Breaker:** {@code freelancerID} (Descending, based on lexicographical order).
 */
public class FreelancerScoreEntry implements Comparable<FreelancerScoreEntry> {
    public final int freelancerIndex;
    public final long compositeScore;
    public final String freelancerID;

    /**
     * Constructs a new score entry.
     * @param idx The freelancer's registry index.
     * @param score The composite score.
     * @param id The freelancer's ID.
     */
    public FreelancerScoreEntry(int idx, long score, String id) {
        this.freelancerIndex = idx;
        this.compositeScore = score;
        this.freelancerID = id;
    }

    /**
     * Defines the ordering of nodes within the AVL Tree (Binary Search Tree property).
     * Mechanism: The comparison dictates where a node is placed:
     * 1. Score: Items with a lower score go to the left, higher score to the right (standard BST).
     * 2. Tie-Breaker (ID): If scores are equal, the freelancer with the **lexicographically larger id**
     * is placed to the left (considered "smaller" by {@code compareTo}).
     *
     * Goal:*This reverse tie-breaker placement ensures that during a Reverse Inorder Traversal
     * (Right -> Root -> Left), the smaller ID is encountered first among tied scores,
     * satisfying the required descending score, then ascending ID sort order.
     *
     * @param other The entry to compare against.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(FreelancerScoreEntry other) {
        // 1. Score comparison (Normal BST order: smaller score goes left)
        if (this.compositeScore != other.compositeScore) {
            return Long.compare(this.compositeScore, other.compositeScore);
        }
        // 2. Tie-breaker: Reverse ID comparison (Larger ID goes left)
        return other.freelancerID.compareTo(this.freelancerID);
    }

    /**
     * Checks if this score entry is equal to another object.
     * Mechanism: Equality is based on having the same composite score and freelancer ID.
     * The freelancerIndex is not included as it's an auxiliary piece of data.
     * @param o The object to compare with.
     * @return True if the objects are considered equal.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FreelancerScoreEntry that = (FreelancerScoreEntry) o;
        return compositeScore == that.compositeScore &&
                freelancerID.equals(that.freelancerID);
    }

    /**
     * Computes a hash code based on the unique combination of composite score and freelancer ID.
     * @return The hash code.
     */
    @Override
    public int hashCode() {
        // Simple hash combining score (primary) and ID (tie-breaker)
        return (int) (compositeScore * 31 + freelancerID.hashCode());
    }
}