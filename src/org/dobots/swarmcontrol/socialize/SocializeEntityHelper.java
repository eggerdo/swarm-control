//package org.dobots.swarmcontrol.socialize;
//
//import java.util.HashMap;
//
//import org.dobots.swarmcontrol.SwarmControlTypes.SwarmAction;
//import org.dobots.utilities.BaseActivity;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import robots.RobotType;
//import android.util.Log;

//import com.socialize.EntityUtils;
//import com.socialize.entity.Entity;
//import com.socialize.error.SocializeException;
//import com.socialize.listener.entity.EntityAddListener;
//import com.socialize.listener.entity.EntityGetListener;
//
//public class SocializeEntityHelper {
//	
//	private static final String TAG = "SocializeEntityHelper";
//	
//	private static Object m_oMutex = new Object();
//
//	// --------------------------------------------------------------------------------------
//	public static void initAllEntities(BaseActivity i_oContext) {
//		Entity oEntity;
//		
//		if ((oEntity = getMainEntity(i_oContext)) == null) {
//			createMainEntity(i_oContext);
//		} else {
//			updateMainEntity(i_oContext, oEntity);
//		}
//		
////		for (RobotType eRobot : RobotType.values()) {
////			if (eRobot.isEnabled()) {
////				if ((oEntity = getRobotEntity(i_oContext, eRobot)) == null) {
////					createRobotEntity(i_oContext, eRobot);
////				} else {
//////					updateRobotEntity(i_oContext, eRobot);
////				}
////			}
////		}
////		
////		for (SwarmAction eAction : SwarmAction.values()) {
////			if (eAction.isEnabled()) {
////				createSwarmActionEntity(i_oContext, eAction);
////			}
////		}
//		
//	}
//
//	// --------------------------------------------------------------------------------------
//	private static Entity createEntity(BaseActivity i_oContext, final String i_strKey, 
//			String i_strTitle, String i_strDescription, String i_strThumb) {
//		Entity oEntity = Entity.newInstance(i_strKey, i_strTitle);
//
//		// Store a custom dictionary as a JSON object
//		JSONObject metaData = new JSONObject();
//		
//		try {
//			metaData.put("szsd_title", i_strTitle);
//			metaData.put("szsd_description", i_strDescription);
//
//			if (i_strThumb != null) {
//				metaData.put("szsd_thumb", i_strThumb);
//			}
//				
//			oEntity.setMetaData(metaData.toString());
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		// The "this" argument refers to the current Activity
//		EntityUtils.saveEntity(i_oContext, oEntity, new EntityAddListener() {
//			
//			@Override
//			public void onError(SocializeException error) {
//				Log.e(TAG, String.format("entity '%s' error", i_strKey));
//			}
//			
//			@Override
//			public void onCreate(Entity result) {
//				getHistory().put(result.getKey(), result);
//				Log.d(TAG, String.format("entity '%s' created", i_strKey));
//			}
//		});
//		
//		Log.d(TAG, String.format("createEntry %s", metaData.toString()));
//		
//		return oEntity;
//	}
//	
//	// MAIN ACTIVITY =========================================================================
//
//	// --------------------------------------------------------------------------------------
//	public static Entity createMainEntity(BaseActivity i_oContext) {
//
//		String strKey = "swarm_control_main";
//		String strTitle = "Swarm Control";
//		String strDescription = "The Swarm Control app lets you connect to your robots, display the robots' sensor data " +
//			"and control them remotely with your phone. Moreover, the app can connect to multiple robots at the same " +
//			"time to group them together and let them execute swarm actions. You can choose from a variety of robots such as " +
//			"the iRobot Roomba, Lego Mindstorms NXT, Wowwee RoboScooper, Parrot AR Drone, Brookstone Rover AC13, " +
//			"Meccano Spykee and our home made robot Dotty. The app is still under heavy developement so new robots and " +
//			"swarm actions are continuously added. Check our website http://www.dobots.nl for up-to-date " +
//			"information on the app and let us know what you think.";
//		
//		return createEntity(i_oContext, strKey, strTitle, strDescription, null);
//
//	}
//	
//	public static void updateMainEntity(BaseActivity i_oContext, Entity i_oEntity) {
//		String strMetaData = i_oEntity.getMetaData();
//		final String strKey = i_oEntity.getKey();
//		try {
//			JSONObject oMetaData = new JSONObject(strMetaData);
//			oMetaData.put("szsd_title", "Swarm");
//			oMetaData.put("szsd_description", "Swarm Update Test");
//			oMetaData.put("szsd_continue_link", "http://www.dobots.nl/blog/-/blogs/swarm-control-update");
//			i_oEntity.setMetaData(oMetaData.toString());
//
//			// The "this" argument refers to the current Activity
//			EntityUtils.saveEntity(i_oContext, i_oEntity, new EntityAddListener() {
//				
//				@Override
//				public void onError(SocializeException error) {
//					Log.e(TAG, String.format("entity update '%s' error", strKey));
//				}
//				
//				@Override
//				public void onCreate(Entity result) {
//					Log.d(TAG, String.format("entity update '%s' created", strKey));
//				}
//				
//			});
//		} catch (JSONException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	// --------------------------------------------------------------------------------------
//	public static Entity getMainEntity(BaseActivity i_oContext) {
//
//		synchronized(m_oMutex) {
//			Entity oEntity;
//			String strKey = "swarm_control_main";
//	
//			if ((oEntity = getHistory().get(strKey)) == null) {
//				EntityHelper oHelper = new SocializeEntityHelper().new EntityHelper();
//				oEntity = oHelper.getEntity(i_oContext, strKey);
//				getHistory().put(strKey, oEntity);
//			}
//
//			return oEntity;
//		}
//	}
//	
//	// ROBOT ================================================================================
//
//	private static String getKey(RobotType i_eRobot) {
//		return i_eRobot.name();
//	}
//	
//	// --------------------------------------------------------------------------------------
//	public static Entity createRobotEntity(BaseActivity i_oContext, RobotType i_eRobot) {
//		String strKey = getKey(i_eRobot);
//		String strTitle = i_eRobot.toString();
//		String strDescription = getDescription(i_eRobot);
//		
//		return createEntity(i_oContext, strKey, strTitle, strDescription, "");
//	}
//
//	// --------------------------------------------------------------------------------------
//	private static String getDescription(RobotType i_eType) {
//		// TODO Auto-generated method stub
//		switch (i_eType) {
//		case RBT_AC13ROVER:
//			return "Control the Brookston AC13 Rover remotely and display the video " +
//					"on the smartphone.";
//		case RBT_DOTTY:
//			return "Control the Dotty remotely and let it stream it's sensor data to the " +
//					"smartphone.";
//		case RBT_NXT:
//			return "Control a Mindstorms NXT robot remotely and choose what sensors are " +
//					"connected to display their data.";
//		case RBT_PARROT:
//			return "Control the Parrot AR Drone and AR Drone 2.0 remotely and display " +
//					"video and other sensor data on the smartphone.";
//		case RBT_ROBOSCOOPER:
//			return "If your Wowwee RoboScooper is equipped with a Brainlink you can control " +
//					"it remotely with your smartphone."; 
//		case RBT_ROOMBA:
//			return "Equip your iRobot Roomba with a Bluetooth module and use this app to " +
//					"control it remotely and to display the available.";
//		case RBT_SPYKEE:
//			return "If your Meccano Spykee is connected to a WiFi network you can use this " +
//					"app to drive it around, play the prerecorded sound files and display the video " +
//					"on the smartphone.";
//		default:
//			return "";
//		}
//	}
//
//	// --------------------------------------------------------------------------------------
//	public static Entity getRobotEntity(BaseActivity i_oContext, RobotType i_eRobot) {
//		
//		synchronized(m_oMutex) {
//			Entity oEntity;
//			String strKey = getKey(i_eRobot);
//			
//			if ((oEntity = getHistory().get(strKey)) == null) {
//				EntityHelper oHelper = new SocializeEntityHelper().new EntityHelper();
//				oEntity = oHelper.getEntity(i_oContext, strKey);
//				getHistory().put(strKey, oEntity);
//			}
//
//			return oEntity;
//		}
//	}
//	
//	// Swarm Action =====================================================================
//	
//	private static void createSwarmActionEntity(BaseActivity i_oContext,
//			SwarmAction eAction) {
//		// TODO Auto-generated method stub
//		
//	}
//	
//	// Entity Helper ====================================================================
//
//	// --------------------------------------------------------------------------------------
//	private static HashMap<String, Entity> HISTORY;
//	public static HashMap<String, Entity> getHistory() {
//		if (HISTORY == null) {
//			HISTORY = new HashMap<String, Entity>();
//		}
//		return HISTORY;
//	}
//
//	private class EntityHelper {
//		Entity m_oEntity;
//
//		// --------------------------------------------------------------------------------------
//		public Entity getEntity(final BaseActivity i_oContext, final String i_strKey) {
//			
//			final Object lock = new Object();
//			EntityUtils.getEntity(i_oContext, i_strKey, new EntityGetListener() {
//				
//				@Override
//				public void onGet(Entity result) {
//					Log.d(TAG, String.format("entity '%s' found", i_strKey));
//					m_oEntity = result;
//					synchronized(lock) {
//						lock.notify();
//					}
//				}
//				
//				@Override
//				public void onError(SocializeException error) {
//					Log.e(TAG, String.format("entity '%s' not found", i_strKey));
//					error.printStackTrace();
//					synchronized(lock) {
//						lock.notify();
//					}
//				}
//			});
//			
//			synchronized(lock) {
//				try {
//					lock.wait(20000);
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			return m_oEntity;
//
//		}
//		
//	}
//	
//}
