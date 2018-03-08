package companion.support.v8.widget;

import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

/**
 * This is a helper class that implements a generic mechanism for
 * associating fragments with the tabs in a tab host.
 * 
 * @author Vitor Ribeiro
 *
 */
public class TabManager implements TabHost.OnTabChangeListener {
	private final FragmentActivity mActivity;
	private final FragmentManager mManager;
	private final TabHost mTabHost;
	private final int mContainerId;
	private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
	private TabInfo mLastTab;
	private OnTabChangeListener mListener;

	public static final class TabInfo {
		private final String tag;
		private final Class<?> clss;
		private final Bundle args;
		private Fragment fragment;

		TabInfo(String _tag, Class<?> _class, Bundle _args) {
			tag = _tag;
			clss = _class;
			args = _args;
		}
	}

	public static class TabFactory implements TabHost.TabContentFactory {
		private final Context mContext;

		public TabFactory(Context context) {
			super();
			mContext = context;
		}

		public View createTabContent(String tag) {
			View v = new View(mContext);
			v.setMinimumWidth(0);
			v.setMinimumHeight(0);
			return v;
		}
	}
	
	public TabManager(FragmentActivity activity, TabHost tabHost, int containerId) {
		this(activity, activity.getSupportFragmentManager(), tabHost, containerId);
	}
	
	public TabManager(FragmentActivity activity, FragmentManager manager, TabHost tabHost, int containerId) {
		mActivity = activity;
		mManager = manager;
		mTabHost = tabHost;
		mContainerId = containerId;
		
		mTabHost.setOnTabChangedListener(this);
	}

	public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
		tabSpec.setContent(new TabFactory(mActivity));
		String tag = tabSpec.getTag();

		TabInfo info = new TabInfo(tag, clss, args);

		// Check to see if we already have a fragment for this tab, probably
		// from a previously saved state.  If so, deactivate it, because our
		// initial state is that a tab isn't shown.
		info.fragment = mManager.findFragmentByTag(tag);
		if (info.fragment != null && !info.fragment.isDetached()) {
			FragmentTransaction ft = mManager.beginTransaction();
			ft.detach(info.fragment);
			ft.commitAllowingStateLoss();
		}

		mTabs.put(tag, info);
		mTabHost.addTab(tabSpec);
	}

	public void onTabChanged(String tabId) {
		TabInfo newTab = mTabs.get(tabId);
		if (mLastTab != newTab) {
			FragmentTransaction ft = mManager.beginTransaction();
			if (mLastTab != null) {
				if (mLastTab.fragment != null) {
					ft.hide(mLastTab.fragment);
				}
			}
			if (newTab != null) {
				if (newTab.fragment == null) {
					newTab.fragment = Fragment.instantiate(mActivity,
							newTab.clss.getName(), newTab.args);
					ft.add(mContainerId, newTab.fragment, newTab.tag);
				} else {
					ft.show(newTab.fragment);
				}
			}

			mLastTab = newTab;
			ft.commitAllowingStateLoss();
			mManager.executePendingTransactions();
		}

		if (mListener != null) mListener.onTabChanged(tabId);
	}

	public Fragment getFragmentFromTab(String tabId) {
		TabInfo info = mTabs.get(tabId);
		if (info!=null) {
			return info.fragment;
		}
		return mManager.findFragmentByTag(tabId);
	}

	public void setOnTabChangedListener(OnTabChangeListener listener) {
		this.mListener = listener;
	}
}