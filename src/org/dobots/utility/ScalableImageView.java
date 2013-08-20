package org.dobots.utility;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ScalableImageView extends ImageView {
	public boolean isScaled = false;
	private int m_nMaxWidth = 0;

    public ScalableImageView(Context context) {
        super(context);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScalableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public void setScale(boolean i_bScale) {
    	isScaled = i_bScale;
    	requestLayout();
    }
    
    public void setMaxWidth(int i_nMaxWidth) {
    	m_nMaxWidth = i_nMaxWidth;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	
    	if (isScaled) {
	        try
	        {
	            Drawable drawable = getDrawable();
	
	            if (drawable == null)
	            {
	                setMeasuredDimension(0, 0);
	            }
	            else
	            {
	            	int width;
	            	if (m_nMaxWidth == 0) {
	            		width = MeasureSpec.getSize(widthMeasureSpec);
	            	} else {
	            		width = m_nMaxWidth;
	            	}
	                int height = width * drawable.getIntrinsicHeight() / drawable.getIntrinsicWidth();
	                setMeasuredDimension(width, height);
	            }
	        }
	        catch (Exception e)
	        {
	            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	        }
    	} else {
    		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    	}
    }
}
