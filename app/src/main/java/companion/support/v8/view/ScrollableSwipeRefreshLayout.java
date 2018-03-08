package companion.support.v8.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.View;

/**
 * A Swipe layout that fixes scroll interference.
 *
 * @author Vitor Ribeiro
 */
@SuppressLint("NewApi")
public class ScrollableSwipeRefreshLayout extends SwipeRefreshLayout {

    private View scrollView;

    public ScrollableSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    public ScrollableSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.scrollView = findViewById(android.R.id.list);
    }

    @Override
    public boolean canChildScrollUp() {
        // Condition to check whether scrollview reached at the top while scrolling or not
        if (this.scrollView != null) {
            return this.scrollView.canScrollVertically(-1);
        }
        return super.canChildScrollUp();
    }

    public void setScrollView(View scrollView) {
        this.scrollView = scrollView;
    }
}
