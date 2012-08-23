package org.dobots.swarmcontrol.behaviours;

import android.content.Intent;

public interface ActivityResultListener {
	
	public void onActivityResult(int requestCode, int resultCode, Intent data);

}
