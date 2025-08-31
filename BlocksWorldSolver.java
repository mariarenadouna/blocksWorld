import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlocksWorldSolver {


    public static List<Action> getApplicableActions(State currentState, List<String> allBlocks) {
        List<Action> applicableActions = new ArrayList<>();

        // --- PICKUP ---
        // Η ενέργεια PICKUP είναι δυνατή μόνο αν το χέρι είναι άδειο
        if (currentState.isHandEmpty()) {
            for (String block : allBlocks) {
                // Ελέγχουμε αν ο κύβος είναι "clear" (δεν έχει τίποτα πάνω του)
                if (currentState.isClear(block)) {
                    PickUpAction pickUp = new PickUpAction(block);
                    // Εάν η ενέργεια είναι εφαρμόσιμη (π.χ. ο κύβος είναι στο τραπέζι)
                    if (pickUp.isApplicable(currentState)) {
                        applicableActions.add(pickUp);
                    }
                }
            }
        }

        // --- PUTDOWN ---
        // Η ενέργεια PUTDOWN είναι δυνατή μόνο αν το χέρι κρατάει έναν κύβο
        if (!currentState.isHandEmpty()) {
            String heldBlock = currentState.getHeldBlock();
            PutDownAction putDown = new PutDownAction(heldBlock);
            if (putDown.isApplicable(currentState)) {
                applicableActions.add(putDown);
            }
        }

        // --- STACK / UNSTACK ---
        if (!currentState.isHandEmpty()) {
            String heldBlock = currentState.getHeldBlock();
            for (String blockY : allBlocks) {
                // Δεν μπορούμε να στοιβάξουμε έναν κύβο πάνω στον εαυτό του
                if (!heldBlock.equals(blockY) && currentState.isClear(blockY)) {
                    StackAction stack = new StackAction(heldBlock, blockY);
                    if (stack.isApplicable(currentState)) {
                        applicableActions.add(stack);
                    }
                }
            }
            // Αν το χέρι είναι άδειο, δοκιμάζουμε την ενέργεια UNSTACK
        } else {
            for (String blockX : allBlocks) {
                String parent = currentState.getBlockPosition().get(blockX);
                if (parent != null && currentState.isClear(blockX)) {
                    UnstackAction unstack = new UnstackAction(blockX, parent);
                    if (unstack.isApplicable(currentState)) {
                        applicableActions.add(unstack);
                    }
                }
            }
        }

        return applicableActions;
    }

    // --- Έλεγχος για την κατάσταση-στόχο ---
    public static boolean isGoal(State currentState, State goalState) {
        return currentState.getBlockPosition().equals(goalState.getBlockPosition()) &&
                currentState.isHandEmpty() == goalState.isHandEmpty();
    }

    // --- Ανακατασκευή της διαδρομής ---
    public static List<Action> reconstructPath(Node goalNode) {
        if (goalNode == null) {
            return null;
        }
        List<Action> path = new ArrayList<>();
        Node current = goalNode;
        while (current != null && current.getAction() != null) {
            path.add(current.getAction());
            current = current.getParent();
        }
        Collections.reverse(path);
        return path;
    }

    // --- Ευρετική Συνάρτηση ---
    private static int calculateHeuristic(State state, State goalState, List<String> allBlocks) {
        int incorrectlyPlacedBlocks = 0;

        for (String block : allBlocks) {
            String currentParent = state.getBlockPosition().get(block);
            String goalParent = goalState.getBlockPosition().get(block);

            // Αν ο κύβος είναι στο χέρι, τότε είναι λάθος τοποθετημένος
            if (!state.isHandEmpty() && state.getHeldBlock().equals(block)) {
                incorrectlyPlacedBlocks++;
                continue;
            }

            if (goalParent != null) {
                if (currentParent == null || !currentParent.equals(goalParent)) {
                    incorrectlyPlacedBlocks++;
                }
            }
        }

        return incorrectlyPlacedBlocks;
    }

    // --- Αλγόριθμος Breadth-First Search ---
    public static List<Action> breadthFirstSearch(State initialState, State goalState, List<String> allBlocks) {
        ArrayDeque<Node> queue = new ArrayDeque<>();
        Set<State> visitedStates = new HashSet<>();
        long startTime = System.currentTimeMillis();
        final long timeout = 300000;

        Node initialNode = new Node(initialState, null, null, 0, 0);
        queue.add(initialNode);
        visitedStates.add(initialState);

        while (!queue.isEmpty()) {
            if (System.currentTimeMillis() - startTime > timeout) {
                System.out.println("\nΣφάλμα: Η αναζήτηση ξεπέρασε το χρονικό περιθώριο.");
                return null;
            }

            Node currentNode = queue.poll();
            if (isGoal(currentNode.getState(), goalState)) {
                System.out.println("BFS: Goal found!");
                return reconstructPath(currentNode);
            }

            for (Action action : getApplicableActions(currentNode.getState(), allBlocks)) {
                State newState = action.apply(currentNode.getState());
                if (newState != null && visitedStates.add(newState)) {
                    Node newNode = new Node(newState, currentNode, action, currentNode.getgCost() + 1, 0);
                    queue.add(newNode);
                }
            }
        }
        System.out.println("BFS: No solution found.");
        return null;
    }

    // --- Αλγόριθμος Depth-First Search ---
    public static List<Action> depthFirstSearch(State initialState, State goalState, List<String> allBlocks) {
        ArrayDeque<Node> stack = new ArrayDeque<>();
        Set<State> visitedStates = new HashSet<>();
        long startTime = System.currentTimeMillis();
        final long timeout = 300000;

        Node initialNode = new Node(initialState, null, null, 0, 0);
        stack.push(initialNode);
        visitedStates.add(initialState);

        while (!stack.isEmpty()) {
            if (System.currentTimeMillis() - startTime > timeout) {
                System.out.println("\nΣφάλμα: Η αναζήτηση ξεπέρασε το χρονικό περιθώριο.");
                return null;
            }

            Node currentNode = stack.pop();
            if (isGoal(currentNode.getState(), goalState)) {
                System.out.println("DFS: Goal found!");
                return reconstructPath(currentNode);
            }

            List<Action> actions = getApplicableActions(currentNode.getState(), allBlocks);
            // push αντίστροφα για να αποφύγουμε Collections.reverse
            for (int i = actions.size() - 1; i >= 0; i--) {
                Action action = actions.get(i);
                State newState = action.apply(currentNode.getState());
                if (newState != null && visitedStates.add(newState)) {
                    Node newNode = new Node(newState, currentNode, action, currentNode.getgCost() + 1, 0);
                    stack.push(newNode);
                }
            }
        }
        System.out.println("DFS: No solution found.");
        return null;
    }

    // --- Αλγόριθμος Best-First Search ---
    public static List<Action> bestFirstSearch(State initialState, State goalState, List<String> allBlocks) {
        long startTime = System.currentTimeMillis();
        final long timeout = 300000;

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(Node::gethCost));
        Map<State, Integer> visitedCosts = new HashMap<>();

        Node initialNode = new Node(initialState, null, null, 0,
                calculateHeuristic(initialState, goalState, allBlocks));
        openList.add(initialNode);
        visitedCosts.put(initialState, initialNode.gethCost());

        while (!openList.isEmpty()) {
            if (System.currentTimeMillis() - startTime > timeout) {
                System.out.println("\nΣφάλμα: Η αναζήτηση ξεπέρασε το χρονικό περιθώριο.");
                return null;
            }

            Node currentNode = openList.poll();
            State currentState = currentNode.getState();

            if (isGoal(currentState, goalState)) {
                System.out.println("Best-First Search: Goal found!");
                return reconstructPath(currentNode);
            }

            for (Action action : getApplicableActions(currentState, allBlocks)) {
                State nextState = action.apply(currentState);
                if (nextState != null) {
                    int hCost = calculateHeuristic(nextState, goalState, allBlocks);

                    if (!visitedCosts.containsKey(nextState) || hCost < visitedCosts.get(nextState)) {
                        Node nextNode = new Node(nextState, currentNode, action,
                                currentNode.getgCost() + 1, hCost);
                        openList.add(nextNode);
                        visitedCosts.put(nextState, hCost);
                    }
                }
            }
        }
        System.out.println("Best-First Search: No solution found.");
        return null;
    }

    // --- Αλγόριθμος A* ---
    public static List<Action> astarSearch(State initialState, State goalState, List<String> allBlocks) {
        long startTime = System.currentTimeMillis();
        final long timeout = 300000;

        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingInt(Node::getfCost));
        Map<State, Integer> visitedCosts = new HashMap<>();

        Node initialNode = new Node(initialState, null, null, 0,
                calculateHeuristic(initialState, goalState, allBlocks));
        initialNode.setfCost(initialNode.getgCost() + initialNode.gethCost());
        openList.add(initialNode);
        visitedCosts.put(initialState, initialNode.getfCost());

        while (!openList.isEmpty()) {
            if (System.currentTimeMillis() - startTime > timeout) {
                System.out.println("\nΣφάλμα: Η αναζήτηση ξεπέρασε το χρονικό περιθώριο.");
                return null;
            }

            Node currentNode = openList.poll();
            State currentState = currentNode.getState();

            if (isGoal(currentState, goalState)) {
                System.out.println("A* Search: Goal found!");
                return reconstructPath(currentNode);
            }

            for (Action action : getApplicableActions(currentState, allBlocks)) {
                State nextState = action.apply(currentState);
                if (nextState != null) {
                    int gCost = currentNode.getgCost() + 1;
                    int hCost = calculateHeuristic(nextState, goalState, allBlocks);
                    int fCost = gCost + hCost;

                    if (!visitedCosts.containsKey(nextState) || fCost < visitedCosts.get(nextState)) {
                        Node nextNode = new Node(nextState, currentNode, action, gCost, hCost);
                        nextNode.setfCost(fCost);
                        openList.add(nextNode);
                        visitedCosts.put(nextState, fCost);
                    }
                }
            }
        }
        System.out.println("A* Search: No solution found.");
        return null;
    }
}
