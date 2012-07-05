package tawusel.android.validators;

public interface Validator <T> {

	public boolean validate(T input);
	public String getErrorMessage();
	
	
}
