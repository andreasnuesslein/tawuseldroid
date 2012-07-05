package tawusel.android.validators;


public class ValidatorMinLen<T> implements Validator<T> {

	private int cmpValue;

	public ValidatorMinLen(int cmpValue) {
		this.cmpValue = cmpValue;
	}

	public boolean validate(T input) {
		String strInput = (String) input;
		return (strInput.length() >= this.cmpValue);

	}

	public String getErrorMessage() {
		return "must have min "+ this.cmpValue +" characters";
	}

}
