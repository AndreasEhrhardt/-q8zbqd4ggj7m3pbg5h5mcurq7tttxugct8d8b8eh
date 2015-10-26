//################################################################################
//## Enums

public enum FarbEnum {

	// ################################################################################
	// ## Properties

	schwarz, wei�;

	// ################################################################################
	// ## Methods

	/**
	 * @param color
	 *            Checks the color of the token and returns it.
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
