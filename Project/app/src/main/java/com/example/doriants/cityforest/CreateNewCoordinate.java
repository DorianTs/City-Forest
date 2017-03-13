package com.example.doriants.cityforest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CreateNewCoordinate extends AppCompatActivity {

    private static final String TAG = "db_on_change";
    private static int cooID = 1;
    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_coordinate);

        database = FirebaseDatabase.getInstance();
        DatabaseReference coordinates = database.getReference("coordinates");

        Map<String, Object> coordinatesMap = new HashMap<>();


        Coordinate co1 = new Coordinate(""+cooID, 31.7736, 35.1640, "end point", "bla");
        Map<String, Object> coordinate = co1.toMap();
        coordinatesMap.put(""+cooID, coordinate);
        cooID++;

        Coordinate co2 = new Coordinate(""+cooID, 34.4351, 23.9892, "תחנת יפה נוף", "תחנה מיוחדת");
        Map<String, Object> coordinate2 = co2.toMap();
        coordinatesMap.put(""+cooID, coordinate2);



        coordinates.setValue(coordinatesMap);

        coordinates.addValueEventListener(new MyValueEventListener());
    }


    private class MyValueEventListener implements ValueEventListener{
        @Override
        public void onCancelled(DatabaseError databaseError) {
            // Failed to read value
            Log.w(TAG, "Failed to read value.", databaseError.toException());
        }

        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            // This method is called once with the initial value and again
            // whenever data at this location is updated.

        }
    }
}
