package tawusel.android.ui.helper;

import java.util.StringTokenizer;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

public class Error {

	public static void createDialog(Context context, String exceptionName, String exceptionMessage) {
		Dialog dialog = new Dialog(context);
		dialog.setTitle(getExceptionTitle(exceptionName));
		TextView tv = new TextView(context);
		tv.setText(exceptionMessage);
		dialog.setContentView(tv);
		dialog.show();
	}
    
	private static String getExceptionTitle(String exceptionName) {
		//remove path in front of the title
		StringTokenizer st = new StringTokenizer(exceptionName, ".");
		String title = "Error";
		while (st.hasMoreElements()) {
			title = st.nextToken();
		}
		
		//remove the comment after the :
		st = new StringTokenizer(title, ":");
		title = st.nextToken();
		
		return title;
	}
}
