package tawusel.android;

import java.net.URLEncoder;
import java.util.Vector;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import tawusel.andorid.R;
import tawusel.android.database.Database;
import tawusel.android.tools.communication.JSONCommunicator;
import tawusel.android.tools.config.PropertyManager;
import tawusel.android.ui.TourActivity;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
			Vector<String> loggedInUserData = db.getLoggedInUser();
			if(!loggedInUserData.isEmpty()) {
				tryToLogin(loggedInUserData.get(0), loggedInUserData.get(1));
			}
		} catch (Exception e) {
			createErrorDialog(e);
		} finally {
			db.closeDatabase();
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
				JSONObject jsonUser = JSONCommunicator.getJSONObject("authentificateByApp/", params, PropertyManager.getJSONServer());
	    		if (jsonUser == null){
	    			Toast.makeText(this,"Login failed. Email and/or password doesn't match.",Toast.LENGTH_LONG).show();
	    		} else {
	    			performLogingAction(userEmail, hashedPassword);
	    		}
			} catch (ClientProtocolException e) {
				createErrorDialog(e);
			}
    	} else {
    		Toast.makeText(this,"Login failed. Email was empty.",Toast.LENGTH_LONG).show();
    	}
    }

    private void performLogingAction(String email, String hashedPassword) {
		if(cbKeepMeLoggedIn != null && cbKeepMeLoggedIn.isChecked()) {
			//save the setting in the db
			try {
				db.openDatabase();
				db.clearTable();
				db.setUserLoggedIn(email, hashedPassword);
			} catch(Exception e) {
				createErrorDialog(e);
			} finally {
				db.closeDatabase();
			}
		}
		openTourActivity();
	}

    private void openTourActivity() {
    	Intent i = new Intent(LoginActivity.this, TourActivity.class);
		startActivity(i);
    }
    
	private void createErrorDialog(Exception e) {
		Dialog dialog = new Dialog(this);
		dialog.setTitle(e.toString());
		TextView tv = new TextView(this);
		tv.setText(e.getMessage());
		dialog.setContentView(tv);
		dialog.show();
	}
    
} 
