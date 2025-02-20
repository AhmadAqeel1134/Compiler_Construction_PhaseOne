import java.util.*;

// =================== ERROR HANDLER CLASS ===================
class ErrorHandler {
    private static final List<String> errors = new ArrayList<>();

    public static void reportError(int line, String message) {
        errors.add("ERROR (Line " + line + "): " + message);
    }

    public static void displayErrors() {
        if (errors.isEmpty()) {
            System.out.println("\nNo errors found.");
        } else {
            System.out.println("\n===== Error Report =====");
            for (String error : errors) {
                System.out.println(error);
            }
            System.out.println("========================");
        }
    }
}

// =================== SYMBOL TABLE CLASS ===================
class SymbolTable {
    private static final Map<String, SymbolEntry> table = new LinkedHashMap<>();

    static class SymbolEntry {
        String name, type, value, scope;
        boolean isConstant;

        SymbolEntry(String name, String type, String value, boolean isConstant, String scope) {
            this.name = name;
            this.type = type;
            this.value = value;
            this.isConstant = isConstant;
            this.scope = scope;
        }
    }

    public static void addVariable(String name, String type, String value, boolean isConstant, String scope, int line) {
        if (!table.containsKey(name)) {
            table.put(name, new SymbolEntry(name, type, value, isConstant, scope));
        } else {
            ErrorHandler.reportError(line, "Variable '" + name + "' already declared in scope: " + scope);
        }
    }

    public static void updateVariable(String name, String value, int line) {
        if (table.containsKey(name)) {
            SymbolEntry entry = table.get(name);
            if (entry.isConstant) {
                ErrorHandler.reportError(line, "Cannot modify constant '" + name + "'");
            } else {
                entry.value = value;
            }
        } else {
            ErrorHandler.reportError(line, "Variable '" + name + "' is not declared!");
        }
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

            if (line.startsWith("--")) continue; // Ignore single-line comments

            if (line.contains("/*")) { // Ignore multi-line comments
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
                } else if (word.matches("[a-zA-Z_][a-zA-Z0-9_]*")) { // Identifier
                    tokens.add("IDENTIFIER: " + word);

                    if (j > 0 && words[j - 1].equals("define")) {
                        if (j + 2 < words.length && dataTypes.contains(words[j + 2])) {
                            String varName = word;
                            String varType = words[j + 2];
                            String varValue = "UNDEFINED";
                            if (j + 4 < words.length && words[j + 3].equals("=")) {
                                varValue = words[j + 4].replace(";", "");
                            }
                            boolean isConstant = false;
                            String scope = "GLOBAL";

                            SymbolTable.addVariable(varName, varType, varValue, isConstant, scope, lineNum);
                        } else {
                            ErrorHandler.reportError(lineNum, "Invalid variable declaration for '" + word + "'");
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
                    ErrorHandler.reportError(lineNum, "Unrecognized token - " + word);
                }
            }
            lineNum++;
        }

        System.out.println("\nTokens Identified:");
        for (String token : tokens) {
            System.out.println(token);
        }

        SymbolTable.displayTable();
        ErrorHandler.displayErrors();
    }
}

// =================== COMPILER DRIVER CLASS ===================
public class ErrorPhase {
    public static void main(String[] args) {
        String sampleCode = """
            define x as wholenum = 10;
            define y as fractnum = 3.14;
            define z as truthval = true;
            define x as singlechar = 'A'; -- Redeclaration Error
            define invalidvar = 100; -- Invalid declaration
            define a as wholenum = 5
        """;
        LexicalAnalyzer.analyze(sampleCode);
    }
}
