package org.dobots.swarmcontrol;

import java.util.Arrays;

import org.dobots.swarmcontrol.SwarmControlTypes.SwarmAction;
import org.dobots.swarmcontrol.behaviours.dancing.DancingMain;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.RobotViewFactory;
import org.dobots.utility.ImprovedArrayAdapter;
import org.dobots.utility.Utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class SwarmControlActivity extends Activity {
	
	private static final String TAG = "MAIN_ACTIVITY";
	
	// The different menu options
	private static final int CONNECT_ID = Menu.FIRST;
	private static final int DISCONNECT_ID = Menu.FIRST + 1;
	private static final int ABOUT_ID = Menu.FIRST + 2;
	private static final int EXIT_ID = Menu.FIRST + 3;

	private static Context CONTEXT;

	private ArrayAdapter<RobotType> m_oRobotAdapter;
	private ArrayAdapter<SwarmAction> m_oSwarmActionAdapter;
	
	private Button m_btnRobots;
	private Button m_btnSwarmActions;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        CONTEXT = this;
        
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
				RobotType.values()) {

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
        
//        showRobot(RobotType.RBT_ROBOSCOOPER);
//        showBehaviour(SwarmAction.sa_Dance);
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
			showRobotSelectionDialog();
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

	public static Context getContext() {
		return CONTEXT;
	}
	
	private void showSwarmActionSelectionDialog() {
		AlertDialog dialog = CreateAdapterDialog("Choose a swarm action", m_oSwarmActionAdapter,
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
		AlertDialog dialog = CreateAdapterDialog("Choose a robot", m_oRobotAdapter, 
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
	
	private AlertDialog CreateAdapterDialog(String i_strTitle, ArrayAdapter i_oAdapter, DialogInterface.OnClickListener i_OnClickListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(i_strTitle);
		builder.setAdapter(i_oAdapter, i_OnClickListener);
		return builder.create();
	}

	public void showRobot(RobotType i_eType) {
		Intent intent = new Intent(SwarmControlActivity.this, RobotViewFactory.getRobotViewClass(i_eType));
		intent.putExtra("RobotType", i_eType);
		intent.putExtra("InventoryIndex", -1);
		startActivity(intent);
	}

	public void showRobot(RobotType i_eType, int i_nIndex) {
		Intent intent = new Intent(SwarmControlActivity.this, RobotViewFactory.getRobotViewClass(i_eType));
		intent.putExtra("RobotType", i_eType);
		intent.putExtra("InventoryIndex", i_nIndex);
		startActivity(intent);
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
	
	@Override
	public void onDestroy() {
		super.onDestroy();
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
		text = recursive(text, rgstrChangelog);

		// write everything into the textview
        TextView changelog = (TextView) findViewById(R.id.lblChangeLog);
        changelog.setText(text, BufferType.SPANNABLE);
	}
	
	private CharSequence recursive(CharSequence text, String[] list) {
		// iterate recursively until the list is empty
		if (list.length == 0) {
			return text;
		} else {
			// add the next item from the list to the already assembled text
			text = assemble(text, list[0]);
			// continue iterating
			return recursive(text, Arrays.copyOfRange(list, 1, list.length));
		}
	}
	
	private CharSequence assemble(CharSequence s1, CharSequence s2) {
		SpannableString ss1 = new SpannableString(s1);
		SpannableString ss2 = checkForBullet(s2);
		return TextUtils.concat(ss1, ss2);
	}
	
	private SpannableString checkForBullet(CharSequence s) {
		SpannableString ss;
		// if the first element of the string is a '-' it means
		// it should be displayed as a bullet. we then remove the
		// '-' from the list and trim the spaces
		s = ((String)s).trim();
		if (s.length() > 0 && s.charAt(0) == '-') {
			s = ((String)s.subSequence(1, s.length())).trim();
			ss = new SpannableString(s + "\n");
			ss.setSpan(new BulletSpan(15), 0, s.length(), 0);
		} else {
			ss = new SpannableString(s + "\n");
		}
		return ss;
	}

}
