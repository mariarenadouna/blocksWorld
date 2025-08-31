import java.util.*;

public class UnstackAction implements Action {
    private final String blockToUnstack; // Ο κύβος που θα ξεστοιβάζεται (το X)
    private final String blockFrom;       // Ο κύβος από τον οποίο θα ξεστοιβάζεται (το Y)

    public UnstackAction(String blockX, String blockY) {
        this.blockToUnstack = blockX;
        this.blockFrom = blockY;
    }

    @Override
    public boolean isApplicable(State currentState) {
        // Η μέθοδος isApplicable ελέγχει τις προϋποθέσεις για την ενέργεια UNSTACK(X, Y)
        // 1. Το χέρι πρέπει να είναι άδειο
        return currentState.isHandEmpty() &&
                // 2. Ο κύβος X πρέπει να υπάρχει στο blockPosition map (δηλαδή να μην είναι στο χέρι)
                currentState.getBlockPosition().containsKey(blockToUnstack) &&
                // 3. Ο κύβος X πρέπει να είναι ακριβώς πάνω στον κύβο Y
                currentState.getBlockPosition().get(blockToUnstack).equals(blockFrom) &&
                // 4. Ο κύβος Y δεν μπορεί να είναι το τραπέζι, καθώς για να μετακινήσεις από το τραπέζι χρησιμοποιείται η PICKUP
                !blockFrom.equalsIgnoreCase("table") &&
                // 5. Ο κύβος X πρέπει να είναι "clear" (να μην έχει κάτι πάνω του)
                currentState.getClearBlocks().contains(blockToUnstack);
    }

    @Override
    public State apply(State currentState) {
        // Η μέθοδος apply δημιουργεί τη νέα κατάσταση μετά την ενέργεια
        if (!isApplicable(currentState)) {
            return null; // Αν η ενέργεια δεν είναι εφαρμόσιμη, επιστρέφει null
        }

        // Δημιουργούμε ένα νέο αντικείμενο State ως αντίγραφο της τρέχουσας κατάστασης
        State newState = new State(currentState);

        // --- Επιδράσεις της ενέργειας ---
        // 1. Αφαιρούμε τον κύβο X από το blockPosition map, καθώς δεν είναι πλέον πάνω στον κύβο Y
        newState.getBlockPosition().remove(blockToUnstack);
        // 2. Το χέρι δεν είναι πλέον άδειο
        newState.setHandEmpty(false);
        // 3. Το χέρι κρατάει τον κύβο X
        newState.setHeldBlock(blockToUnstack);

        return newState; // Επιστρέφουμε τη νέα κατάσταση
    }

    @Override
    public String getName() {
        // Επιστρέφει το όνομα της ενέργειας σε μορφή string
        return "UNSTACK " + blockToUnstack + " " + blockFrom;
    }
}