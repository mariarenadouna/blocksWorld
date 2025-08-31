import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public static Problem parse(String filename) throws IOException {
        // Η κύρια μέθοδος που διαβάζει και αναλύει το αρχείο PDDL του προβλήματος
        List<String> allBlocks = new ArrayList<>();
        Map<String, String> initialStatePositions = new HashMap<>();
        Map<String, String> goalStatePositions = new HashMap<>();

        Set<String> blocksInitiallyOnTable = new HashSet<>();

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        StringBuilder initContentBuilder = new StringBuilder();
        StringBuilder goalContentBuilder = new StringBuilder();
        boolean inInitSection = false;
        boolean inGoalSection = false;

        // --- Ανάγνωση του αρχείου γραμμή-γραμμή ---

        while ((line = reader.readLine()) != null) {
            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            // --- 1. Ανάλυση της γραμμής (:objects...) για να βρούμε όλους τους κύβους ---
            if (line.startsWith("(:objects")) {
                Pattern p = Pattern.compile(":\\s*objects\\s*([A-Z\\s]+)");
                Matcher m = p.matcher(line);
                if (m.find()) {
                    String blockNames = m.group(1).trim();
                    for (String block : blockNames.split("\\s+")) {
                        if (!block.isEmpty()) {
                            allBlocks.add(block);
                        }
                    }
                }
                // --- 2. Ανάγνωση του block (:INIT...) ---
            } else if (line.startsWith("(:INIT")) {
                inInitSection = true;
                inGoalSection = false;
                initContentBuilder.append(line.substring(line.indexOf("(:INIT") + 6));

                // --- 3. Ανάγνωση του block (:goal...) ---
            } else if (line.startsWith("(:goal")) {
                inGoalSection = true;
                inInitSection = false;
                goalContentBuilder.append(line.substring(line.indexOf("(:goal") + 6));
            } else if (line.endsWith(")") && (inInitSection || inGoalSection)) {
                if (inInitSection) {
                    initContentBuilder.append(line);
                    inInitSection = false;
                } else if (inGoalSection) {
                    goalContentBuilder.append(line);
                    inGoalSection = false;
                }
            } else if (inInitSection) {
                initContentBuilder.append(line);
            } else if (inGoalSection) {
                goalContentBuilder.append(line);
            }
        }
        reader.close();

        parsePredicates(initContentBuilder.toString(), initialStatePositions, blocksInitiallyOnTable, allBlocks, "INIT");
        parsePredicates(goalContentBuilder.toString(), goalStatePositions, null, allBlocks, "GOAL");
        for (String block : allBlocks) {
            if (!initialStatePositions.containsKey(block)) {
                initialStatePositions.put(block, "table");
            }
        }

        State initialState = new State(allBlocks, initialStatePositions);
        State goalState = new State(allBlocks, goalStatePositions);

        return new Problem(initialState, goalState, allBlocks);
    }

    private static void parsePredicates(String content, Map<String, String> positions, Set<String> onTableBlocks, List<String> allBlocks, String type) {

        content = content.replace(")", " ").replace("(", " ").trim();
        if (content.startsWith("AND")) {
            content = content.substring(3).trim();
        }

        Pattern p = Pattern.compile("([A-Z]+)\\s+([A-Z])(?:\\s+([A-Z]))?");
        Matcher m = p.matcher(content);

        while (m.find()) {
            String predicateType = m.group(1);
            String block1 = m.group(2);
            String block2 = m.group(3);

            if ("ON".equals(predicateType)) {
                if (block2 != null) {
                    positions.put(block1, block2);
                    if (onTableBlocks != null) {
                        onTableBlocks.remove(block1);
                    }
                }
            } else if ("ONTABLE".equals(predicateType)) {
                if (onTableBlocks != null) {
                    onTableBlocks.add(block1);
                } else {
                    positions.put(block1, "table");
                }
            }
        }
    }

    public static class Problem {
        private final State initialState;
        private final State goalState;
        private final List<String> allBlocks;

        public Problem(State initialState, State goalState, List<String> allBlocks) {
            this.initialState = initialState;
            this.goalState = goalState;
            this.allBlocks = allBlocks;
        }

        public State getInitialState() {
            return initialState;
        }

        public State getGoalState() {
            return goalState;
        }

        public List<String> getAllBlocks() {
            return allBlocks;
        }
    }
}