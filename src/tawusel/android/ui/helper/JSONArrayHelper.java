package tawusel.android.ui.helper;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * This class provides to method to sort json array, because 
 * the are parsed in arbitary order and in some methods there
 * is a specific ordering needed.
 *
 */
public class JSONArrayHelper {

	public static Vector<JSONArray> sort(Vector<JSONArray> arrays) throws JSONException {
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
	
	public static Vector<JSONArray> sortPassengersArray(Vector<JSONArray> arrays) throws JSONException {
		JSONArray tmpNameArray = arrays.get(0);
		JSONArray tmpValArray = arrays.get(1);
		JSONArray sortedNameArray= new JSONArray(); 
		JSONArray sortedValArray = new JSONArray(); 
		
		for (int i = 0; i < tmpNameArray.length(); i++) {
			if(tmpNameArray.get(i).equals("id")) {
				sortedNameArray.put(0, tmpNameArray.get(i)) ;
				sortedValArray.put(0, tmpValArray.get(i));
			} else if(tmpNameArray.get(i).equals("email")) {
				sortedNameArray.put(1, tmpNameArray.get(i)) ;
				sortedValArray.put(1, tmpValArray.get(i));
			} else if(tmpNameArray.get(i).equals("firstname")) {
				sortedNameArray.put(2, tmpNameArray.get(i)) ;
				sortedValArray.put(2, tmpValArray.get(i));
			} else if(tmpNameArray.get(i).equals("lastname")) {
				sortedNameArray.put(3, tmpNameArray.get(i)) ;
				sortedValArray.put(3, tmpValArray.get(i));				
			} else if(tmpNameArray.get(i).equals("cellphone")) {
				sortedNameArray.put(4, tmpNameArray.get(i)) ;
				sortedValArray.put(4, tmpValArray.get(i));
			} else if(tmpNameArray.get(i).equals("password")) {
				sortedNameArray.put(5, tmpNameArray.get(i)) ;
				sortedValArray.put(5, tmpValArray.get(i));
			}
		}
		
		arrays.clear();
		arrays.add(sortedNameArray);
		arrays.add(sortedValArray);
		return arrays;
		}
 }
