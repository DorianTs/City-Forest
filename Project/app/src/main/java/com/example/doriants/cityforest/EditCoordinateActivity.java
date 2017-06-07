package com.example.doriants.cityforest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.HashMap;
import java.util.Map;

import static com.example.doriants.cityforest.Constants.COORDINATE_CREATED;
import static com.example.doriants.cityforest.Constants.COORDINATE_KEY;

public class EditCoordinateActivity extends AppCompatActivity {

    private static final String TAG = "db_on_change";
    private FirebaseDatabase database;
    private DatabaseReference coordinates;
    private DatabaseReference points_of_interest;
    private String coordinateKey;
    private EditText titleField;
    private EditText snippetField;
    private Button updateButt;
    private Button cancelButt;
    private CheckBox isPointOfInterest;
    private Spinner typeOfPoint;

    /*This parameter helps me know whether the edited point is regular coordinate perhaps
    * a point of interest*/
    private boolean was_point_of_interest = false;
    private LatLng coordinateLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_coordinate);

        database = FirebaseDatabase.getInstance();
        coordinates = database.getReference("coordinates");
        points_of_interest = database.getReference("points_of_interest");

        Intent i = getIntent();
        coordinateKey = i.getStringExtra(COORDINATE_KEY);

        titleField = (EditText)findViewById(R.id.titleField);
        snippetField = (EditText)findViewById(R.id.SummaryField);
        isPointOfInterest = (CheckBox)findViewById(R.id.isPointOfInterestCheckbox);
        typeOfPoint = (Spinner)findViewById(R.id.typeOfPoint);
        updateButt = (Button)findViewById(R.id.updateButt);
        cancelButt = (Button)findViewById(R.id.cancelButt);
        updateButt.setOnClickListener(new MyClickListener());
        cancelButt.setOnClickListener(new MyClickListener());

        updateScreenValues();
        isPointOfInterest.setClickable(false);
    }

    private void initiateSpinner(Spinner spinner,  int spinner_type, String value){
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                spinner_type, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        if(value != null)
            spinner.setSelection(adapter.getPosition(value));
    }


    private class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v.getId() == updateButt.getId()){

                if(isPointOfInterest.isChecked()){
                    boolean canSave = checkFields();
                    if(canSave){
                        updateDatabaseForPointOfInterest();
                    }
                    else
                        return;
                }
                else
                    updateDatabaseForCoordinate();

                Intent i = new Intent(EditCoordinateActivity.this, EditorPanelActivity.class);
                setResult(COORDINATE_CREATED);
                startActivity(i);
            }
            // else, go back to last page you came from
            if(v.getId() == cancelButt.getId()){
                onBackPressed();
            }
        }
    }

    private void updateDatabaseForCoordinate() {
        /*If it wasn't a point of interest, we should just update the coordinate in db */
        if(!was_point_of_interest){
            coordinates.child(coordinateKey).child("title").setValue(titleField.getText().toString());
            coordinates.child(coordinateKey).child("snippet").setValue(snippetField.getText().toString());
        }
        /*Else, it was a point of interest, so we need first to delete the point from the db
        * and create new coordinate*/
        /*else{
            points_of_interest.child(coordinateKey).removeValue();

            Coordinate coordinate = new Coordinate(
                    coordinateLatLng.getLongitude(),
                    coordinateLatLng.getLatitude(),
                    titleField.getText().toString(),
                    snippetField.getText().toString());


            Map<String, Object> coordinateMap = coordinate.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(coordinateKey, coordinateMap);
            coordinates.updateChildren(childUpdates);
        }*/
    }

    private void updateDatabaseForPointOfInterest(){
        /*if it was a point of interest and I want only to change it's values, update the db*/
        if(was_point_of_interest){
            points_of_interest.child(coordinateKey).child("title").setValue(titleField.getText().toString());
            points_of_interest.child(coordinateKey).child("snippet").setValue(snippetField.getText().toString());
            points_of_interest.child(coordinateKey).child("type").setValue(typeOfPoint.getSelectedItem().toString());

            long logo = PointOfInterest.whatIsTheLogoForType(typeOfPoint.getSelectedItem().toString());
            points_of_interest.child(coordinateKey).child("logo").setValue(logo);
        }
        /*Else, it was a coordinate before and now we want to make it a point of interest,
        * so we need to delete from coordinates reference the coordinate and make a new
        * point of interest instead*/
        /*else{
            coordinates.child(coordinateKey).removeValue();

            PointOfInterest point_of_interest = new PointOfInterest(
                    coordinateLatLng.getLongitude(),
                    coordinateLatLng.getLatitude(),
                    titleField.getText().toString(),
                    snippetField.getText().toString(),
                    typeOfPoint.getSelectedItem().toString());


            Map<String, Object> coordinateMap = point_of_interest.toMap();

            Map<String, Object> childUpdates = new HashMap<>();
            childUpdates.put(coordinateKey, coordinateMap);
            points_of_interest.updateChildren(childUpdates);
        }*/
    }


    private void updateScreenValues(){
        coordinates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> coordinatesMap = (Map<String, Object>)dataSnapshot.getValue();
                if(coordinatesMap == null){
                    isPointOfInterest.setChecked(true);
                    typeOfPoint.setVisibility(View.VISIBLE);
                    was_point_of_interest = true;
                    updateScreenPointOfInterestValue();
                    return;
                }

                Map<String, Object> cor = ((Map<String, Object>)coordinatesMap.get(coordinateKey));

                /*if cor is not null, it means that it is a regular coordinate*/
                if(cor != null){
                    titleField.setText((String)cor.get("title"));
                    snippetField.setText((String)cor.get("snippet"));
                    initiateSpinner(typeOfPoint, R.array.points_of_interest, null);
                    was_point_of_interest = false;

                    coordinateLatLng = retreiveLatLngFromJson((String)cor.get("position"));
                }
                /*If cor is null, we know for sure that it's a point of interest and we should
                * check for it in the other set in database for points*/
                else{
                    isPointOfInterest.setChecked(true);
                    typeOfPoint.setVisibility(View.VISIBLE);
                    was_point_of_interest = true;
                    updateScreenPointOfInterestValue();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateScreenPointOfInterestValue() {
        points_of_interest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> pointsMap = (Map<String, Object>)dataSnapshot.getValue();
                if(pointsMap == null)
                    return;
                Map<String, Object> point = ((Map<String, Object>)pointsMap.get(coordinateKey));
                titleField.setText((String)point.get("title"));
                snippetField.setText((String)point.get("snippet"));
                coordinateLatLng = retreiveLatLngFromJson((String)point.get("position"));
                initiateSpinner(typeOfPoint, R.array.points_of_interest, (String)point.get("type"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private boolean checkFields() {
        if(titleField.getText().toString().equals("")){
            Toast.makeText(this, R.string.title_field_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        if(typeOfPoint.getSelectedItem().toString().equals(getResources().getString(R.string.choose_type_of_point))){
            Toast.makeText(this, R.string.choose_type_of_point_empty, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private LatLng retreiveLatLngFromJson(String stringExtra) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        LatLng obj = gson.fromJson(stringExtra, LatLng.class);
        return obj;
    }
}
