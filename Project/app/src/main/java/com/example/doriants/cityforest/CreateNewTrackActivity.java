package com.example.doriants.cityforest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.services.directions.v5.models.DirectionsRoute;

import java.util.HashMap;
import java.util.Map;

import static com.example.doriants.cityforest.Constants.CHOSEN_TRACK;
import static com.example.doriants.cityforest.Constants.TRACK_CREATED;


public class CreateNewTrackActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference tracks;
    private DirectionsRoute current_route;

    private EditText track_name_field;
    private Spinner starting_point;
    private Spinner ending_point;
    private EditText duration_field;
    private EditText distance_field;
    private Spinner track_level;
    private Spinner season;
    private CheckBox has_water;
    private CheckBox suitable_for_bikes;
    private CheckBox suitable_for_families;
    private CheckBox suitable_for_dogs;
    private CheckBox is_romantic;
    private EditText additional_info;

    private Button save_button;
    private Button cancel_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_track);

        database = FirebaseDatabase.getInstance();
        tracks = database.getReference("tracks");

        Intent i = getIntent();
        current_route = retreiveRouteFromJson(i.getStringExtra(CHOSEN_TRACK));

        track_name_field = (EditText)findViewById(R.id.trackNameField);
        starting_point = (Spinner)findViewById(R.id.startingPoint);
        ending_point = (Spinner)findViewById(R.id.endingPoint);
        duration_field = (EditText)findViewById(R.id.durationField);
        distance_field = (EditText)findViewById(R.id.distanceField);
        track_level = (Spinner)findViewById((R.id.trackLevel));
        season = (Spinner)findViewById(R.id.season);
        has_water = (CheckBox)findViewById(R.id.hasWaterCheckbox);
        suitable_for_bikes = (CheckBox)findViewById(R.id.suitableForBikesCheckbox);
        suitable_for_families = (CheckBox)findViewById(R.id.suitableForFamiliesCheckbox);
        suitable_for_dogs = (CheckBox)findViewById(R.id.suitableForDogsCheckbox);
        is_romantic = (CheckBox)findViewById(R.id.isRomanticCheckbox);
        additional_info = (EditText)findViewById(R.id.trackSummaryField);
        save_button = (Button)findViewById(R.id.saveButton);
        cancel_button = (Button)findViewById(R.id.cancelButton);


        initiateSpinner(starting_point, R.array.train_stations);
        initiateSpinner(ending_point, R.array.train_stations);
        initiateSpinner(track_level, R.array.track_level);
        initiateSpinner(season, R.array.season);

        duration_field.setText(""+(current_route.getDuration()/3600));
        distance_field.setText(""+(current_route.getDistance()/1000));

        save_button.setOnClickListener(new MyClickListener());
        cancel_button.setOnClickListener(new MyClickListener());
    }

    private class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v.getId() == save_button.getId()){
                boolean canSave = checkFields();
                if(canSave){
                    writeNewTrack();

                    Intent i = new Intent(CreateNewTrackActivity.this, EditorPanelActivity.class);
                    setResult(TRACK_CREATED);
                    startActivity(i);
                }
            }
            if(v.getId() == cancel_button.getId()){
                onBackPressed();
            }
        }
    }

    private boolean checkFields(){
        if(starting_point.getSelectedItem().toString().equals(getResources().getString(R.string.choose_a_station))
                || ending_point.getSelectedItem().toString().equals(getResources().getString(R.string.choose_a_station))){
            Toast.makeText(this, R.string.choose_station_empty, Toast.LENGTH_LONG).show();
            return false;
        }

        if(distance_field.getText().toString().equals("")){
            Toast.makeText(this, R.string.choose_distance_empty, Toast.LENGTH_LONG).show();
            return false;
        }

        if(track_level.getSelectedItem().toString().equals(getResources().getString(R.string.choose_a_level))){
            Toast.makeText(this, R.string.choose_level_empty, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void initiateSpinner(Spinner spinner,  int spinner_type){
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                spinner_type, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }

    private DirectionsRoute retreiveRouteFromJson(String stringExtra) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        DirectionsRoute obj = gson.fromJson(stringExtra, DirectionsRoute.class);
        return obj;
    }

    private void writeNewTrack(){

        String key = tracks.push().getKey();

        double duration = Double.parseDouble(duration_field.getText().toString());
        double distance = Double.parseDouble(distance_field.getText().toString());

        Track track = new Track(
                castRouteToJson(),
                key,
                track_name_field.getText().toString(),
                starting_point.getSelectedItem().toString(),
                ending_point.getSelectedItem().toString(),
                duration,
                distance,
                track_level.getSelectedItem().toString(),
                season.getSelectedItem().toString(),
                has_water.isChecked(),
                suitable_for_bikes.isChecked(),
                suitable_for_dogs.isChecked(),
                suitable_for_families.isChecked(),
                is_romantic.isChecked(),
                additional_info.getText().toString());

        /*Converting our track object to a map, that makes
        * the track ready to be entered to the JSON tree*/
        Map<String, Object> trackMap = track.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put(key, trackMap);
        tracks.updateChildren(childUpdates);
    }

    public String castRouteToJson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        String json = gson.toJson(this.current_route, DirectionsRoute.class);
        return json;
    }
}
