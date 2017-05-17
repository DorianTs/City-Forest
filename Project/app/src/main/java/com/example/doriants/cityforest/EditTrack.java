package com.example.doriants.cityforest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.services.directions.v5.models.DirectionsRoute;

import static com.example.doriants.cityforest.Constants.TRACK_EDITED;

public class EditTrack extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference tracks;
    private Track track = null;

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

    private Button update_button;
    private Button cancel_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_track);

        database = FirebaseDatabase.getInstance();
        tracks = database.getReference("tracks");

        Intent i = getIntent();
        track = retreiveTrackFromJson(i.getStringExtra(TRACK_EDITED));

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
        update_button = (Button)findViewById(R.id.updateButton);
        cancel_button = (Button)findViewById(R.id.cancelButton);

        initiateSpinner(starting_point, R.array.train_stations);
        initiateSpinner(ending_point, R.array.train_stations);
        initiateSpinner(track_level, R.array.track_level);
        initiateSpinner(season, R.array.season);

        //update_button.setOnClickListener();
        //cancel_button.setOnClickListener();


        initiateScreenValues();
    }

    private void initiateScreenValues() {
        track_name_field.setText(track.getTrack_name());
        duration_field.setText(""+track.getDuration());
        distance_field.setText(""+track.getLength());
        has_water.setActivated(track.getHas_water());
        suitable_for_bikes.setActivated(track.getSuitable_for_bikes());
        suitable_for_dogs.setActivated(track.getSuitable_for_dogs());
        suitable_for_families.setActivated(track.getSuitable_for_families());
        is_romantic.setActivated(track.getIs_romantic());
        additional_info.setText(track.getAdditional_info());
    }

    private Track retreiveTrackFromJson(String stringExtra) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        Track obj = gson.fromJson(stringExtra, Track.class);
        return obj;
    }

    private void initiateSpinner(Spinner spinner,  int spinner_type){
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                spinner_type, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        spinner.setSelection(adapter.getPosition(track.getStarting_point()));
    }

}
