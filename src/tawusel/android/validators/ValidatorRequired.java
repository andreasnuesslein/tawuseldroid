package tawusel.android.validators;


public class ValidatorRequired<T> implements Validator<T> {


	public ValidatorRequired() {
	}

	public boolean validate(T input) {
		String strInput = (String) input;
		return (strInput.length() > 0 );

	}

	public String getErrorMessage() {
		return "is required";
	}

}
