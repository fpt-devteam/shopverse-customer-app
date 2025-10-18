package com.example.shopverse_customer_app.ui.maps;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Adapter for AutoCompleteTextView that provides place suggestions from Google Places API.
 */
public class PlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private static final String TAG = "PlacesAutocomplete";

    private final PlacesClient placesClient;
    private final List<AutocompletePrediction> predictions = new ArrayList<>();
    private final AutocompleteSessionToken token;

    public PlacesAutocompleteAdapter(Context context, PlacesClient placesClient) {
        super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        this.placesClient = placesClient;
        this.token = AutocompleteSessionToken.newInstance();
    }

    @Override
    public int getCount() {
        return predictions.size();
    }

    @Override
    public String getItem(int position) {
        if (position >= 0 && position < predictions.size()) {
            AutocompletePrediction prediction = predictions.get(position);
            return prediction.getFullText(null).toString();
        }
        return null;
    }

    /**
     * Gets the place ID for a prediction at the given position.
     */
    public String getPlaceId(int position) {
        if (position >= 0 && position < predictions.size()) {
            return predictions.get(position).getPlaceId();
        }
        return null;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            private static final long TIMEOUT_SEC = 5L;

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() < 3) {
                    results.values = new ArrayList<AutocompletePrediction>();
                    results.count = 0;
                    return results;
                }
                try {
                    RectangularBounds bounds = RectangularBounds.newInstance(
                            new LatLng(10.0, 105.0),
                            new LatLng(12.0, 108.0)
                    );
                    FindAutocompletePredictionsRequest request =
                            FindAutocompletePredictionsRequest.builder()
                                    .setSessionToken(token)
                                    .setQuery(constraint.toString())
                                    .setLocationBias(bounds)
                                    .setCountries("VN")
                                    .setTypeFilter(TypeFilter.REGIONS)
                                    .build();
                    var response = Tasks.await(
                            placesClient.findAutocompletePredictions(request),
                            TIMEOUT_SEC, TimeUnit.SECONDS
                    );
                    List<AutocompletePrediction> list = response.getAutocompletePredictions();
                    results.values = list != null ? list : new ArrayList<AutocompletePrediction>();
                    results.count = list != null ? list.size() : 0;
                } catch (Exception e) {
                    results.values = new ArrayList<AutocompletePrediction>();
                    results.count = 0;
                }
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                predictions.clear();
                if (results != null && results.values instanceof List && results.count > 0) {
                    predictions.addAll((List<AutocompletePrediction>) results.values);
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
