package tawusel.android;

import java.net.URLEncoder;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import tawusel.andorid.R;
import tawusel.android.database.Database;
import tawusel.android.tools.communication.JSONCommunicator;
import tawusel.android.tools.config.PropertyManager;
import tawusel.android.ui.ErrorDialog;
import tawusel.android.ui.TourActivity;
import tawusel.android.ui.helper.JSONArrayHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class LoginActivity extends Activity implements OnClickListener {
	private EditText etUsername, etPassword;
	private CheckBox cbKeepMeLoggedIn;
    private Button bLogin;
    
    Database db = new Database(LoginActivity.this);
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
     
        //load .properties file
        PropertyManager.loadConfiguration(this);
        
        initDatabase();
        initFields();
        bLogin.setOnClickListener(this);
    }
    
    private void initDatabase() {
    	try {
			db.openDatabase();
			db.resetDatabase();
			updateStatusFromWebservice();
			Vector<String> loggedInUserData = db.getLoggedInUser();
			if(!loggedInUserData.isEmpty()) {
				String email = loggedInUserData.get(0);
				String hashedPassword = loggedInUserData.get(1);
				tryToLogin(email, hashedPassword);
			}
		} catch (Exception e) {
			ErrorDialog errorDialog = new ErrorDialog(this, e.toString(), e.getMessage());
			errorDialog.show();
		} finally {
			db.closeDatabase();
		}
		
	}

	private void updateStatusFromWebservice() {
		try {
			JSONArray jsonStates = JSONCommunicator.getJSONArray("getTourStatusValuesByApp", "", PropertyManager.getJSONServer());
			for (int i = 0; i < jsonStates.length(); i++) {
				JSONObject state = jsonStates.getJSONObject(i);
				JSONArray stateNameArray = state.names();
				JSONArray stateValArray = state.toJSONArray(stateNameArray);
				
				//need to sort the json arrays because they are not build in the way they are sended
				Vector<JSONArray> arrays = new Vector<JSONArray>();
				arrays.add(stateNameArray);
				arrays.add(stateValArray);
				arrays = JSONArrayHelper.sort(arrays);
				stateValArray = arrays.get(1);
				
				String[] stateData = new String[3];
				for (int j = 0; j < stateValArray.length(); j++) {
					stateData[j] = stateValArray.get(j).toString();;
				}	
				db.insertState(stateData);
			}
		} catch (Exception e) {
			ErrorDialog errorDialog = new ErrorDialog(this, e.toString(), e.getMessage());
			errorDialog.show();
		}
	}

	public void initFields(){
        etUsername = (EditText)findViewById(R.id.login_etEmail);
        etPassword = (EditText)findViewById(R.id.login_etPassword);
        cbKeepMeLoggedIn = (CheckBox)findViewById(R.id.login_cbKeepMeLoggedIn);
        bLogin = (Button)findViewById(R.id.login_bLogin); 
    }
    
    public void onClick(View v) {
    	if (v == bLogin) {
    		String userEmail = etUsername.getText().toString();
    		String hashedPassword = JSONCommunicator.getHashString(etPassword.getText().toString());
    		tryToLogin(userEmail, hashedPassword);
    	}
    }
    
    public void tryToLogin(String userEmail, String hashedPassword){
    	if (!(userEmail.isEmpty())){
			try {
	    		String encodedEMail = URLEncoder.encode(userEmail);
	    		String params = encodedEMail+"/"+hashedPassword;
				JSONObject jsonUser = JSONCommunicator.getJSONObject("authenticateByApp/", params, PropertyManager.getJSONServer());
	    		if (jsonUser == null){
	    			ErrorDialog errorDialog = new ErrorDialog(this, "Login failed", "Email and/or password doesn't match.");
	    			errorDialog.show();
	    		} else {
	    			performLogingAction(userEmail, hashedPassword);
	    		}
			} catch (Exception e) {
				ErrorDialog errorDialog = new ErrorDialog(this, e.toString(), e.getMessage());
				errorDialog.show();
			}
    	} else {
    		ErrorDialog errorDialog = new ErrorDialog(this, "Login failed", "Email field was empty");
			errorDialog.show();
    	}
    }

    private void performLogingAction(String email, String hashedPassword) {
    	//save the setting in the db
		try {
			db.openDatabase();
			db.clearUserTable();
    	
			if(cbKeepMeLoggedIn != null && cbKeepMeLoggedIn.isChecked()) {
				db.setUserLoggedIn(email, hashedPassword, 1);
			} else {
				db.setUserLoggedIn(email, hashedPassword, 0);
			}
		} catch(Exception e) {
			ErrorDialog errorDialog = new ErrorDialog(this, e.toString(), e.getMessage());
			errorDialog.show();
		} finally {
			db.closeDatabase();
		}
		openTourActivity();
	}

    private void openTourActivity() {
    	Intent i = new Intent(LoginActivity.this, TourActivity.class);
		startActivity(i);
    }
} 
