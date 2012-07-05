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
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TourDetailsDialog extends Dialog implements OnClickListener {
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
				Toast.makeText(context, R.string.tourDetailsDialog_userJoined, Toast.LENGTH_LONG).show();
			}
		} else if(v == bLeave) {
			boolean isLeft = tryToLeaveTour();
			if(isLeft) {
				updateDialog();
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
}
