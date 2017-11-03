package companion.support.v8.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

/**
 * The default {@link ViewPager} always matches the parent in width/height. 
 * It does not support wrap content which can cause problems when included in a scroll view for example. 
 * This implementation fixes this.
 */
public class ContainedViewPager extends SmartViewPager {
	
	public ContainedViewPager(Context context) {
		this(context, null);
	}

	public ContainedViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
	
	    int height = 0;
	    for(int i = 0; i < getChildCount(); i++) {
	        View child = getChildAt(i);
	        child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
	        int h = child.getMeasuredHeight();
	        if(h > height) height = h;
	    }
	
	    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
	
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
