import java.util.Iterator;
/**
 * Implements a basic Doubly Linked List structure. This list allows
 * elements to be added to the tail and, crucially, supports O(1) time
 * complexity for removal of an existing element given its {@link Node} reference.
 * It also provides an {@code Iterable} implementation for easy traversal.O(N)
 * @param <T> The type of elements held in this list.
 */
public class SimpleDoublyLinkedList<T> implements Iterable<T> {
    private Node<T> head, tail;
    private int size;

    /**
     * Represents a single node within the doubly linked list.
     * Stores the data item and references to the previous and next nodes.
     * @param <T> The type of data stored in the node.
     */
    public static class Node<T> {
        T data;
        Node<T> prev, next;
        Node(T item) { data = item; }
    }

    /**
     * Initializes an empty doubly linked list.
     */
    public SimpleDoublyLinkedList() {
        head = tail = null; size = 0;
    }

    /**
     * Adds a new item to the **tail (end)** of the list.
     * Mechanism:Creates a new node, links it to the current tail, and updates the tail reference.
     * @param item The item to be added.
     * @return The newly created {@link Node} reference. This reference is crucial for O(1) removal later.
     */
    public Node<T> add(T item) {
        Node<T> node = new Node<>(item);
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node; node.prev = tail; tail = node;
        }
        size++;
        return node;
    }

    /**
     * Removes the specified node from the list.
     * Mechanism: Uses the provided node's {@code prev} and {@code next} pointers
     * to bypass the node being removed, effectively unlinking it in O(1) time.
     * @param node The specific {@link Node} object to be removed.
     */
    public void remove(Node<T> node) {
        if (node == null || node.data == null) return;
        // Update the previous node's next pointer
        if (node.prev != null) node.prev.next = node.next;
        else head = node.next;
        // Update the next node's previous pointer
        if (node.next != null) node.next.prev = node.prev;
        else tail = node.prev;

        // Nullify pointers/data to assist garbage collection
        node.data = null;
        node.prev = null;
        node.next = null;

        size--;
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence (from head to tail).
     * @return An Iterator that yields elements of type T.
     */
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            Node<T> curr = head;
            /**
             * Checks if the list has more elements to iterate over.
             * @return True if the current node is not null.
             */
            public boolean hasNext() { return curr != null; }
            /**
             * Returns the next element in the sequence and advances the iterator.
             * @return The data item of the current node.
             */
            public T next() {
                T it = curr.data;
                curr = curr.next;
                return it;
            }
        };
    }
}