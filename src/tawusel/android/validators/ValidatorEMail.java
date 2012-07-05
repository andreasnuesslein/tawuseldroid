package tawusel.android.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorEMail<String> implements Validator<String> {

	private Pattern pattern;
	private Matcher matcher;

	private java.lang.String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	public ValidatorEMail() {
		pattern = Pattern.compile(EMAIL_PATTERN);
	}

	
	public boolean validate(String input) {

		matcher = pattern.matcher((java.lang.String)input);
		return matcher.matches();
	}


	public java.lang.String getErrorMessage() {
		
		return "no valid email";
	}
	
	

}
