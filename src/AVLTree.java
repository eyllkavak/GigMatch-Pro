/**
 * AVLTree is a self-balancing Binary Search Tree (BST) structure
 * used to maintain a sorted set of {@link FreelancerScoreEntry} objects efficiently.
 * Insertion,deletion and search O(logN) time complexity and traversals/getTopK: O(N) or O(K + log N) if K is small
 * It is so efficent when we are looking top K freelancer.
 */
public class AVLTree {
    private Node root;
    private int size;

    /**
     * Represents a node in the AVL tree. Each node stores a
     * {@link FreelancerScoreEntry} and its current height in the tree.
     */
    private class Node {
        FreelancerScoreEntry entry;
        Node left, right;
        int height;

        /**
         * Constructs a new Node with the specified entry.
         * The initial height of a new node (leaf) is 1.
         * @param entry The data entry to store in the node.
         */
        Node(FreelancerScoreEntry entry) {
            this.entry = entry;
            this.height = 1;
        }
    }
    //Initializes an empty AVLTree.
    public AVLTree() {
        root = null;
        size = 0;
    }

    /**
     * Helper method to safely get the height of a node.
     * A null node has a height of 0.
     * @param n The node to check.
     * @return The height of the node, or 0 if the node is null.
     */
    private int height(Node n) {
        return n == null ? 0 : n.height;
    }

    /**
     * Computes the balance factor of a node: height(left_subtree) - height(right_subtree).
     * @param n The node to check.
     * @return The balance factor.
     */
    private int getBalance(Node n) {
        return n == null ? 0 : height(n.left) - height(n.right);
    }

    /**
     * Recalculates and updates the height of a node based on its children's heights.
     * @param n The node whose height needs updating.
     */
    private void updateHeight(Node n) {
        if (n != null) {
            n.height = 1 + Math.max(height(n.left), height(n.right));
        }
    }

    /**
     * Performs a **Right Rotation** around the given node 'y' (the new pivot).
     * Used to rebalance a **Left-Left** case.
     * @param y The root of the unbalanced subtree.
     * @return The new root of the balanced subtree (x).
     */
    private Node rotateRight(Node y) {
        if (y == null || y.left == null) return y;
        Node x = y.left;
        Node T2 = x.right;
        x.right = y;
        y.left = T2;
        updateHeight(y);
        updateHeight(x);

        return x;
    }

    /**
     * Performs a **Left Rotation** around the given node 'x' (the new pivot).
     * Used to rebalance a **Right-Right** case.
     * @param x The root of the unbalanced subtree.
     * @return The new root of the balanced subtree (y).
     */
    private Node rotateLeft(Node x) {
        if (x == null || x.right == null) return x;
        Node y = x.right;
        Node T2 = y.left;
        y.left = x;
        x.right = T2;
        updateHeight(x);
        updateHeight(y);

        return y;
    }

    /**
     * Inserts a new {@link FreelancerScoreEntry} into the AVL tree.
     * @param entry The entry to insert.
     */
    public void insert(FreelancerScoreEntry entry) {
        root = insertRec(root, entry);
    }

