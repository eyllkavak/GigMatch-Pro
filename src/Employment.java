/**
 * Represents a single employment contract or job instance linking a specific
 * {@link Customer} and a {@link Freelancer}.
 * This class serves primarily as a container for the relationship and
 * critically stores a reference to its corresponding node within the
 * customer's employment list (SimpleDoublyLinkedList.Node). This reference
 * allows for O(1) removal of this employment record from the list,
 */
public class Employment {
    public Customer customer;
    public Freelancer freelancer;
    /**
     * A direct reference to the node in the {@link SimpleDoublyLinkedList}
     * that holds this Employment object.
     * This is critical for achieving O(1) time complexity, when removing this specific employment record from the customer's list,
     * as it eliminates the need for linear searching (O(N)).
     */
    public SimpleDoublyLinkedList.Node<Employment> listNode;

    /**
     * Constructs a new Employment instance, linking a customer and a freelancer.
     * @param c The customer initiating the contract.
     * @param f The freelancer performing the service.
     */
    public Employment(Customer c, Freelancer f) {
        this.customer = c;
        this.freelancer = f;
        this.listNode = null;
    }
}