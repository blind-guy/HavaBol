package havabol;

/*
 * This class is just for testing different components separate from the HavaBol main class. Feel free
 * to use it as you need to.
 */
public class TestMain {

	public static void main(String[] args) {
		// Let's test ResultValue functions
		try {
			ResultValue anInt1 = new ResultValue(Token.STRING, "1");
			ResultValue anInt2 = new ResultValue(Token.INTEGER, "1");
			ResultValue res = anInt1.performOperation(anInt2, "==");
			System.out.println(anInt1 + " == " + anInt2 + " = " + res);
			
			ResultValue anInt3 = new ResultValue(Token.STRING, "T");
			ResultValue anInt4 = new ResultValue(Token.BOOLEAN, "T");
			ResultValue res2 = anInt3.performOperation(anInt4, "<=");
			System.out.println(anInt3 + " <= " + anInt4 + " = " + res2);
			
			ResultValue anInt5 = new ResultValue(Token.STRING, "2");
			ResultValue anInt6 = new ResultValue(Token.INTEGER, "1");
			ResultValue res3 = anInt5.performOperation(anInt6, "#");
			System.out.println(anInt5 + " # " + anInt6 + " = " + res3);
			
			anInt5 = new ResultValue(Token.STRING, "2");
			anInt6 = new ResultValue(Token.STRING, "1");
			res3 = anInt5.performOperation(anInt6, "#");
			System.out.println(anInt5 + " # " + anInt6 + " = " + res3);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
