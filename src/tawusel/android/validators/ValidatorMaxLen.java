package tawusel.android.validators;


public class ValidatorMaxLen<T> implements Validator<T> {

	private int cmpValue;

	public ValidatorMaxLen(int cmpValue) {
		this.cmpValue = cmpValue;
	}

	public boolean validate(T input) {
		String strInput = (String) input;
		return (strInput.length() <= this.cmpValue);

	}

	public String getErrorMessage() {
		return "must have min "+ this.cmpValue +" characters";
	}

}
