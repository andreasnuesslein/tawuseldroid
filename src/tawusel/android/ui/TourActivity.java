package tawusel.android.ui;

import java.util.Vector;

import tawusel.andorid.R;
import tawusel.android.database.Database;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class TourActivity extends Activity implements OnClickListener {
	private Button bNewTour;
	
	 Database db = new Database(TourActivity.this);
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tours);
        
        initFields();
        bNewTour.setOnClickListener(this);
        
    }
	
    private void initFields() {
    	bNewTour = (Button)findViewById(R.id.tours_bCreateNewTour); 
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

	public void onClick(View v) {
		if(v == bNewTour) {
			createNewTour();
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_newTour:
	            createNewTour();
	            return true;
	        case R.id.menu_help:
	            showHelp();
	            return true;
	        case R.id.menu_logout:
	        	logoutUser();
	        	return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void createNewTour() {
		Toast.makeText(this,"TODO, create a new Tour",Toast.LENGTH_LONG).show();
	}

	private void showHelp() {
		Toast.makeText(this,"TODO, create HELP",Toast.LENGTH_LONG).show();
	}

	private void logoutUser() {
		db.openDatabase();
		Vector<String> loggedInUserData = db.getLoggedInUser();
		if(!loggedInUserData.isEmpty()) {
			db.clearTable();
		}
		this.finish();
		
	}
	
}
