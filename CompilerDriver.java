import java.util.*;

// =================== SYMBOL TABLE CLASS ===================
class SymbolTable {
    private static final Map<String, SymbolEntry> table = new LinkedHashMap<>();

    static class SymbolEntry {
        String name;
        String type;
        String value;
        boolean isConstant;
        String scope;

        SymbolEntry(String name, String type, String value, boolean isConstant, String scope) {
            this.name = name;
            this.type = type;
            this.value = value;
            this.isConstant = isConstant;
            this.scope = scope;
        }
    }

    public static void addVariable(String name, String type, String value, boolean isConstant, String scope) {
        if (!table.containsKey(name)) {
            table.put(name, new SymbolEntry(name, type, value, isConstant, scope));
        } else {
            System.out.println("ERROR: Variable '" + name + "' already declared in scope: " + scope);
        }
    }

    public static void updateVariable(String name, String value) {
        if (table.containsKey(name)) {
            SymbolEntry entry = table.get(name);
            if (entry.isConstant) {
                System.out.println("ERROR: Cannot modify constant '" + name + "'");
            } else {
                entry.value = value;
            }
        } else {
            System.out.println("ERROR: Variable '" + name + "' is not declared!");
        }
    }

    public static boolean isDeclared(String name) {
        return table.containsKey(name);
    }

    public static void displayTable() {
        System.out.println("\n===== Symbol Table =====");
        System.out.printf("%-10s %-10s %-15s %-10s %-10s\n", "Name", "Type", "Value", "Constant", "Scope");
        System.out.println("--------------------------------------------------------");
        for (SymbolEntry entry : table.values()) {
            System.out.printf("%-10s %-10s %-15s %-10s %-10s\n",
                    entry.name, entry.type, entry.value, entry.isConstant, entry.scope);
        }
        System.out.println("========================================================");
    }
}

// =================== LEXICAL ANALYZER CLASS ===================
class LexicalAnalyzer {
    private static final Set<String> keywords = new HashSet<>(Arrays.asList(
            "define", "as", "show", "receive", "if", "else", "while", "for", "return",
            "true", "false", "and", "or", "not"));

    private static final Set<Character> operators = new HashSet<>(Arrays.asList(
            '+', '-', '*', '/', '%', '^', '=', '!', '<', '>'));

    private static final Set<String> dataTypes = new HashSet<>(Arrays.asList(
            "wholenum", "fractnum", "truthval", "singlechar"));

    private static final Set<Character> symbols = new HashSet<>(Arrays.asList(
            ';', '(', ')', '{', '}'));

    public static void analyze(String input) {
        List<String> tokens = new ArrayList<>();
        int lineNum = 1;
        String[] lines = input.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();

            if (line.startsWith("--")) continue;

            if (line.contains("/*")) {
                while (!line.contains("*/") && i < lines.length - 1) {
                    i++;
                    line = lines[i].trim();
                }
                continue;
            }

            String[] words = line.split("(?=[;(){}=+\\-*/%])|\\s+|(?<=[;(){}=+\\-*/%])");


            for (int j = 0; j < words.length; j++) {
                String word = words[j].trim();
                if (word.isEmpty()) continue;

                if (keywords.contains(word)) {
                    tokens.add("KEYWORD: " + word);
                } else if (dataTypes.contains(word)) {
                    tokens.add("DATA_TYPE: " + word);
                } else if (word.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    tokens.add("IDENTIFIER: " + word);

                    if (j > 0 && words[j - 1].equals("define")) {
                        String varName = word;
                        String varType = (j + 2 < words.length) ? words[j + 2] : "UNKNOWN";
                        String varValue = "UNDEFINED";
                        if (j + 4 < words.length && words[j + 3].equals("=")) {
                            varValue = words[j + 4].replace(";", "");
                        }
                        boolean isConstant = false;
                        String scope = "GLOBAL";

                        if (dataTypes.contains(varType)) {
                            SymbolTable.addVariable(varName, varType, varValue, isConstant, scope);
                        }
                    }
                } else if (word.matches("\\d+(\\.\\d+)?")) {
                    tokens.add("NUMBER: " + word);
                } else if (word.matches("\\\".*\\\"")) {
                    tokens.add("STRING: " + word);
                } else if (word.length() == 1 && operators.contains(word.charAt(0))) {
                    tokens.add("OPERATOR: " + word);
                } else if (word.length() == 1 && symbols.contains(word.charAt(0))) {
                    tokens.add("SYMBOL: " + word);
                } else {
                    System.out.println("ERROR (Line " + lineNum + "): Unrecognized token - " + word);
                }
            }
            lineNum++;
        }

        System.out.println("\nTokens Identified:");
        for (String token : tokens) {
            System.out.println(token);
        }

        SymbolTable.displayTable();
    }
}

// =================== COMPILER DRIVER CLASS ===================
public class CompilerDriver {
    public static void main(String[] args) {
        String sampleCode = """
            define x as wholenum = 10;
            define y as fractnum = 3.14;
            define z as truthval = true;
        """;
        LexicalAnalyzer.analyze(sampleCode);
    }
}
