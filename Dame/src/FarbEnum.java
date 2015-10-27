//################################################################################
//## Enums

public enum FarbEnum {

	// ################################################################################
	// ## Properties

	schwarz, wei�;

	// ################################################################################
	// ## Methods

	/**
	 * Checks the color of the token and returns it.
	 * @param color
	 */

	public static String getColorName(FarbEnum color) {
		if (color == wei�)
			return "Wei�";
		else if (color == schwarz)
			return "Schwarz";
		else
			return "UNKOWN";
	}
}
