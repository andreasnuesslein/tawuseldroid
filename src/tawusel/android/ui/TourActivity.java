package tawusel.android.ui;

import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tawusel.andorid.R;
import tawusel.android.database.Database;
import tawusel.android.enums.TourKind;
import tawusel.android.tools.communication.JSONCommunicator;
import tawusel.android.tools.config.PropertyManager;
import tawusel.android.ui.helper.JSONArrayHelper;
import tawusel.android.ui.helper.Time;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

/**
 * This is the main class of the app. It is responsible for
 * displaying the tours table, opening the specific details or 
 * create dialogs and showing the apps's menu.<br><br>
 * 
 * It contains to private sub classes which extend android.app.Dialog 
 * and need to have access to the {@link TourActivity#updateTourRows(boolean)} 
 * method.
 * ({@link TourDetailsDialog} & {@link CreateTourDialog}
 */
public class TourActivity extends Activity {
	final Context context = this; 
	Database db = new Database(TourActivity.this);
	 
	private TableLayout tblTours;
	private boolean resumeHasRun = false;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tours);
        
        //get the tableLayout object
        tblTours = (TableLayout) findViewById(R.id.tours_tourTable);
        
        updateTourRows(true);
    }
	
    @Override
    public void onResume() {
    	super.onResume();
        if (!resumeHasRun) {
            resumeHasRun = true;
            return;
        }
    	updateTourRows(false);
    }
    
    /**
     * Displays the menu defiened in the res/menu/menu.xml
     */
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
	        case R.id.menu_update:
	        	performManualUpdate();
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
	
	private void performManualUpdate() {
		db.openDatabase();
		db.clearTemplateTable();
		db.closeDatabase();
		updateTourRows(true);
	}

	/**
	 * Updates the rows in the tours table
	 * 
	 * @param updateLocalDbFromService a boolean which represents if the
	 * update should ask the webservice for new data or just the local db
	 */
	public void updateTourRows(boolean updateLocalDbFromService) {
		if (updateLocalDbFromService) {
			updateTourDataFromWebService();
		}
		clearTourRows();
		createTourViews();
	}
	
	/**
	 * Deletes all rows from the tour table except for the head row.
	 */
	private void clearTourRows() {
		for (int i = tblTours.getChildCount(); i > 0; i--) {
			tblTours.removeView(tblTours.getChildAt(i));
		}
	}

	private void createTourViews() {
		db.openDatabase();
		Vector<String[]> tours = db.getAllTours();
		db.closeDatabase();
		for (String[] tour : tours) {
			addTourRow(tour);
		}
	}
	
	private void updateTourDataFromWebService() {
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
	
	/**
	 * Calls the webservice by its REST api to retrieve new tours. The first 
	 * parameter defines the type of tours which should be retrieved by this 
	 * execution of the method
	 * 
	 * @param methodName as a string
	 * @param isTemplateTour as a boolean
	 * @throws Exception
	 */
	private void getToursFromWebservice(String methodName, boolean isTemplateTour) throws Exception {
		Vector<String> userEntry = getLoggedInUser();
		String encodedEMail = URLEncoder.encode(userEntry.get(0));
		JSONArray jsonTours = JSONCommunicator.getJSONArray(methodName, encodedEMail, PropertyManager.getJSONServer());

		for (int i = 0; i < jsonTours.length(); i++) {
			JSONObject tour = jsonTours.getJSONObject(i);
			
			if(!isTemplateTour) {
				String[] tourData = new String[10];
				tourData[0] = tour.getString("id");
				tourData[1] = tour.getJSONObject("town").getString("name");
				tourData[2] = tour.getJSONObject("l1").getString("name");
				tourData[3] = tour.getJSONObject("l2").getString("name");
				tourData[4] = tour.getString("dep");
				tourData[5] = tour.getString("arr");
				tourData[6] = tour.getString("state");
				tourData[7] = tour.getString("users");
				tourData[8] = tour.getString("mod");
				
				db.openDatabase();
				if(methodName.contains("Active")) {
					tourData[9] = TourKind.ACTIVE.toString();
					db.checkAndUpdateTour(tourData);
				} else {
					tourData[9] = TourKind.AVAILABLE.toString();
					db.checkAndUpdateTour(tourData);
				}
				db.closeDatabase();
			} else {
				String[] templateData = new String[5];
				templateData[0] = tour.getJSONObject("town").getString("name");
				templateData[1] = tour.getJSONObject("l1").getString("name");
				templateData[2] = tour.getJSONObject("l2").getString("name");
				templateData[3] = tour.getString("dep"); 
				templateData[4] = tour.getString("arr");
				
				db.openDatabase();
				db.insertTemplate(templateData);
				db.closeDatabase();
			}
		}
	}
	
	/**
	 * After the data was updated this method starts to fill the tours table
	 * dynamically. It adds a row with the data of the Tour which is given as 
	 * a parameter.
	 * 
	 * @param tourFromDb as a string array
	 */
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
	
	/**
	 * Loads the specific background for the type of tour in the table row
	 * object and returns it back to the calling method.
	 *  
	 * @param tourKind
	 * @return a table row object
	 */
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

	/**
	 * Sets the text color of the text views in the specific row in dependence
	 * of the tour type.
	 * 
	 * @param columnText
	 * @param tourKind
	 * @return
	 */
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

	/**
	 * Defines the action which happens if the users clicks on a row in
	 * the tour table. If this row was a row with a template tour a new
	 * create tour dialog is opened. Otherwise a tour details dialog is 
	 * shown on the screen
	 * 
	 * @param newRow - the row the listener should be attached to
	 * @return
	 */
	private TableRow appendOnClickListener(TableRow newRow) {
		newRow.setOnClickListener(new OnClickListener() {
		       public void onClick(View v) {
		    	   String rowTag = v.getTag().toString();
		    	   if(!rowTag.startsWith("t")) {
		    		   int tourId = Integer.parseInt(rowTag);
			    	   db.openDatabase();
			    	   String[] tour = db.getTour(tourId);
			    	   String[] status = db.getState(Integer.parseInt(tour[6]));
			    	   Vector<String> user = db.getLoggedInUser();
			    	   db.closeDatabase();
			    	   
			    	   TourDetailsDialog tourDetailsDialog = new TourDetailsDialog(context, tour, status, user);
			    	   tourDetailsDialog.show();
		    	   } else {
		    		   int tourId = Integer.parseInt(rowTag.replace("t", ""));
		    		   db.openDatabase();
			    	   String[] tour = db.getTemplate(tourId);
			    	   Vector<String> user = db.getLoggedInUser();
			    	   db.closeDatabase();
			    	   
			    	   CreateTourDialog createTourDialog = new CreateTourDialog(context, tour, user);
			    	   createTourDialog.show();
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
		CreateTourDialog createTourDialog = new CreateTourDialog(context, getLoggedInUser());
  	   	createTourDialog.show();
	}

	private void showHelp() {
		Dialog dialog = new Dialog(this);

		dialog.setContentView(R.layout.help_dialog);
		dialog.setTitle("Help");

		TextView text = (TextView) dialog.findViewById(R.id.helpDialog_text);
		text.setText("On the main screen you can see the tours table. Your active tours " +
				"are displayed in green while all available tours provide by other users have the " +
				"color white. Last but not least there are your template tours. \n" +
				"By clicking on a tour you can either join or leave it. By clicking on a template " +
				"you can create a new tour.");
		dialog.show();
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
		db.clearTemplateTable();
		db.closeDatabase();
		super.onDestroy();
	}
	
	/**
	 * This class is responsible for the displaying of a tour details
	 * dialog. Therefore it extends android.app.Dialog. Since it contains buttons
	 * it has to implement android.view.View.OnClickListener as well. It is 
	 * implemented as a private class because it needs to have access to the 
	 * {@link TourActivity#updateTourRows(boolean)}, which is not possible if it was
	 * a class defined in another file.
	 *
	 */
	private class TourDetailsDialog extends Dialog implements OnClickListener {
		Database db;
		
		private TextView tvCity, tvFrom, tvTo, tvTime, tvStatus, tvPassengers;
		private Button bJoin, bLeave;
		
		private String[] tour = new String[10];
		private String[] status = new String[3];
		private Vector<String> user;
		private Context context;
		
		@SuppressWarnings("unchecked")
		public TourDetailsDialog(Context context, String[] tour, String[] status, Vector<String> user) {
			super(context);
			
			this.setContentView(R.layout.tour_details_dialog);
	 	   	this.setTitle(R.string.tourDetailsDialog_head);
	 	   	
	 	   	this.tour = tour;
	 	   	this.status = status;
	 	   	this.user = (Vector<String>) user.clone();
	 	   	this.context = context;
	 	   	this.db = new Database(context);
	 	   	
	 	   	initGuiElements();
	 	   	updateDialog();
		}
		
		private void initGuiElements() {
			tvCity = (TextView) findViewById(R.id.tourDetailsDialog_tvCity);
			tvFrom = (TextView) findViewById(R.id.tourDetailsDialog_tvFrom);
			tvTo = (TextView) findViewById(R.id.tourDetailsDialog_tvTo);
			tvTime = (TextView) findViewById(R.id.tourDetailsDialog_tvTime);
			tvStatus = (TextView) findViewById(R.id.tourDetailsDialog_tvStatus);
			tvPassengers = (TextView) findViewById(R.id.tourDetailsDialog_tvPassengers);
			bJoin = (Button) findViewById(R.id.tourDetailsDialog_bJoinButton);
			bJoin.setOnClickListener(this);
			bLeave = (Button) findViewById(R.id.tourDetailsDialog_bLeaveButton);
			bLeave.setOnClickListener(this);
		}
		
		private void updateDialog() {
			setTextFieldValues();
			disableButton();
		}
		
		private void setTextFieldValues() {
			tvCity.setText(tour[1]);
			tvFrom.setText(tour[2]);
			tvTo.setText(tour[3]);
			TourKind tourKind = TourKind.parseTourKind(tour[9]);
			tvTime.setText(Time.formatString(tour[4], tour[5], tourKind));
			tvStatus.setText(status[1] + " - " + status[2]);
			tvPassengers.setText(getPassengers(tour[7]));
		}
		
		/**
		 * Calls the webservice by its REST api to retrieve all passenger of 
		 * the current tour. It is called when the text values are set.
		 * 
		 * @param jsonArrayString
		 * @return a string with the firstnames of all passengers seperated by ","
		 */
		private String getPassengers(String jsonArrayString) {
			String passengers = "";
			try {
				JSONArray jsonPassengers = JSONCommunicator.parseJSArray(jsonArrayString);
				
				for (int i = 0; i < jsonPassengers.length(); i++) {
					JSONObject passengersObject = jsonPassengers.getJSONObject(i);
					JSONArray passengersNameArray = passengersObject.names();
					JSONArray passengersValArray = passengersObject.toJSONArray(passengersNameArray);
					
					//need to sort the json arrays because they are not build in the way they are sended
					Vector<JSONArray> arrays = new Vector<JSONArray>();
					arrays.add(passengersNameArray);
					arrays.add(passengersValArray);
					arrays = JSONArrayHelper.sortPassengersArray(arrays);
					passengersValArray = arrays.get(1);
					
					if(i!=0) {
						passengers += ", ";
					}
					passengers += passengersValArray.getString(2);
				}
				return passengers;
			} catch (Exception e) {
				return passengers;
			}
		}
		   
		/**
		 * Disables either the join or the leave button depending on the 
		 * list of passengers. If the user is in it the leave button is 
		 * acitvated, otherwise the join button.
		 */
		private void disableButton() {
			//check if emailstring of logged in user is contained in pessengers
			boolean isUserPassengerOfTour = tour[7].contains(user.get(0));
			if(!user.isEmpty() && isUserPassengerOfTour) {
				bJoin.setEnabled(false);
				bLeave.setEnabled(true);
			} else if(!isUserPassengerOfTour) {
				bLeave.setEnabled(false);
				bJoin.setEnabled(true);
			}
		}

		public void onClick(View v) {
			if(v == bJoin) {
				boolean isJoined = tryToJoinTour();
				if(isJoined) {
					updateDialog();
					TourActivity.this.updateTourRows(false);
					Toast.makeText(context, R.string.tourDetailsDialog_userJoined, Toast.LENGTH_LONG).show();
					this.dismiss();
				} else {
					Toast.makeText(context, R.string.tourDetailsDialog_userNotJoined, Toast.LENGTH_LONG).show();
				}
			} else if(v == bLeave) {
				boolean isLeft = tryToLeaveTour();
				if(isLeft) {
					updateDialog();
					TourActivity.this.updateTourRows(false);
					Toast.makeText(context, R.string.tourDetailsDialog_userLeft, Toast.LENGTH_LONG).show();
					this.dismiss();
				} else {
					Toast.makeText(context, R.string.tourDetailsDialog_userNotLeft, Toast.LENGTH_LONG).show();
				}
			}
		}
		
		/**
		 * Calls the webservice by its REST api to try to join the current tour. If 
		 * this action was successful the tour is copied into the local database.
		 */
		private boolean tryToJoinTour() {
			String params = URLEncoder.encode(user.get(0)) + "/" + tour[0];
			try {
				JSONObject result = JSONCommunicator.getJSONObject("joinTourByApp/", params, PropertyManager.getJSONServer());
				
				if(result != null) {
					parseJSONObject(result);
					tour[9] = TourKind.ACTIVE.toString();
					
					db.openDatabase();
					db.updateTour(tour);
					db.closeDatabase();
					
					return true;
				}
				return false;
			} catch (Exception e) {
				ErrorDialog errorDialog = new ErrorDialog(context, "Unable to join the tour", e.toString() + " - " +e.getMessage());
				errorDialog.show();
				return false;
			}
		}

		/**
		 * Calls the webservice by its REST api to try to leave the current tour. If 
		 * this action was successful the tour in the local database is updated. If the
		 * user was the only passenger of the tour it is deleted from the local database.
		 */
		private boolean tryToLeaveTour() {
			String params = URLEncoder.encode(user.get(0)) + "/" + tour[0];
			try {
				JSONObject result = JSONCommunicator.getJSONObject("leaveTourByApp/", params, PropertyManager.getJSONServer());
				
				if(result != null) {
					if(result.has("_1")) {
						db.openDatabase();
						db.deleteTour(Integer.parseInt(tour[0]));
						db.closeDatabase();
						return true;
					} else if(result.has("_2")){
						return false;
					} else {
						parseJSONObject(result);
						tour[9] = TourKind.AVAILABLE.toString();
						
						db.openDatabase();
						db.updateTour(tour);
						db.closeDatabase();
						
						return true;
					}
				}
				return false;
			} catch (Exception e) {
				ErrorDialog errorDialog = new ErrorDialog(context, "Unable to leave the tour", e.toString() + " - " +e.getMessage());
				errorDialog.show();
				return false;
			}
		}
	
		private void parseJSONObject(JSONObject result) throws JSONException {
			tour[0] = result.getString("id");
			tour[1] = result.getJSONObject("town").getString("name");
			tour[2] = result.getJSONObject("l1").getString("name");
			tour[3] = result.getJSONObject("l2").getString("name");
			tour[4] = result.getString("dep");
			tour[5] = result.getString("arr");
			tour[6] = result.getString("state");
			tour[7] = result.getString("users");
			tour[8] = result.getString("mod");
		}
	}
	
	/**
	 * This class is responsible for the displaying of a create tour 
	 * dialog. Therefore it extends android.app.Dialog. Since it contains buttons
	 * it has to implement android.view.View.OnClickListener as well. It is 
	 * implemented as a private class because it needs to have access to the 
	 * {@link TourActivity#updateTourRows(boolean)}, which is not possible if it was
	 * a class defined in another file.
	 *
	 */
	private class CreateTourDialog extends Dialog implements OnClickListener, OnItemSelectedListener {
		Database db;
		
		private Spinner spCity, spFrom, spTo;
		private TextView tvDate, tvFromTime, tvToTime;
		private Button bCreate;
		
		private String[] tour = new String[10];
		private Vector<String> user;
		private Context context;
		
		private List<CharSequence> townList = new ArrayList<CharSequence>();
		private int townSpinnerSelectedId = 0;
		private List<CharSequence> locationList = new ArrayList<CharSequence>();
		private int fromLocationSpinnerSelectedId = 0;
		private int toLocationSpinnerSelectedId = 0;
		private boolean hasChangedTown = false;
		
		private int suggestedYear;
		private int suggestedMonth;
		private int suggestedDay;
		
		private int fromHours;
		private int fromMinutes;
		private int toHours;
		private int toMinutes;
		private String estimateRequest = "googleest/";
		
		/**
		 * This constructor is called by clicking on a template row in the tours
		 * table. The existing tour data is used for the hole class.
		 * 
		 * @param context
		 * @param tour
		 * @param user
		 */
		@SuppressWarnings("unchecked")
		public CreateTourDialog(Context context, String[] tour, Vector<String> user) {
			super(context);
			
			this.setContentView(R.layout.create_tour_dialog);
	 	   	this.setTitle(R.string.createTourDialog_head);
	 	   	
	 	   	this.tour = tour;
	 	   	this.user = (Vector<String>) user.clone();
	 	   	this.context = context;
	 	   	this.db = new Database(context);
	 	   	
	 		initGuiElements();
	 		initSuggestedDate();
	 		updateLists();

	 	   	addItemstoCitySpinner();
	 	   	updateTvDate();
	 	   	updateTvTimes();
		}
		
		/**
		 * This constructor is called if the dialog is opend by the menu.
		 * Therefoe a tour must be generated by inital values.
		 * @param context
		 * @param user
		 */
		@SuppressWarnings("unchecked")
		public CreateTourDialog(Context context, Vector<String> user) {
			super(context);
			
			this.setContentView(R.layout.create_tour_dialog);
	 	   	this.setTitle(R.string.createTourDialog_head);
	 	   	
	 	   	this.user = (Vector<String>) user.clone();
	 	   	this.context = context;
	 	   	this.db = new Database(context);
	 	   	this.tour = createNewTour();
	 	   	
	 	   	initGuiElements();
	 	   	initSuggestedDate();
	 	   	updateLists();
	 	   	addItemstoCitySpinner();
	 	   	
	 	   	updateTvDate();
	 	   	updateTvTimes();

		}

		private String[] createNewTour() {
			db.openDatabase();
	 	   	String[] newTour = new String[10];
	 	   	String[] townObjectFromDb = db.getTown(1);
	 	   	Vector<String[]> locationsFromDb = db.getAllLocations(townObjectFromDb[1]); 
	 	   	
	 	   	newTour[1] = townObjectFromDb[1];
	 	   	newTour[2] = (locationsFromDb.get(0))[1];
	 	   	newTour[3] = (locationsFromDb.get(1))[1];
	 	   	newTour[4] = Long.toString(getSuggestedTime());
	 	   	newTour[5] = Long.toString(getSuggestedTime()+600000);
	 	   	newTour[9] = TourKind.ACTIVE.toString();
	 	   	db.closeDatabase();
	 	   	
	 	   	return newTour;
		}
		
		private Long getSuggestedTime() {
			Calendar cal = Calendar.getInstance();
	 	   	long time = cal.getTimeInMillis();
	 	   	return time + (1000 * 60 * 30);
		}
		
		private void updateLists() {
			db.openDatabase();
			
			//townList
			if(townList.isEmpty()) {
				Vector<String[]> allTowns = db.getAllTowns();
				for(String[] town : allTowns) {
					townList.add(town[1]);
					if(town[1].equals(tour[1])) {
						townSpinnerSelectedId = townList.size()-1;
					}
				}
			}
			
			//locationList
			Vector<String[]> allLocations = db.getAllLocations(tour[1]);
			locationList.clear();
			fromLocationSpinnerSelectedId = -1;
			toLocationSpinnerSelectedId = -1;
			for(String[] location : allLocations) {
				locationList.add(location[1]);
				if(location[1].equals(tour[2])) {
					fromLocationSpinnerSelectedId = locationList.size()-1;
				} else if(location[1].equals(tour[3])) {
					toLocationSpinnerSelectedId = locationList.size()-1;
				}
			}
			
			if(fromLocationSpinnerSelectedId==-1) {
				fromLocationSpinnerSelectedId=0;
			}
			if(toLocationSpinnerSelectedId==-1) {
				toLocationSpinnerSelectedId=1;
			}
			db.closeDatabase();
		}
		
		private void initGuiElements() {
			spCity = (Spinner) findViewById(R.id.createTourDialog_spCity);
			spCity.setOnItemSelectedListener(this);
			spFrom = (Spinner) findViewById(R.id.createTourDialog_spFrom);
			spFrom.setOnItemSelectedListener(this);
			spTo = (Spinner) findViewById(R.id.createTourDialog_spTo);
			spTo.setOnItemSelectedListener(this);
			bCreate = (Button) findViewById(R.id.createTourDialog_bCreate);
			bCreate.setOnClickListener(this);
			tvDate = (TextView) findViewById(R.id.createTourDialog_tvDate);
			tvDate.setOnClickListener(this);
			tvFromTime = (TextView) findViewById(R.id.createTourDialog_tvFromTime);
			tvFromTime.setOnClickListener(this);
			tvToTime = (TextView) findViewById(R.id.createTourDialog_tvToTime);
			tvToTime.setOnClickListener(this);
		}
		
		private void addItemstoCitySpinner() {
			ArrayAdapter<CharSequence> dataAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, townList);
			dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spCity.setAdapter(dataAdapter);
			spCity.setSelection(townSpinnerSelectedId);
		}
		
		private void addItemstoLocationSpinners() {
			ArrayAdapter<CharSequence> fromDataAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, locationList);
			fromDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spFrom.setAdapter(fromDataAdapter);
			spFrom.setSelection(fromLocationSpinnerSelectedId);
				
			ArrayAdapter<CharSequence> toDataAdapter = new ArrayAdapter<CharSequence>(context, android.R.layout.simple_spinner_item, locationList);
			toDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spTo.setAdapter(toDataAdapter);
			spTo.setSelection(toLocationSpinnerSelectedId);
		}
		
		/**
		 * This method is called if an item was selceted in one of the three spinners.
		 * If the city spinners selected item has changed the location spinners have to 
		 * be adapted as well.
		 */
		public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
			if(parent==spCity) {
				townSpinnerSelectedId = pos;
				tour[1] = townList.get(pos).toString();
				updateLists();
				addItemstoLocationSpinners();
				hasChangedTown = true;
			} else if(parent==spFrom) {
				fromLocationSpinnerSelectedId = pos;
				tour[2] = locationList.get(pos).toString();
				
				if(!hasChangedTown) {
					fromTimeSetListener.onTimeSet(null, fromHours, fromMinutes);
				}
			} else if(parent==spTo) {
				toLocationSpinnerSelectedId = pos;
				tour[3] = locationList.get(pos).toString();
				toTimeSetListener.onTimeSet(null, toHours, toMinutes);
				hasChangedTown = false;
			}
		}

		public void onNothingSelected(AdapterView<?> parent) {
			//nothing to do here
		}
		
		private void updateTvDate() {
			String date = prependZeros(suggestedDay) + "." + prependZeros(suggestedMonth) + 
					"." + suggestedYear;
			tvDate.setText(date);
		}
		
		private void updateTvTimes() {
			String fromTime = prependZeros(fromHours) + ":" + prependZeros(fromMinutes) + " ";
			tvFromTime.setText(fromTime);
			
			String toTime = prependZeros(toHours) + ":" + prependZeros(toMinutes);
			tvToTime.setText(toTime);
		}
		
		private String prependZeros(int c) {
			if (c >= 10)
			   return String.valueOf(c);
			else
			   return "0" + String.valueOf(c);
		}
		
		private String getTimeString(Date date) {
			SimpleDateFormat df = new SimpleDateFormat("HH:mm");
			return df.format(date);
		}
		
		private String getDateString(Date date) {
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy");
			return df.format(date);
		}
		
		/**
		 * This method checks if the tours start time lies in the past. In this
		 * case the next day is the suggested day of the journey.
		 */
		private void initSuggestedDate() {
			String depTime = getTimeString(new Date(Long.parseLong(tour[4])));
			String arrTime = getTimeString(new Date(Long.parseLong(tour[5])));
			Calendar cal = Calendar.getInstance();
			Date today = cal.getTime();
			String currentTime = getTimeString(today);
			
			SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			try {
				Date now = df.parse(getDateString(today) + " " + currentTime);
				Date suggestion = df.parse(getDateString(today) + " " + depTime);
				if(now.before(suggestion)) {
					setDateInts(cal);
				} else {
					cal.add(cal.DAY_OF_MONTH, 1);
					setDateInts(cal);
				}
			} catch (Exception e) {
				setDateInts(cal);
			}
			setTimeInts(depTime, arrTime);
		}

		private void setDateInts(Calendar cal) {
			suggestedYear = cal.get(Calendar.YEAR);
			//starts with 0? wtf?
			suggestedMonth = cal.get(Calendar.MONTH)+1;
			suggestedDay = cal.get(Calendar.DAY_OF_MONTH);
		}
		
		private void setTimeInts(String depTime, String arrTime) {
			StringTokenizer st = new StringTokenizer(depTime, ":");
			if(st.countTokens()==2) {
				fromHours = Integer.parseInt(st.nextToken());
				fromMinutes = Integer.parseInt(st.nextToken());
			}
			
			st = new StringTokenizer(arrTime, ":");
			if(st.countTokens()==2) {
				toHours = Integer.parseInt(st.nextToken());
				toMinutes = Integer.parseInt(st.nextToken());
			}
		}

		/**
		 * If one clicks on the dates or times textfields a picker dialog is opened.
		 * In this dialog the user can choose the time desired.
		 */
		public void onClick(View v) {
			if(v==tvDate) {
				//wtf? does it start at moth + 1?
				DatePickerDialog dialog = new DatePickerDialog(context, dateSetListener, suggestedYear, suggestedMonth-1, suggestedDay);
				dialog.show();
			} else if(v==tvFromTime) {
				TimePickerDialog dialog = new TimePickerDialog(context, fromTimeSetListener, fromHours, fromMinutes, true);
				dialog.show();
			} else if(v==tvToTime) {
				TimePickerDialog dialog = new TimePickerDialog(context, toTimeSetListener, toHours, toMinutes, true);
				dialog.show();
			} else if(v==bCreate) {
				tryToCreateATour();
			}
		}

		/**
		 * The callback method of the date picker. This one is called 
		 * if a user closes the date picker with the ok button.
		 */
		private DatePickerDialog.OnDateSetListener dateSetListener =
                new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int dpYear, int dpMonthOfYear,
                                int dpDayOfMonth) {
                        suggestedYear = dpYear;
                        suggestedMonth = dpMonthOfYear+1;
                        suggestedDay = dpDayOfMonth;
                        updateTvDate();
                }
        };
        
        /**
		 * The callback method of the from time picker. This one is called 
		 * if a user closes the from time picker with the ok button.
		 */    
        private TimePickerDialog.OnTimeSetListener fromTimeSetListener = 
                new TimePickerDialog.OnTimeSetListener() {
    		
        		public void onTimeSet(TimePicker view, int tpHour, int tpMinute) {
	    			fromHours = tpHour;
	    			fromMinutes = tpMinute;
	     
	    			try {
						int duration = getDuration();
						int timeSum = duration + fromMinutes;
						
						if(timeSum >= 60) {
							toHours = fromHours + 1;
							if (toHours==24) {
								toHours = 0;
							}
							toMinutes = timeSum % 60;
						} else {
							toHours = fromHours;
							toMinutes = timeSum; 
						}
					} catch (Exception e) {
						ErrorDialog errorDialog = new ErrorDialog(context, e.toString(), e.getMessage());
						errorDialog.show();
					}
	    			
	    			updateTvTimes();
	    		}
    	};
    	
    	/**
		 * The callback method of the to time picker. This one is called 
		 * if a user closes the to time picker with the ok button.
		 */
    	private TimePickerDialog.OnTimeSetListener toTimeSetListener = 
                new TimePickerDialog.OnTimeSetListener() {
    		
        		public void onTimeSet(TimePicker view, int tpHour, int tpMinute) {
    			toHours = tpHour;
    			toMinutes = tpMinute;
     
    			try {
					int duration = getDuration();
					int timeDifference = toMinutes - duration;
					
					if(duration <= toMinutes) {
						fromHours = toHours;
						fromMinutes = timeDifference;
					} else {
						fromHours = toHours - 1; 
						if (fromHours==-1) {
							fromHours = 23;
						}
						fromMinutes = 60 + timeDifference; 
					}
				} catch (Exception e) {
					ErrorDialog errorDialog = new ErrorDialog(context, e.toString(), e.getMessage());
					errorDialog.show();
				}
    			
    			updateTvTimes();
    		}
    	};
		
    	private String getLocationParameters() {
    		db.openDatabase();
			int fromId = db.getLocationId(tour[2], tour[1]);
			int toId = db.getLocationId(tour[3], tour[1]);
			db.closeDatabase();
			
			return fromId + "/" + toId;
    	}

    	private int getDuration() throws Exception {
    		JSONObject minutesObject = JSONCommunicator.getJSONObject(estimateRequest, 
					getLocationParameters(), PropertyManager.getJSONServer());
			return minutesObject.getInt("dur");
    	}
    	
    	/**
    	 * This method sends a post request to the webservice. With this request
    	 * the webservice tries to create a new tour. If it could be created a duplicate
    	 * is written into the local database. Otherwise a error dialog is shown
    	 */
    	private void tryToCreateATour() {
    		JSONObject json = new JSONObject();
			try {
				db.openDatabase();
				json.put("email", user.get(0));
				json.put("depature", db.getLocationId(tour[2], tour[1]));
				json.put("arrival", db.getLocationId(tour[3], tour[1]));
				json.put("deptime", getDate(tvDate.getText(), tvFromTime.getText()));
				json.put("arrtime", getDate(tvDate.getText(), tvToTime.getText()));
				db.closeDatabase();
				
				String response = JSONCommunicator.postJSONObject(
						PropertyManager.getJSONServer() + "createTourByApp", json);
				
				if (response.equalsIgnoreCase("error, tour could not be created") 
						|| response.contains("Exception")) {
					Log.i("CREATE TOUR", response.toString());
					ErrorDialog errorDialog = new ErrorDialog(context, "Error", "Couldn't create the tour for some reasons");
					errorDialog.show();
					this.dismiss();
				} else {
					//the webservice returns the id & the users & the mod 
					StringTokenizer st = new StringTokenizer(response, "&");
					
					if (st.countTokens()==3) {
						tour[0] = st.nextToken();
						tour[4] = getDate(tvDate.getText(), tvFromTime.getText());
						tour[5] = getDate(tvDate.getText(), tvToTime.getText());
						tour[6] = Integer.toString(1);
						tour[7] = st.nextToken();
						tour[8] = st.nextToken();
						tour[9] = TourKind.ACTIVE.toString();
						
						db.openDatabase();
						db.insertTour(tour);
						db.closeDatabase();
						
						TourActivity.this.updateTourRows(false);
						Toast.makeText(context, R.string.createTourDialog_createdTour, Toast.LENGTH_LONG).show();
						this.dismiss();
					} else {
						ErrorDialog errorDialog = new ErrorDialog(context, "Error", "Couldn't create the tour for some reasons");
						errorDialog.show();
						this.dismiss();
					}
				}
			} catch (Exception e) {
				ErrorDialog errorDialog = new ErrorDialog(context, e.toString(), e.getMessage());
				errorDialog.show();
			}
    	}
    	
    	private String getDate(CharSequence date, CharSequence time) throws ParseException {
    		SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm");
			Date javaDate = df.parse(date + " " + time);
			return Long.toString(javaDate.getTime());
    	}
	}
}



