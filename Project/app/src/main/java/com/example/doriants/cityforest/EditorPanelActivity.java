package com.example.doriants.cityforest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.models.Position;

import static com.example.doriants.cityforest.Constants.CHOSEN_COORDINATE;
import static com.example.doriants.cityforest.Constants.DEFAULT_JERUSALEM_COORDINATE;
import static com.example.doriants.cityforest.Constants.NEW_COORDINATE;

public class EditorPanelActivity extends AppCompatActivity {

    private MapView mapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_editor_panel);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new myOnMapReadyCallback());
    }

    private class myOnMapReadyCallback implements OnMapReadyCallback {
        @Override
        public void onMapReady(MapboxMap mapboxMap) {
            mapboxMap.setOnMapClickListener(new MyOnMapClickListener());
            mapboxMap.setStyleUrl(Style.OUTDOORS);

            /*Showing the default position to the editor*/
            double[] cameraPosValue = new double[5];
            cameraPosValue[0] = DEFAULT_JERUSALEM_COORDINATE.getLatitude();
            cameraPosValue[1] = DEFAULT_JERUSALEM_COORDINATE.getLongitude();
            cameraPosValue[2] = 0;
            cameraPosValue[3] = 0;
            cameraPosValue[4] = 10;

            CameraPosition.Builder cpBuilder = new CameraPosition.Builder(cameraPosValue);
            CameraPosition MountHerzlCameraPosition = cpBuilder.build();
            mapboxMap.setCameraPosition(MountHerzlCameraPosition);

            //TODO: show all the coordinates that are currently in the db
        }
    }

    private class MyOnMapClickListener implements MapboxMap.OnMapClickListener{
        @Override
        public void onMapClick(@NonNull LatLng point) {
            /*When clicking on a location in the map,
            * we want to ask the editor if he wants to add a new coordinate
            * in the database for this specific location*/
            dialogAddNewCoordinate(point);
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
