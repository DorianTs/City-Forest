package com.example.doriants.cityforest;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

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
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.commons.geojson.LineString;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.models.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.doriants.cityforest.Constants.DEFAULT_JERUSALEM_COORDINATE;
import static com.example.doriants.cityforest.Constants.ROUTE_LINE_WIDTH;
import static com.example.doriants.cityforest.Constants.TRACK_EDIT;

public class EditTracksActivity extends AppCompatActivity {

    private MapView mapView;
    private MapboxMap map;
    private FirebaseDatabase database;
    private DatabaseReference tracks;
    private PolylineOptions routeLine;

    private ListView track_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapboxAccountManager.start(this, getString(R.string.access_token));
        setContentView(R.layout.activity_edit_tracks);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new myOnMapReadyCallback());

        database = FirebaseDatabase.getInstance();
        tracks = database.getReference("tracks");

        track_list = (ListView) findViewById(R.id.track_list);

        track_list.setOnItemClickListener(new ItemClickListener());


        MyFirebaseListAdapter adapter = new MyFirebaseListAdapter(this,Track.class ,
                R.layout.track_list_view, tracks);
        track_list.setAdapter(adapter);
    }

    private void getAllTracksFromDb(final TracksAdapter adapter) {
       /*Reading one time from the database, we get the tracks map list*/
       tracks.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               Map<String, Object> tracksMap = (Map<String, Object>)dataSnapshot.getValue();
               if(tracksMap == null)
                   return;

               /*Iterating all the tracks in the list and adding them to the dedicated array list*/
               for (Map.Entry<String, Object> entry : tracksMap.entrySet()) {
                   Track track = convertMapToTrack(entry);
                   //adapter.add(track);
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {
           }
       });
    }

    private Track convertMapToTrack(Map.Entry<String, Object> entry) {
        Map<String, Object> trackMap = ((Map<String, Object>) entry.getValue());

        double duration = (double)trackMap.get("duration");
        double length = (double)trackMap.get("length");
        boolean has_water = (boolean)trackMap.get("has_water");
        boolean suitable_for_bikes = (boolean)trackMap.get("suitable_for_bikes");
        boolean suitable_for_dogs = (boolean)trackMap.get("suitable_for_dogs");
        boolean suitable_for_families = (boolean)trackMap.get("suitable_for_families");
        boolean is_romantic = (boolean)trackMap.get("is_romantic");

        return new Track((String)trackMap.get("route"),
                (String)trackMap.get("key"),
                (String)trackMap.get("track_name"),
                (String)trackMap.get("starting_point"),
                (String)trackMap.get("ending_point"),
                duration,
                length,
                (String)trackMap.get("level"),
                (String)trackMap.get("season"),
                has_water,
                suitable_for_bikes,
                suitable_for_dogs,
                suitable_for_families,
                is_romantic,
                (String)trackMap.get("additional_info"));
    }

    private class myOnMapReadyCallback implements OnMapReadyCallback {
        @Override
        public void onMapReady(MapboxMap mapboxMap) {
            map = mapboxMap;
            map.setStyleUrl(Style.OUTDOORS);
            showDefaultLocation();
        }
    }

    private class ItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Track track = (Track) parent.getItemAtPosition(position);

            dialogOptions(track);
        }

        private void dialogOptions(final Track track) {
            CharSequence options[] = new CharSequence[] {
                    getResources().getString(R.string.edit_options_see_on_map),
                    getResources().getString(R.string.edit_options_edit_track),
                    getResources().getString(R.string.edit_options_delete_track)};

            final AlertDialog.Builder builder = new AlertDialog.Builder(EditTracksActivity.this);
            builder.setTitle("Choose your action");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int selection) {
                    switch(selection){
                        case 0:{ // see on map selection
                            DirectionsRoute route = retrieveRouteFromJson(track.getRoute());
                            if(routeLine != null)
                                map.removePolyline(routeLine.getPolyline());
                            drawRoute(route);
                            break;
                        }
                        case 1:{ // edit the track
                            Intent i = new Intent(EditTracksActivity.this, EditTrack.class);
                            i.putExtra(TRACK_EDIT, track.getDb_key());
                            EditTracksActivity.this.startActivity(i);
                            break;
                        }
                        case 2:{ // delete the track
                            dialogDeleteTrack(track.getDb_key());
                            break;
                        }
                    }
                }
            });
            builder.show();
        }

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

    public DirectionsRoute retrieveRouteFromJson(String route) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        DirectionsRoute obj = gson.fromJson(route, DirectionsRoute.class);
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

    private void dialogDeleteTrack(final String key){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditTracksActivity.this);
        builder.setMessage(getResources().getString(R.string.dialog_delete_track));

        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                tracks.child(key).removeValue();
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

