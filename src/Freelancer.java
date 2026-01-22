/**
 * Represents a Freelancer entity, tracking all professional, transactional,
 * and performance metrics necessary for system operation, ranking, and service matching.
 * The class contains both real-time status fields (e.g., availability, burnout)
 * and metrics used for complex calculations (e.g., average rating, composite score,
 * monthly job counts).
 */
public class Freelancer {
    public String id;
    public String serviceType;
    public int price;
    public int[] skills; // [T,C,R,E,A]
    public double averageRating; //
    public int ratingCount;
    public int completedJobs;
    public int cancelledJobs;
    public boolean available;
    public boolean burnout;
    public boolean platformBlacklisted;
    public Employment currentEmployment;
    public int jobsThisMonth;
    public int cancelsThisMonth;

    public String queuedNewServiceType;
    public int queuedNewPrice;
    public long cachedCompositeScore;
    public int registryIndex;

    /**
     * Constructs a new Freelancer instance, initializing base attributes and setting
     * initial state (e.g., available, default rating of 5.0).
     * @param id The unique identifier.
     * @param serviceType The initial service type.
     * @param price The initial price/rate.
     * @param skills The initial skill array.
     */
    public Freelancer(String id, String serviceType, int price, int[] skills) {
        this.id = id;
        this.serviceType = serviceType;
        this.price = price;
        this.skills = skills;
        this.ratingCount = 1;
        this.completedJobs = 0;
        this.cancelledJobs = 0;
        this.available = true;
        this.burnout = false;
        this.platformBlacklisted = false;
        this.currentEmployment = null;
        this.jobsThisMonth = 0;
        this.cancelsThisMonth = 0;
        this.queuedNewServiceType = null;
        this.queuedNewPrice = -1;
        this.cachedCompositeScore = 0;
        this.averageRating = 5.0;
    }

    /**
     * Retrieves the current average rating of the freelancer.
     * @return The average customer rating.
     */
    public double getAverageRating() {
        return this.averageRating;
    }
}