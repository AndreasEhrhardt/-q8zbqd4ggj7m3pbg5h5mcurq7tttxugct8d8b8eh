//################################################################################
//## Enums

public enum FarbEnum {
	
	//################################################################################
	//## Properties
	
	schwarz, wei�;

	//################################################################################
	//## Methods
	
	public static String getColorName(FarbEnum color){
		if(color == wei�) return "Wei�";
		else if(color == schwarz) return "Schwarz";
		else return "UNKOWN";
	}
}
