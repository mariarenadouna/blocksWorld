import java.util.*;

public class State {
    // blockPosition: Κλειδί -> ο κύβος, Τιμή -> τι υπάρχει κάτω του ("TABLE" ή άλλος κύβος)
    // Αυτό το map περιγράφει τη διάταξη των κύβων στον κόσμο.
    private final Map<String, String> blockPosition;
    // blocks: Μια λίστα με όλα τα ονόματα των κύβων του προβλήματος.
    private final List<String> blocks;
    // handEmpty: True αν το χέρι είναι άδειο, False αν κρατάει έναν κύβο.
    private boolean handEmpty;
    // heldBlock: Το όνομα του κύβου που κρατάει το χέρι.
    private String heldBlock;

    // --- Constructor για τη δημιουργία της αρχικής κατάστασης ---
    public State(List<String> allBlocks, Map<String, String> initialPositions) {
        this.blocks = new ArrayList<>(allBlocks);
        this.blockPosition = new HashMap<>(initialPositions);
        this.handEmpty = true; // Αρχικά, το χέρι είναι άδειο
        this.heldBlock = null; // Και δεν κρατάει τίποτα
    }

    // --- Copy Constructor για τη δημιουργία νέων καταστάσεων από μια υπάρχουσα ---
    public State(State other) {
        this.blocks = new ArrayList<>(other.blocks); // Αντιγραφή της λίστας των κύβων
        this.blockPosition = new HashMap<>(other.blockPosition); // Βαθιά αντιγραφή του map
        this.handEmpty = other.handEmpty;
        this.heldBlock = other.heldBlock;
    }

    /**
     * Υπολογίζει και επιστρέφει τη λίστα των 'clear' κύβων.
     * Ένας κύβος είναι 'clear' αν δεν έχει τίποτα πάνω του, και δεν κρατιέται από το χέρι.
     */
    public List<String> getClearBlocks() {
        // Blocks που δεν είναι clear. Ένας κύβος δεν είναι clear αν υπάρχει ως τιμή (value) στο blockPosition map.
        Set<String> blocksThatAreNotClear = new HashSet<>(blockPosition.values());

        List<String> clearBlocks = new ArrayList<>();
        for (String block : blocks) {
            // Ένας κύβος είναι clear αν δεν υπάρχει άλλος κύβος πάνω του, και δεν τον κρατάει το χέρι
            if (!blocksThatAreNotClear.contains(block) && (heldBlock == null || !heldBlock.equals(block))) {
                clearBlocks.add(block);
            }
        }
        return clearBlocks;
    }

    // --- Getters and Setters για τα πεδία της κατάστασης ---
    public Map<String, String> getBlockPosition() {
        return blockPosition;
    }

    public boolean isHandEmpty() {
        return handEmpty;
    }

    public void setHandEmpty(boolean handEmpty) {
        this.handEmpty = handEmpty;
    }

    public String getHeldBlock() {
        return heldBlock;
    }

    public void setHeldBlock(String heldBlock) {
        this.heldBlock = heldBlock;
    }

    /**
     * Μέθοδος για την αναπαράσταση της κατάστασης σε αναγνώσιμη μορφή.
     */
    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        // Εκτυπώνουμε τη θέση για κάθε κύβο
        for (String block : blocks) {
            // Αν ο κύβος κρατιέται από το χέρι, δεν τον εκτυπώνουμε ως μέρος της στοίβας
            if (heldBlock != null && heldBlock.equals(block)) {
                continue;
            }

            String position = blockPosition.get(block);
            if (position != null) {
                if (position.equals("TABLE")) {
                    output.append(block).append(" is on the table.\n");
                } else {
                    output.append(block).append(" is on ").append(position).append(".\n");
                }
            } else {
                output.append(block).append(" has an undefined position (Error or in hand).\n");
            }
        }
        // Εκτυπώνουμε την κατάσταση του χεριού
        if (heldBlock != null) {
            output.append("Hand is holding: ").append(heldBlock).append(".\n");
        } else {
            output.append("Hand is empty.\n");
        }
        // Εκτυπώνουμε τη λίστα με τους clear κύβους
        output.append("Block Positions Map: ").append(blockPosition).append("\n");
        output.append("Clear Blocks:\n").append(getClearBlocks()).append("\n");
        return output.toString();
    }

    /**
     * Συγκρίνει δύο αντικείμενα State για ισότητα.
     * Κρίσιμο για την αναζήτηση, ώστε να αποφεύγονται οι επαναληπτικές επισκέψεις στις ίδιες καταστάσεις.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State that = (State) o;
        // Συγκρίνουμε όλα τα κρίσιμα πεδία που ορίζουν μοναδικά την κατάσταση
        return handEmpty == that.handEmpty &&
                Objects.equals(heldBlock, that.heldBlock) &&
                blockPosition.equals(that.blockPosition);
    }

    /**
     * Υπολογίζει τον hash code της κατάστασης.
     * Κρίσιμο για τη χρήση σε HashSet και HashMap.
     */
    @Override
    public int hashCode() {
        return Objects.hash(blockPosition, handEmpty, heldBlock);
    }

    /**
     * Ελέγχει αν ένας συγκεκριμένος κύβος είναι 'clear'.
     * Αυτή η μέθοδος είναι βοηθητική για την υλοποίηση των ενεργειών.
     */
    public boolean isClear(String block) {
        // Ένας κύβος είναι 'clear' αν δεν υπάρχει άλλος κύβος πάνω του
        Set<String> blocksThatAreNotClear = new HashSet<>(blockPosition.values());
        // ... και δεν τον κρατάει το χέρι
        return !blocksThatAreNotClear.contains(block) &&
                (heldBlock == null || !heldBlock.equals(block));
    }
}