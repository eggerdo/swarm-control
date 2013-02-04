package org.dobots.swarmcontrol;

import java.util.Arrays;

import org.dobots.robots.spykee.SpykeeTypes;
import org.dobots.swarmcontrol.SwarmControlTypes.SwarmAction;
import org.dobots.swarmcontrol.behaviours.dancing.DancingMain;
import org.dobots.swarmcontrol.robots.RobotType;
import org.dobots.swarmcontrol.robots.RobotViewFactory;
import org.dobots.swarmcontrol.socialize.SocializeEntityHelper;
import org.dobots.swarmcontrol.socialize.SocializeHelper;
import org.dobots.swarmcontrol.socialize.SocializeHelper.IChangeEventListener;
import org.dobots.swarmcontrol.socialize.SocializeHelper.ILikeEventListener;
import org.dobots.utility.ImprovedArrayAdapter;
import org.dobots.utility.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import com.socialize.ActionBarUtils;
import com.socialize.CommentUtils;
import com.socialize.EntityUtils;
import com.socialize.LikeUtils;
import com.socialize.ShareUtils;
import com.socialize.Socialize;
import com.socialize.UserUtils;
import com.socialize.ViewUtils;
import com.socialize.android.ioc.IOCContainer;
import com.socialize.entity.Entity;
import com.socialize.entity.User;
import com.socialize.error.SocializeException;
import com.socialize.listener.SocializeInitListener;
import com.socialize.listener.entity.EntityAddListener;
import com.socialize.listener.entity.EntityGetListener;
import com.socialize.listener.view.ViewAddListener;
import com.socialize.ui.actionbar.ActionBarOptions;
import com.socialize.ui.actionbutton.LikeButtonListener;
import com.socialize.ui.comment.LinkifyCommentViewActionListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
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
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;

public class SwarmControlActivity extends BaseActivity {
	
	private static final String TAG = "MAIN_ACTIVITY";
	
	private static final int PREFERENCES_DLG = 1;
	
	// The different menu options
	private static final int ABOUT_ID = Menu.FIRST;
	private static final int EXIT_ID = ABOUT_ID + 1;
	private static final int SOCIALIZE_SETTINGS = EXIT_ID + 1; 
	private static final int PREFERENCES = SOCIALIZE_SETTINGS + 1;

	private static Context CONTEXT;

	private ArrayAdapter<RobotType> m_oRobotAdapter;
	private ArrayAdapter<SwarmAction> m_oSwarmActionAdapter;
	
	private Button m_btnRobots;
	private Button m_btnSwarmActions;
	private LinearLayout m_laySocializeActionBar;

	private boolean m_bSocializeConnected = false;
	private boolean m_bHideActionBar = false;
	private boolean m_bIsLiked = false;

	private Entity m_oEntity;
	
	// set to true if Socialize Entities need to be created
	private final boolean m_bInitSocializeEntities = false; 
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        CONTEXT = this;

        setContentView(R.layout.main);
        
        m_laySocializeActionBar = (LinearLayout) findViewById(R.id.laySocializeActionBar);
        
