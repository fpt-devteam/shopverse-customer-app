package com.example.shopverse_customer_app.location;

/**
 * Utility class for calculating distances between geographic coordinates.
 */
public class DistanceUtils {

    /**
     * Calculates the distance between two points using the Haversine formula.
     * This gives great-circle distances between two points on a sphere from their longitudes and latitudes.
     *
     * @param lat1 Latitude of first point in degrees
     * @param lon1 Longitude of first point in degrees
     * @param lat2 Latitude of second point in degrees
     * @param lon2 Longitude of second point in degrees
     * @return Distance in kilometers
     */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0; // Earth's radius in kilometers

        // Convert latitude and longitude from degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distance in kilometers
        return R * c;
    }

    /**
     * Formats a distance value for display.
     *
     * @param distanceKm Distance in kilometers
     * @return Formatted string (e.g., "1.5 km" or "500 m")
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            // Show in meters if less than 1 km
            int meters = (int) (distanceKm * 1000);
            return meters + " m";
        } else {
            // Show in kilometers with 1 decimal place
            return String.format("%.1f km", distanceKm);
        }
    }
}
