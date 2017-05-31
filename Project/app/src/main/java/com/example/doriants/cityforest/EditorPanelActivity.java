package com.example.doriants.cityforest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class EditorPanelActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;
    private DirectionsRoute currentRoute;
    private PolylineOptions routeLine;
    private FirebaseDatabase database;
    private DatabaseReference coordinates;
    private Button add_coordinate_button;
    private Button delete_coordinate_button;
    private Button edit_coordinate_button;
    private Button add_track_button;
    private Button finish_edit_track_butt;
    private Button save_track;
    private Button continue_editing;
    private Button edit_tracks_button;
    private TextView counter_coordinates;

    /*Count how many coordinates selected for creating a track (max of 25 coordinates)*/
    private int count_coordinates_selected = 0;
    private ArrayList<Double> track_coordinates = new ArrayList<>();
    private ArrayList<Marker> track_markers = new ArrayList<>();
    private ArrayList<Position> track_positions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*Mapbox and firebase initializations*/
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_editor_panel);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new myOnMapReadyCallback());

        database = FirebaseDatabase.getInstance();
        coordinates = database.getReference("coordinates");

        /*Buttons for different edit map modes*/
        add_coordinate_button = (Button)findViewById(R.id.addCoordinateButt);
        delete_coordinate_button = (Button)findViewById(R.id.deleteCoordinateButt);
        edit_coordinate_button = (Button)findViewById(R.id.editCoordinateButt);
        edit_tracks_button = (Button)findViewById(R.id.editTrackButton);
        add_track_button = (Button)findViewById(R.id.addTrackButt);
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
                    add_track_button.setText(R.string.add_track_button);
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

                    add_track_button.setText(R.string.cancel_add_track_butt);
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
                Intent i = new Intent(EditorPanelActivity.this, CreateNewTrack.class);
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
        }
    }

    private void showAllCoordinates() {
        /*Reading one time from the database, we get the coordinates map list*/
        coordinates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> coordinatesMap = (Map<String, Object>)dataSnapshot.getValue();
                if(coordinatesMap == null)
                   return;
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
        public boolean onMarkerClick(@NonNull Marker marker) {
            if(DELETE_COORDINATE_MODE){
                dialogDeleteCoordinate(marker);
                return true;
            }
            if(EDIT_COORDINATE_MODE){
                dialogEditCoordinate(marker);
                return true;
            }
            if(ADD_TRACK_MODE){
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
                    return false;
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
                Intent i = new Intent(EditorPanelActivity.this, EditCoordinate.class);
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
        map.removeMarker(marker);
    }

    /*Dialog function to ask the editor if he wants to add new coordinate with details.
    * if yes, we are directed to the creating new coordinate activity*/
    private void dialogAddNewCoordinate(final LatLng point) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditorPanelActivity.this);
        builder.setMessage(getResources().getString(R.string.dialog_add_new_coordinate));


        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent i = new Intent(EditorPanelActivity.this, CreateNewCoordinate.class);
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
