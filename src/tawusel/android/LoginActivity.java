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
import tawusel.android.ui.RegisterActivity;
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

/**
 * This class is responsible for displaying the login screen. It is the 
 * first class called when the app is started. 
 */
public class LoginActivity extends Activity implements OnClickListener {
	private EditText etUsername, etPassword;
	private CheckBox cbKeepMeLoggedIn;
    private Button bLogin;
    private Button bRegister;
    
    Database db = new Database(LoginActivity.this);
	
    private final String statusUpdateMethodName = "getTourStatusValuesByApp";
    private final String townUpdateMethodName = "getTownValuesByApp";
    private final String locationUpdateMethodName = "getLocationValuesByApp";
    
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
        bRegister.setOnClickListener(this);

    }
    
    /**
     * This method checks if the the a user check the stay logged in box
     * the last time it uses the app. If this is the case it can retriev 
     * a user from the local database and it trys to login with the old
     * user data.
     */
    private void initDatabase() {
    	try {
			db.openDatabase();
//			db.resetDatabase();
			updateStableValues();
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

    private void updateStableValues() {
    	updateStableValuesFromWebservice(statusUpdateMethodName);
    	updateStableValuesFromWebservice(townUpdateMethodName);
    	updateStableValuesFromWebservice(locationUpdateMethodName);
    }
    
    /**
     * Calls the {@link JSONCommunicator} and sends a request
     * to the method specified by the parameter. Afterwards it parses the answer
     * and writes the retrieve static values into the local database.
     * 
     * @param methodName 
     */
	private void updateStableValuesFromWebservice(String methodName) {
		try {
			JSONArray jsonArray = JSONCommunicator.getJSONArray(methodName, "", PropertyManager.getJSONServer());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				JSONArray objectNameArray = jsonObject.names();
				JSONArray objectValArray = jsonObject.toJSONArray(objectNameArray);
				
				//need to sort the json arrays because they are not build in the way they are sended
				Vector<JSONArray> arrays = new Vector<JSONArray>();
				arrays.add(objectNameArray);
				arrays.add(objectValArray);
				arrays = JSONArrayHelper.sort(arrays);
				objectValArray = arrays.get(1);
				
				String[] data = new String[4];
				for (int j = 0; j < objectValArray.length(); j++) {
					data[j] = objectValArray.get(j).toString();;
				}	
				
				if(methodName.equals(statusUpdateMethodName)) {
					db.insertState(data);
				} else if(methodName.equals(townUpdateMethodName)) {
					db.insertTown(data);
				} else if(methodName.equals(locationUpdateMethodName)) {
					db.insertLocation(data);
				}
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
        bRegister = (Button)findViewById(R.id.login_bRegisterButton);
    }
    
    public void onClick(View v) {
    	if (v == bLogin) {
    		String userEmail = etUsername.getText().toString();
    		String hashedPassword = JSONCommunicator.getHashString(etPassword.getText().toString());
    		tryToLogin(userEmail, hashedPassword);
    	} else if (v == bRegister){
    		openRegisterActivity();
    	}
    }
    
    /**
     * Calls the authenticateByApp/ method of the webservice and checks 
     * if the users has entered the right email password combination. If this 
     * is the case {@link LoginActivity#performLogingAction(String, String)} is called,
     * otherwise a error is shown on the screen. 
     * 
     * @param userEmail as a string
     * @param hashedPassword as a string
     */
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

    /**
     * Writes the users data (user is logged in at this point) in the local
     * database and opens the tour activity. 
     * 
     * @param email
     * @param hashedPassword
     */
    private void performLogingAction(String email, String hashedPassword) {
    	//save the setting in the db
		try {
			db.openDatabase();
			db.clearUserTable();
    	
			//set a flag if a user was to stay logged in
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
    
    private void openRegisterActivity() {
    	Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
		startActivity(i);
    }
    
} 
