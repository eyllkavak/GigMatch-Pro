GigMatch Pro: Freelancer Matchmaking & Platform Simulation

GigMatch Pro is a high-scale backend engine designed to manage a dynamic gig economy platform. It processes real-time service requests, maintains a sophisticated ranking system, and simulates a living ecosystem with skill evolution and loyalty tiers .


System Logic & Core Features
The engine follows a strict set of rules for user interactions and platform mechanics:

1. User & Service Structure

Dual User System: Manages Customers and Freelancers with globally unique IDs.

Predefined Categories: Supports 10 service types (e.g., web_dev, paint, tutoring), each with specific skill requirements .

Freelancer Constraints: Each freelancer offers one service at a fixed price and can work for one customer at a time.



2. Matchmaking & Ranking (The Engine)

The system calculates a Composite Score [0-10000] for every job request to find the ideal match :

Skill Score (55%): Normalized dot product of freelancer skills vs. service requirements .

Rating Score (25%): Calculated based on the average star rating (0-5 scale).

Reliability Score (20%): Derived from the ratio of completed to total (completed + cancelled) jobs .

Burnout Penalty: A deduction of 0.45 is applied to the score if the freelancer is in a burnout state .



3. Dynamic Skill Evolution

Freelancer skills (Technical, Communication, Creativity, Efficiency, Attention to Detail) change based on performance:

Gains: Ratings ≥4 award +2 points to the primary skill and +1 point to secondary skills of the service .

Degradation: Any freelancer-initiated cancellation results in a permanent -3 penalty across all 5 skills .



4. Simulation Mechanics

Monthly Cycles (simulate_month): Triggers burnout checks, loyalty tier updates, and processes queued service changes .

Burnout: Triggered by completing ≥5 jobs in a month; recovery requires completing ≤2 jobs in the following month .

Loyalty Tiers: Customers move through Bronze, Silver, Gold, and Platinum tiers based on spending, receiving 0% to 15% discounts .

Platform Bans: Freelancers who cancel 5 or more jobs within a simulated month are permanently blacklisted from the system.




Performance & Academic Constraints
Scalability: Optimized to handle Large Scale datasets with 100,000 to 500,000 users.

Data Structures: As per Boğaziçi University CmpE 250 requirements, only ArrayList and LinkedList were used from the standard library. All other complex structures (AVL Trees, Hash Tables) are custom-implemented for high-speed lookup and ranking .

Numerical Precision: Implements integer-only arithmetic and floor functions to ensure deterministic results across different environments.



How to Run

javac *.java

java Main <input_file> <output_file>
