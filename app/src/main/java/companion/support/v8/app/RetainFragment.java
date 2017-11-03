package companion.support.v8.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * A simple non-UI Fragment that stores a single Object 
 * and is retained over configuration changes.
 * 
 * @author Vitor Ribeiro
 *
 */
public class RetainFragment extends Fragment {
	private Object mObject;

	/**
	 * Factory method to create a new instance of this fragment.
	 * 
	 * @return a new instance of this fragment.
	 */
	public static RetainFragment getInstance() {
		RetainFragment fragment = new RetainFragment();
		return fragment;
	}

	/**
	 * Empty constructor as per the Fragment documentation.
	 */
	public RetainFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Make sure this Fragment is retained over a configuration change
		setRetainInstance(true);
	}

	/**
	 * Store a single object in this Fragment.
	 *
	 * @param object to store.
	 */
	public void setObject(Object object) {
		mObject = object;
	}

	/**
	 * Get the stored object.
	 *
	 * @return the stored object
	 */
	public Object getObject() {
		return mObject;
	}

	/**
	 * Locate an existing instance of this Fragment or if not found, create and
	 * add it using FragmentManager.
	 *
	 * @param fm the FragmentManager manager to use.
	 * @param fragmentName tag name for the fragment.
	 * @return the existing instance of the Fragment or the new instance if just created.
	 */
	public static RetainFragment findOrCreateRetainFragment(FragmentManager fm, String fragmentName) {
		// Check to see if we have retained the worker fragment.
		RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(fragmentName);

		// If not retained (or first time running), we need to create and add it.
		if (mRetainFragment == null) {
			mRetainFragment = new RetainFragment();
			fm.beginTransaction().add(mRetainFragment, fragmentName).commitAllowingStateLoss();
		}

		return mRetainFragment;
	}
	
	/**
	 * Locate an existing instance of this Fragment and remove it.
	 *
	 * @param fm the FragmentManager manager to use.
	 * @param fragmentName tag name for the fragment.
	 */
	public static void removeRetainFragment(FragmentManager fm, String fragmentName) {
		// Check to see if we have retained the worker fragment.
		RetainFragment mRetainFragment = (RetainFragment) fm.findFragmentByTag(fragmentName);

		// If retained, we delete it.
		if (mRetainFragment != null) {
			mRetainFragment.setObject(null);
			fm.beginTransaction().remove(mRetainFragment).commitAllowingStateLoss();
		}
	}
}
