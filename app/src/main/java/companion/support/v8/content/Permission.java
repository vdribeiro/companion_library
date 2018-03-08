package companion.support.v8.content;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Permissions utility class.
 *
 * @author Vitor Ribeiro
 */
public class Permission {

    // Permission codes
    public static final int LOCATION_PERMISSION = 10;
    public static final int STORAGE_PERMISSION = 20;

    /** Hidden constructor to prevent instantiation. */
    private Permission() {
    }

    /**
     * Get location permission strings.
     * @return string array.
     */
    private static String[] getLocationPermissions() {
        return new String[] {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION };
    }

    /**
     * Get storage permission strings.
     * @return string array.
     */
    private static String[] getStoragePermissions() {
        return new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
    }

    /**
     * Check if all permissions were granted.
     * @param context of the caller.
     * @param permissions permission strings.
     * @return true if all permissions were granted, false if even one was not.
     */
    private static boolean checkPermissionsList(Activity context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Request permissions given a list.
     * @param context of the caller.
     * @param permissions list of permission strings.
     * @param requestCode code for activity result callback.
     * @return true if all permissions were granted, false if even one was not.
     */
    private static boolean requestPermissionsList(Activity context, String[] permissions, int requestCode) {
        if (checkPermissionsList(context, permissions)) {
            return true;
        }

        ActivityCompat.requestPermissions(context, permissions, requestCode);
        return false;
    }

    /**
     * Request location permissions.
     * @param context of the caller.
     * @return true if permissions were granted, false otherwise.
     */
    public static boolean requestLocationPermission(Activity context) {
        return requestPermissionsList(context, getLocationPermissions(), LOCATION_PERMISSION);
    }

    /**
     * Request storage permissions.
     * @param context of the caller.
     * @return true if permissions were granted, false otherwise.
     */
    public static boolean requestStoragePermission(Activity context) {
        return requestPermissionsList(context, getStoragePermissions(), STORAGE_PERMISSION);
    }

    /**
     * Check permission results.
     * @param grantResults the grant results for the corresponding permissions.
     * @return true if permission is granted, false otherwise.
     */
    public static boolean isPermissionGranted(int[] grantResults) {
        return grantResults != null && grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }
}
