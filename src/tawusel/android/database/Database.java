package tawusel.android.database;

import java.util.Vector;

import tawusel.android.enums.TourKind;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * This class provides the interface to the local database on the android
 * phone. It's responsible for the saving of tours, templates, status 
 * messages, city and location names. 
 * It uses the inner class {@link DBHelper} to fulfill the the ddl- and 
 * dml statements on the database.
 */
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
	
	public static final String KEY_NAME = "name";
	public static final String KEY_DESCRITPTION = "description";
	
	public static final String KEY_TOWN_ID = "town_id";
	public static final String KEY_ADDRESS = "address";
	
	private static final String DATABASE_NAME = "TawuselHandyDB";
	private static final String USER_TABLE_NAME = "loggedInUser";
	private static final String TOUR_TABLE_NAME = "tour";
	private static final String TEMPLATE_TABLE_NAME = "template";
	private static final String STATE_TABLE_NAME = "tour_state";
	private static final String TOWN_TABLE_NAME = "town";
	private static final String LOCATION_TABLE_NAME = "location";
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
	
	/**
	 * If the user set on the specific checkbox while logging in, this 
	 * method is called to save the users email and password in the 
	 * users table on the local database. 
	 *  
	 * @param email - the users email
	 * @param password - the users password (sha1 hash)
	 * @param stayLoggedIn - boolen value, if this is set users will not be 
	 * deleted after closing the app -> next time the app is started the login
	 * is fulfilled automatically
	 */
	public void setUserLoggedIn(String email, String password, int stayLoggedIn) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_EMAIL, email);
		cv.put(KEY_PASSWORD, password);
		cv.put(KEY_STAY_LOGGED_IN, stayLoggedIn);
		database.insert(USER_TABLE_NAME, null, cv);
	}
	
	/**
	 * Call this method if in activity needs the users data.
	 * 
	 * @return - the users data as a vector of strings without it's id 
	 */
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
	
	/**
	 * Checks if the the tour given by parameter already is saved in the
	 * local database. If it is, the method calls {@link Database#updateTour(String[])} 
	 * else it inserts the tour by calling {@link Database#insertTour(String[])}.
	 * 
	 * @param tourData - a string array which contains the data retrieved by the 
	 * tawusel webservice
	 */
	public void checkAndUpdateTour(String[] tourData) {
		String[] tour = getTour(Integer.parseInt(tourData[0]));
		if(tour[0] == null) {
			insertTour(tourData);
		} else {
			updateTour(tourData);
		}
	}
	
	/**
	 * Parses the tourData array and inserts it into the local database.
	 * 
	 * @param tourData - a string array which contains the data retrieved by the 
	 * tawusel webservice
	 */
	public void insertTour(String[] tourData) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_ID, Integer.parseInt(tourData[0]));
		cv.put(KEY_CITY, tourData[1]);
		cv.put(KEY_DEPATURE_LOCATION, tourData[2]);
		cv.put(KEY_ARRIVAL_LOCATION, tourData[3]);
		cv.put(KEY_DEPATURE_TIME, tourData[4]);
		cv.put(KEY_ARRIVAL_TIME, tourData[5]);
		cv.put(KEY_STATE, tourData[6]);
		cv.put(KEY_MEMBERS, tourData[7]);
		cv.put(KEY_MOD, tourData[8]);
		cv.put(KEY_KIND, tourData[9]);
		database.insert(TOUR_TABLE_NAME, null, cv);
	}
	
	/**
	 * Parses the tourData array and updates its entry in the local database
	 * 
	 * @param tourData - a string array which contains the data retrieved by the 
	 * tawusel webservice
	 */
	public void updateTour(String[] tourData) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_CITY, tourData[1]);
		cv.put(KEY_DEPATURE_LOCATION, tourData[2]);
		cv.put(KEY_ARRIVAL_LOCATION, tourData[3]);
		cv.put(KEY_DEPATURE_TIME, tourData[4]);
		cv.put(KEY_ARRIVAL_TIME, tourData[5]);
		cv.put(KEY_STATE, tourData[6]);
		cv.put(KEY_MEMBERS, tourData[7]);
		cv.put(KEY_MOD, tourData[8]);
		cv.put(KEY_KIND, tourData[9]);
		database.update(TOUR_TABLE_NAME, cv, KEY_ID + " = " + tourData[0] , null);
	}
	
	/**
	 * Sends a database query to retrieve the tour with the given id from
	 * the local database. If the select contains only one entry the values
	 * are returned in a string array. 
	 * 
	 * @param id - an integer which represents the tour id
	 * @return a string array which contains the tours data
	 */
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

	/**
	 * Parses the tourData array and inserts it into the local database. Notice:
	 * This method is called with a template tour as parameter so it's array contains
	 * only five values, since there are no members and there is no status. 
	 * 
	 * @param tourData - a string array which contains the data retrieved by the 
	 * tawusel webservice (Values: city name, depature name, arrival name, depature time
	 * and arrival time)
	 */
	public void insertTemplate(String[] tourData) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_CITY, tourData[0]);
		cv.put(KEY_DEPATURE_LOCATION, tourData[1]);
		cv.put(KEY_ARRIVAL_LOCATION, tourData[2]);
		cv.put(KEY_DEPATURE_TIME, tourData[3]);
		cv.put(KEY_ARRIVAL_TIME, tourData[4]);
		database.insert(TEMPLATE_TABLE_NAME, null, cv);
	}
	
	/**
	 * Sends a database query to retrieve the template with the given id from
	 * the local database. If the select contains only one entry the values
	 * are returned in a string array. 
	 * 
	 * @param id - an integer which represents the template id
	 * @return a string array which contains the templates data (which is formated 
	 * like a tour)
	 */
	public String[] getTemplate(int id) {
		String[] columns = new String[] {KEY_ID, KEY_CITY, KEY_DEPATURE_LOCATION, KEY_ARRIVAL_LOCATION,
				KEY_DEPATURE_TIME, KEY_ARRIVAL_TIME};
		Cursor c = database.query(TEMPLATE_TABLE_NAME, columns, KEY_ID + " = " + id, null, null, null, null);
		String[] result = new String[10];

		int iCity = c.getColumnIndex(KEY_CITY);		
		int iDepatureLocation = c.getColumnIndex(KEY_DEPATURE_LOCATION);
		int iArrivalLocation = c.getColumnIndex(KEY_ARRIVAL_LOCATION);
		int iDepatureTime = c.getColumnIndex(KEY_DEPATURE_TIME);
		int iArrivalTime = c.getColumnIndex(KEY_ARRIVAL_TIME);
		
		if(c.getCount() == 1) {
			c.moveToLast();
			result[0] = Integer.toString(id);
			result[1] = c.getString(iCity);
			result[2] = c.getString(iDepatureLocation);
			result[3] = c.getString(iArrivalLocation);
			result[4] = c.getString(iDepatureTime);
			result[5] = c.getString(iArrivalTime);
			result[6] = "";
			result[7] = "";
			result[8] = "";
			result[9] = TourKind.TEMPLATE.toString();
		}
		c.close();
		return result;
	}
	
	/**
	 * Sends a database queries to retrieve the all tours (active- , template- and 
	 * available tours) from the local database. The method uses 
	 * {@link Database#getTours(String[], String)} and {@link Database#getTemplates(String[])} 
	 * to get the specific tour types.
	 * 
	 * @return a vector of string arrays with the data of the tours
	 */
	public Vector<String[]> getAllTours() {
		Vector<String[]> tours = new Vector<String[]>();
		String[] tourColumns = new String[] {KEY_ID, KEY_CITY, KEY_DEPATURE_LOCATION, KEY_ARRIVAL_LOCATION,
				KEY_DEPATURE_TIME, KEY_ARRIVAL_TIME, KEY_STATE, KEY_MEMBERS, KEY_MOD, KEY_KIND};
		String[] templateColumns = new String[] {KEY_ID, KEY_CITY, KEY_DEPATURE_LOCATION, KEY_ARRIVAL_LOCATION,
				KEY_DEPATURE_TIME, KEY_ARRIVAL_TIME};
		
		String whereClause = KEY_KIND + " = '" + TourKind.ACTIVE.toString() + "'";
		tours.addAll(getTours(tourColumns, whereClause));
		
		tours.addAll(getTemplates(templateColumns));
		
		whereClause = KEY_KIND + " = '" + TourKind.AVAILABLE.toString() + "'";
		tours.addAll(getTours(tourColumns, whereClause));
		
		return tours;
	}
	
	/**
	 * Sends a database query to select all tours of a specific kind from the tours
	 * table of the local database.
	 * 
	 * @param tourColumns - columns that should be retrieved by the statement
	 * @param whereClause - which contains the tourKind (ACITVE or AVAILABE)
	 * @return a vector of string arrays with the data of the tours
	 */
	private Vector<String[]> getTours(String[] tourColumns, String whereClause) {
		Vector<String[]> tours = new Vector<String[]>();
		Cursor c = database.query(TOUR_TABLE_NAME, tourColumns, whereClause, null, null, null, null);
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
	
	/**
	 * Sends a database query to select all template tours from the templates
	 * table of the local database.
	 * 
	 * @param tourColumns - columns that should be retrieved by the statement
	 * @return a vector of string arrays with the data of the tours
	 */
	private Vector<String[]> getTemplates(String[] templateColumns) {
		Vector<String[]> tours = new Vector<String[]>();
		Cursor c = database.query(TEMPLATE_TABLE_NAME, templateColumns, null, null, null, null, null);
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String[] values = new String[10];
			for(int i = 0; i < c.getColumnCount(); i++) {
				values[i] = c.getString(i);
			}
			for (int i = 6; i < values.length; i++) {
				values[i] = "";
			}
			values[9] = TourKind.TEMPLATE.toString();
			tours.add(values);
		}
		
		c.close();
		return tours;
	}
	
	/**
	 * Parses the stateData array and inserts it into the local database. 
	 * 
	 * @param stateData - a string array which contains the data retrieved by the 
	 * tawusel webservice (Values: city name, depature name, arrival name, depature time
	 * and arrival time)
	 */
	public void insertState(String[] stateData) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_ID, Integer.parseInt(stateData[0]));
		cv.put(KEY_NAME, stateData[1]);
		cv.put(KEY_DESCRITPTION, stateData[2]);
		database.insert(STATE_TABLE_NAME, null, cv);
	}
	
	/**
	 * Sends a database query to retrieve the state with the given id from
	 * the local database. If the select contains only one entry the values
	 * are returned in a string array. 
	 * 
	 * @param id - an integer which represents the state id
	 * @return a string array which contains the states data 
	 */
	public String[] getState(int id) {
		String[] columns = new String[] {KEY_ID, KEY_NAME, KEY_DESCRITPTION};
		Cursor c = database.query(STATE_TABLE_NAME, columns, KEY_ID + " = " + id, null, null, null, null);
		String[] result = new String[3];
		
		int iName = c.getColumnIndex(KEY_NAME);		
		int iDescription = c.getColumnIndex(KEY_DESCRITPTION);
		
		if(c.getCount() == 1) {
			c.moveToLast();
			result[0] = Integer.toString(id);
			result[1] = c.getString(iName);
			result[2] = c.getString(iDescription);
		}
		c.close();
		return result;
	}
	
	/**
	 * Parses the townData array and inserts it into the local database. 
	 * 
	 * @param townData - a string array which contains the data retrieved by the 
	 * tawusel webservice 
	 */
	public void insertTown(String[] townData) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_ID, Integer.parseInt(townData[0]));
		cv.put(KEY_NAME, townData[1]);
		database.insert(TOWN_TABLE_NAME, null, cv);
	}
	
	/**
	 * Sends a database query to retrieve the town with the given id from
	 * the local database. If the select contains only one entry the values
	 * are returned in a string array. 
	 * 
	 * @param id - an integer which represents the town id
	 * @return a string array which contains the towns data 
	 */
	public String[] getTown(int id) {
		String[] columns = new String[] {KEY_ID, KEY_NAME};
		Cursor c = database.query(TOWN_TABLE_NAME, columns, KEY_ID + " = " + id, null, null, null, null);
		String[] result = new String[3];
		
		int iName = c.getColumnIndex(KEY_NAME);		
		
		if(c.getCount() == 1) {
			c.moveToLast();
			result[0] = Integer.toString(id);
			result[1] = c.getString(iName);
		}
		c.close();
		return result;
	}
	
	/**
	 * Sends a database queries to retrieve the all towns from the local database. 
	 * 
	 * @return a vector of string arrays with the data of the towns
	 */
	public Vector<String[]> getAllTowns() {
		Vector<String[]> towns = new Vector<String[]>();
		String[] townColumns = new String[] {KEY_ID, KEY_NAME};
		Cursor c = database.query(TOWN_TABLE_NAME, townColumns, null, null, null, null, null);
		
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			String[] values = new String[2];
			for (int i = 0; i < c.getColumnCount(); i++) {
				values[i] = c.getString(i);
			}
			towns.add(values);
		}
		
		c.close();
		return towns;
	}

	/**
	 * Sends a database query to retrieve the town id with the given town name from
	 * the local database. If the select contains only one entry the values
	 * are returned in a string array. 
	 * 
	 * @param townName as a string
	 * @return the town id as an integer 
	 */
	public int getTownId(String townName) {
		int townId = 0;
		String[] townColumns = new String[] {KEY_ID};
		Cursor c = database.query(TOWN_TABLE_NAME, townColumns, KEY_NAME + " = '" + townName + "'", null, null, null, null);
		
		if(c.getCount()==1) {
			c.moveToLast();
			townId = c.getInt(0);
		}
		c.close();
		return townId;
	}
	
	/**
	 * Parses the locationData array and inserts it into the local database. 
	 * 
	 * @param locationData - a string array which contains the data retrieved by the 
	 * tawusel webservice 
	 */
	public void insertLocation(String[] locationData) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_ID, Integer.parseInt(locationData[0]));
		cv.put(KEY_TOWN_ID, Integer.parseInt(locationData[1]));
		cv.put(KEY_NAME, locationData[2]);
		cv.put(KEY_ADDRESS, locationData[3]);
		database.insert(LOCATION_TABLE_NAME, null, cv);
	}
	
	/**
	 * Sends a database queries to retrieve the all locations from the local database. 
	 * 
	 * @return a vector of string arrays with the data of the location
	 */
	public Vector<String[]> getAllLocations(String townName) {
		Vector<String[]> locations = new Vector<String[]>();
		int townId = getTownId(townName);
		String[] locationColumns = new String[] {KEY_ID, KEY_TOWN_ID, KEY_NAME, KEY_ADDRESS};
		Cursor cur = database.query(LOCATION_TABLE_NAME, locationColumns, KEY_TOWN_ID + " = " + townId, null, null, null, null);
		
		for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
			String[] values = new String[2];
			values[0] = cur.getString(0);
			values[1] = cur.getString(2);
			locations.add(values);
		}
		
		cur.close();
		return locations;
	}
	
	/**
	 * Sends a database query to retrieve the location id with the given location name 
	 * from the local database. If the select contains only one entry the values
	 * are returned in a string array. 
	 * 
	 * @param locationName as a string
	 * @param townName as a string
	 * @return the location id as an integer 
	 */
	public int getLocationId(String locationName, String townName) {
		int townId = getTownId(townName);
		int locationId = 0;
		String[] locationColumns = new String[] {KEY_ID};
		String whereClause = KEY_NAME + " = '" + locationName + "' AND " + KEY_TOWN_ID + " = " + townId;
		Cursor c = database.query(LOCATION_TABLE_NAME, locationColumns, whereClause, null, null, null, null);
		
		if(c.getCount()==1) {
			c.moveToLast();
			locationId = c.getInt(0);
		}
		c.close();
		return locationId;
	}
	
 	public void clearUserTable(){
		database.delete(USER_TABLE_NAME, KEY_ID + "> 0", null);
	}
	
	public void clearTourTable(){
		database.delete(TOUR_TABLE_NAME, KEY_ID + "> 0", null);
		
	}
	
	public void clearTemplateTable() {
		database.delete(TEMPLATE_TABLE_NAME, KEY_ID + "> 0", null);
	}
	
	public void clearStateTable() {
		database.delete(STATE_TABLE_NAME, KEY_ID + "> 0", null);
	}
	
	public void clearTownTable() {
		database.delete(TOWN_TABLE_NAME, KEY_ID + "> 0", null);
	}
	
	public void clearLocationTable() {
		database.delete(LOCATION_TABLE_NAME, KEY_ID + "> 0", null);
	}
	
	public void resetDatabase() {
		dbHelper.onUpgrade(database,0, 1);
	}
	
	/**
	 * This class provides an interface to the sql staments itself. It is used 
	 * to access the data (read and write) of the database. It creates the tables
	 * needed for using tawusel app.
	 */
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
					KEY_ID + " INTEGER PRIMARY KEY, " +
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
			db.execSQL("CREATE TABLE " + TEMPLATE_TABLE_NAME + " (" +
					KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_CITY + " TEXT NOT NULL, " +
					KEY_DEPATURE_LOCATION + " TEXT NOT NULL, " +
					KEY_ARRIVAL_LOCATION + " TEXT NOT NULL, " +
					KEY_DEPATURE_TIME + " TEXT NOT NULL, " + 
					KEY_ARRIVAL_TIME + " TEXT NOT NULL);"
					);
			db.execSQL("CREATE TABLE " + STATE_TABLE_NAME + " (" +
					KEY_ID + " INTEGER PRIMARY KEY, " +
					KEY_NAME + " TEXT NOT NULL, " +
					KEY_DESCRITPTION + " TEXT NOT NULL);"
					);
			db.execSQL("CREATE TABLE " + TOWN_TABLE_NAME + " (" +
					KEY_ID + " INTEGER PRIMARY KEY, " +
					KEY_NAME + " TEXT NOT NULL); " 
					);
			db.execSQL("CREATE TABLE " + LOCATION_TABLE_NAME + " (" +
					KEY_ID + " INTEGER PRIMARY KEY, " +
					KEY_TOWN_ID + " INTEGER NOT NULL, " +
					KEY_NAME + " TEXT NOT NULL, " +
					KEY_ADDRESS + " TEXT NOT NULL); " 
					);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME + ";");
			db.execSQL("DROP TABLE IF EXISTS " + TOUR_TABLE_NAME + ";");
			db.execSQL("DROP TABLE IF EXISTS " + TEMPLATE_TABLE_NAME + ";");
			db.execSQL("DROP TABLE IF EXISTS " + STATE_TABLE_NAME + ";");
			db.execSQL("DROP TABLE IF EXISTS " + TOWN_TABLE_NAME + ";");
			db.execSQL("DROP TABLE IF EXISTS " + LOCATION_TABLE_NAME + ";");
			onCreate(db);
		}
		
	}
	
}