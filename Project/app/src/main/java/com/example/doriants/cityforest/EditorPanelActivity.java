package com.example.doriants.cityforest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.commons.ServicesException;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.DirectionsCriteria;
import com.mapbox.services.directions.v5.MapboxDirections;
import com.mapbox.services.directions.v5.models.DirectionsResponse;
import com.mapbox.services.directions.v5.models.DirectionsRoute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.doriants.cityforest.Constants.ADD_COORDINATE_MODE;
import static com.example.doriants.cityforest.Constants.ADD_TRACK_MODE;
import static com.example.doriants.cityforest.Constants.CHOSEN_COORDINATE;
import static com.example.doriants.cityforest.Constants.CHOSEN_TRACK;
import static com.example.doriants.cityforest.Constants.COORDINATE_KEY;
import static com.example.doriants.cityforest.Constants.DEFAULT_JERUSALEM_COORDINATE;
import static com.example.doriants.cityforest.Constants.DELETE_COORDINATE_MODE;
import static com.example.doriants.cityforest.Constants.EDIT_COORDINATE_MODE;
import static com.example.doriants.cityforest.Constants.FINISH_EDIT_TRACK_MODE;
import static com.example.doriants.cityforest.Constants.MAX_NUM_OF_TRACK_COORDINATES;
import static com.example.doriants.cityforest.Constants.NEW_COORDINATE;
import static com.example.doriants.cityforest.Constants.NEW_TRACK;
import static com.example.doriants.cityforest.Constants.ROUTE_LINE_WIDTH;
import static com.example.doriants.cityforest.Constants.ZOOM_LEVEL_CURRENT_LOCATION;
import static com.mapbox.services.android.telemetry.location.AndroidLocationEngine.getLocationEngine;

public class EditorPanelActivity extends AppCompatActivity implements PermissionsListener {

    private MapView mapView;
    private MapboxMap map;
    private DirectionsRoute currentRoute;
    private PolylineOptions routeLine;
    private FirebaseDatabase database;
    private DatabaseReference coordinates;
    private DatabaseReference points_of_interest;
    private ImageButton add_coordinate_button;
    private ImageButton delete_coordinate_button;
    private ImageButton edit_coordinate_button;
    private ImageButton add_track_button;
    private Button finish_edit_track_butt;
    private Button save_track;
    private Button continue_editing;
    private ImageButton edit_tracks_button;
    private TextView counter_coordinates;

    /*Count how many coordinates selected for creating a track (max of 25 coordinates)*/
    private int count_coordinates_selected = 0;
    private ArrayList<Double> track_coordinates = new ArrayList<>();
    private ArrayList<Marker> track_markers = new ArrayList<>();
    private ArrayList<Position> track_positions = new ArrayList<>();

