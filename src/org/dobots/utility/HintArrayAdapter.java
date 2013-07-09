package org.dobots.utility;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class HintArrayAdapter<T> extends ArrayAdapter<T> {

	private String m_strHint = "";
	
	public HintArrayAdapter(Context context, int textViewResourceId, T[] objects) {
		super(context, textViewResourceId, objects);
	}
	
	public void setHint(String strHint) {
		m_strHint = strHint;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View v = super.getView(position, convertView, parent);
		if (position == getCount()) {
			((TextView)v.findViewById(android.R.id.text1)).setText("");
            ((TextView)v.findViewById(android.R.id.text1)).setHint(m_strHint); //"Hint to be displayed"
		}
		
		return v;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return super.getCount()-1;
	}
	
}
