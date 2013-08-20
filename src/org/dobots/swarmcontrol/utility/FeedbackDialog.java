package org.dobots.swarmcontrol.utility;

import org.dobots.swarmcontrol.R;
import org.dobots.utility.IButtonPressListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

public class FeedbackDialog extends Activity {
	
	private static IButtonPressListener onUpPress = null;
	private static IButtonPressListener onDownPress = null;
	
	public static final String TITLE = "Title";
	public static final String MESSAGE = "Message";
	
	public static final int FEEDBACK_RESULT = 1050;
	
	public static final int RESULT_UP = RESULT_FIRST_USER;
	public static final int RESULT_DOWN = RESULT_UP + 1;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.feedback_dialog);
 
        // Set result CANCELED incase the user backs out
        setResult(RESULT_CANCELED);

        // Initialize the button to perform device discovery
        Button btnUp = (Button) findViewById(R.id.btnFeedback_Up);
        btnUp.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					if (onUpPress != null) {
						onUpPress.onButtonPressed(false);
					} else {
						setResult(RESULT_UP);
		                finish();
					}
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					if (onUpPress != null) {
						onUpPress.onButtonPressed(true);
					} else {
						// set result once the button is released
					}
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;					
				case MotionEvent.ACTION_MOVE:
					break;
				}
				return false;
			}
		});
        
        Button btnOK = (Button) findViewById(R.id.btnFeedback_Ok);
        btnOK.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_OK);
				finish();
			}
		});
        
        Button btnDown = (Button) findViewById(R.id.btnFeedback_Down);
        btnDown.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent e) {
				int action = e.getAction();
				switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP:
					if (onDownPress != null) {
						onDownPress.onButtonPressed(false);
					} else {
						setResult(RESULT_DOWN);
		                finish();
					}
					break;
				case MotionEvent.ACTION_POINTER_UP:
					break;
				case MotionEvent.ACTION_DOWN:
					if (onDownPress != null) {
						onDownPress.onButtonPressed(true);
					} else {
						// set result once the button is released
					}
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;					
				case MotionEvent.ACTION_MOVE:
					break;
				}
				return false;
			}
		});
        
        Bundle oParameter = this.getIntent().getExtras();
        String strTitle = oParameter.getString(TITLE);
        String strMessage = oParameter.getString(MESSAGE);
        
//        TextView lblTitle = (TextView) findViewById(R.id.lblFeedback_Title);
//        lblTitle.setText(strTitle);
        setTitle(strTitle);
        
        TextView lblMessage = (TextView) findViewById(R.id.lblFeedback_Message);
        lblMessage.setText(strMessage);
    }
    
    public static void createAndShow(Activity i_oActivity, String i_strTitle, String i_strMessage, IButtonPressListener i_onUpPress, IButtonPressListener i_onDownPress) {

		Intent oIntent = new Intent(i_oActivity, FeedbackDialog.class);
		Bundle oParam = new Bundle();
		oParam.putString(TITLE, i_strTitle);
		oParam.putString(MESSAGE, i_strMessage);
		oIntent.putExtras(oParam);
		
		FeedbackDialog.onUpPress = i_onUpPress;
		FeedbackDialog.onDownPress = i_onDownPress;
		
		i_oActivity.startActivityForResult(oIntent, FEEDBACK_RESULT);
    }

}
