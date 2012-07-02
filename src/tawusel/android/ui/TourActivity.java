package tawusel.android.ui;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tawusel.andorid.R;
import tawusel.android.database.Database;
import tawusel.android.enums.TourKind;
import tawusel.android.tools.communication.JSONCommunicator;
import tawusel.android.tools.config.PropertyManager;
import tawusel.android.ui.helper.Error;
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
			e.printStackTrace();
			Error.createDialog(this, "Error while updating Tour data", e + " " + e.getMessage());
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
			arrays = sortJSONArrays(arrays);
			tourValArray = arrays.get(1);
			
			String[] tourData = new String[8];
			if(!isTemplateTour) {
				for (int j = 1; j < tourValArray.length(); j++) {
					tourData[j-1] = tourValArray.get(j).toString();;
				}	
			} else {
				for (int j = 0; j < tourValArray.length(); j++) {
					tourData[j] = tourValArray.get(j).toString();;
				}
				tourData[5] = "";
				tourData[6] = "";
				tourData[7] = "";
			}
			
			db.openDatabase();
			if(!isTemplateTour) {
				if(methodName.contains("Active")) {
					db.insertTour(tourData, TourKind.ACTIVE);
				} else {
					db.insertTour(tourData, TourKind.AVAILABLE);
				}
			} else {
				db.insertTour(tourData, TourKind.TEMPLATE);
			}
		}
		db.closeDatabase();
	}
	
	private Vector<JSONArray> sortJSONArrays(Vector<JSONArray> arrays) throws JSONException {
		JSONArray tmpNameArray = arrays.get(0);
		JSONArray tmpValArray = arrays.get(1);
		JSONArray sortedNameArray= new JSONArray(); 
		JSONArray sortedValArray = new JSONArray(); 
		
		for (int i = 0; i < tmpNameArray.length(); i++) {
			if(tmpNameArray.get(i).equals("_1")) {
				sortedNameArray.put(0, tmpNameArray.get(i)) ;
				sortedValArray.put(0, tmpValArray.get(i));
			} else if(tmpNameArray.get(i).equals("_2")) {
				sortedNameArray.put(1, tmpNameArray.get(i)) ;
				sortedValArray.put(1, tmpValArray.get(i));
			} else if(tmpNameArray.get(i).equals("_3")) {
				sortedNameArray.put(2, tmpNameArray.get(i)) ;
				sortedValArray.put(2, tmpValArray.get(i));
			} else if(tmpNameArray.get(i).equals("_4")) {
				sortedNameArray.put(3, tmpNameArray.get(i)) ;
				sortedValArray.put(3, tmpValArray.get(i));				
			} else if(tmpNameArray.get(i).equals("_5")) {
				sortedNameArray.put(4, tmpNameArray.get(i)) ;
				sortedValArray.put(4, tmpValArray.get(i));
			} else if(tmpNameArray.get(i).equals("_6")) {
				sortedNameArray.put(5, tmpNameArray.get(i)) ;
				sortedValArray.put(5, tmpValArray.get(i));
			} else if(tmpNameArray.get(i).equals("_7")) {
				sortedNameArray.put(6, tmpNameArray.get(i)) ;
				sortedValArray.put(6, tmpValArray.get(i));
			} else if(tmpNameArray.get(i).equals("_8")) {
				sortedNameArray.put(7, tmpNameArray.get(i)) ;
				sortedValArray.put(7, tmpValArray.get(i));
			} else if(tmpNameArray.get(i).equals("_9")) {
				sortedNameArray.put(8, tmpNameArray.get(i)) ;
				sortedValArray.put(8, tmpValArray.get(i));
			}
		}
	
		arrays.clear();
		arrays.add(sortedNameArray);
		arrays.add(sortedValArray);
		return arrays;
	}

	private void addTourRow(String[] tour) {
		TourKind tourKind = parseTourKind(tour[9]);
		
		TableRow newRow = createNewRowLayout(tourKind);
		TextView tvFrom = createNewColumnText(tour[2], tourKind);
		TextView tvTo = createNewColumnText(tour[3], tourKind);
		TextView tvTime = createNewColumnText(getTimeString(tour[4], tour[5], tourKind), tourKind);
		
		newRow.addView(tvFrom);
		newRow.addView(tvTo);
		newRow.addView(tvTime);
			
		//add newRow to the tablelayout
		tblTours.addView(newRow, new TableLayout.LayoutParams(
                LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT));
	}
	
	private TourKind parseTourKind(String kindString) {
		if(kindString.equals("ACTIVE")) {
			return TourKind.ACTIVE;
		} else if(kindString.equals("TEMPLATE")) {
			return TourKind.TEMPLATE;
		} else {
			return TourKind.AVAILABLE;
		}
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

	private String getTimeString(String depTimeString, String arrTimeString, TourKind tourKind) {
		long depTimeInMillis = Long.parseLong(depTimeString);
		long arrTimeInMillis = Long.parseLong(arrTimeString);
		Date depTime = new Date(depTimeInMillis);
		Date arrTime = new Date(arrTimeInMillis);
		
		if(depTime!=null && arrTime!=null) {
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			String timeString = timeFormat.format(depTime) + " - " + timeFormat.format(arrTime);
			if(!tourKind.equals(TourKind.TEMPLATE)) {
				timeString += " (" + dateFormat.format(depTime) + ")";
			}
			return timeString;
		} else return "";
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
			db.clearUserTable();
		}
		this.finish();
	}
	
}
