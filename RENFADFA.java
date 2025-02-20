import java.util.*;

// Token Classifier
class TokenClassifier {
    private static final String[] keywords = {"define", "as", "show", "receive", "if", "else", "while", "for", "return", "true", "false", "and", "or", "not"};
    private static final char[] operators = {'+', '-', '*', '/', '%', '^', '=', '!', '<', '>'};
    private static final String[] dataTypes = {"wholenum", "fractnum", "truthval", "singlechar"};
    
    public static boolean isKeyword(String word) {
        for (String k : keywords) {
            if (k.equals(word)) return true;
        }
        return false;
    }
    
    public static boolean isDataType(String word) {
        for (String d : dataTypes) {
            if (d.equals(word)) return true;
        }
        return false;
    }
    
    public static boolean isOperator(char c) {
        for (char op : operators) {
            if (op == c) return true;
        }
        return false;
    }
}

// NFA Generator
class NFAGenerator {
    private List<String> states = new ArrayList<>();
    private Map<String, Map<Character, String>> transitions = new HashMap<>();
    private String startState;
    private String acceptState;
    
    public void generateNFA(String token) {
        states.clear();
        transitions.clear();
        startState = "S0";
        states.add(startState);
        String current = startState;
        
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            String next = "S" + (i + 1);
            states.add(next);
            transitions.putIfAbsent(current, new HashMap<>());
            transitions.get(current).put(c, next);
            current = next;
        }
        acceptState = current;
    }
    
    public void displayNFA(String token) {
        System.out.println("\nNFA for: " + token);
        System.out.println("States: " + states);
        System.out.println("Start: " + startState);
        System.out.println("Accept: " + acceptState);
        System.out.println("Transitions:");
        System.out.printf("%-12s %-12s %-12s\n", "From", "Input", "To");
        System.out.println("--------------------------------------");
        for (String from : transitions.keySet()) {
            for (Map.Entry<Character, String> entry : transitions.get(from).entrySet()) {
                System.out.printf("%-12s %-12s %-12s\n", from, entry.getKey(), entry.getValue());
            }
        }
    }
    
    public String getStart() { return startState; }
    public String getAccept() { return acceptState; }
    public Map<String, Map<Character, String>> getTransitions() { return transitions; }
}

// DFA Transformer
class DFABuilder {
    private Set<String> dfaStates = new HashSet<>();
    private Set<String> finalStates = new HashSet<>();
    private Map<String, Map<Character, String>> transitionTable = new HashMap<>();
    
    public void constructDFA(NFAGenerator nfa) {
        dfaStates.clear();
        finalStates.clear();
        transitionTable.clear();
        
        List<Character> alphabet = new ArrayList<>();
        for (Map<Character, String> trans : nfa.getTransitions().values()) {
            for (Character symbol : trans.keySet()) {
                if (!alphabet.contains(symbol)) alphabet.add(symbol);
            }
        }
        
        List<String> queue = new ArrayList<>();
        queue.add(nfa.getStart());
        dfaStates.add(nfa.getStart());
        
        while (!queue.isEmpty()) {
            String current = queue.remove(0);
            transitionTable.putIfAbsent(current, new HashMap<>());
            
            for (Character symbol : alphabet) {
                String next = nfa.getTransitions().getOrDefault(current, new HashMap<>()).get(symbol);
                if (next != null && !dfaStates.contains(next)) {
                    dfaStates.add(next);
                    queue.add(next);
                }
                transitionTable.get(current).put(symbol, next);
            }
        }
        
        if (dfaStates.contains(nfa.getAccept())) {
            finalStates.add(nfa.getAccept());
        }
    }
    
    public void displayDFA(String token) {
        System.out.println("\nDFA for: " + token);
        System.out.println("States: " + dfaStates);
        System.out.println("Final States: " + finalStates);
        System.out.println("Transitions:");
        System.out.printf("%-20s %-10s %-20s\n", "Current State", "Input", "Next State");
        System.out.println("--------------------------------------------------------");
        for (String state : transitionTable.keySet()) {
            for (Map.Entry<Character, String> entry : transitionTable.get(state).entrySet()) {
                System.out.printf("%-20s %-10s %-20s\n", state, entry.getKey(), entry.getValue());
            }
        }
    }
}

// Execution Engine
public class RENFADFA {
    public static void main(String[] args) {
        String[] tokens = {"define", "show", "if", "return", "+", "-", "*", "/", "<=", ">=", "true", "false"};
        
        for (String token : tokens) {
            NFAGenerator nfa = new NFAGenerator();
            DFABuilder dfa = new DFABuilder();
            
            nfa.generateNFA(token);
            nfa.displayNFA(token);
            
            dfa.constructDFA(nfa);
            dfa.displayDFA(token);
        }
    }
}
