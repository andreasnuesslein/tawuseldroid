package tawusel.android.ui;

import org.json.JSONException;
import org.json.JSONObject;

import tawusel.andorid.R;
import tawusel.android.LoginActivity;
import tawusel.android.tools.communication.JSONCommunicator;
import tawusel.android.tools.config.PropertyManager;
import tawusel.android.validators.Validator;
import tawusel.android.validators.ValidatorEMail;
import tawusel.android.validators.ValidatorMaxLen;
import tawusel.android.validators.ValidatorMinLen;
import tawusel.android.validators.ValidatorRequired;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 *
 */
public class RegisterActivity extends Activity implements OnClickListener {

	private EditText etFirstname, etLastname, etEMail, etMobile, etPassword,
			etPasswordRepeat;
	private Button btnRegister;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);
		initFields();
		btnRegister.setOnClickListener(this);
	}

	private void initFields() {
		etFirstname = (EditText) findViewById(R.id.register_etFirstname);
		etLastname = (EditText) findViewById(R.id.register_etLastname);
		etEMail = (EditText) findViewById(R.id.register_etEmail);
		etMobile = (EditText) findViewById(R.id.register_etMobilephone);
		etPassword = (EditText) findViewById(R.id.register_etPassword);
		etPasswordRepeat = (EditText) findViewById(R.id.register_etRepeatpassword);
		btnRegister = (Button) findViewById(R.id.register_bRegister);
	}

	
	public void onClick(View v) {

		if (v == btnRegister) {
			if (validateInput()) {
				JSONObject json = new JSONObject();
				try {
					json.put("email", this.etEMail.getText().toString());
					json.put("firstname", this.etFirstname.getText().toString());
					json.put("lastname", this.etLastname.getText().toString());
					json.put("phone", this.etMobile.getText().toString());
					json.put("password", this.etPassword.getText().toString());
				} catch (JSONException e) {
					ErrorDialog errorDialog = new ErrorDialog(this, e.toString(), e.getMessage());
					errorDialog.show();
				}
				if (writeUser(json)) {
					openLoginActivity();
				}
			}
		}
	}

	private void openLoginActivity() {
		Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
		startActivity(i);
	}

	
	/**
	 * validate the input, return true if everything is correct, false otherwise
	 * @return boolean
	 */
	public boolean validateInput() {
		String errMessage = "";

		Validator<String> valReq = new ValidatorRequired<String>();
		Validator<String> valEmail = new ValidatorEMail<String>();
		Validator<String> valMinLen = new ValidatorMinLen<String>(6);
		Validator<String> valMaxLen = new ValidatorMaxLen<String>(45);

		if (!valReq.validate(this.etFirstname.getText().toString())) {
			errMessage += "firstname : " + valReq.getErrorMessage() + "\n";
		}
		if (!valMaxLen.validate(this.etFirstname.getText().toString())) {
			errMessage += "firstname : " + valReq.getErrorMessage() + "\n";
		}

		if (!valReq.validate(this.etLastname.getText().toString())) {
			errMessage += "lastname : " + valReq.getErrorMessage() + "\n";
		}
		if (!valMaxLen.validate(this.etLastname.getText().toString())) {
			errMessage += "lastname : " + valReq.getErrorMessage() + "\n";
		}

		if (!valReq.validate(this.etEMail.getText().toString())) {
			errMessage += "email : " + valReq.getErrorMessage() + "\n";
		}
		if (!valEmail.validate(this.etEMail.getText().toString())) {
			errMessage += "email : " + valEmail.getErrorMessage() + "\n";
		}

		if (!valReq.validate(this.etEMail.getText().toString())) {
			errMessage += "password : " + valReq.getErrorMessage() + "\n";
		}
		if (!valMinLen.validate(this.etEMail.getText().toString())) {
			errMessage += "password : " + valEmail.getErrorMessage() + "\n";
		}

		if (!(this.etPassword.getText().toString().equals(this.etPasswordRepeat
				.getText().toString()))) {
			errMessage += "please check your password. the confirmation doesn't match";
		}
		if (errMessage.length() > 0) {
			Toast.makeText(this, errMessage, Toast.LENGTH_LONG).show();
			return false;
		}
		return true;
	}

	/**
	 * try to write the User to the json-Server via HttPost
	 * @param json
	 * @return boolean
	 */
	public boolean writeUser(JSONObject json) {

		String response = JSONCommunicator.postJSONObject(
				PropertyManager.getJSONServer() + "registerByApp", json);
		if (response.equalsIgnoreCase("success")) {
			Toast.makeText(this, "registration successful", Toast.LENGTH_LONG)
					.show();
			return true;
		} else {
			Log.i("REGISTER", response.toString());
			Toast.makeText(this, response.toString(), Toast.LENGTH_LONG).show();
			return false;
		}
	}
}
