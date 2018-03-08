package companion.support.v8.time;

/**
 * Class with support map utility methods.
 *
 * @author Vitor Ribeiro
 */
public class MapUtils {

    // Map URIs
    public static final String MAPS_ADDRESS = "http://maps.google.com/maps?saddr=%f,%f&daddr=%f,%f";
    public static final String LOCATION = "geo:%f,%f";

    /** Hidden constructor to prevent instantiation. */
    private MapUtils() {
    }

    // TODO: MapUtils
//    /**
//     * Get support map object.
//     * @return map reference.
//     */
//    public static SupportMapFragment getSupportMap() {
//        return SupportMapFragment.newInstance(new GoogleMapOptions()
//                .mapType(GoogleMap.MAP_TYPE_NORMAL)
//                .compassEnabled(true)
//                .mapToolbarEnabled(true)
//                .scrollGesturesEnabled(true)
//                .rotateGesturesEnabled(true)
//                .tiltGesturesEnabled(true)
//                .zoomControlsEnabled(true)
//                .zoomGesturesEnabled(true)
//        );
//    }
//
//    /**
//     * Create and move to marker on map.
//     * @param googleMap support map reference.
//     * @param name of the marker.
//     * @param latitude of the marker.
//     * @param longitude of the marker.
//     */
//    public static void moveToMarker(GoogleMap googleMap, String name, double latitude, double longitude) {
//        LatLng position = new LatLng(latitude, longitude);
//        MarkerOptions marker = new MarkerOptions().position(position).title(name);
//        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(14.0f).build();
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
//
//        googleMap.addMarker(marker);
//        googleMap.moveCamera(cameraUpdate);
//    }
}
