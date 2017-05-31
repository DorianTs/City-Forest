package com.example.doriants.cityforest;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.LocationSource;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.models.DirectionsRoute;

import java.util.List;
import java.util.Map;

import static com.example.doriants.cityforest.Constants.DEFAULT_JERUSALEM_COORDINATE;
import static com.example.doriants.cityforest.Constants.ROUTE_LINE_WIDTH;
import static com.example.doriants.cityforest.Constants.SELECTED_TRACK;
import static com.mapbox.services.android.telemetry.location.AndroidLocationEngine.getLocationEngine;

public class SelectedTrack extends AppCompatActivity implements PermissionsListener {

    private MapView mapView;
    private MapboxMap map;
    private FirebaseDatabase database;
    private DatabaseReference tracks;
    private String track_db_key;

    private FloatingActionButton floatingActionButton;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    private PermissionsManager permissionsManager;

    private TextView track_name_field;
    private TextView starting_point_field;
    private TextView ending_point_field;
    private TextView distance_field;
    private TextView duration_field;
    private TextView level_field;
    private TextView season_field;
    private TextView summary_field;
    private CheckBox has_water_checkbox;
    private CheckBox suitable_for_bikes_checkbox;
    private CheckBox suitable_for_dogs_checkbox;
    private CheckBox suitable_for_families_checkbox;
    private CheckBox is_romantic_checkbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_selected_track);

        // Get the location engine object for later use.
        locationEngine = getLocationEngine(this);
        locationEngine.activate();

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new myOnMapReadyCallback());

        database = FirebaseDatabase.getInstance();
        tracks = database.getReference("tracks");

        Intent i = getIntent();
        track_db_key = i.getStringExtra(SELECTED_TRACK);

        track_name_field = (TextView) findViewById(R.id.trackNameField);
        starting_point_field = (TextView) findViewById(R.id.startingPointField);
        ending_point_field = (TextView) findViewById(R.id.endingPointField);
        distance_field = (TextView) findViewById(R.id.distanceField);
        duration_field = (TextView) findViewById(R.id.durationField);
        level_field = (TextView) findViewById(R.id.levelField);
        season_field = (TextView) findViewById(R.id.seasonField);
        summary_field = (TextView) findViewById(R.id.summaryField);
        has_water_checkbox = (CheckBox) findViewById(R.id.hasWaterCheckbox);
        suitable_for_bikes_checkbox = (CheckBox) findViewById(R.id.suitableForBikesCheckbox);
        suitable_for_dogs_checkbox = (CheckBox) findViewById(R.id.suitableForDogsCheckbox);
        suitable_for_families_checkbox = (CheckBox) findViewById(R.id.suitableForFamiliesCheckbox);
        is_romantic_checkbox = (CheckBox) findViewById(R.id.isRomanticCheckbox);

        has_water_checkbox.setClickable(false);
        suitable_for_bikes_checkbox.setClickable(false);
        suitable_for_dogs_checkbox.setClickable(false);
        suitable_for_families_checkbox.setClickable(false);
        is_romantic_checkbox.setClickable(false);

        initiateScreenValues();

        floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (map != null) {
                    toggleGps(!map.isMyLocationEnabled());
                }
            }
        });
    }


    private void initiateScreenValues() {
        tracks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> tracksMap = (Map<String, Object>) dataSnapshot.getValue();
                Map<String, Object> track = (Map<String, Object>) tracksMap.get(track_db_key);

                DirectionsRoute route = retrieveRouteFromJson((String) track.get("route"));
                drawRoute(route);

                track_name_field.setText((String) track.get("track_name"));
                starting_point_field.setText((String) track.get("starting_point"));
                ending_point_field.setText((String) track.get("ending_point"));
                distance_field.setText(track.get("length").toString());
                duration_field.setText(track.get("duration").toString());
                level_field.setText((String) track.get("level"));
                season_field.setText((String) track.get("season"));
                summary_field.setText((String) track.get("additional_info"));
                has_water_checkbox.setChecked((boolean) track.get("has_water"));
                suitable_for_bikes_checkbox.setChecked((boolean) track.get("suitable_for_bikes"));
                suitable_for_families_checkbox.setChecked((boolean) track.get("suitable_for_families"));
                suitable_for_dogs_checkbox.setChecked((boolean) track.get("suitable_for_dogs"));
                is_romantic_checkbox.setChecked((boolean) track.get("is_romantic"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void drawRoute(DirectionsRoute route) {
        // Convert LineString coordinates into LatLng[]
        LineString lineString = LineString.fromPolyline(route.getGeometry(), com.mapbox.services.Constants.OSRM_PRECISION_V5);
        List<Position> coordinates = lineString.getCoordinates();
        LatLng[] points = new LatLng[coordinates.size()];
        for (int i = 0; i < coordinates.size(); i++) {
            points[i] = new LatLng(
                    coordinates.get(i).getLatitude(),
                    coordinates.get(i).getLongitude());
        }

        // Draw Points on MapView
        PolylineOptions routeLine = new PolylineOptions()
                .add(points)
                .color(Color.RED)
                .width(ROUTE_LINE_WIDTH);
        map.addPolyline(routeLine);

    }

    public DirectionsRoute retrieveRouteFromJson(String route) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        DirectionsRoute obj = gson.fromJson(route, DirectionsRoute.class);
        return obj;
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "This app needs location permissions in order to show its functionality.",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation(true);
        } else {
            Toast.makeText(this, "You didn't grant location permissions.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }


    private class myOnMapReadyCallback implements OnMapReadyCallback {
        @Override
        public void onMapReady(MapboxMap mapboxMap) {
            map = mapboxMap;
            map.setStyleUrl(Style.OUTDOORS);
            showDefaultLocation();
        }
    }


    private void showDefaultLocation() {
        /*Showing the default position to the editor*/
        double[] cameraPosValue = new double[5];
        cameraPosValue[0] = DEFAULT_JERUSALEM_COORDINATE.getLatitude();
        cameraPosValue[1] = DEFAULT_JERUSALEM_COORDINATE.getLongitude();
        cameraPosValue[2] = 0;
        cameraPosValue[3] = 0;
        cameraPosValue[4] = 10;

        CameraPosition.Builder cpBuilder = new CameraPosition.Builder(cameraPosValue);
        CameraPosition tempPos = cpBuilder.build();
        map.setCameraPosition(tempPos);
    }


    private void toggleGps(boolean enableGps) {
        if (enableGps) {
            // Check if user has granted location permission
            permissionsManager = new PermissionsManager(this);
            if (!PermissionsManager.areLocationPermissionsGranted(this)) {
                permissionsManager.requestLocationPermissions(this);
            } else {
                enableLocation(true);
            }
        } else {
            enableLocation(false);
            showDefaultLocation();
        }
    }

    private void enableLocation(boolean enabled) {
        if (enabled) {
            // If we have the last location of the user, we can move the camera to that position.
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location lastLocation = locationEngine.getLastLocation();
            if (lastLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 16));
            }

            locationEngineListener = new LocationEngineListener() {
                @Override
                public void onConnected() {
                    // No action needed here.
                }

                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        // Move the map camera to where the user location is and then remove the
                        // listener so the camera isn't constantly updating when the user location
                        // changes. When the user disables and then enables the location again, this
                        // listener is registered again and will adjust the camera once again.
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
                        locationEngine.removeLocationEngineListener(this);
                    }
                }
            };
            locationEngine.addLocationEngineListener(locationEngineListener);
            floatingActionButton.setImageResource(R.drawable.ic_location_disabled_24dp);
        } else {
            floatingActionButton.setImageResource(R.drawable.ic_my_location_24dp);
        }
        // Enable or disable the location layer on the map
        map.setMyLocationEnabled(enabled);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }










    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        // Ensure no memory leak occurs if we register the location listener but the call hasn't
        // been made yet.
        if (locationEngineListener != null) {
            locationEngine.removeLocationEngineListener(locationEngineListener);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()){
            case R.id.aboutActivity:
                //i = new Intent(this, AboutUs.class);
                //startActivity(i);
                return true;

            case R.id.contactUsActivity:
                //i = new Intent(this, ContactUs.class);
                //startActivity(i);
                return true;

            case R.id.homeActivity:
                //i = new Intent(this, Home.class);
                //startActivity(i);
                return true;

            case R.id.tracksActivity:
                //i = new Intent(this, Tracks.class);
                //startActivity(i);
                return true;

            case R.id.userGuideActivity:
                //i = new Intent(this, UserGuide.class);
                //startActivity(i);
                return true;

            case R.id.searchTracksActivity:
                //i = new Intent(this, SearchTracksActivity.class);
                //startActivity(i);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
