package org.dobots.swarmcontrol;

import org.dobots.swarmcontrol.About;
import org.dobots.swarmcontrol.robots.RobotDevice;
import org.dobots.swarmcontrol.robots.RobotDeviceFactory;
import org.dobots.swarmcontrol.robots.RobotType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SwarmControlActivity extends Activity {
	
	private static final String TAG = "MAIN_ACTIVITY";

	// The different menu options
	private static final int CONNECT_ID = Menu.FIRST;
	private static final int DISCONNECT_ID = Menu.FIRST + 1;
	private static final int ABOUT_ID = Menu.FIRST + 2;
	private static final int EXIT_ID = Menu.FIRST + 3;

	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        Spinner spinner = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.swarm_action, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, CONNECT_ID, 1, "Connect");
		menu.add(0, DISCONNECT_ID, 2, "Disconnect");
		menu.findItem(DISCONNECT_ID).setVisible(false);
		menu.add(0, ABOUT_ID, 3, getResources().getString(R.string.about))
		.setIcon(R.drawable.ic_menu_about);
		menu.add(0, EXIT_ID, 4, "Exit");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case CONNECT_ID:
			AlertDialog dlgConnect = CreateConnectDialog();
			dlgConnect.show();
			return true;
		case DISCONNECT_ID:
			return true;
		case ABOUT_ID:
			About about = new About();
			about.show(this);
			return true;
		case EXIT_ID:
			finish();
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	private AlertDialog CreateConnectDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Choose a robot");
		final ArrayAdapter<RobotType> adapter = new ArrayAdapter<RobotType>(this, android.R.layout.select_dialog_item,
				RobotType.values());
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				RobotType eRobot = adapter.getItem(which);
				RobotDevice oRobot = RobotDeviceFactory.getRobotDevice(eRobot);
				oRobot.show(SwarmControlActivity.this, eRobot);
			}
		});
		return builder.create();
	}

}
