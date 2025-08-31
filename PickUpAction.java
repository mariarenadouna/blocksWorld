import java.util.*;

public class PickUpAction implements Action {
    private final String blockToPickUp; // Ο κύβος που θα σηκωθεί

    public PickUpAction(String block) {
        this.blockToPickUp = block;
    }

    @Override
    public boolean isApplicable(State currentState) {
        // Η μέθοδος isApplicable ελέγχει αν η ενέργεια PICKUP μπορεί να εκτελεστεί.
        // Πρέπει να ισχύουν όλες οι παρακάτω προϋποθέσεις ταυτόχρονα.

        // 1. Το χέρι πρέπει να είναι άδειο.
        return currentState.isHandEmpty() &&
                // 2. Ο κύβος πρέπει να βρίσκεται πάνω στο τραπέζι.
                // Αυτό ελέγχεται από το blockPosition map.
                currentState.getBlockPosition().get(blockToPickUp).equals("TABLE") &&
                // 3. Ο κύβος πρέπει να είναι "clear" (να μην έχει τίποτα πάνω του).
                currentState.getClearBlocks().contains(blockToPickUp);
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
        // 1. Αφαιρούμε τον κύβο από το blockPosition map, καθώς δεν είναι πλέον στο τραπέζι.
        newState.getBlockPosition().remove(blockToPickUp);
        // 2. Το χέρι δεν είναι πλέον άδειο.
        newState.setHandEmpty(false);
        // 3. Το χέρι κρατάει τον κύβο που σηκώθηκε.
        newState.setHeldBlock(blockToPickUp);

        return newState; // Επιστρέφουμε τη νέα κατάσταση
    }

    @Override
    public String getName() {
        // Επιστρέφει το όνομα της ενέργειας σε μια αναγνώσιμη μορφή string.
        return "PICKUP " + blockToPickUp;
    }
}