package havabol;

public class Parse {
	// Create the SymbolTable
    private SymbolTable symbolTable = new SymbolTable();
    // scanner is passed in from havabol class
    private Scanner scan;
    // the debug will be created in constructor if it is null
    private static Debug dBug;
	
    public Parse(Scanner s){
		 this.scan = s;
		 // we only want one instance of the debug class
		 if(dBug == null)
		 {
			 dBug = new Debug();
		 }
	}

	
	public void parseStmt(){
		// continue until end of file
		while(scan.currentToken.primClassif != Token.EOF){
			// check the primary class of the current token
			switch(scan.currentToken.primClassif){
			
			// in the case of debug statement set the appropriate variables 
				case Token.DEBUG:
					if(scan.currentToken.tokenStr.equals("bShowToken")){
						if(scan.nextToken.tokenStr.equals("on")){
							dBug.setShowToken(true);
						}else if(scan.nextToken.tokenStr.equals("off")){
							dBug.setShowToken(false);
						}
					}
					if(scan.currentToken.tokenStr.equals("bShowExpr")){
						if(scan.nextToken.tokenStr.equals("on")){
							dBug.setShowExpr(true);
						}else if(scan.nextToken.tokenStr.equals("off")){
							dBug.setShowExpr(false);
						}
					}
					if(scan.currentToken.tokenStr.equals("bShowAssin")){
						if(scan.nextToken.tokenStr.equals("on")){
							dBug.setShowAssign(true);
						}else if(scan.nextToken.tokenStr.equals("off")){
							dBug.setShowAssign(false);
						}
					}
					
			}
		}
		
		
	}
}
