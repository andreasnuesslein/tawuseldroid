package tawusel.android.database;

import java.util.Vector;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database {
	
	public static final String KEY_ID = "_id"; 
	public static final String KEY_EMAIL = "email";
	public static final String KEY_PASSWORD = "password";

	private static final String DATABASE_NAME = "TawuselHandyDB";
	private static final String TABLE_NAME = "loggedInUser";
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
	
	public void setUserLoggedIn(String email, String password) {
		ContentValues cv = new ContentValues();
		cv.put(KEY_EMAIL, email);
		cv.put(KEY_PASSWORD, password);
		database.insert(TABLE_NAME, null, cv);
	}
	
	public Vector<String> getData() {
		String[] columns = new String[] {KEY_ID, KEY_EMAIL,KEY_PASSWORD};
		Cursor c = database.query(TABLE_NAME, columns, null, null, null, null, null);
		Vector<String> result = new Vector<String>();
		
		int iName = c.getColumnIndex(KEY_EMAIL);
		
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			result.add(c.getString(iName)); 
		}
		return result;
	}
	
	public Vector<String> getLoggedInUser() {
		String[] columns = new String[] {KEY_ID, KEY_EMAIL,KEY_PASSWORD};
		Cursor c = database.query(TABLE_NAME, columns, null, null, null, null, null);
		Vector<String> result = new Vector<String>();

		int iMail = c.getColumnIndex(KEY_EMAIL);
		int iPassword = c.getColumnIndex(KEY_PASSWORD);
		
		for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
			result.add(c.getString(iMail));
			result.add(c.getString(iPassword));
		}
		return result;
	}
	
	public void clearTable(){
		database.delete(TABLE_NAME, KEY_ID + "> 0", null);
	}
	
	private static class DBHelper extends SQLiteOpenHelper {

		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
					KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
					KEY_EMAIL + " TEXT NOT NULL, "+
					KEY_PASSWORD + " TEXT NOT NULL);"
					);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
			onCreate(db);
		}
		
	}

	
}