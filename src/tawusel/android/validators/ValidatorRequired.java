package tawusel.android.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorRequired<T> implements Validator<T> {


	public ValidatorRequired() {
	}

	public boolean validate(T input) {
		String strInput = (String) input;
		return (strInput.length() > 0 );

	}

	@Override
	public String getErrorMessage() {
		return "is required";
	}

}
