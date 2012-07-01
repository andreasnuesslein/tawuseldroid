package tawusel.android.ui;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import tawusel.andorid.R;
import tawusel.android.database.Database;
import tawusel.android.enums.TourKind;
import tawusel.android.tools.communication.JSONCommunicator;
import tawusel.android.tools.config.PropertyManager;
import tawusel.android.ui.helper.Error;
import android.R.bool;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class TourActivity extends Activity {
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
		Vector<String[]> activeTours = getTours("getActiveToursByApp/", false);
		for (String[] tourData : activeTours) {
			addTourRow(tourData, TourKind.ACTIVE);
		}
		
		Vector<String[]> templateTours = getTours("getTourTemplatesByApp/", true);
		for (String[] tourData : templateTours) {
			addTourRow(tourData, TourKind.TEMPLATE);
		}
		
		Vector<String[]> availableTours = getTours("getAvailableToursByApp/", false);
		for (String[] tourData : availableTours) {
			addTourRow(tourData, TourKind.AVAILABLE);
		}
	}
	
	private Vector<String[]> getTours(String methodName, boolean isTemplateTour) {
		Vector<String[]> result = new Vector<String[]>();
		Vector<String> userEntry = getLoggedInUser();
		String encodedEMail = URLEncoder.encode(userEntry.get(0));
		try {
			JSONArray jsonTours = JSONCommunicator.getJSONArray(methodName, encodedEMail, PropertyManager.getJSONServer());

			for (int i = 0; i < jsonTours.length(); i++) {
				JSONObject tour = jsonTours.getJSONObject(i);
				JSONArray tourNameArray = tour.names();
				JSONArray tourValArray = tour.toJSONArray(tourNameArray);
				
				String[] tableRow = new String[3];
				Date depTime = null;
				Date arrTime = null;
				int fromArrayPosition = 3;
				int toArrayPosition = 4;
				int depTimeArrayPositon = 5;
				int arrTimeArrayPosition = 6;
				
				if(isTemplateTour) {
					fromArrayPosition = 2;
					toArrayPosition = 3;
					depTimeArrayPositon = 4;
					arrTimeArrayPosition = 5;
				}
				
				for(int j = 0; j < tourNameArray.length(); j++) {
					String name = tourNameArray.get(j).toString();
					if(name.equals("_"+fromArrayPosition)) {
						tableRow[0] = tourValArray.get(j).toString();
					} else if(name.equals("_"+toArrayPosition)) {
						tableRow[1] = tourValArray.get(j).toString();
					} else if(name.equals("_"+depTimeArrayPositon)) {
						depTime = new Date(tourValArray.getLong(j));
					} else if(name.equals("_"+arrTimeArrayPosition)) {
						arrTime = new Date(tourValArray.getLong(j));
					}
				}
				 tableRow[2] = getTimeString(depTime, arrTime, isTemplateTour);
				 result.add(tableRow);
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	private String getTimeString(Date depTime, Date arrTime, boolean isTemplateTour) {
		if(depTime!=null && arrTime!=null) {
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			String timeString = timeFormat.format(depTime) + " - " + timeFormat.format(arrTime);
			if(!isTemplateTour) {
				timeString += " (" + dateFormat.format(depTime) + ")";
			}
			return timeString;
		} else return "";
		
	}

	private void addTourRow(String[] tourData, TourKind tourKind) {
		TableRow newRow = new TableRow(this);
		newRow.setLayoutParams(new LayoutParams(
				LayoutParams.FILL_PARENT, 
				LayoutParams.WRAP_CONTENT));
		
		for (int i = 0; i < tourData.length; i++) {
			TextView columnText = new TextView(this);
			columnText.setLayoutParams(new LayoutParams(0, LayoutParams.FILL_PARENT, 1));
			if(tourKind==TourKind.AVAILABLE) {
				columnText.setTextColor(Color.BLACK);
			} else {
				columnText.setTextColor(Color.WHITE);
			}
			
			columnText.setText(tourData[i]);
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
			
			newRow.addView(columnText);
		}
		
		//add newRow to the tablelayout
		tblTours.addView(newRow, new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
	}
	
	private Vector<String> getLoggedInUser() {
		Vector<String> userEntry = new Vector<String>();
		try {
			db.openDatabase();
			userEntry = db.getLoggedInUser();
		} catch (Exception e) {
			Error.createDialog(this, e.toString(), e.getMessage());
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
			db.clearTable();
		}
		this.finish();
	}
	
}
