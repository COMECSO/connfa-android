package com.lemberg.connfa.ui.fragment;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.lemberg.connfa.R;
import com.lemberg.connfa.app.App;
import com.lemberg.connfa.model.Model;
import com.lemberg.connfa.model.UpdateRequest;
import com.lemberg.connfa.model.UpdatesManager;
import com.lemberg.connfa.model.data.Location;
import com.lemberg.connfa.model.managers.LocationManager;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

public class LocationFragment extends Fragment {

    public static final String TAG = "LocationsFragment";

    private static final int ZOOM_LEVEL = 15;
    private static final int TILT_LEVEL = 0;
    private static final int BEARING_LEVEL = 0;

    private View rootView;

    private MapView mapView;

    private MapboxMap mapboxMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(Objects.requireNonNull(this.getContext()), App.getContext().getString(R.string.api_mpabox));

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fr_location, container, false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mapView.onDestroy();
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rootView = view;

        mapView = rootView.findViewById(R.id.mapView);

        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap map) {
                map.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        mapboxMap = map;

                        new LoadLocations().execute();
                    }
                });
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadLocations extends AsyncTask<Void, Void, List<Location>> {
        @Override
        protected List<Location> doInBackground(Void... params) {
            LocationManager locationManager = Model.instance().getLocationManager();

            return locationManager.getLocations();
        }

        @Override
        protected void onPostExecute(List<Location> locations) {
            fillMapViews(locations);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void fillMapViews(List<Location> locations) {
        if (locations == null || locations.isEmpty()) {
            TextView textViewAddress = Objects.requireNonNull(getView()).findViewById(R.id.txtAddress);

            textViewAddress.setText(getString(R.string.placeholder_location));
        }

        if (locations != null && locations.size() > 0) {
            Location location = locations.get(0);

            fillTextViews(location);

            LatLng position = new LatLng(location.getLat(), location.getLon());

            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition
                    .Builder()
                    .target(position)
                    .zoom(ZOOM_LEVEL)
                    .tilt(TILT_LEVEL)
                    .bearing(BEARING_LEVEL)
                    .build()));

            mapboxMap.addMarker(new MarkerOptions().position(position).title(location.getName()));

            rootView.findViewById(R.id.progressBar).setVisibility(View.GONE);

            mapView.setVisibility(View.VISIBLE);
        }
    }

    private void fillTextViews(Location location) {
        if (getView() == null) {
            return;
        }

        TextView txtPlace = getView().findViewById(R.id.txtPlace);
        TextView txtAddress = getView().findViewById(R.id.txtAddress);

        String locationName = location.getName();

        txtPlace.setText(locationName);

        String address = location.getAddress();

        address = address.replace(", ", "\n");

        txtAddress.setText(address.trim());
    }
}
