package tawusel.android.tools.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;

/**
 * This class holds the default configuration parameters for the system.<br>
 * It can be used to restore the default configuration and enables typesafe
 * usage of the property parameters.<br>
 * Also you can safe/load the configuration from/to a file.<br>
 * <br>
 * @see #loadDefaultConfiguration()
 * @see #saveConfiguration(String)
 * @see #getBaseDirPath()
 * @see #getIntProperty(String, int)
 * @see #getStringProperty(String, String)
 * @see #getFloatProperty(String, float)
 * @see #getBooleanProperty(String, boolean)
 * @see PropertyManager
 * 
 * @author Sebastian Schulz
 */
public class PropertyManager {
	protected static Properties properties = new Properties();
	
	//stings
	private static final String BOOKED_TOUR_COLOR = "i_BookedTour_Color";
	private static final String TEMPLATE_TOUR_COLOR = "i_TemplateTour_Color";
	private static final String ACTIVE_TOUR_COLOR = "i_ActiveTour_Color";
	private static final String JSON_SERVER = "s_JSON_Server";
	
	//default values
	private static int defaultBookedTourColor = Color.WHITE;
	private static int defaultTemplateTourColor = Color.BLACK;
	private static int defaultActiveTourColor = Color.GREEN;
	private static String defaultJSONServer = "http://localhost:9000/";
	

	public static void loadConfiguration(Context context) {
		Resources resources = context.getResources();
		AssetManager assetManager = resources.getAssets();
		try {
			InputStream inputStream = assetManager.open("tawusel.properties");
		    properties = new Properties();
		    properties.load(inputStream);

		} catch (IOException ioException) {
		}
	}
	
	public static int getBookedTourColor() {
		return getIntProperty(BOOKED_TOUR_COLOR, defaultBookedTourColor);
	}
	
	public static int getTemplateTourColor() {
		return getIntProperty(TEMPLATE_TOUR_COLOR, defaultTemplateTourColor);
	}
	
	public static int getActiveTourColor() {
		return getIntProperty(ACTIVE_TOUR_COLOR, defaultActiveTourColor);
	}
	
	public static String getJSONServer() {
		String str = getStringProperty(JSON_SERVER, defaultJSONServer);
		return str;
		
	}
	
	
	
	@SuppressWarnings("unused")
	private static void setPositiveIntegerInConfiguration(final String name, final int value) {
		if (value >= 0) {
			properties.setProperty(name, String.valueOf(value));
		}
		else {
		}
	}
	
	private static int getIntProperty(String propName, int defaults) {
		int value;
		try {
			value = Integer.parseInt(properties.getProperty(propName));
			return value;
		} catch (Exception e) {
			properties.setProperty(propName, String.valueOf(defaults));
			return defaults;
		}		 
	}
	
	private static String getStringProperty(String propName, String defaults) {
		String value="";
		try {
			value = properties.getProperty(propName);
			return value;
		} catch (Exception e) {
			properties.setProperty(propName, defaults);
			return defaults;
		}
	}
	
	@SuppressWarnings("unused")
	private static float getFloatProperty(String propName, float defaults) {
		float value;
		try {
			value = Float.parseFloat(properties.getProperty(propName));
			return value;
		} catch (Exception e) {
			properties.setProperty(propName, String.valueOf(defaults));
			return defaults;
		}	
	}
	
	@SuppressWarnings("unused")
	private static boolean getBooleanProperty(String propName, boolean defaults) {
		boolean value;
		try {
			String s = properties.getProperty(propName);
			if( s == null ) throw new Exception();
			value = Boolean.parseBoolean(s);
			return value;
		} catch (Exception e) {
			properties.setProperty(propName, String.valueOf(defaults));
			return defaults;
		}	
	}
	
}
	