import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String searchMethod = "";
        boolean validMethod = false;

        while (!validMethod) {
            System.out.print("Enter search method ('breadth', 'depth', 'best', or 'astar'): ");
            searchMethod = scanner.nextLine().toLowerCase().trim(); // Μετατροπή σε πεζά και αφαίρεση κενών

            if (searchMethod.equals("depth") || searchMethod.equals("breadth") || searchMethod.equals("best") || searchMethod.equals("astar")) {
                validMethod = true;
            } else {
                System.out.println("Invalid search method. Please enter one of the following: 'breadth', 'depth', 'best', or 'astar'.");
            }
        }

        System.out.print("Enter path to problem file (e.g., src/problems/probBLOCKS-4-0.txt): ");
        String problemFilePath = scanner.nextLine();

        System.out.print("Enter path for solution output file (e.g., solution.txt): ");
        String solutionFilePath = scanner.nextLine();

        List<String> allBlocks = new ArrayList<>();
        Map<String, String> initialBlockPositions = new HashMap<>();
        Map<String, String> goalBlockPositions = new HashMap<>();

        //Ανάλυση των γραμμών του αρχείου PPDL
        Pattern objectsPattern = Pattern.compile(":\\s*objects\\s+(.+?)\\s*\\)");
        Pattern fullPredicatePattern = Pattern.compile("\\([A-Z]+\\s*(?:[A-Z]+(?:\\s+[A-Z]+)?)?\\)");

        try (BufferedReader reader = new BufferedReader(new FileReader(problemFilePath))) {
            String line;

            StringBuilder initBlockContent = new StringBuilder();
            StringBuilder goalBlockContent = new StringBuilder();
            boolean inInitBlock = false;
            boolean inGoalBlock = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // --- 1. Ανάλυση της γραμμής (:objects) ---
                Matcher objectsMatcher = objectsPattern.matcher(line);
                if (objectsMatcher.find()) {
                    String blocksStr = objectsMatcher.group(1).trim();
                    String[] blocksArray = blocksStr.split("\\s+");
                    for (String block : blocksArray) {
                        if (!block.isEmpty()) {
                            allBlocks.add(block);
                        }
                    }
                    continue;
                }

                // --- 2. Ανάλυση του block (:INIT...) ---
                if (line.startsWith("(:INIT")) {
                    inInitBlock = true;
                    initBlockContent.append(line.substring("(:INIT".length()).trim());
                    if (line.endsWith("))")) {
                        inInitBlock = false;
                    }
                    continue;
                }

                if (inInitBlock) {
                    initBlockContent.append(" ").append(line);
                    if (line.endsWith("))") || line.endsWith(")")) {
                        inInitBlock = false;
                    }
                    continue;
                }

                // --- 3. Ανάλυση του block (:goal...) ---
                if (line.startsWith("(:goal")) {
                    inGoalBlock = true;
                    goalBlockContent.append(line.substring("(:goal".length()).trim());
                    if (line.endsWith(")))")) {
                        inGoalBlock = false;
                    }
                    continue;
                }

                if (inGoalBlock) {
                    goalBlockContent.append(" ").append(line);
                    if (line.endsWith(")))") || line.endsWith("))") || line.endsWith(")")) {
                        inGoalBlock = false;
                    }
                }
            }

            // --- Μετά την ανάγνωση του αρχείου, γίνεται η ανάλυση του περιεχομένου των blocks ---

            if (!initBlockContent.isEmpty()) {
                String finalInitContent = initBlockContent.toString().trim();
                if (finalInitContent.startsWith("(")) {
                    finalInitContent = finalInitContent.substring(1).trim();
                }
                if (finalInitContent.endsWith(")")) {
                    finalInitContent = finalInitContent.substring(0, finalInitContent.length() - 1).trim();
                }

                Matcher predicateMatcher = fullPredicatePattern.matcher(finalInitContent);
                while (predicateMatcher.find()) {
                    String fullPredicate = predicateMatcher.group(0);

                    if (fullPredicate.startsWith("(ONTABLE")) {
                        String block = fullPredicate.substring("(ONTABLE ".length(), fullPredicate.length() - 1);
                        initialBlockPositions.put(block, "TABLE");
                    } else if (fullPredicate.startsWith("(ON")) {
                        String content = fullPredicate.substring("(ON ".length(), fullPredicate.length() - 1);
                        String[] parts = content.split("\\s+");
                        if (parts.length == 2) {
                            initialBlockPositions.put(parts[0], parts[1].toUpperCase());
                        }
                    }
                }
            }

            if (!goalBlockContent.isEmpty()) {
                Map<String, String> tempGoalPositions = new HashMap<>();
                for(String block : allBlocks) {
                    tempGoalPositions.put(block, "TABLE");
                }

                String finalGoalContent = goalBlockContent.toString().trim();
                int startIndex = finalGoalContent.indexOf("(AND") + "(AND".length();
                int endIndex = finalGoalContent.lastIndexOf(')');
                if (endIndex > startIndex) {
                    finalGoalContent = finalGoalContent.substring(startIndex, endIndex).trim();
                } else {
                    finalGoalContent = "";
                }

                Matcher predicateMatcher = fullPredicatePattern.matcher(finalGoalContent);
                while (predicateMatcher.find()) {
                    String fullPredicate = predicateMatcher.group(0);

                    if (fullPredicate.startsWith("(ON")) {
                        String content = fullPredicate.substring("(ON ".length(), fullPredicate.length() - 1);
                        String[] parts = content.split("\\s+");
                        if (parts.length == 2) {
                            tempGoalPositions.put(parts[0], parts[1].toUpperCase());
                        }
                    }
                }
                goalBlockPositions = tempGoalPositions;
            }

        } catch (IOException e) {
            System.err.println("Error reading problem file: " + e.getMessage());
            return;
        }

        State initialState = new State(allBlocks, initialBlockPositions);
        State goalState = new State(allBlocks, goalBlockPositions);
        goalState.setHandEmpty(true);
        goalState.setHeldBlock(null);

        List<Action> solutionPath;
        long startTime = System.currentTimeMillis();

        if (searchMethod.equalsIgnoreCase("breadth")) {
            solutionPath = BlocksWorldSolver.breadthFirstSearch(initialState, goalState, allBlocks);
        } else if (searchMethod.equalsIgnoreCase("depth")) {
            solutionPath = BlocksWorldSolver.depthFirstSearch(initialState, goalState, allBlocks);
        } else if (searchMethod.equalsIgnoreCase("best")) {
            solutionPath = BlocksWorldSolver.bestFirstSearch(initialState, goalState, allBlocks);
        } else if (searchMethod.equalsIgnoreCase("astar")) {
            solutionPath = BlocksWorldSolver.astarSearch(initialState, goalState, allBlocks);
        } else {
            System.out.println("Invalid search method. Please choose 'depth', 'breadth', 'best', or 'astar'.");
            return;
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // --- Εκτύπωση και αποθήκευση της λύσης --
        if (solutionPath != null) {

            try (PrintWriter writer = new PrintWriter(solutionFilePath)) {
                printSolutionSteps(solutionPath, writer);
            } catch (IOException e) {
                System.err.println("Error writing solution to file: " + e.getMessage());
            }
            System.out.println();
            System.out.println("Solution written to " + solutionFilePath);
        } else {
            System.out.println("No solution found for the given problem.");
            try (PrintWriter writer = new PrintWriter(solutionFilePath)) {
                writer.println("No solution found.\n");
            } catch (IOException e) {
                System.err.println("Error writing to solution file: " + e.getMessage());
            }
        }

        System.out.println("Execution time: " + duration + "ms");
        scanner.close();
    }

    // --- Βοηθητική μέθοδος για την εκτύπωση των βημάτων της λύσης ---
    private static void printSolutionSteps(List<Action> solutionPath, PrintWriter writer) {
        List<String> combinedMoves = new ArrayList<>();
        for (int i = 0; i < solutionPath.size(); i++) {
            Action currentAction = solutionPath.get(i);
            String actionName = currentAction.getName();
            String[] parts = actionName.split(" ");

            if (parts[0].equals("PICKUP") && i + 1 < solutionPath.size()) {
                Action nextAction = solutionPath.get(i + 1);
                String[] nextParts = nextAction.getName().split(" ");

                if (nextParts[0].equals("STACK") && parts[1].equals(nextParts[1])) {
                    String block = parts[1];
                    String to = nextParts[2];
                    combinedMoves.add("Move(" + block + ", ontable, " + to + ")");
                    i++;
                    continue;
                }
            } else if (parts[0].equals("UNSTACK") && i + 1 < solutionPath.size()) {
                Action nextAction = solutionPath.get(i + 1);
                String[] nextParts = nextAction.getName().split(" ");

                if (nextParts[0].equals("PUTDOWN") && parts[1].equals(nextParts[1])) {
                    String block = parts[1];
                    String from = parts[2];
                    combinedMoves.add("Move(" + block + ", " + from + ", ontable)");
                    i++;
                    continue;
                }
            }

            combinedMoves.add(convertActionToString(currentAction));
        }
        System.out.println("\nSolution Found! Steps:" + combinedMoves.size());

        for (String move : combinedMoves) {
            System.out.println(move);
            writer.println(move);
        }
    }

    // --- Βοηθητική μέθοδος για τη μετατροπή Action σε String ---
    private static String convertActionToString(Action action) {
        String actionString = action.getName();
        String[] parts = actionString.split(" ");
        String actionName = parts[0];
        String block = "";
        String from = "";
        String to = "";

        if (parts.length > 1) {
            block = parts[1];
        }

        switch (actionName) {
            case "PICKUP":
                from = "ontable";
                to = "hand";
                break;
            case "PUTDOWN":
                from = "hand";
                to = "ontable";
                break;
            case "STACK":
                from = "hand";
                to = parts[2];
                break;
            case "UNSTACK":
                from = parts[2];
                to = "hand";
                break;
        }
        return "Move(" + block + ", " + from + ", " + to + ")";
    }
}