/**
 *   Copyright 2012 Anne van Rossum
**/

package org.dobots.swarmcontrol;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.app.Activity;
import android.app.Dialog;
import android.widget.Button;

class About {

    private Dialog dialog;
    
	public void show(Activity myActivity) {
		dialog = new Dialog(myActivity);
		dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.aboutbox);

    	Button buttonOK = (Button) dialog.findViewById(R.id.AboutOKbutton);
    	buttonOK.setOnClickListener(new OnClickListener() {
    		public void onClick(View v)
    		{
    			dialog.dismiss();
    		}
    	});
		dialog.show();
	}	
}
