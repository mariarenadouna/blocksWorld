
public interface Action {

    boolean isApplicable(State currentState);
    State apply(State currentState);
    String getName();
}