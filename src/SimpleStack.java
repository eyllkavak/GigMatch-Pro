/**
 * Implements a Last-In, First-Out (LIFO) Stack data structure using a
 * singly linked list. This implementation is designed to be simple and
 * efficient, focusing on the core stack operations (push, pop, peek).
 * It uses the {@code top} reference to manage the head of the list,
 * ensuring all primary operations execute in O(1) time.
 * @param <T> The type of elements held in the stack.
 */
public class SimpleStack<T> {
    private Node<T> top;
    private int size;

    /**
     * Represents a single node in the linked list structure that forms the stack.
     * Each node holds the data and a reference to the next node in the sequence.
     * @param <T> The type of data stored in the node.
     */
    private static class Node<T> {
        T data;
        Node<T> next;

        /**
         * Constructs a new node with the given data.
         * @param data The data item to store.
         */
        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }
    /**
     * Initializes an empty stack.
     * **Time Complexity:** O(1)
     */
    public SimpleStack() {
        this.top = null;
        this.size = 0;
    }

    /**
     * Pushes (adds) a new item onto the top of the stack.
     * The new item becomes the new head of the linked list.
     * @param item The item to be added.
     */
    public void push(T item) {
        Node<T> newNode = new Node<>(item);
        newNode.next = top;
        top = newNode;
        size++;
    }

    /**
     * Removes and returns the item currently at the top of the stack.
     * @return The item from the top of the stack, or null if the stack is empty.
     */
    public T pop() {
        if (isEmpty()) {
            return null;
        }
        T data = top.data;
        top = top.next;
        size--;
        return data;
    }
    /**
     * Checks if the stack contains any elements.
     * @return True if the stack is empty (top is null), false otherwise.
     */
    public boolean isEmpty() {
        return top == null;
    }

}