import java.util.*;

public class StackAction implements Action {
    private final String blockToStack;    // Ο κύβος που θα στοιβαχτεί (το X)
    private final String blockToStackOn;  // Ο κύβος πάνω στον οποίο θα στοιβαχτεί (το Y)

    public StackAction(String blockX, String blockY) {
        this.blockToStack = blockX;
        this.blockToStackOn = blockY;
    }

    @Override
    public boolean isApplicable(State currentState) {
        // Η μέθοδος isApplicable ελέγχει αν η ενέργεια STACK(X, Y) μπορεί να εκτελεστεί.
        // Πρέπει να ισχύουν όλες οι παρακάτω προϋποθέσεις ταυτόχρονα.

        // 1. Το χέρι δεν πρέπει να είναι άδειο.
        return !currentState.isHandEmpty() &&
                // 2. Το χέρι πρέπει να κρατάει τον κύβο που θέλουμε να στοιβάξουμε (blockToStack).
                currentState.getHeldBlock() != null &&
                currentState.getHeldBlock().equals(blockToStack) &&
                // 3. Ο κύβος πάνω στον οποίο θα στοιβάξουμε (blockToStackOn) πρέπει να είναι "clear",
                // δηλαδή να μην έχει τίποτα πάνω του.
                currentState.getClearBlocks().contains(blockToStackOn) &&
                // 4. Ο κύβος δεν μπορεί να στοιβαχτεί πάνω στον εαυτό του.
                !blockToStack.equals(blockToStackOn);
    }

    @Override
    public State apply(State currentState) {
        // Η μέθοδος apply δημιουργεί τη νέα κατάσταση μετά την ενέργεια.
        // Δεν τροποποιεί ποτέ την αρχική κατάσταση (currentState).

        if (!isApplicable(currentState)) {
            return null; // Αν η ενέργεια δεν είναι εφαρμόσιμη, επιστρέφει null
        }

        // Δημιουργούμε ένα νέο αντικείμενο State ως αντίγραφο της τρέχουσας κατάστασης
        State newState = new State(currentState);

        // --- Επιδράσεις της ενέργειας ---
        // 1. Το χέρι γίνεται άδειο.
        newState.setHandEmpty(true);
        // 2. Το χέρι δεν κρατάει πλέον κανέναν κύβο.
        newState.setHeldBlock(null);
        // 3. Ο κύβος blockToStack τοποθετείται πάνω στον κύβο blockToStackOn.
        // Αυτή η αλλαγή καταγράφεται στο blockPosition map.
        newState.getBlockPosition().put(blockToStack, blockToStackOn);

        return newState; // Επιστρέφουμε τη νέα κατάσταση
    }

    @Override
    public String getName() {
        // Επιστρέφει το όνομα της ενέργειας σε μια αναγνώσιμη μορφή string.
        return "STACK " + blockToStack + " " + blockToStackOn;
    }
}