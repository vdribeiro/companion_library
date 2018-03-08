package companion.support.v8.app;

import android.app.Activity;
import android.view.View;

import companion.support.v8.os.Utils;

/**
 * A utility class that helps with showing and hiding system UI such as the
 * status bar and navigation/system bar. This class uses backward-compatibility
 * techniques described in <a href=
 * "http://developer.android.com/training/backward-compatible-ui/index.html">
 * Creating Backward-Compatible UIs</a> to ensure that devices running any
 * version of ndroid OS are supported. More specifically, there are separate
 * implementations of this abstract class: for newer devices,
 * {@link #getInstance} will return a {@link SystemUiHiderHoneycomb} instance,
 * while on older devices {@link #getInstance} will return a
 * {@link SystemUiHiderBase} instance.
 * <p>
 * For more on system bars, see <a href=
 * "http://developer.android.com/design/get-started/ui-overview.html#system-bars"
 * > System Bars</a>.
 * <p>
 * Example code:
 * <pre>
 *	// Set up an instance of SystemUiHider to control the system UI for this activity.
 * 	mSystemUiHider = SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
 * 	mSystemUiHider.setup();
 * 	mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
 * 		// Cached values.
 * 		int mControlsHeight;
 * 		int mShortAnimTime;
 * 
 * 		public void onVisibilityChange(boolean visible) {
 * 			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
 * 				// If the ViewPropertyAnimator API is available
 * 				// (Honeycomb MR2 and later), use it to animate the
 * 				// in-layout UI controls at the bottom of the
 * 				// screen.
 * 				if (mControlsHeight == 0) {
 * 					mControlsHeight = controlsView.getHeight();
 * 				}
 * 				if (mShortAnimTime == 0) {
 * 					mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
 * 				}
 * 				controlsView
 * 					.animate()
 * 					.translationY(visible ? 0 : mControlsHeight)
 * 					.setDuration(mShortAnimTime);
 * 			} else {
 * 				// If the ViewPropertyAnimator APIs aren't
 * 				// available, simply show or hide the in-layout UI
 * 				// controls.
 * 				controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
 * 			}
 *
 * 			if (visible && AUTO_HIDE) {
 * 				// Schedule a hide().
 * 				delayedHide(AUTO_HIDE_DELAY_MILLIS);
 * 			}
 * 		}
 * 	});
 *
 * 	// Set up the user interaction to manually show or hide the system UI.
 * 	contentView.setOnClickListener(new View.OnClickListener() {
 * 		public void onClick(View view) {
 * 			if (TOGGLE_ON_CLICK) {
 * 				mSystemUiHider.toggle();
 * 			} else {
 * 				mSystemUiHider.show();
 * 			}
 * 		}
 * 	});
 *
 * 	// Upon interacting with UI controls, delay any scheduled hide()
 * 	// operations to prevent the jarring behavior of controls going away
 * 	// while interacting with the UI.
 * 	findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
 * 
 * </pre>
 * 
 * @author Vitor Ribeiro
 *    
 * @see android.view.View#setSystemUiVisibility(int)
 * @see android.view.WindowManager.LayoutParams#FLAG_FULLSCREEN
 *
 */
public abstract class SystemUiHider {
	/**
	 * When this flag is set, the
	 * {@link android.view.WindowManager.LayoutParams#FLAG_LAYOUT_IN_SCREEN}
	 * flag will be set on older devices, making the status bar "float" on top
	 * of the activity layout. This is most useful when there are no controls at
	 * the top of the activity layout.
	 * <p>
	 * This flag isn't used on newer devices because the <a
	 * href="http://developer.android.com/design/patterns/actionbar.html">action
	 * bar</a>, the most important structural element of an Android app, should
	 * be visible and not obscured by the system UI.
	 */
	public static final int FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES = 0x1;

	/**
	 * When this flag is set, {@link #show()} and {@link #hide()} will toggle
	 * the visibility of the status bar. If there is a navigation bar, show and
	 * hide will toggle low profile mode.
	 */
	public static final int FLAG_FULLSCREEN = 0x2;

	/**
	 * When this flag is set, {@link #show()} and {@link #hide()} will toggle
	 * the visibility of the navigation bar, if it's present on the device and
	 * the device allows hiding it. In cases where the navigation bar is present
	 * but cannot be hidden, show and hide will toggle low profile mode.
	 */
	public static final int FLAG_HIDE_NAVIGATION = FLAG_FULLSCREEN | 0x4;

	/**
	 * The activity associated with this UI hider object.
	 */
	protected Activity mActivity;

	/**
	 * The view on which {@link View#setSystemUiVisibility(int)} will be called.
	 */
	protected View mAnchorView;

	/**
	 * The current UI hider flags.
	 * 
	 * @see #FLAG_FULLSCREEN
	 * @see #FLAG_HIDE_NAVIGATION
	 * @see #FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES
	 */
	protected int mFlags;

	/**
	 * The current visibility callback.
	 */
	protected OnVisibilityChangeListener mOnVisibilityChangeListener = simpleListener;

	/**
	 * Creates and returns an instance of {@link SystemUiHider} that is
	 * appropriate for this device. The object will be either a
	 * {@link SystemUiHiderBase} or {@link SystemUiHiderHoneycomb} depending on
	 * the device.
	 * 
	 * @param activity
	 *            The activity whose window's system UI should be controlled by
	 *            this class.
	 * @param anchorView
	 *            The view on which {@link View#setSystemUiVisibility(int)} will
	 *            be called.
	 * @param flags
	 *            Either 0 or any combination of {@link #FLAG_FULLSCREEN},
	 *            {@link #FLAG_HIDE_NAVIGATION}, and
	 *            {@link #FLAG_LAYOUT_IN_SCREEN_OLDER_DEVICES}.
	 */
	public static SystemUiHider getInstance(Activity activity, View anchorView, int flags) {
		if (Utils.hasHoneycomb()) {
			return new SystemUiHiderHoneycomb(activity, anchorView, flags);
		} else {
			return new SystemUiHiderBase(activity, anchorView, flags);
		}
	}

	protected SystemUiHider(Activity activity, View anchorView, int flags) {
		mActivity = activity;
		mAnchorView = anchorView;
		mFlags = flags;
	}

	/**
	 * Sets up the system UI hider. Should be called from
	 * {@link Activity#onCreate}.
	 */
	public abstract void setup();

	/**
	 * Returns whether or not the system UI is visible.
	 */
	public abstract boolean isVisible();

	/**
	 * Hide the system UI.
	 */
	public abstract void hide();

	/**
	 * Show the system UI.
	 */
	public abstract void show();

	/**
	 * Toggle the visibility of the system UI.
	 */
	public void toggle() {
		if (isVisible()) {
			hide();
		} else {
			show();
		}
	}

	/**
	 * Registers a callback, to be triggered when the system UI visibility
	 * changes.
	 */
	public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) {
		if (listener == null) {
			listener = simpleListener;
		}

		mOnVisibilityChangeListener = listener;
	}

	/**
	 * A simple no-op callback for use when there is no other listener set.
	 */
	private static OnVisibilityChangeListener simpleListener = new OnVisibilityChangeListener() {
		@Override
		public void onVisibilityChange(boolean visible) {
		}
	};

	/**
	 * A callback interface used to listen for system UI visibility changes.
	 */
	public interface OnVisibilityChangeListener {
		/**
		 * Called when the system UI visibility has changed.
		 * 
		 * @param visible
		 *            True if the system UI is visible.
		 */
		void onVisibilityChange(boolean visible);
	}
}
