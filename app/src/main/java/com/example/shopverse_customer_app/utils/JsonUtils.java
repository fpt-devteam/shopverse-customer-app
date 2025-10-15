package com.example.shopverse_customer_app.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility class for reading JSON files from the assets folder.
 */
public class JsonUtils {
    private static final String TAG = "JsonUtils";

    /**
     * Reads a JSON file from the assets folder and returns it as a String.
     *
     * @param context Application context
     * @param fileName Name of the file in the assets folder (e.g., "stores.json")
     * @return JSON content as String, or null if an error occurs
     */
    public static String readJsonFromAssets(Context context, String fileName) {
        StringBuilder jsonBuilder = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            // Open the file from assets
            inputStream = context.getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // Read line by line
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            return jsonBuilder.toString();

        } catch (IOException e) {
            Log.e(TAG, "Error reading JSON from assets: " + fileName, e);
            return null;
        } finally {
            // Close resources
            try {
                if (reader != null) {
                    reader.close();
                }
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error closing stream", e);
            }
        }
    }
}