    private FloatingActionButton floatingActionButton;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;
    private PermissionsManager permissionsManager;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Mapbox and firebase initializations*/
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_editor_panel);

        // Get the location engine object for later use.
        locationEngine = getLocationEngine(this);
        locationEngine.activate();

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new myOnMapReadyCallback());

        database = FirebaseDatabase.getInstance();
        coordinates = database.getReference("coordinates");
        points_of_interest = database.getReference("points_of_interest");

        /*Buttons for different edit map modes*/
        add_coordinate_button = (ImageButton)findViewById(R.id.addCoordinateButt);
        delete_coordinate_button = (ImageButton)findViewById(R.id.deleteCoordinateButt);
        edit_coordinate_button = (ImageButton)findViewById(R.id.editCoordinateButt);
        edit_tracks_button = (ImageButton)findViewById(R.id.editTrackButton);
        add_track_button = (ImageButton)findViewById(R.id.addTrackButt);
        finish_edit_track_butt = (Button)findViewById(R.id.finishEditTrack);
        save_track = (Button)findViewById(R.id.saveTrack);
        continue_editing = (Button)findViewById(R.id.continueEditTrack);
        counter_coordinates = (TextView)findViewById(R.id.counterCoordinates);

        ClickListener clickListener = new ClickListener();
        add_coordinate_button.setOnClickListener(clickListener);
        delete_coordinate_button.setOnClickListener(clickListener);
        edit_coordinate_button.setOnClickListener(clickListener);
        add_track_button.setOnClickListener(clickListener);
        finish_edit_track_butt.setOnClickListener(clickListener);
        save_track.setOnClickListener(clickListener);
        continue_editing.setOnClickListener(clickListener);
        edit_tracks_button.setOnClickListener(clickListener);

        ADD_COORDINATE_MODE = false;
        DELETE_COORDINATE_MODE = false;
        EDIT_COORDINATE_MODE = false;
        ADD_TRACK_MODE = false;
        FINISH_EDIT_TRACK_MODE = false;

        counter_coordinates.setVisibility(View.INVISIBLE);
        finish_edit_track_butt.setVisibility(View.INVISIBLE);
        save_track.setVisibility(View.INVISIBLE);
        continue_editing.setVisibility(View.INVISIBLE);

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
        super.onStart();
    }

    private class ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            /*Handling the clicks on different modes, updating different modes too*/
            if(v.getId() == add_coordinate_button.getId()) {
                if(ADD_COORDINATE_MODE){
                    ADD_COORDINATE_MODE = false;
                    add_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);
                }
                else{
                    ADD_COORDINATE_MODE = true;
                    DELETE_COORDINATE_MODE = false;
                    EDIT_COORDINATE_MODE = false;
                    ADD_TRACK_MODE = false;

                    add_coordinate_button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    delete_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);
                    edit_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);
                    add_track_button.setBackgroundResource(android.R.drawable.btn_default);
                }
            }
            if(v.getId() == delete_coordinate_button.getId()){
                if(DELETE_COORDINATE_MODE){
                    DELETE_COORDINATE_MODE = false;
                    delete_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);
                }
                else{
                    ADD_COORDINATE_MODE = false;
                    DELETE_COORDINATE_MODE = true;
                    EDIT_COORDINATE_MODE = false;
                    ADD_TRACK_MODE = false;

                    delete_coordinate_button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    add_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);
                    edit_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);
                    add_track_button.setBackgroundResource(android.R.drawable.btn_default);
                }
            }
            if(v.getId() == edit_coordinate_button.getId()){
                if(EDIT_COORDINATE_MODE){
                    EDIT_COORDINATE_MODE = false;
                    edit_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);
                }
                else{
                    ADD_COORDINATE_MODE = false;
                    DELETE_COORDINATE_MODE = false;
                    EDIT_COORDINATE_MODE = true;
                    ADD_TRACK_MODE = false;

                    edit_coordinate_button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    add_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);
                    delete_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);
                    add_track_button.setBackgroundResource(android.R.drawable.btn_default);
                }
            }
            if(v.getId() == add_track_button.getId()){
                if(ADD_TRACK_MODE){
                    ADD_TRACK_MODE = false;
                    add_track_button.setBackgroundResource(android.R.drawable.btn_default);

                    /*
                    Passing on track_coordinates markers
                    * and change their color back to red.
                    * And clearing both arrays for the next time building a new track*/
                    for(int i=0; i<track_markers.size(); i++){
                        addMarkerForCoordinate(track_markers.get(i).getPosition(), track_markers.get(i).getTitle(),
                                track_markers.get(i).getSnippet());
                        map.removeMarker(track_markers.get(i));
                    }
                    track_coordinates.clear();
                    track_markers.clear();
                    track_positions.clear();
                    count_coordinates_selected = 0;

                    counter_coordinates.setVisibility(View.INVISIBLE);
                    finish_edit_track_butt.setVisibility(View.INVISIBLE);
                    save_track.setVisibility(View.INVISIBLE);
                    continue_editing.setVisibility(View.INVISIBLE);

                    add_coordinate_button.setClickable(true);
                    delete_coordinate_button.setClickable(true);
                    edit_coordinate_button.setClickable(true);
                }
                else{
                    ADD_COORDINATE_MODE = false;
                    DELETE_COORDINATE_MODE = false;
                    EDIT_COORDINATE_MODE = false;
                    ADD_TRACK_MODE = true;

                    add_track_button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                    add_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);
                    delete_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);
                    edit_coordinate_button.setBackgroundResource(android.R.drawable.btn_default);

                    updateScreenCounter();
                    counter_coordinates.setVisibility(View.VISIBLE);
                    finish_edit_track_butt.setVisibility(View.VISIBLE);
                    add_coordinate_button.setClickable(false);
                    delete_coordinate_button.setClickable(false);
                    edit_coordinate_button.setClickable(false);
                }
            }
            /*If editor clicked this button, we need to show him the resulted track by creating
            * the route object and save it as the class's property*/
            if(v.getId() == finish_edit_track_butt.getId()){
                if(track_markers.size() < 2){
                    Toast.makeText(EditorPanelActivity.this, R.string.not_enough_coordinates_selected, Toast.LENGTH_SHORT).show();
                    return;
                }

                final String last_marker_key = getMarkerHashKey(track_markers.get(track_markers.size()-1));
                points_of_interest.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Map<String, Object> pointsMap = (Map<String, Object>)dataSnapshot.getValue();
                        if(pointsMap == null) {
                            Toast.makeText(EditorPanelActivity.this, R.string.toast_ending_point, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Map<String, Object> point = ((Map<String, Object>)pointsMap.get(last_marker_key));

                        if(point == null){
                            Toast.makeText(EditorPanelActivity.this, R.string.toast_ending_point, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String type = (String)point.get("type");
                        if(! type.equals("תחנת רכבת")){
                            Toast.makeText(EditorPanelActivity.this, R.string.toast_ending_point, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //here we turn off ADD_TRACK_MODE
                        ADD_TRACK_MODE = false;
                        add_track_button.setClickable(false);
                        FINISH_EDIT_TRACK_MODE = true;
                        finish_edit_track_butt.setVisibility(View.INVISIBLE);
                        save_track.setVisibility(View.VISIBLE);
                        continue_editing.setVisibility(View.VISIBLE);

                        try {
                            getRoute();
                        } catch (ServicesException servicesException) {
                            servicesException.printStackTrace();
                        }

                        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                .include(track_markers.get(0).getPosition())
                                .include(track_markers.get(track_markers.size()-1).getPosition())
                                .build();

                        map.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 200), 100);

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            if(v.getId() == continue_editing.getId()){
                //here we turn on ADD_TRACK_MODE
                ADD_TRACK_MODE = true;
                add_track_button.setClickable(true);
                save_track.setVisibility(View.INVISIBLE);
                continue_editing.setVisibility(View.INVISIBLE);
                finish_edit_track_butt.setVisibility(View.VISIBLE);


                map.removePolyline(routeLine.getPolyline());
            }
            if(v.getId() == save_track.getId()){
                Intent i = new Intent(EditorPanelActivity.this, CreateNewTrackActivity.class);
                i.putExtra(CHOSEN_TRACK, castRouteToJson(currentRoute));
                startActivityForResult(i, NEW_TRACK);
            }
            if(v.getId() == edit_tracks_button.getId()){
                Intent i = new Intent(EditorPanelActivity.this, EditTracksActivity.class);
                startActivity(i);
            }
        }
    }

    private class myOnMapReadyCallback implements OnMapReadyCallback {
        @Override
        public void onMapReady(MapboxMap mapboxMap) {
            map = mapboxMap;
            map.setOnMapClickListener(new MyOnMapClickListener());
            map.setOnMarkerClickListener(new MyOnMarkerClickListener());
            map.setStyleUrl(Style.OUTDOORS);
            showDefaultLocation();
            showAllCoordinates();
            showAllPointsOfInterest();
        }
    }

    private void showAllPointsOfInterest() {
        /*Reading one time from the database, we get the points of interest map list*/
        points_of_interest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> pointsMap = (Map<String, Object>)dataSnapshot.getValue();
                if(pointsMap == null)
                    return;
                /*Iterating all the coordinates in the list*/
                for (Map.Entry<String, Object> entry : pointsMap.entrySet())
                {
                    /*For each coordinate in the database, we want to create a new marker
                    * for it and to show the marker on the map*/
                    Map<String, Object> point = ((Map<String, Object>) entry.getValue());
                    /*Now the object 'cor' holds a *map* for a specific coordinate*/
                    String positionJSON = (String) point.get("position");
                    Position position = retrievePositionFromJson(positionJSON);

                    /*Creating the marker on the map*/
                    LatLng latlng = new LatLng(
                            position.getLongitude(),
                            position.getLatitude());

                    long logo = (long)point.get("logo");
                    addMarkerForPointOfInterest(latlng, (String)point.get("title"), (String)point.get("snippet"), (int) logo);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });
    }

    private void showAllCoordinates() {
        /*Reading one time from the database, we get the coordinates map list*/
        coordinates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> coordinatesMap = (Map<String, Object>)dataSnapshot.getValue();
                if(coordinatesMap == null){
                    return;
                }

                /*Iterating all the coordinates in the list*/
                for (Map.Entry<String, Object> entry : coordinatesMap.entrySet())
                {
                    /*For each coordinate in the database, we want to create a new marker
                    * for it and to show the marker on the map*/
                    Map<String, Object> cor = ((Map<String, Object>) entry.getValue());
                    /*Now the object 'cor' holds a *map* for a specific coordinate*/
                    String positionJSON = (String) cor.get("position");
                    Position position = retrievePositionFromJson(positionJSON);

                    /*Creating the marker on the map*/
                    LatLng latlng = new LatLng(
                            position.getLongitude(),
                            position.getLatitude());
                    addMarkerForCoordinate(latlng, (String)cor.get("title"), (String)cor.get("snippet"));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });


    }

    private void getRoute() throws ServicesException {
        MapboxDirections client = new MapboxDirections.Builder()
                .setCoordinates(track_positions)
                .setProfile(DirectionsCriteria.PROFILE_WALKING)
                .setAccessToken(MapboxAccountManager.getInstance().getAccessToken())
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
                // You can get the generic HTTP info about the response

                if (response.body() == null) {
                    return;
                } else if (response.body().getRoutes().size() < 1) {
                    return;
                }

                // Print some info about the route
                currentRoute = response.body().getRoutes().get(0);
                Toast.makeText(
                        EditorPanelActivity.this,
                        "Route is " + currentRoute.getDistance() + " meters long.",
                        Toast.LENGTH_LONG).show();

                // Draw the route on the map
                drawRoute(currentRoute);
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Toast.makeText(EditorPanelActivity.this, "Error: " + throwable.getMessage(), Toast.LENGTH_SHORT).show();
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
        routeLine = new PolylineOptions()
                .add(points)
                .color(Color.RED)
                .width(ROUTE_LINE_WIDTH);
        map.addPolyline(routeLine);

    }

    /*Method get String that represents a Position Json object.
    * Method retrieve the position object and returns it*/
    public Position retrievePositionFromJson(String posJs) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        Position obj = gson.fromJson(posJs, Position.class);
        return obj;
    }

    private void showDefaultLocation(){
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(DEFAULT_JERUSALEM_COORDINATE.getLatitude(),
                        DEFAULT_JERUSALEM_COORDINATE.getLongitude()), 10));
    }

    private class MyOnMapClickListener implements MapboxMap.OnMapClickListener{
        @Override
        public void onMapClick(@NonNull LatLng point) {
            if(ADD_COORDINATE_MODE)
                dialogAddNewCoordinate(point);
            /*When clicking on a location (which is not a marker!) in the map while
            * being in ADD_COORDINATE_MODE,
            * we want to ask the editor if he wants to add a new coordinate
            * in the database for this specific location*/

            if(ADD_TRACK_MODE && count_coordinates_selected < MAX_NUM_OF_TRACK_COORDINATES) {
                if(count_coordinates_selected == 0){
                    Toast.makeText(EditorPanelActivity.this, R.string.toast_starting_point, Toast.LENGTH_SHORT).show();
                    return;
                }
                else{
                    writeGenericCoordinateToDB(point);
                    MarkerView marker = addMarkerForCoordinate(point, "", "");
                    IconFactory iconFactory = IconFactory.getInstance(EditorPanelActivity.this);
                    Icon icon = iconFactory.fromResource(R.drawable.blue_marker);
                    marker.setIcon(icon);


                    track_coordinates.add(point.getLongitude());
                    track_coordinates.add(point.getLatitude());
                    track_markers.add(marker);
                    Position temp = Position.fromCoordinates(point.getLongitude(), point.getLatitude());
                    track_positions.add(temp);
                    count_coordinates_selected++;
                    updateScreenCounter();
                }

            }
            else if(ADD_TRACK_MODE && count_coordinates_selected >= MAX_NUM_OF_TRACK_COORDINATES){
                Toast.makeText(EditorPanelActivity.this, R.string.reached_limit_of_coordinates, Toast.LENGTH_SHORT).show();
            }

        }
    }

    private MarkerView addMarkerForCoordinate(LatLng point, String title, String snippet) {
        MarkerViewOptions markerViewOptions = new MarkerViewOptions()
                .position(point)
                .title(title)
                .snippet(snippet);
        map.addMarker(markerViewOptions);
        return markerViewOptions.getMarker();
    }

    private MarkerView addMarkerForPointOfInterest(LatLng point, String title, String snippet, int logo){
        MarkerViewOptions markerViewOptions = new MarkerViewOptions()
                .position(point)
                .title(title)
                .snippet(snippet);
        map.addMarker(markerViewOptions);

        if(logo != -1) {
            IconFactory iconFactory = IconFactory.getInstance(EditorPanelActivity.this);
            Icon icon = iconFactory.fromResource(logo);
            markerViewOptions.getMarker().setIcon(icon);
        }
        return markerViewOptions.getMarker();
    }

    private void writeGenericCoordinateToDB(LatLng point) {
        String key = getCoordinateHashKey(point);

        Coordinate coordinate = new Coordinate(
                point.getLongitude(),
                point.getLatitude(),
                "",
                "");

        /*Converting our coordinate object to a map, that makes
        * the coordinate ready to be entered to the JSON tree*/
        Map<String, Object> coordinateMap = coordinate.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, coordinateMap);
        coordinates.updateChildren(childUpdates);
    }



    /*Marker clicked listener. We can delete/edit a coordinate*/
    private class MyOnMarkerClickListener implements MapboxMap.OnMarkerClickListener{
        @Override
        public boolean onMarkerClick(@NonNull final Marker marker) {

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().getLatitude(),
                    marker.getPosition().getLongitude()), 16));

            if(DELETE_COORDINATE_MODE){
                dialogDeleteCoordinate(marker);
                return true;
            }
            if(EDIT_COORDINATE_MODE){
                dialogEditCoordinate(marker);
                return true;
            }

            if(ADD_TRACK_MODE) {
                for(int i=0; i<track_markers.size(); i++) {
                            /*If the editor clicked a marker that is already part of the track
                            * we want to remove it from the track coordinates array*/
                    if(track_markers.get(i).toString().equals(marker.toString())){
                        track_coordinates.remove(marker.getPosition().getLongitude());
                        track_coordinates.remove(marker.getPosition().getLatitude());
                        track_markers.remove(marker);
                        Position temp = Position.fromCoordinates(marker.getPosition().getLongitude(), marker.getPosition().getLatitude());
                        track_positions.remove(temp);
                        count_coordinates_selected--;
                        updateScreenCounter();
                        addMarkerForCoordinate(marker.getPosition(), marker.getTitle(), marker.getSnippet());
                        map.removeMarker(marker);
                        return true;
                    }
                }

                /*If we arrived this point, we know that the clicked marker wasn't chosen before,
                * that's why we need to add him now to the track coordinates*/
                if(count_coordinates_selected >= MAX_NUM_OF_TRACK_COORDINATES){
                    Toast.makeText(EditorPanelActivity.this, R.string.reached_limit_of_coordinates, Toast.LENGTH_SHORT).show();
                    return true;
                }

                /*We want to make sure that the first chosen coordinate is train station*/
                if(count_coordinates_selected == 0){
                    final String key = getMarkerHashKey(marker);
                    points_of_interest.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Map<String, Object> pointsMap = (Map<String, Object>)dataSnapshot.getValue();
                            if(pointsMap == null) {
                                Toast.makeText(EditorPanelActivity.this, R.string.toast_starting_point, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Map<String, Object> point = ((Map<String, Object>)pointsMap.get(key));


                            if(point == null){
                                Toast.makeText(EditorPanelActivity.this, R.string.toast_starting_point, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            String type = (String)point.get("type");
                            if(! type.equals("תחנת רכבת")){
                                Toast.makeText(EditorPanelActivity.this, R.string.toast_starting_point, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            IconFactory iconFactory = IconFactory.getInstance(EditorPanelActivity.this);
                            Icon icon = iconFactory.fromResource(R.drawable.blue_marker);

                            track_coordinates.add(marker.getPosition().getLongitude());
                            track_coordinates.add(marker.getPosition().getLatitude());
                            track_markers.add(marker);
                            Position temp = Position.fromCoordinates(marker.getPosition().getLongitude(), marker.getPosition().getLatitude());
                            track_positions.add(temp);
                            count_coordinates_selected++;
                            updateScreenCounter();


                            marker.setIcon(icon);

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                else{
                    IconFactory iconFactory = IconFactory.getInstance(EditorPanelActivity.this);
                    Icon icon = iconFactory.fromResource(R.drawable.blue_marker);

                    track_coordinates.add(marker.getPosition().getLongitude());
                    track_coordinates.add(marker.getPosition().getLatitude());
                    track_markers.add(marker);
                    Position temp = Position.fromCoordinates(marker.getPosition().getLongitude(), marker.getPosition().getLatitude());
                    track_positions.add(temp);
                    count_coordinates_selected++;
                    updateScreenCounter();


                    marker.setIcon(icon);
                    return true;
                }

            }

            return false;
        }
    }

    private void updateScreenCounter(){
        StringBuilder s = new StringBuilder().append(getResources().getString(R.string.coordinates_selected_txt)).
                append(" "+count_coordinates_selected);
        counter_coordinates.setText(s.toString());
    }

    private void dialogEditCoordinate(final Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorPanelActivity.this);
        builder.setMessage(getResources().getString(R.string.dialog_edit_coordinate));

        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                /*Sending the key for the edit coordinate activity
                * to be able to update this coordinate in the database*/
                String key = getMarkerHashKey(marker);
                Intent i = new Intent(EditorPanelActivity.this, EditCoordinateActivity.class);
                i.putExtra(COORDINATE_KEY, key);
                startActivity(i);
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getMarkerHashKey(final Marker marker) {
        double longitude = marker.getPosition().getLongitude();
        //double latitude = chosenCoordinateLatLng.getLatitude();

        int hash = (int) (10000000*longitude);
        return "" + hash;
    }

    private String getCoordinateHashKey(LatLng point) {
        double longitude = point.getLongitude();
        //double latitude = chosenCoordinateLatLng.getLatitude();
        int hash = (int) (10000000*longitude);
        return "" + hash;
    }

    private void dialogDeleteCoordinate(final Marker marker){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorPanelActivity.this);
        builder.setMessage(getResources().getString(R.string.dialog_delete_coordinate));

        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteCoordinateFromDb(marker);
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*Method gets the marker to delete from the database, gets it's key in
    * the db and erase it.*/
    private void deleteCoordinateFromDb(final Marker marker) {
        String key = getMarkerHashKey(marker);
        coordinates.child(key).removeValue();
        points_of_interest.child(key).removeValue();
        map.removeMarker(marker);
    }

    /*Dialog function to ask the editor if he wants to add new coordinate with details.
    * if yes, we are directed to the creating new coordinate activity*/
    private void dialogAddNewCoordinate(final LatLng point) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorPanelActivity.this);
        builder.setMessage(getResources().getString(R.string.dialog_add_new_coordinate));


        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent i = new Intent(EditorPanelActivity.this, CreateNewCoordinateActivity.class);
                i.putExtra(CHOSEN_COORDINATE, castLatLngToJson(point));
                startActivityForResult(i, NEW_COORDINATE);
            }
        });
        builder.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*Method casts LatLng object to Json, to be able to send it via intent*/
    public String castLatLngToJson(LatLng point){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        String json = gson.toJson(point, LatLng.class);
        return json;
    }

    /*Method casts LatLng object to Json, to be able to send it via intent*/
    public String castRouteToJson(DirectionsRoute route){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        String json = gson.toJson(route, DirectionsRoute.class);
        return json;
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
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), ZOOM_LEVEL_CURRENT_LOCATION));
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
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), ZOOM_LEVEL_CURRENT_LOCATION));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.editor_home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
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
                        Intent i = new Intent(EditorPanelActivity.this, SignInActivity.class);
                        startActivity(i);
                    }});
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent(EditorPanelActivity.this, EditorHomeActivity.class);
        startActivity(i);
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
