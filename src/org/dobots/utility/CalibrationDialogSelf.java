package org.dobots.utility;

import org.dobots.swarmcontrol.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class CalibrationDialogSelf extends Activity {
	
	public interface OnRunClick {
		public void onClick(double i_nTime, double i_nSpeed);
	}

	public static final String TITLE = "Title";
	public static final String MESSAGE = "Message";
	public static final String SPEED = "Speed";
	public static final String CALIBRATED_VALUE = "CALIBRATED_VALUE";
	
	public static final int CALIBRATION_RESULT = 1050;
	
	public static final int RESULT_UP = RESULT_FIRST_USER;
	public static final int RESULT_DOWN = RESULT_UP + 1;
	
	public static final double START = 50;
	public static final double DEFAULT_TIME = 3;
	public static final double INITIAL_STEP = 20;
	
	private double m_dblSpeed = START;
	private double m_dblTime = DEFAULT_TIME;
	
	private double m_dblStep = 0.0;
	
	private static OnRunClick onRunClicked;
	
	TextView m_txtSpeed;
	EditText m_edtTime;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.calibration_dialog);
 
        // Set result CANCELED incase the user backs out
        setResult(RESULT_CANCELED);

        Button btnUp = (Button) findViewById(R.id.btnCalibration_toohigh);
        btnUp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// if the last step too low we half the step and invert the step direction
				// if the last step was already too high we make the step bigger and try again
				if (m_dblStep > 0) {
					m_dblStep = - m_dblStep / 2;
				} else if (m_dblStep < 0) {
					m_dblStep = m_dblStep * 1.8;
				} else {
					m_dblStep = -INITIAL_STEP;
				}
					
				
				m_dblSpeed += m_dblStep;
				if (m_dblSpeed > 100) {
					m_dblSpeed = 100;
				}
				updateSpeedDisplay();
			}
		});

        // Initialize the button to perform device discovery
        Button btnDown = (Button) findViewById(R.id.btnCalibration_toolow);
        btnDown.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// if the last step too high we half the step and invert the step direction
				// if the last step was already too low we make the step bigger and try again
				if (m_dblStep < 0) {
					m_dblStep = - m_dblStep / 2;
				} else if (m_dblStep > 0) {
					m_dblStep = m_dblStep * 1.8;
				} else {
					m_dblStep = INITIAL_STEP;
				}
				
				m_dblSpeed += m_dblStep;
				if (m_dblSpeed > 100) {
					m_dblSpeed = 100;
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

		Intent oIntent = new Intent(i_oActivity, CalibrationDialogSelf.class);
		Bundle oParam = new Bundle();
		oParam.putString(TITLE, i_strTitle);
		oParam.putString(MESSAGE, i_strMessage);
		oParam.putDouble(SPEED, i_dblSpeed);
		oIntent.putExtras(oParam);
		
		CalibrationDialogSelf.onRunClicked = i_onRunClicked;
		
		i_oActivity.startActivityForResult(oIntent, CALIBRATION_RESULT);
    }

}
