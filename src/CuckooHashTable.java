/**
 * Implements a Hash Table with Cuckoo Hashing using two separate hash tables and two independent
 * hash functions (hash1 and hash2). The structure provides O(1) expected time
 * complexity for insertion, search, and deletion, leveraging the "cuckoo" eviction
 * process to resolve collisions.
 * If resize is necessary it takes time O(N) but it occurs rarely.
 */
public class CuckooHashTable {
    private Node[] table1;
    private Node[] table2;
    private int capacity;
    private int count;
    private static final int INIT_SIZE = 524_288;
    private static final double LOAD_FACTOR = 0.45;
    private static final int MAX_ITERATIONS = 5000;

    /**
     * Represents a key-value pair stored in the hash table.
     */
    public static class Node {
        public String key;
        public int value;

        /**
         * Constructs a new Node.
         *
         * @param k The string key.
         * @param v The integer value.
         */
        Node(String k, int v) {
            key = k;
            value = v;
        }
    }

    //Initializes the Cuckoo Hash Table with the initial capacity.
    public CuckooHashTable() {
        capacity = INIT_SIZE;
        table1 = new Node[capacity];
        table2 = new Node[capacity];
        count = 0;
    }

    /**
     * Calculates the index for the first hash table (table1).
     * Mechanism: Uses Java's native hashCode(), then blends the high-value parts
     * of the number with the low-value parts to create a more uniformly distributed
     * result. This ensures all digits of the hash code are used before applying the modulus.
     *
     * @param key The string key.
     * @return The calculated index for table1.
     */
    private int hash1(String key) {
        int h = key.hashCode();
        h = h + (h / 65536);
        return Math.abs(h % capacity);
    }

    /**
     * Calculates the index for the second hash table (table2).
     * Mechanism: Uses a custom polynomial rolling hash
     * and a different blend approach to ensure independence from hash1.
     *
     * @param key The string key.
     * @return The calculated index for table2.
     */
    private int hash2(String key) {
        int h = 0;
        for (int i = 0; i < key.length(); i++) {
            h = h * 31 + key.charAt(i);
        }
        h = h + (h / 2048);
        return Math.abs(h % capacity);
    }


    /**
     * Inserts a key-value pair or updates the value if the key already exists.
     * Mechanism: Checks for existing keys first. If not found, attempts cuckoo insertion.
     * If insertion fails (cycle detected) or load factor is exceeded, a resize is triggered,
     * and the insertion is retried.
     *
     * @param key   The key to insert.
     * @param value The value associated with the key.
     */
    public void put(String key, int value) {
        if (count * 1.0 / capacity > LOAD_FACTOR) {
            resize();
        }

        int h1 = hash1(key);
        int h2 = hash2(key);

        //Existing update/check logic
        if (table1[h1] != null && table1[h1].key.equals(key)) {
            table1[h1].value = value;
            return;
        }
        if (table2[h2] != null && table2[h2].key.equals(key)) {
            table2[h2].value = value;
            return;
        }

        Node toInsert = new Node(key, value);
        if (cuckooInsert(toInsert)) {
            count++;
        } else {
            // Cycle detected - resize and retry
            resize();
            put(key, value);
        }
    }

    /**
     * Attempts to insert a node using the Cuckoo mechanism, displacing existing elements
     * until a free slot is found or a cycle is detected.
     * Mechanism: Alternates between table1 (using hash1) and table2 (using hash2)
     * for a maximum of MAX_ITERATIONS. If a slot is occupied, the occupant is evicted
     * and becomes the new item to insert in the other table.
     *
     * @param node The node to insert.
     * @return True if insertion was successful, false if a cycle was detected.
     */
    private boolean cuckooInsert(Node node) {
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // Try inserting into Table 1 (h1)
            int h1 = hash1(node.key);
            if (table1[h1] == null) {
                table1[h1] = node;
                return true;
            }
            // Evict and prepare for Table 2
            Node temp = table1[h1];
            table1[h1] = node;
            node = temp;
            // Try inserting into Table 2 (h2)
            int h2 = hash2(node.key);
            if (table2[h2] == null) {
                table2[h2] = node;
                return true;
            }
            // Evict and prepare for Table 1
            temp = table2[h2];
            table2[h2] = node;
            node = temp;
        }
        return false;  // Cycle detected
    }

    /**
     * Checks if the hash table contains the specified key.
     * Mechanism:Checks both possible locations (hash1 in table1 and hash2 in table2).
     *
     * @param key The key to search for.
     * @return True if the key is found, false otherwise.
     */
    public boolean containsKey(String key) {
        int h1 = hash1(key);
        int h2 = hash2(key);
        return (table1[h1] != null && table1[h1].key.equals(key)) ||
                (table2[h2] != null && table2[h2].key.equals(key));
    }

    /**
     * Retrieves the value associated with the specified key.
     * Mechanism:Checks both hash locations sequentially.
     *
     * @param key The key to look up.
     * @return The associated value, or -1 if the key is not found.
     */
    public int get(String key) {
        int h1 = hash1(key);
        int h2 = hash2(key);
        if (table1[h1] != null && table1[h1].key.equals(key))
            return table1[h1].value;
        if (table2[h2] != null && table2[h2].key.equals(key))
            return table2[h2].value;
        return -1;
    }

    /**
     * Doubles the capacity of the hash tables and re-inserts all existing elements.
     * This is required when the load factor is exceeded or an insertion cycle occurs.
     * Mechanism: Creates new, larger tables. Iterates through all nodes in the old tables
     * and calls put() for each, which re-calculates the new hash indices and performs
     * cuckoo insertion in the new tables.
     */
    private void resize() {
        int oldCap = capacity;
        capacity *= 2;
        Node[] oldTable1 = table1;
        Node[] oldTable2 = table2;
        table1 = new Node[capacity];
        table2 = new Node[capacity];
        count = 0; // count reset; put() will increment

        // Rehash all elements from old tables into the new, larger tables.
        for (int i = 0; i < oldCap; i++) {
            if (oldTable1[i] != null) {
                put(oldTable1[i].key, oldTable1[i].value);
            }
            if (oldTable2[i] != null) {
                // Check if element was already inserted from table1, as keys can't be duplicated.
                if (!containsKey(oldTable2[i].key)) {
                    if (!containsKey(oldTable2[i].key)) {
                        put(oldTable2[i].key, oldTable2[i].value);
                    }
                }
            }
        }
    }
}