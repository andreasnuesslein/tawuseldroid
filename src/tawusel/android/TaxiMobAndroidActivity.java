package tawusel.android;

import java.net.URLEncoder;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONObject;

import tawusel.andorid.R;
import tawusel.communcation.JSONCommunicator;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class TaxiMobAndroidActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    private EditText etUsername;
    private EditText etPassword;
    private Button btnLogin;
    private Button btnCancel;;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        this.initFields();
        btnLogin.setOnClickListener(this);
      
    }
    
    public void initFields(){
        etUsername = (EditText)findViewById(R.id.login_etEmail);
        etPassword = (EditText)findViewById(R.id.login_etPassword);
        btnLogin = (Button)findViewById(R.id.login_bLogin); 
    }
    
    public void onClick(View v) {
    	System.out.println("onclick");	
    	if (v == btnLogin) {
    		System.out.println("drin");		
    		this.login();
    	}
    }
    
    public void login(){
    	if (!(etUsername.getText().toString().isEmpty())){
    		String hashPassword = JSONCommunicator.getHashString(etPassword.getText().toString());
    		String encodedEMail = URLEncoder.encode(etUsername.getText().toString());
    		String params = encodedEMail+"/"+hashPassword;
    		JSONObject jsonUser;
			try {
				jsonUser = JSONCommunicator.getJSONObject("authentificateByApp/",params);
	    		if (jsonUser == null){
	    			Toast.makeText(this,"Login failed. Username and/or password doesn't match.",Toast.LENGTH_LONG).show();
	    		} else {
	    			Toast.makeText(this,"Login alles gut.",Toast.LENGTH_LONG).show();
	    		}
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Toast.makeText(this,"Es gibt gerade keine zum Server",Toast.LENGTH_LONG).show();
				e.printStackTrace();
			}
    	}
    }
    
//    btnLogin.setOnClickListener(new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            // Check Login
//            String username = etEmail.getText().toString();
//            String password = etPassword.getText().toString();
//             
//            if(username.equals("guest") && password.equals("guest")){
//                lblResult.setText("Login successful.");
//            } else {
//                lblResult.setText("Login failed. Username and/or password doesn't match.");
//            }
//        }
//    });
//    btnCancel.setOnClickListener(new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            // Close the application
//            finish();
//        }
//    });
    
}