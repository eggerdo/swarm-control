package org.dobots.swarmcontrol;

import org.dobots.swarmcontrol.behaviours.IActivityResultListener;

import android.app.Activity;
import android.content.Intent;

public class BaseActivity extends Activity {

	private IActivityResultListener m_oListener;
	
	public void startActivityForResult(Intent intent, int requestCode, IActivityResultListener listener) {
		m_oListener = listener;
		super.startActivityForResult(intent, requestCode);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		
		if (m_oListener != null) {
			m_oListener.onActivityResult(requestCode, resultCode, data);
		}
	}
	
}
