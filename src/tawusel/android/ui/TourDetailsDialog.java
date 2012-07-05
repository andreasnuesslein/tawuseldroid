package tawusel.android.ui;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import tawusel.andorid.R;
import tawusel.android.enums.TourKind;
import tawusel.android.tools.communication.JSONCommunicator;
import tawusel.android.ui.helper.JSONArrayHelper;
import tawusel.android.ui.helper.Time;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TourDetailsDialog extends Dialog implements OnClickListener {
	private TextView tvCity, tvFrom, tvTo, tvTime, tvStatus, tvPassengers;
	private Button bJoin, bLeave;
	
	private String[] tour = new String[10];
	private String[] status = new String[3];
	private Vector<String> user; 
	
	@SuppressWarnings("unchecked")
	public TourDetailsDialog(Context context, String[] tour, String[] status, Vector<String> user) {
		super(context);
		
		this.setContentView(R.layout.tour_details_dialog);
 	   	this.setTitle(R.string.tourDetailsDialog_head);
 	   	
 	   	this.tour = tour;
 	   	this.status = status;
 	   	this.user = (Vector<String>) user.clone();
 	   	
 	   	initGuiElements();
 	   	setTextFieldValues();
 	   	disableButton();
	}
	
	private void initGuiElements() {
		tvCity = (TextView) findViewById(R.id.tourDetailsDialog_tvCity);
		tvFrom = (TextView) findViewById(R.id.tourDetailsDialog_tvFrom);
		tvTo = (TextView) findViewById(R.id.tourDetailsDialog_tvTo);
		tvTime = (TextView) findViewById(R.id.tourDetailsDialog_tvTime);
		tvStatus = (TextView) findViewById(R.id.tourDetailsDialog_tvStatus);
		tvPassengers = (TextView) findViewById(R.id.tourDetailsDialog_tvPassengers);
		bJoin = (Button) findViewById(R.id.tourDetailsDialog_bJoinButton);
		
		bLeave = (Button) findViewById(R.id.tourDetailsDialog_bLeaveButton);
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
		} else if(!isUserPassengerOfTour) {
			bLeave.setEnabled(false);
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
	   

	   
		
}
