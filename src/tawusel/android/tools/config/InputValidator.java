package tawusel.android.tools.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides the methods to check if the registration
 * data given by the user is correcty. Basically its methods match 
 * the Strings given by the input fields with an regular expression
 * and return booleans to validate the inputs.
 */
public class InputValidator {

	private static Pattern pattern;
	private static Matcher matcher;

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static final String PHONE_PATTERN = "[0-9 ]*";


	/**
	 * Validate hex with regular expression
	 * 
	 * @param hex
	 *            hex for validation
	 * @return true valid hex, false invalid hex
	 */
	public boolean validateEMail(final String hex) {
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(hex);
		return matcher.matches();

	}
	
	public boolean validatePhone(final String hex) {
		pattern = Pattern.compile(EMAIL_PATTERN);
		matcher = pattern.matcher(hex);
		return matcher.matches();

	}
}