package org.dobots.utility;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ImprovedArrayAdapter<T> extends ArrayAdapter<T> {

	public ImprovedArrayAdapter(Context context, int textViewResourceId, T[] objects) {
		super(context, textViewResourceId, objects);
	}

	@Override
	public boolean areAllItemsEnabled() {
		for (int i = 0; i < getCount(); i++) {
			if (!isEnabled(i)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public View getDropDownView(int position, View convertView,
			ViewGroup parent) {
		// TODO Auto-generated method stub
		View v = super.getDropDownView(position, convertView, parent);
		TextView tv = (TextView)v.findViewById(android.R.id.text1);
		if (!isEnabled(position)) {
			tv.setTextColor(Color.GRAY);
		} else {
			tv.setTextColor(Color.BLACK);
		}
		return v;
	}
	
	@Override
	public View getView(int position, View convertView,
			ViewGroup parent) {
		// TODO Auto-generated method stub
		View v = super.getView(position, convertView, parent);
		TextView tv = (TextView)v.findViewById(android.R.id.text1);
		if (!isEnabled(position)) {
			tv.setTextColor(Color.GRAY);
		} else {
			tv.setTextColor(Color.BLACK);
		}
		return v;
	}
	
}
