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
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TableRow.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

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

	public void updateTourRows(boolean updateLocalDbFromService) {
		if (updateLocalDbFromService) {
			updateTourDataFromWebService();
		}
		clearTourRows();
		createTourViews();
	}
	
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
				String[] tourData = new String[10];
				for (int j = 0; j < tourValArray.length(); j++) {
					tourData[j] = tourValArray.get(j).toString();;
				}
				
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
		db.clearTemplateTable();
		db.closeDatabase();
		super.onDestroy();
	}
	
	private class TourDetailsDialog extends Dialog implements OnClickListener, OnDismissListener {
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
				}
			} else if(v == bLeave) {
				boolean isLeft = tryToLeaveTour();
				if(isLeft) {
					updateDialog();
					TourActivity.this.updateTourRows(false);
					Toast.makeText(context, R.string.tourDetailsDialog_userLeft, Toast.LENGTH_LONG).show();
				}
			}
		}
		
		private boolean tryToJoinTour() {
			String params = URLEncoder.encode(user.get(0)) + "/" + tour[0];
			try {
				JSONArray jsonResult = JSONCommunicator.getJSONArray("joinTourByApp/", params, PropertyManager.getJSONServer());
				
				if(jsonResult != null) {
					JSONObject result = jsonResult.getJSONObject(0);
					JSONArray resultNameArray = result.names();
					JSONArray resultValArray = result.toJSONArray(resultNameArray);
					
					//need to sort the json arrays because they are not build in the way they are sended
					Vector<JSONArray> arrays = new Vector<JSONArray>();
					arrays.add(resultNameArray);
					arrays.add(resultValArray);
					arrays = JSONArrayHelper.sort(arrays);
					resultValArray = arrays.get(1);
					
					if(!resultValArray.get(0).toString().equals(false)) {
						for (int j = 0; j < resultValArray.length(); j++) {
							tour[j] = resultValArray.get(j).toString();;
						}
						tour[9] = TourKind.ACTIVE.toString();
						
						db.openDatabase();
						db.updateTour(tour);
						db.closeDatabase();
						
						return true;
					}
				}	
				return false;
			} catch (Exception e) {
				ErrorDialog errorDialog = new ErrorDialog(context, "Unable to join the tour", e.toString() + " - " +e.getMessage());
				errorDialog.show();
				return false;
			}
		}

		private boolean tryToLeaveTour() {
			String params = URLEncoder.encode(user.get(0)) + "/" + tour[0];
			try {
				JSONArray jsonResult = JSONCommunicator.getJSONArray("leaveTourByApp/", params, PropertyManager.getJSONServer());
				
				if(jsonResult != null) {
					JSONObject result = jsonResult.getJSONObject(0);
					JSONArray resultNameArray = result.names();
					JSONArray resultValArray = result.toJSONArray(resultNameArray);
					
					//need to sort the json arrays because they are not build in the way they are sended
					Vector<JSONArray> arrays = new Vector<JSONArray>();
					arrays.add(resultNameArray);
					arrays.add(resultValArray);
					arrays = JSONArrayHelper.sort(arrays);
					resultValArray = arrays.get(1);
					
					if(!resultValArray.get(0).toString().equals(false)) {
						for (int j = 0; j < resultValArray.length(); j++) {
							tour[j] = resultValArray.get(j).toString();;
						}
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

		public void onDismiss(DialogInterface dialog) {
			TourActivity.this.updateTourRows(false);
		}
	}

	
}


