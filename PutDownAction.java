public class PutDownAction implements Action {

    private final String blockToPutDown;

    public PutDownAction(String block) {
        this.blockToPutDown = block;
    }

    @Override
    public boolean isApplicable(State currentState) {
        // Προϋποθέσεις για το PUTDOWN(X):
        // 1. Το χέρι δεν πρέπει να είναι άδειο.
        // 2. Το χέρι πρέπει να κρατάει τον κύβο X.
        // 3. Ο κύβος X πρέπει να είναι "clear" (δεν έχει κάτι πάνω του - αν και αυτό καλύπτεται από το χέρι που τον κρατάει).
        // (Δεν χρειάζεται να είναι clear, αφού το χέρι τον κρατάει, άρα είναι clear by definition)

        return (!currentState.isHandEmpty() && currentState.getHeldBlock()!=null && currentState.getHeldBlock().equals(blockToPutDown));

    }

    @Override
    public State apply(State currentState) {
        if (!isApplicable(currentState)) {
            return null; // Η ενέργεια δεν μπορεί να εφαρμοστεί
        }


        State newState = new State(currentState); // Χρησιμοποιεί τον copy constructor

        // Ο κύβος πηγαίνει στο τραπέζι
        newState.getBlockPosition().put(blockToPutDown, "table");
        newState.setHandEmpty(true);
        newState.setHeldBlock(null);
        return newState;
    }

    @Override
    public String getName() {
        return "PUTDOWN " + blockToPutDown;
    }
}