        loadPreferences();
        setupSocialize();
     		
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
        
//        showRobot(RobotType.RBT_ROOMBA);
//        showBehaviour(SwarmAction.sa_Dance);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(0, ABOUT_ID, ABOUT_ID, getResources().getString(R.string.about))
		    .setIcon(R.drawable.ic_menu_about);
		menu.add(0, EXIT_ID, EXIT_ID, "Exit");
		menu.add(1, SOCIALIZE_SETTINGS, SOCIALIZE_SETTINGS, "Socialize");
		menu.add(2, PREFERENCES, PREFERENCES, "Preferences");
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
		case SOCIALIZE_SETTINGS:
			showSocializeSettings();
			return true;
		case PREFERENCES:
			showDialog(PREFERENCES_DLG);
			break;
		}

		return super.onMenuItemSelected(featureId, item);
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	menu.setGroupVisible(1, m_bSocializeConnected);
    	menu.setGroupVisible(2, m_bIsLiked); // so long as only the show/hide action bar is in the preferences we only show the preferences if we already got the like
    	
    	return true;
    }
    	
	public static Context getContext() {
		return CONTEXT;
	}
	
	private void showSocializeSettings() {
		UserUtils.showUserSettings(this);
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
	protected void onPause() {
		super.onPause();
		
		Socialize.onPause(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		Socialize.onResume(this);
	}
	
	@Override
	public void onDestroy() {
		Socialize.onDestroy(this);
		
		super.onDestroy();
	}
	
	private void loadPreferences() {
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		m_bHideActionBar = prefs.getBoolean(SwarmControlTypes.HIDE_ACTION_BAR, SwarmControlTypes.HIDE_ACTION_BAR_DEFAULT);
	}

    /**
     * This is called when a dialog is created for the first time.  The given
     * "id" is the same value that is passed to showDialog().
     */
    @Override
    protected Dialog onCreateDialog(int id) {
    	LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		switch (id) {
    	case PREFERENCES_DLG:
        	builder.setTitle("Preferences")
        	       .setView(inflater.inflate(R.layout.preferences, null))
        	       .setPositiveButton("Save", new DialogInterface.OnClickListener() {
        		
        		@Override
    			public void onClick(DialogInterface dialog, int which) {
    				adjustPreferences((AlertDialog)dialog);
    			}
    		});
        	return builder.create();
    	}
    	return null;
    }

    /**
     * This is called each time a dialog is shown.
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
    	if (id == PREFERENCES_DLG) {
    		// Pre-fill the text fields with the saved login settings.
    		CheckBox checkBox;
    		
    		checkBox = (CheckBox) dialog.findViewById(R.id.cbxHideActionBar);
    		checkBox.setChecked(m_bHideActionBar);
    	}
    }
    
    private void adjustPreferences(Dialog dialog) {
		CheckBox cbxHideActionBar = (CheckBox) dialog.findViewById(R.id.cbxHideActionBar);
		
		m_bHideActionBar = cbxHideActionBar.isChecked();
		hideSocializeActionBar(m_bHideActionBar);
		
		SharedPreferences prefs = getPreferences(MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(SwarmControlTypes.HIDE_ACTION_BAR, cbxHideActionBar.isChecked());
		editor.commit();
    	
    }
    
	// Socialize ---------------------------------------------------------------
    
	private void setupSocialize() {

    	// Initialize socialize
    	Socialize.initAsync(SwarmControlActivity.this, new SocializeInitListener() {
    		
    		@Override
    		public void onError(SocializeException error) {
    			m_bSocializeConnected = false;
    		}
    		
    		@Override
    		public void onInit(Context context, IOCContainer container) {
    			m_bSocializeConnected = true;
    		}
    	});
    	
    	// set up socialize elements asynchronously so as not to delay the ui thread loading the activity
    	Utils.runAsyncTask(new Runnable() {

			@Override
			public void run() {

				// ui elements need to be updated by the ui thread
	    		Utils.runAsyncUiTask(new Runnable() {
					
					@Override
					public void run() {
						enableSocializeActionBar(false);
					}
				});

		    	// only set to true if entities have to be updated / created
		    	if (m_bInitSocializeEntities) {
		    		SocializeEntityHelper.initAllEntities(SwarmControlActivity.this);
		    	}
		    	
				m_oEntity = SocializeEntityHelper.getMainEntity(SwarmControlActivity.this);
				if (m_oEntity != null) {
			    	SocializeHelper.setupActionBar(SwarmControlActivity.this, m_oEntity, new ILikeEventListener() {
	
						@Override
						public void onLike() {
							m_bIsLiked = true;
						}
	
						@Override
						public void onUnlike() {
							m_bIsLiked = false;
						}
					});
	
		    		m_bIsLiked = m_oEntity.getUserEntityStats().isLiked();
	
					// ui elements need to be updated by the ui thread
		    		Utils.runAsyncUiTask(new Runnable() {
						
						@Override
						public void run() {
							hideSocializeActionBar(m_bHideActionBar && m_bIsLiked);
						}
					});
		    		
		    		// increase view count
		    		SocializeHelper.registerMainView(SwarmControlActivity.this);
				}
			}
		});
	}

    private void enableSocializeActionBar(Boolean i_bEnable) {
    	for (int i = 0; i < m_laySocializeActionBar.getChildCount(); i++) {
    		View v = m_laySocializeActionBar.getChildAt(i);
    		v.setEnabled(i_bEnable);
    	}
    }
    
    private void hideSocializeActionBar(Boolean i_bHide) {
		if (i_bHide) {
			m_laySocializeActionBar.setVisibility(View.GONE);
    	} else {
    		m_laySocializeActionBar.setVisibility(View.VISIBLE);
        	enableSocializeActionBar(true);
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
