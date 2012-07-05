package tawusel.android.ui;

import java.net.URLEncoder;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import tawusel.andorid.R;
import tawusel.android.database.Database;
import tawusel.android.enums.TourKind;
import tawusel.android.tools.communication.JSONCommunicator;
import tawusel.android.tools.config.PropertyManager;
import tawusel.android.ui.helper.JSONArrayHelper;
import tawusel.android.ui.helper.Time;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class TourActivity extends Activity {
	final Context context = this; 
	Database db = new Database(TourActivity.this);
	 
	 private TableLayout tblTours;
	 
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tours);
        
        //get the tableLayout object
        tblTours = (TableLayout) findViewById(R.id.tours_tourTable);
        
        createTourRows();
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
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

	private void createTourRows() {
		updateTourData();
		
		db.openDatabase();
		Vector<String[]> tours = db.getAllTours();
		db.closeDatabase();
		
		for (String[] tour : tours) {
			addTourRow(tour);
		}
	}
	
	private void updateTourData() {
		try {
			getToursFromWebservice("getActiveToursByApp/", false);
			getToursFromWebservice("getTourTemplatesByApp/", true);
			getToursFromWebservice("getAvailableToursByApp/", false);
		} catch (Exception e) {
			ErrorDialog errorDialog = new ErrorDialog(this, "Error while updating Tour data", e + " " + e.getMessage());
			errorDialog.show();
			db.closeDatabase();
		}
	}
	
	private void getToursFromWebservice(String methodName, boolean isTemplateTour) throws Exception {
		Vector<String> userEntry = getLoggedInUser();
		String encodedEMail = URLEncoder.encode(userEntry.get(0));
		JSONArray jsonTours = JSONCommunicator.getJSONArray(methodName, encodedEMail, PropertyManager.getJSONServer());

		for (int i = 0; i < jsonTours.length(); i++) {
			JSONObject tour = jsonTours.getJSONObject(i);
			JSONArray tourNameArray = tour.names();
			JSONArray tourValArray = tour.toJSONArray(tourNameArray);
			
			//need to sort the json arrays because they are not build in the way they are sended
			Vector<JSONArray> arrays = new Vector<JSONArray>();
			arrays.add(tourNameArray);
			arrays.add(tourValArray);
			arrays = JSONArrayHelper.sort(arrays);
			tourValArray = arrays.get(1);
			
			if(!isTemplateTour) {
				String[] tourData = new String[9];
				for (int j = 0; j < tourValArray.length(); j++) {
					tourData[j] = tourValArray.get(j).toString();;
				}
				
				db.openDatabase();
				if(methodName.contains("Active")) {
					db.insertTour(tourData, TourKind.ACTIVE);
				} else {
					db.insertTour(tourData, TourKind.AVAILABLE);
				}
				db.closeDatabase();
			} else {
				String[] templateData = new String[5];
				for (int j = 0; j < tourValArray.length(); j++) {
					templateData[j] = tourValArray.get(j).toString();;
				}
				
				db.openDatabase();
				db.insertTemplate(templateData);
				db.closeDatabase();
			}
		}
	}
	
	private void addTourRow(String[] tourFromDb) {
		String[] tour = tourFromDb;
		TourKind tourKind = TourKind.parseTourKind(tour[9]);
		
		TableRow newRow = createNewRowLayout(tourKind);
		//set the tag - so we can figure out the id of the tour in its onclicklistener later
		if(tourKind.equals(TourKind.TEMPLATE)) {
			newRow.setTag("t" + tour[0]);
		} else {
			newRow.setTag(tour[0]);
		}
		TextView tvFrom = createNewColumnText(tour[2], tourKind);
		TextView tvTo = createNewColumnText(tour[3], tourKind);
		String timeString = Time.formatString(tour[4], tour[5], tourKind);
		TextView tvTime = createNewColumnText(timeString, tourKind);
		
		newRow.addView(tvFrom);
		newRow.addView(tvTo);
		newRow.addView(tvTime);
		
		newRow = appendOnClickListener(newRow);
		
		//add newRow to the tablelayout
		tblTours.addView(newRow, new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
	}
	
	private TableRow createNewRowLayout(TourKind tourKind) {
		TableRow newRow = new TableRow(this);
		newRow.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, 
				LayoutParams.WRAP_CONTENT));
		
		//set the rows background color
		switch (tourKind) {
		case ACTIVE:
			newRow.setBackgroundResource(R.drawable.active_row_shape);
			break;
		case TEMPLATE:
			newRow.setBackgroundResource(R.drawable.template_row_shape);
			break;
		case AVAILABLE:
			newRow.setBackgroundResource(R.drawable.available_row_shape);
			break;
		default:
			break;
		}
		return newRow;
	}

	private TextView createNewColumnText(String columnText, TourKind tourKind) {
		TextView tvColumn = new TextView(this);
		tvColumn.setLayoutParams(new LayoutParams(0, LayoutParams.FILL_PARENT, 1));
		if(tourKind==TourKind.AVAILABLE) {
			tvColumn.setTextColor(Color.BLACK);
		} else {
			tvColumn.setTextColor(Color.WHITE);
		}
		tvColumn.setText(columnText);
		return tvColumn;
	}

	private TableRow appendOnClickListener(TableRow newRow) {
		newRow.setOnClickListener(new OnClickListener() {
		       public void onClick(View v) {
		    	   String rowTag = v.getTag().toString();
		    	   if(!rowTag.startsWith("t")) {
		    		   int tourId = Integer.parseInt(rowTag);
			    	   db.openDatabase();
			    	   String[] tour = db.getTour(tourId);
			    	   String[] status = db.getState(Integer.parseInt(tour[8]));
			    	   Vector<String> user = db.getLoggedInUser();
			    	   db.closeDatabase();
			    	   
			    	   TourDetailsDialog tourDetailsDialog = new TourDetailsDialog(context, tour, status, user);
			    	   tourDetailsDialog.show();
		    	   }
		       }
		});
		return newRow;
	}
	
	private Vector<String> getLoggedInUser() {
		Vector<String> userEntry = new Vector<String>();
		try {
			db.openDatabase();
			userEntry = db.getLoggedInUser();
		} catch (Exception e) {
			ErrorDialog errorDialog = new ErrorDialog(this, e.toString(), e.getMessage());
			errorDialog.show();
		}
		db.closeDatabase();
		return userEntry;
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
		int stayLoggedIn = Integer.parseInt(loggedInUserData.get(2));
		if(stayLoggedIn!=1) {
			db.clearUserTable();
		}
		db.closeDatabase();
		this.finish();
	}
	
	@Override
	protected void onDestroy() {
		db.openDatabase();
		db.clearTourTable();
		db.closeDatabase();
		super.onDestroy();
	}
	
}
