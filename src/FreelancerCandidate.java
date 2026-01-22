/**
 * This class used to combine a {@link Freelancer} object with its
 * pre-calculated ranking metric, the Composite Score.
 * This class implements {@code Comparable<FreelancerCandidate>} to define the
 * ordering for data manipulation (e.g., insertion into specialized structures)
 * and the final presentation of results.
 *
 * **Key Comparison Rules (Final Ranking):**
 * 1. **Primary:** Higher {@code compositeScore} is **better**.
 * 2. **Tie-Breaker:** Lexicographically **smaller ID** is **better**.
 */
public class FreelancerCandidate implements Comparable<FreelancerCandidate> {
    public final Freelancer freelancer;
    public final long compositeScore;

    /**
     * Constructs a FreelancerCandidate by pairing a Freelancer with their composite score.
     * @param freelancer The associated Freelancer object.
     * @param compositeScore The ranking score calculated for the freelancer.
     */
    public FreelancerCandidate(Freelancer freelancer, long compositeScore) {
        this.freelancer = freelancer;
        this.compositeScore = compositeScore;
    }

    /**
     * Defines the primary ordering for this class.
     * Mechanism:This comparison logic is used to define how {@code FreelancerCandidate}
     * objects are ordered within data structures. When used for Top K selection
     * algorithms, it typically defines the inverse of the final desired ranking
     * (in a Min-Heap structure used to track the 'K best' candidates)
     * @param other The FreelancerCandidate to compare against.
     * @return A negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(FreelancerCandidate other) {
        // 1. Score ascending: Lower score is considered "less" (comes first).
        if (this.compositeScore != other.compositeScore) {
            return Long.compare(this.compositeScore, other.compositeScore);
        }
        // 2. Tie-breaker: If scores are equal, prefer lexicographically larger id
        return other.freelancer.id.compareTo(this.freelancer.id);
    }

}
