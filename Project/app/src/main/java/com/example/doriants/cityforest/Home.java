package com.example.doriants.cityforest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
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
import static com.mapbox.services.android.telemetry.location.AndroidLocationEngine.getLocationEngine;

public class Home extends AppCompatActivity implements PermissionsListener {

    private MapView mapView;
    private MapboxMap map;
    private FirebaseDatabase database;
    private DatabaseReference tracks;
    private Button advanced_search_butt;
    private Button make_own_track_butt;
    private Button tracks_list_butt;

    private FloatingActionButton floatingActionButton;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    private PermissionsManager permissionsManager;

    private GoogleApiClient mGoogleApiClient;
    private ProgressBar loading_map_progress_bar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_home);

        // Get the location engine object for later use.
        locationEngine = getLocationEngine(this);
        locationEngine.activate();

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new myOnMapReadyCallback());

        database = FirebaseDatabase.getInstance();
        tracks = database.getReference("tracks");

        advanced_search_butt = (Button)findViewById(R.id.advancedSearchButt);
        make_own_track_butt = (Button)findViewById(R.id.makeOwnTrackButt);
        tracks_list_butt = (Button)findViewById(R.id.tracksButt);
        loading_map_progress_bar = (ProgressBar)findViewById(R.id.loadingMapProgress);
        advanced_search_butt.setOnClickListener(new ClickListener());
        make_own_track_butt.setOnClickListener(new ClickListener());
        tracks_list_butt.setOnClickListener(new ClickListener());

        floatingActionButton = (FloatingActionButton) findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (map != null) {
                    toggleGps(!map.isMyLocationEnabled());
                }
            }
        });

        loading_map_progress_bar.setVisibility(View.VISIBLE);
    }

    /*In order to be able to sign out from the logged in account, I have to
    * check who is the logged in user.*/
    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String welcome_message = "Hello, " + userName;
        setTitle(welcome_message);
        super.onStart();
    }

    private class ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v.getId() == advanced_search_butt.getId()){
                Intent i = new Intent(Home.this, AdvancedSearchTracksActivity.class);
                startActivity(i);
            }
            if(v.getId() == make_own_track_butt.getId()){
                Intent i = new Intent(Home.this, MakeOwnTrackActivity.class);
                startActivity(i);
            }
            if(v.getId() == tracks_list_butt.getId()){
                Intent i = new Intent(Home.this, TracksActivity.class);
                startActivity(i);
            }
        }
    }

    private class myOnMapReadyCallback implements OnMapReadyCallback {
        @Override
        public void onMapReady(MapboxMap mapboxMap) {
            map = mapboxMap;
            map.setStyleUrl(Style.OUTDOORS);
            showDefaultLocation();
            showAllTracks();
            toggleGps(true);

        }
    }

    private void showAllTracks() {
        tracks.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> tracksMap = (Map<String, Object>)dataSnapshot.getValue();
                if(tracksMap == null)
                    return;

                /*Iterating all the coordinates in the list*/
                for (Map.Entry<String, Object> entry : tracksMap.entrySet()) {
                    /*For each coordinate in the database, we want to create a new marker
                    * for it and to show the marker on the map*/
                    Map<String, Object> track = ((Map<String, Object>) entry.getValue());

                    String route_st = (String)track.get("route");
                    DirectionsRoute route = retrieveRouteFromJson(route_st);
                    drawRoute(route);
                }
                loading_map_progress_bar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                loading_map_progress_bar.setVisibility(View.INVISIBLE);
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

    private void showDefaultLocation(){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(DEFAULT_JERUSALEM_COORDINATE.getLatitude(),
                        DEFAULT_JERUSALEM_COORDINATE.getLongitude()), 10));
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
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 13));
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
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 13));
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
                i = new Intent(this, AboutUsActivity.class);
                startActivity(i);
                return true;

            case R.id.contactUsActivity:
                i = new Intent(this, ContactUsActivity.class);
                startActivity(i);
                return true;

            case R.id.homeActivity:
                return true;

            case R.id.userGuideActivity:
                i = new Intent(this, UserGuideActivity.class);
                startActivity(i);
                return true;

            case R.id.searchTracksActivity:
                i = new Intent(this, AdvancedSearchTracksActivity.class);
                startActivity(i);
                return true;

            case R.id.makeOwnTrackActivity:
                i = new Intent(this, MakeOwnTrackActivity.class);
                startActivity(i);
                return true;

            case R.id.tracksActivity:
                i = new Intent(this, TracksActivity.class);
                startActivity(i);
                return true;

            case R.id.signOut:
                signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*Method signs out user's google account*/
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent i = new Intent(Home.this, SignInActivity.class);
                        startActivity(i);
                    }});
    }

    /*Exit app dialog if the user clicked back button*/
    @Override
    public void onBackPressed() {
        String current_user_uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if(current_user_uid.equals(getResources().getString(R.string.permitted_editor))){
            Intent i = new Intent(Home.this, EditorHomeActivity.class);
            startActivity(i);
        }
        else{
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_exit_app_title)
                    .setMessage(R.string.dialog_exit_app_body)
                    .setPositiveButton(R.string.dialog_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                           /*Exits the application*/
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                        }
                    })
                    .setNegativeButton(R.string.dialog_cancel, null)
                    .show();
        }
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
    }
}
