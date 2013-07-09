package org.dobots.swarmcontrol.utility;

import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.utility.CalibrationDialogSelf.OnRunClick;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CalibrationDialogUser extends Activity {

	public static final String TITLE = "Title";
	public static final String MESSAGE = "Message";
	public static final String SPEED = "Speed";
	public static final String CALIBRATED_VALUE = "CALIBRATED_VALUE";
	
	public static final int CALIBRATION_RESULT = 1050;
	
	public static final int RESULT_UP = RESULT_FIRST_USER;
	public static final int RESULT_DOWN = RESULT_UP + 1;
	
	public static final double BIG = 5.0;
	public static final double SMALL = 1.0;
	public static final double START = 50.0;
	public static final double DEFAULT_TIME = 3.0;
	
	private double m_dblSpeed = START;
	private double m_dblTime = DEFAULT_TIME;
	
	private static OnRunClick onRunClicked;
	
	TextView m_txtSpeed;
	EditText m_edtTime;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.calib_dialog);
 
        // Set result CANCELED incase the user backs out
        setResult(RESULT_CANCELED);

        // Initialize the button to perform device discovery
        Button btnBigInc = (Button) findViewById(R.id.btnCalibration_BigInc);
        btnBigInc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_dblSpeed += BIG;
				if (m_dblSpeed > 100) {
					m_dblSpeed = 100;
				}
				updateSpeedDisplay();
			}
		});
        
        Button btnSmallInc = (Button) findViewById(R.id.btnCalibration_SmallInc);
        btnSmallInc.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_dblSpeed += SMALL;
				if (m_dblSpeed > 100) {
					m_dblSpeed = 100;
				}
				updateSpeedDisplay();
			}
		});

        // Initialize the button to perform device discovery
        Button btnBigDec = (Button) findViewById(R.id.btnCalibration_BigDec);
        btnBigDec.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_dblSpeed -= BIG;
				if (m_dblSpeed < 1) {
					m_dblSpeed = 1;
				}
				updateSpeedDisplay();
			}
		});
        
        Button btnSmallDec = (Button) findViewById(R.id.btnCalibration_SmallDec);
        btnSmallDec.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_dblSpeed -= SMALL;
				if (m_dblSpeed < 1) {
					m_dblSpeed = 1;
				}
				updateSpeedDisplay();
			}
		});
        
        Button btnRun = (Button) findViewById(R.id.btnCalibration_Run);
        btnRun.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				m_dblTime = new Double(m_edtTime.getText().toString());
				onRunClicked.onClick(m_dblTime * 1000, m_dblSpeed);
			}
		});
        
        Button btnOK = (Button) findViewById(R.id.btnCalibration_Ok);
        btnOK.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent result = new Intent();
				Bundle data = new Bundle();
				data.putDouble(CALIBRATED_VALUE, m_dblSpeed);
				result.putExtras(data);
				setResult(RESULT_OK, result);
				finish();
			}
		});
        
        m_txtSpeed = (TextView) findViewById(R.id.txtCalibration_Speed);
        updateSpeedDisplay();
        
        m_edtTime = (EditText) findViewById(R.id.edtCalibration_Time);
        m_edtTime.setText(Double.toString(m_dblTime));
        
        Bundle oParameter = this.getIntent().getExtras();
        String strTitle = oParameter.getString(TITLE);
        String strMessage = oParameter.getString(MESSAGE);
        m_dblSpeed = oParameter.getDouble(SPEED);
        updateSpeedDisplay();
        
        setTitle(strTitle);
        
        TextView lblMessage = (TextView) findViewById(R.id.lblFeedback_Message);
        lblMessage.setText(strMessage);
    }

    private void updateSpeedDisplay() {
    	m_txtSpeed.setText(Double.toString(m_dblSpeed));
    }
    
    public static void createAndShow(Activity i_oActivity, String i_strTitle, String i_strMessage, double i_dblSpeed, OnRunClick i_onRunClicked) {

		Intent oIntent = new Intent(i_oActivity, CalibrationDialogUser.class);
		Bundle oParam = new Bundle();
		oParam.putString(TITLE, i_strTitle);
		oParam.putString(MESSAGE, i_strMessage);
		oParam.putDouble(SPEED, i_dblSpeed);
		oIntent.putExtras(oParam);
		
		CalibrationDialogUser.onRunClicked = i_onRunClicked;
		
		i_oActivity.startActivityForResult(oIntent, CALIBRATION_RESULT);
    }

}
