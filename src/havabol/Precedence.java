package havabol;

import java.util.HashMap;

public class Precedence {
	private HashMap<String, Integer> tokenPrecedence;
	private HashMap<String, Integer> stackPrecedence;
	
	public Precedence() {
		this.tokenPrecedence = new HashMap<String, Integer>();
		this.stackPrecedence = new HashMap<String, Integer>();
		tokenPrecedence.put("(", 15);
		stackPrecedence.put("(", 2);
		
		tokenPrecedence.put("u-", 12);
		stackPrecedence.put("u-", 12);
		
		tokenPrecedence.put("^", 11);
		stackPrecedence.put("^", 10);
		
		tokenPrecedence.put("*", 9);
		stackPrecedence.put("*", 9);
		
		tokenPrecedence.put("/", 9);
		stackPrecedence.put("/", 9);
		
		tokenPrecedence.put("+", 8);
		stackPrecedence.put("+", 8);
		
		tokenPrecedence.put("-", 8);
		stackPrecedence.put("-", 8);
		
		tokenPrecedence.put("#", 7);
		stackPrecedence.put("#", 7);
		
		tokenPrecedence.put("<", 6);
		stackPrecedence.put("<", 6);
		
		tokenPrecedence.put(">", 6);
		stackPrecedence.put(">", 6);
		
		tokenPrecedence.put("<=", 6);
		stackPrecedence.put("<=", 6);
		
		tokenPrecedence.put(">=", 6);
		stackPrecedence.put(">=", 6);
		
		tokenPrecedence.put("==", 6);
		stackPrecedence.put("==", 6);
		
		tokenPrecedence.put("!=", 6);
		stackPrecedence.put("!=", 6);
		
		tokenPrecedence.put("in", 6);
		stackPrecedence.put("in", 6);
		
		tokenPrecedence.put("notin", 6);
		stackPrecedence.put("notin", 6);
		
		tokenPrecedence.put("not", 5);
		stackPrecedence.put("not", 5);
		
		tokenPrecedence.put("and", 4);
		stackPrecedence.put("and", 4);
		
		tokenPrecedence.put("or", 4);
		stackPrecedence.put("or", 4);
	}
	
	public int getTokenPrecedence(String key) {
		return tokenPrecedence.get(key);
	}
	
	public int getStackPrecedence(String key) {
		return stackPrecedence.get(key);
	}
	
	public boolean containsKey(String key) {
		return tokenPrecedence.containsKey(key);
	}
}
