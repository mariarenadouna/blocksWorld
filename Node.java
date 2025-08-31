import java.util.Objects;

public class Node implements Comparable<Node> {

    private final State state;
    private final Node parent;
    private final Action action;
    private final int gCost;
    private int hCost;
    private int fCost;

    public Node(State state, Node parent, Action action, int gCost, int hCost) {
        this.state = state;
        this.parent = parent;
        this.action = action;
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = gCost + hCost;
    }

    public State getState() {
        return state;
    }

    public Node getParent() {
        return parent;
    }

    public Action getAction() {
        return action;
    }

    public int gethCost() {
        return hCost;
    }

    public int getgCost() {
        return gCost;
    }

    public int getfCost() {
        return fCost;
    }

    // Διορθωμένη μέθοδος για να θέτει την τιμή του fCost
    public void setfCost(int fCost) {
        this.fCost = fCost;
    }

    public void sethCost(int hCost) {
        this.hCost = hCost;
        this.fCost = this.gCost + this.hCost;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.fCost, other.fCost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return Objects.equals(state, node.state); // Η ισότητα καθορίζεται από την κατάσταση
    }

    @Override
    public int hashCode() {
        return Objects.hash(state); // Ο hash καθορίζεται από την κατάσταση
    }
}