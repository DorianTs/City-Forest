package com.example.doriants.cityforest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.example.doriants.cityforest.Constants.CHOSEN_COORDINATE;
import static com.example.doriants.cityforest.Constants.COORDINATE_CREATED;
import static com.example.doriants.cityforest.Constants.IS_COORDINATE_MAP_CREATED;
import static com.example.doriants.cityforest.Constants.NEW_COORDINATE_ID;

public class CreateNewCoordinate extends AppCompatActivity {

    private static final String TAG = "db_on_change";
    private FirebaseDatabase database;
    private DatabaseReference coordinates;
    private LatLng chosenCoordinateLatLng;
    private EditText titleField;
    private EditText snippetField;
    private Button saveButt;
    private Button cancelButt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_coordinate);

        database = FirebaseDatabase.getInstance();
        coordinates = database.getReference("coordinates");

        Intent i = getIntent();
        chosenCoordinateLatLng = retreiveLatLngFromJson(i.getStringExtra(CHOSEN_COORDINATE));
        titleField = (EditText)findViewById(R.id.titleField);
        snippetField = (EditText)findViewById(R.id.SummaryField);
        saveButt = (Button)findViewById(R.id.saveButt);
        cancelButt = (Button)findViewById(R.id.cancelButt);
        saveButt.setOnClickListener(new MyClickListener());
        cancelButt.setOnClickListener(new MyClickListener());
    }


    private class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v.getId() == saveButt.getId()){
                writeNewCoordinate();

                Intent i = new Intent(CreateNewCoordinate.this, EditorPanelActivity.class);
                setResult(COORDINATE_CREATED);
                startActivity(i);
            }
            // else, go back to last page you came from
            if(v.getId() == cancelButt.getId()){
                onBackPressed();
            }
        }
    }

    private LatLng retreiveLatLngFromJson(String stringExtra) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        LatLng obj = gson.fromJson(stringExtra, LatLng.class);
        return obj;
    }




    /*Method writes to the Firebase db a new coordinate
    * with all the details*/
    private void writeNewCoordinate(){
        String key = hashFunction();

        Coordinate coordinate = new Coordinate(
                chosenCoordinateLatLng.getLongitude(),
                chosenCoordinateLatLng.getLatitude(),
                titleField.getText().toString(),
                snippetField.getText().toString());

        /*Converting our coordinate object to a map, that makes
        * the coordinate ready to be entered to the JSON tree*/
        Map<String, Object> coordinateMap = coordinate.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, coordinateMap);
        coordinates.updateChildren(childUpdates);
    }


    private String hashFunction() {
        double longitude = chosenCoordinateLatLng.getLongitude();
        //double latitude = chosenCoordinateLatLng.getLatitude();


        int hash = (int) (10000000*longitude);
        return "" + hash;
    }
}
