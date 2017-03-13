package com.example.doriants.cityforest;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.models.Position;

import static com.example.doriants.cityforest.Constants.DEFAULT_JERUSALEM_COORDINATE;

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
        mapView.setOnMapClickListener(new MyOnMapClickListener());
        //mapView.setOnMapClickListener(new MyOnMapClickListener());
    }

    private class myOnMapReadyCallback implements OnMapReadyCallback {
        @Override
        public void onMapReady(MapboxMap mapboxMap) {
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
