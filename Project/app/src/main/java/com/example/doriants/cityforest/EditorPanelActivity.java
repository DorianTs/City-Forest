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
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.android.geocoder.ui.GeocoderAutoCompleteView;

import java.util.Map;

import static com.example.doriants.cityforest.Constants.ADD_COORDINATE_MODE;
import static com.example.doriants.cityforest.Constants.ADD_TRACK_MODE;
import static com.example.doriants.cityforest.Constants.CHOSEN_COORDINATE;
import static com.example.doriants.cityforest.Constants.DEFAULT_JERUSALEM_COORDINATE;
import static com.example.doriants.cityforest.Constants.DELETE_COORDINATE_MODE;
import static com.example.doriants.cityforest.Constants.EDIT_COORDINATE_MODE;
import static com.example.doriants.cityforest.Constants.NEW_COORDINATE;

public class EditorPanelActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;
    private FirebaseDatabase database;
    private DatabaseReference coordinates;
    private Button add_coordinate_button;
    private Button delete_coordinate_button;
    private Button edit_coordinate_button;
    private Button add_track_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_editor_panel);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new myOnMapReadyCallback());

        database = FirebaseDatabase.getInstance();
        coordinates = database.getReference("coordinates");

        add_coordinate_button = (Button)findViewById(R.id.addCoordinateButt);
        delete_coordinate_button = (Button)findViewById(R.id.deleteCoordinateButt);
        edit_coordinate_button = (Button)findViewById(R.id.editCoordinateButt);
        add_track_button = (Button)findViewById(R.id.addTrackButt);

        ClickListener clickListener = new ClickListener();
        add_coordinate_button.setOnClickListener(clickListener);
        delete_coordinate_button.setOnClickListener(clickListener);
        edit_coordinate_button.setOnClickListener(clickListener);
        add_track_button.setOnClickListener(clickListener);

        ADD_COORDINATE_MODE = false;
        DELETE_COORDINATE_MODE = false;
        EDIT_COORDINATE_MODE = false;
        ADD_TRACK_MODE = false;
    }

    private class ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
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
                }
            }
        }
    }

    private class myOnMapReadyCallback implements OnMapReadyCallback {
        @Override
        public void onMapReady(MapboxMap mapboxMap) {
            map = mapboxMap;
            map.setOnMapClickListener(new MyOnMapClickListener());
            map.setStyleUrl(Style.OUTDOORS);
            showDefaultLocation();
            showAllCoordinates();
        }
    }

    private void showAllCoordinates() {
        coordinates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> coordinatesMap = (Map<String, Object>)dataSnapshot.getValue();
                if(coordinatesMap == null)
                   return;
                for (Map.Entry<String, Object> entry : coordinatesMap.entrySet())
                {
                    /*For each coordinate in the database, we want to create a new marker
                    * for it and to show the marker on the map*/
                    Map<String, Object> cor = ((Map<String, Object>) entry.getValue());
                    String positionJSON = (String) cor.get("position");
                    Position position = retrievePositionFromJson(positionJSON);

                    LatLng latlng = new LatLng(
                            position.getLongitude(),
                            position.getLatitude());
                    MarkerViewOptions markerViewOptions = new MarkerViewOptions()
                            .position(latlng)
                            .title((String)cor.get("title"))
                            .snippet((String)cor.get("snippet"));
                    map.addMarker(markerViewOptions);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        });

    }

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
        CameraPosition MountHerzlCameraPosition = cpBuilder.build();
        map.setCameraPosition(MountHerzlCameraPosition);
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

        }
    }

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

    public String castLatLngToJson(LatLng point){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        String json = gson.toJson(point, LatLng.class);
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
