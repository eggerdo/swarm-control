package org.dobots.swarmcontrol;

import java.util.Arrays;

import org.dobots.communication.zmq.ZmqHandler;
import org.dobots.robots.RobotDeviceFactory;
import org.dobots.swarmcontrol.SwarmControlTypes.SwarmAction;
import org.dobots.swarmcontrol.behaviours.dancing.DancingMain;
import org.dobots.swarmcontrol.robots.RobotViewFactory;
import org.dobots.utilities.BaseActivity;
import org.dobots.utilities.BaseApplication;
import org.dobots.utilities.RTFUtils;
import org.dobots.utilities.Utils;
import org.dobots.utility.ImprovedArrayAdapter;

import robots.RobotType;
import robots.ctrl.IRobotDevice;
import robots.gui.RobotInventory;
import robots.gui.RobotView;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class SwarmControlActivity extends BaseActivity {
	
	private static final String TAG = "MAIN_ACTIVITY";
	
	// The different menu options
	private static final int ABOUT_ID = Menu.FIRST;
	private static final int EXIT_ID = ABOUT_ID + 1;

	private static Context CONTEXT;
	
	private ZmqHandler m_oHandler;

	private ArrayAdapter<RobotType> m_oRobotAdapter;
	private ArrayAdapter<SwarmAction> m_oSwarmActionAdapter;
	
	private Button m_btnRobots;
	private Button m_btnSwarmActions;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.w(TAG, "Start SwarmControl app");

        CONTEXT = this;
        Utils.setContext(this);
        
        m_oHandler = new ZmqHandler(this);

        setContentView(R.layout.main);
        
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
		
        m_oSwarmActionAdapter = new ImprovedArrayAdapter<SwarmAction>(this, 
				android.R.layout.select_dialog_item, SwarmAction.values()) {

        	@Override
        	public boolean isEnabled(int position) {
        		SwarmAction eAction = m_oSwarmActionAdapter.getItem(position);
        		return eAction.isEnabled();
        	}
        };
        
        m_btnSwarmActions = (Button) findViewById(R.id.btnSwarmActions);
        m_btnSwarmActions.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showSwarmActionSelectionDialog();
			}
		});
        
        m_oRobotAdapter = new ImprovedArrayAdapter<RobotType>(this, android.R.layout.select_dialog_item,
				RobotType.getRobots()) {

        	@Override
        	public boolean isEnabled(int position) {
        		RobotType eType = m_oRobotAdapter.getItem(position);
        		return eType.isEnabled();
        	}
        	
        };
        
        
        m_btnRobots = (Button) findViewById(R.id.btnRobots);
        m_btnRobots.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showRobotSelectionDialog();
			}
		});
        
        writeChangeLog();
        
//        showRobot(RobotType.RBT_NXT);
//        showBehaviour(SwarmAction.sa_Dance);
    }
    
    

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ABOUT_ID, ABOUT_ID, getResources().getString(R.string.about))
		    .setIcon(R.drawable.ic_menu_about);
		menu.add(0, EXIT_ID, EXIT_ID, "Exit");
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
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

	public static Context getContext() {
		return CONTEXT;
	}
	
	private void showSwarmActionSelectionDialog() {
		AlertDialog dialog = Utils.CreateAdapterDialog(this, "Choose a swarm action", m_oSwarmActionAdapter,
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				SwarmAction eAction = m_oSwarmActionAdapter.getItem(which);
				dialog.dismiss();
				showBehaviour(eAction);
			}
		});
		dialog.show();
	}
	
	private void showRobotSelectionDialog() {
		AlertDialog dialog = Utils.CreateAdapterDialog(this, "Choose a robot", m_oRobotAdapter, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				RobotType eRobot = m_oRobotAdapter.getItem(which);
				dialog.dismiss();
				showRobot(eRobot);
			}
		});
		dialog.show();
	}

	public void showRobot(RobotType i_eType) {
		try {
			String i_strRobotID = createRobot(i_eType);
			createRobotView(i_eType, i_strRobotID, true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// TODO; replace by RobotLaunchHelper once all robots are moved to the Robot-Lib
	public void showRobot(RobotType i_eType, String i_strRobotID) {
		createRobotView(i_eType, i_strRobotID, false);
	}
	
	public void createRobotView(RobotType i_eType, String i_strRobotID, boolean i_bOwnsRobot) {
		Intent intent = new Intent(SwarmControlActivity.this, RobotViewFactory.getRobotViewClass(i_eType));
		intent.putExtra("RobotType", i_eType);
		intent.putExtra("RobotID", i_strRobotID);
		intent.putExtra("OwnsRobot", i_bOwnsRobot);
		startActivity(intent);
	}
	
	public String createRobot(RobotType i_eType) throws Exception {
		IRobotDevice oRobot = RobotDeviceFactory.getRobotDevice(i_eType);
		String i_strRobotID = RobotInventory.getInstance().addRobot(oRobot);
		return i_strRobotID;
	}

	public void showBehaviour(SwarmAction eAction) {
		Intent intent = null;
		switch(eAction) {
		case sa_Dance:
			intent = new Intent(SwarmControlActivity.this, DancingMain.class);
			startActivity(intent);
			break;
//		case sa_Race:
//			intent = new Intent(SwarmControlActivity.this, Racing.class);
//			startActivity(intent);
//			break;
		default:
			Utils.showToast("This swarm action is not yet implemented", Toast.LENGTH_LONG);
			break;
		}
	}
	
	// Rich Text Formatting ----------------------------------------------------
	
	private void writeChangeLog() {
		String strVersion;
		try {
			strVersion = "Version " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			strVersion = "?";
		}
		String[] rgstrChangelog = getResources().getStringArray(R.array.changelog);
		
		// write title together with the version and increase the text size slightly
		SpannableString title = new SpannableString("Changelog " + strVersion + "\n\n");
		title.setSpan(new RelativeSizeSpan(1.3f), 0, title.length(), 0);
		CharSequence text = title;
	
		// assemble the rest of the changelog
		text = RTFUtils.recursive(text, rgstrChangelog);

		// write everything into the textview
        TextView changelog = (TextView) findViewById(R.id.lblChangeLog);
        changelog.setText(text, BufferType.SPANNABLE);
	}

}
