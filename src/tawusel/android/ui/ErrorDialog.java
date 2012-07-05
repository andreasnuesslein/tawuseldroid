package tawusel.android.ui;

import java.util.StringTokenizer;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

public class ErrorDialog extends Dialog {

	public ErrorDialog(Context context, String exceptionName, String exceptionMessage) {
		super(context);
		setTitle(getExceptionTitle(exceptionName));
		TextView tv = new TextView(context);
		tv.setText(exceptionMessage);
		setContentView(tv);
	}
	
	private String getExceptionTitle(String exceptionName) {
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
