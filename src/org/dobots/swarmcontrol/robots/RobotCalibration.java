package org.dobots.swarmcontrol.robots;

import org.dobots.swarmcontrol.R;
import org.dobots.swarmcontrol.utility.CalibrationDialogSelf;
import org.dobots.swarmcontrol.utility.CalibrationDialogSelf.OnRunClick;
import org.dobots.swarmcontrol.utility.CalibrationDialogUser;
import org.dobots.utilities.BaseActivity;

import robots.RobotInventory;
import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.gui.MessageTypes;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RobotCalibration extends BaseActivity {
	
	private IRobotDevice m_oRobot;
	
	private Button m_btnCalibrateCircleSelf;
	private Button m_btnCalibrateCircleUser;
	private Button m_btnCalibrateDistance;
	private Button m_btnCalibrateSave;
	private Button m_btnCalibrateDiscard;
	
	private BaseActivity m_oActivity;
	
	private double m_dblSpeed;

	public static final String CALIBRATED_SPEED = "CALIBRATED_SPEED";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		m_oActivity = this;
		
    	m_dblSpeed = (Double) getIntent().getExtras().get("Speed");
        String strRobotID = (String) getIntent().getExtras().get("RobotID");
        
        m_oRobot = RobotInventory.getInstance().getRobot(strRobotID);

    	setProperties();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case MessageTypes.CALIBRATION_RESULT:
			if (resultCode == RESULT_OK) {
				m_dblSpeed = data.getExtras().getDouble(CalibrationDialogSelf.CALIBRATED_VALUE);
			}
		}
		
	}
	
	private void setProperties() {
		setContentView(R.layout.robot_calibration);

        setTitle("Robot Calibration");
        
		m_btnCalibrateCircleSelf = (Button) findViewById(R.id.btnCalibrate_Circle_Self);
		m_btnCalibrateCircleSelf.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CalibrationDialogSelf.createAndShow(m_oActivity, "Calibrate Circle", "Check if Circle was done correctly and adjust if necessary", m_dblSpeed,
						new OnRunClick() {
							
							@Override
							public void onClick(double i_nTime, double i_nSpeed) {
								m_oRobot.executeCircle(i_nTime, i_nSpeed);
							}
						});
			}
		});
		
		m_btnCalibrateCircleUser = (Button) findViewById(R.id.btnCalibrate_Circle_User);
		m_btnCalibrateCircleUser.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				CalibrationDialogUser.createAndShow(m_oActivity, "Calibrate Circle", "Check if Circle was done correctly and adjust if necessary", m_dblSpeed,
						new OnRunClick() {
							
							@Override
							public void onClick(double i_nTime, double i_nSpeed) {
								m_oRobot.executeCircle(i_nTime, i_nSpeed);
							}
						});
			}
		});
		m_btnCalibrateSave = (Button) findViewById(R.id.btnCalibrate_Save);
		m_btnCalibrateSave.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent result = new Intent();
				Bundle data = new Bundle();
				data.putDouble(CALIBRATED_SPEED, m_dblSpeed);
				result.putExtras(data);
				setResult(RESULT_OK, result);
				finish();
			}
		});
		
		m_btnCalibrateDiscard = (Button) findViewById(R.id.btnCalibrate_Discard);
		m_btnCalibrateDiscard.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
	}

	public static void createAndShow(Activity i_oActivity, RobotType i_eType, String strID, double i_dblSpeed) {
		Intent intent = new Intent(i_oActivity, RobotCalibration.class);
		intent.putExtra("RobotType", i_eType);
		intent.putExtra("RobotID", strID);
		intent.putExtra("Speed", i_dblSpeed);
		i_oActivity.startActivityForResult(intent, MessageTypes.ROBOT_CALIBRATION_RESULT);
	}
	
}
