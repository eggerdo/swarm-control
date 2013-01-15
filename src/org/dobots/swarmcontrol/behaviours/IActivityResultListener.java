package org.dobots.swarmcontrol.behaviours;

import android.content.Intent;

public interface IActivityResultListener {
	
	public void onActivityResult(int requestCode, int resultCode, Intent data);

}