    /**
     * Recursive helper for insertion. It traverses like a standard BST insert,
     * but ensures **height updating and rebalancing (rotations)** on the way back up.
     * @param node The current node in the recursion.
     * @param entry The entry to insert.
     * @return The root of the (potentially rebalanced) subtree.
     */
    private Node insertRec(Node node, FreelancerScoreEntry entry) {
        if (node == null) {
            size++;
            return new Node(entry);
        }

        int answ = entry.compareTo(node.entry);

        if (answ < 0) {
            node.left = insertRec(node.left, entry);
        } else if (answ > 0) {
            node.right = insertRec(node.right, entry);
        } else {
            node.entry = entry;
            return node;
        }

        updateHeight(node);
        int balance = getBalance(node);
        // Simple Rotations (LL, RR)
        if (balance > 1 && node.left != null && entry.compareTo(node.left.entry) < 0) {
            return rotateRight(node);
        }
        if (balance < -1 && node.right != null && entry.compareTo(node.right.entry) > 0) {
            return rotateLeft(node);
        }
        // Complex Rotations (LR, RL)
        if (balance > 1 && node.left != null && entry.compareTo(node.left.entry) > 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        if (balance < -1 && node.right != null && entry.compareTo(node.right.entry) < 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    /**
     * Removes the specified entry from the AVL tree.
     * @param entry The entry to remove.
     */
    public void remove(FreelancerScoreEntry entry) {
        root = removeRec(root, entry);
    }

    /**
     * Recursive helper for deletion. It first removes the node (standard BST logic)
     * and then ensures **height updating and rebalancing** on the way back up.
     * @param node The current node in the recursion.
     * @param entry The entry to remove.
     * @return The root of the (potentially rebalanced) subtree.
     */
    private Node removeRec(Node node, FreelancerScoreEntry entry) {
        if (node == null) return null;

        int answ = entry.compareTo(node.entry);

        if (answ < 0) {
            node.left = removeRec(node.left, entry);
        } else if (answ > 0) {
            node.right = removeRec(node.right, entry);
        } else {
            // Node found. Handle 0 or 1 child cases directly.
            if (node.left == null) {
                size--;
                return node.right;
            } else if (node.right == null) {
                size--;
                return node.left;
            }
            // Case with 2 children: Replace with inorder successor
            Node minRight = findMin(node.right);
            node.entry = minRight.entry;
            node.right = removeRec(node.right, minRight.entry);
        }

        if (node == null) return null;
        updateHeight(node);
        // Rebalance after deletion: four cases (LL, LR, RR, RL)
        int balance = getBalance(node);

        if (balance > 1 && getBalance(node.left) >= 0) {
            return rotateRight(node);
        }

        if (balance > 1 && getBalance(node.left) < 0) {
            node.left = rotateLeft(node.left);
            return rotateRight(node);
        }

        if (balance < -1 && getBalance(node.right) <= 0) {
            return rotateLeft(node);
        }

        if (balance < -1 && getBalance(node.right) > 0) {
            node.right = rotateRight(node.right);
            return rotateLeft(node);
        }

        return node;
    }

    /**
     * Finds the node with the minimum key in the subtree rooted at 'node'.
     * This is used during deletion to find the inorder successor.
     * @param node The starting node of the subtree.
     * @return The node with the minimum key.
     */
    private Node findMin(Node node) {
        while (node != null && node.left != null) {
            node = node.left;
        }
        return node;
    }

    /**
     * Returns a descending iterator over the elements in the AVL tree.
     * The iterator traverses the elements from **largest score to smallest score**.
     * Mechanism: Uses an iterative approach with a stack (simulating recursion)
     * to perform a Reverse Inorder Traversal without recursion limits.
     * @return An iterator that yields elements in descending order.
     */
    public java.util.Iterator<FreelancerScoreEntry> descendingIterator() {
        return new java.util.Iterator<FreelancerScoreEntry>() {
            private SimpleStack<Node> stack = new SimpleStack<>();

            {
                // Constructor: Start traversal from the largest element
                pushAllRight(root);
            }

            /**
             * Pushes the current node and all its right descendants onto the stack.
             * This finds the largest element in the current subtree.
             */
            private void pushAllRight(Node node) {
                while (node != null) {
                    stack.push(node);
                    node = node.right;
                }
            }

            /**
             * Checks if there is a next element to return.
             * @return true if the stack is not empty.
             */
            @Override
            public boolean hasNext() {
                return !stack.isEmpty();
            }

            /**
             * Returns the next element in descending order.
             * @return The next largest FreelancerScoreEntry.
             */
            @Override
            public FreelancerScoreEntry next() {
                if (!hasNext()) {
                    return null;
                }

                Node node = stack.pop();
                FreelancerScoreEntry result = node.entry;

                // Move to the left child of the current node and push all its right descendants.
                // This ensures the next largest element is found in the left subtree.
                if (node.left != null) {
                    pushAllRight(node.left);
                }

                return result;
            }
        };
    }
}