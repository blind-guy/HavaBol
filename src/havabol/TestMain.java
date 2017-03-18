package havabol;

import java.util.Stack;

/*
 * This class is just for testing different components separate from the HavaBol main class. Feel free
 * to use it as you need to.
 */
public class TestMain {
	private static Scanner scan;
	private static SymbolTable symbolTable;

	public static void main(String[] args) {
		// Let's test ResultValue functions
		try {
			ResultValue anInt1 = new ResultValue(Token.STRING, "1");
			ResultValue anInt2 = new ResultValue(Token.INTEGER, "1");
			ResultValue res = anInt1.performOperation(anInt2, "!=");
			System.out.println(anInt1 + " != " + anInt2 + " = " + res);
			
			ResultValue anInt3 = new ResultValue(Token.BOOLEAN, true);
			ResultValue anInt4 = new ResultValue(Token.BOOLEAN, true);
			ResultValue res2 = anInt3.performOperation(anInt4, "and");
			System.out.println(anInt3 + " and " + anInt4 + " = " + res2);
			
			ResultValue anInt5 = new ResultValue(Token.STRING, "2");
			ResultValue anInt6 = new ResultValue(Token.INTEGER, "1");
			ResultValue res3 = anInt5.performOperation(anInt6, "#");
			System.out.println(anInt5 + " # " + anInt6 + " = " + res3);
			
			anInt5 = new ResultValue(Token.STRING, "2");
			anInt6 = new ResultValue(Token.STRING, "1");
			res3 = anInt5.performOperation(anInt6, "#");
			System.out.println(anInt5 + " # " + anInt6 + " = " + res3);
			
			symbolTable = new SymbolTable();
			scan = new Scanner("docs/project3/p3Input.txt", symbolTable);
			//expressions();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public static void expressions() throws Exception {
		Stack<ResultValue> operandStack = new Stack<ResultValue>();
		Stack<String> operatorStack = new Stack<String>();
		boolean startCollectingExpression = false;
		
		ResultValue temp = new ResultValue();
		String tempOp = "";
		
		while (scan.currentToken.primClassif != Token.EOF) {
			scan.getNext();
			
			if (scan.currentToken.primClassif == Token.FLOW
					|| (scan.currentToken.primClassif == Token.OPERATOR
					&& scan.currentToken.tokenStr.equals("="))
					|| scan.currentToken.primClassif == Token.SEPARATOR
					&& !scan.currentToken.tokenStr.equals(";"))
			{
				startCollectingExpression = true;
				while (startCollectingExpression) {
					scan.getNext();
					if (scan.currentToken.subClassif == Token.SEPARATOR) {
						startCollectingExpression = false;
						break;
					}
					
					if (scan.currentToken.primClassif == Token.OPERAND)
						temp = new ResultValue(scan.currentToken.subClassif, 
							scan.currentToken.tokenStr);
					else if (scan.currentToken.primClassif == Token.OPERATOR)
						tempOp = scan.currentToken.tokenStr;
						
					if (scan.currentToken.primClassif == Token.OPERAND)
						operandStack.push(temp);
					else if (scan.currentToken.primClassif == Token.OPERATOR)
						operatorStack.push(tempOp);
					else
						throw new Exception("neither operator, nor operand, but something dreadful");
				}
				
				if (operandStack.size() == 1)
				{
					if (operatorStack.size() == 1)
						System.out.println("\t" + operatorStack.pop() + " " + operandStack.pop());
					else
						System.out.println("\t" + operandStack.pop());
				}
				else if (operandStack.size() == 2) {
					ResultValue op1, op2;
					op2 = operandStack.pop();
					op1 = operandStack.pop();
					String oper = operatorStack.pop();
					System.out.println("\t" + op1 + " " + oper + " " + op2 + " = " 
							+ op1.performOperation(op2, oper));
				}
			}
		}
	}

}
