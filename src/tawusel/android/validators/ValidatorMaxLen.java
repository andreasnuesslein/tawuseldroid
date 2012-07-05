package tawusel.android.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ValidatorMaxLen<T> implements Validator<T> {

	private int cmpValue;

	public ValidatorMaxLen(int cmpValue) {
		this.cmpValue = cmpValue;
	}

	public boolean validate(T input) {
		String strInput = (String) input;
		return (strInput.length() <= this.cmpValue);

	}

	@Override
	public String getErrorMessage() {
		return "must have min "+ this.cmpValue +" characters";
	}

}
