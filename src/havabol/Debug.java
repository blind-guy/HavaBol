package havabol;

public class Debug {
	
	// these variables are set to private out of habit 
	// we can change to public and get rid of the getters and setters
	private boolean bShowToken;
	private boolean bShowExpr;
	private boolean bShowAssign;
	
	public Debug(){
		this.bShowToken = false;
		this.bShowExpr = false;
		this.bShowAssign = false;
	}
	// these functions could be eliminated just here because variables are private
	public boolean getShowToken(){
		return this.bShowToken;
	}
	
	public boolean getShowExpr(){
		return this.bShowExpr;
	}
	
	public boolean getShowAssign(){
		return this.bShowAssign;
	}
	
	public void setShowToken(boolean toggle){
		this.bShowToken = toggle;
	}
	
	public void setShowExpr(boolean toggle){
		this.bShowExpr = toggle;
	}
	
	public void setShowAssign(boolean toggle){
		this.bShowAssign = toggle;
	}

}
