package tawusel.android.database;

import java.util.Vector;

import tawusel.android.enums.TourKind;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database {
	
	public static final String KEY_ID = "_id"; 
	
	public static final String KEY_EMAIL = "email";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_STAY_LOGGED_IN = "stay_logged_in";
	
	public static final String KEY_CITY = "city";
	public static final String KEY_DEPATURE_LOCATION = "dep_location";
	public static final String KEY_ARRIVAL_LOCATION = "arr_location";
	public static final String KEY_DEPATURE_TIME = "depature";
	public static final String KEY_ARRIVAL_TIME = "arrival";
	public static final String KEY_STATE = "state";
	public static final String KEY_MEMBERS = "members";
	public static final String KEY_MOD = "mod";
	public static final String KEY_KIND = "kind";

	private static final String DATABASE_NAME = "TawuselHandyDB";
	private static final String USER_TABLE_NAME = "loggedInUser";
	private static final String TOUR_TABLE_NAME = "tour";
	private static final int DATABASE_VERSION = 1;
	
	private DBHelper dbHelper;
	private final Context dbContext;
	private SQLiteDatabase database;
	
	public Database(Context context) {
		dbContext = context;
	}
	
	public Database openDatabase() {
		dbHelper = new DBHelper(dbContext);
		database = dbHelper.getWritableDatabase();
		return this;
	}
	
	public void closeDatabase() {
		dbHelper.close();
	}
	
	public void setUserLoggedIn(String email, String password, int stayLoggedIn) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_EMAIL, email);
		cv.put(KEY_PASSWORD, password);
		cv.put(KEY_STAY_LOGGED_IN, stayLoggedIn);
		database.insert(USER_TABLE_NAME, null, cv);
	}
	
	public Vector<String> getLoggedInUser() {
		String[] columns = new String[] {KEY_ID, KEY_EMAIL,KEY_PASSWORD,KEY_STAY_LOGGED_IN};
		Cursor c = database.query(USER_TABLE_NAME, columns, null, null, null, null, null);
		Vector<String> result = new Vector<String>();

		int iMail = c.getColumnIndex(KEY_EMAIL);
		int iPassword = c.getColumnIndex(KEY_PASSWORD);
		int iStayLoggedIn = c.getColumnIndex(KEY_STAY_LOGGED_IN);
		
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			result.add(c.getString(iMail));
			result.add(c.getString(iPassword));
			result.add(Integer.toString(c.getInt(iStayLoggedIn)));
		}
		c.close();
		return result;
	}
	
	public void insertTour(String[] tourData, TourKind kind) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_CITY, tourData[0]);
		cv.put(KEY_DEPATURE_LOCATION, tourData[1]);
		cv.put(KEY_ARRIVAL_LOCATION, tourData[2]);
		cv.put(KEY_DEPATURE_TIME, tourData[3]);
		cv.put(KEY_ARRIVAL_TIME, tourData[4]);
		cv.put(KEY_STATE, tourData[5]);
		cv.put(KEY_MEMBERS, tourData[6]);
		cv.put(KEY_MOD, tourData[7]);
		cv.put(KEY_KIND, kind.toString());
		database.insert(TOUR_TABLE_NAME, null, cv);
	}
	
	public Vector<String[]> getAllTours() {
		Vector<String[]> tours = new Vector<String[]>();
		String[] columns = new String[] {KEY_ID, KEY_CITY, KEY_DEPATURE_LOCATION, KEY_ARRIVAL_LOCATION,
				KEY_DEPATURE_TIME, KEY_ARRIVAL_TIME, KEY_STATE, KEY_MEMBERS, KEY_MOD, KEY_KIND};
		Cursor c = database.query(TOUR_TABLE_NAME, columns, null, null, null, null, null);
		
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String[] values = new String[10];
			for (int i = 0; i < c.getColumnCount(); i++) {
				values[i] = c.getString(i);
			}
			tours.add(values);
		}
		c.close();
		
		return tours;
	}
	
	public String[] getTour(int id) {
		String[] columns = new String[] {KEY_ID, KEY_CITY, KEY_DEPATURE_LOCATION, KEY_ARRIVAL_LOCATION,
				KEY_DEPATURE_TIME, KEY_ARRIVAL_TIME, KEY_STATE, KEY_MEMBERS, KEY_MOD, KEY_KIND};
		Cursor c = database.query(TOUR_TABLE_NAME, columns, KEY_ID + " = " + id, null, null, null, null);
		String[] result = new String[10];

		int iCity = c.getColumnIndex(KEY_CITY);		
		int iDepatureLocation = c.getColumnIndex(KEY_DEPATURE_LOCATION);
		int iArrivalLocation = c.getColumnIndex(KEY_ARRIVAL_LOCATION);
		int iDepatureTime = c.getColumnIndex(KEY_DEPATURE_TIME);
		int iArrivalTime = c.getColumnIndex(KEY_ARRIVAL_TIME);
		int iState = c.getColumnIndex(KEY_STATE);
		int iMembers = c.getColumnIndex(KEY_MEMBERS);
		int iMod = c.getColumnIndex(KEY_MOD);
		int iKind = c.getColumnIndex(KEY_KIND);
		
		if(c.getCount() == 1) {
			c.moveToLast();
			result[0] = Integer.toString(id);
			result[1] = c.getString(iCity);
			result[2] = c.getString(iDepatureLocation);
			result[3] = c.getString(iArrivalLocation);
			result[4] = c.getString(iDepatureTime);
			result[5] = c.getString(iArrivalTime);
			result[6] = c.getString(iState);
			result[7] = c.getString(iMembers);
			result[8] = c.getString(iMod);
			result[9] = c.getString(iKind);
		}
		c.close();
		return result;
	}
	
	public void clearUserTable(){
		database.delete(USER_TABLE_NAME, KEY_ID + "> 0", null);
	}
	
	public void clearTourTable(){
		database.delete(TOUR_TABLE_NAME, KEY_ID + "> 0", null);
		//dirty version - set autoincrement = 0
//		database.execSQL("delete from sqlite_sequence where name='" + TOUR_TABLE_NAME + "';");
	}
	
	public void resetDatabase() {
		dbHelper.onUpgrade(database,0, 1);
	}
	
	private static class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + USER_TABLE_NAME + " (" +
					KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_EMAIL + " TEXT NOT NULL, " +
					KEY_PASSWORD + " TEXT NOT NULL, " +
					KEY_STAY_LOGGED_IN + " INT NOT NULL);"
					);
			db.execSQL("CREATE TABLE " + TOUR_TABLE_NAME + " (" +
					KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_CITY + " TEXT NOT NULL, " +
					KEY_DEPATURE_LOCATION + " TEXT NOT NULL, " +
					KEY_ARRIVAL_LOCATION + " TEXT NOT NULL, " +
					KEY_DEPATURE_TIME + " TEXT NOT NULL, " + 
					KEY_ARRIVAL_TIME + " TEXT NOT NULL, " + 
					KEY_STATE + " TEXT NOT NULL, " +
					KEY_MEMBERS + " TEXT, " + 
					KEY_MOD + " TEXT, " +
					KEY_KIND + " TEXT NOT NULL" + ");"
					);
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME + ";");
			db.execSQL("DROP TABLE IF EXISTS " + TOUR_TABLE_NAME + ";");
			onCreate(db);
		}
		
	}

	
}