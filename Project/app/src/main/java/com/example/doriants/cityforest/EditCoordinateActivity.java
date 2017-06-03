package com.example.doriants.cityforest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import static com.example.doriants.cityforest.Constants.COORDINATE_CREATED;
import static com.example.doriants.cityforest.Constants.COORDINATE_KEY;

public class EditCoordinateActivity extends AppCompatActivity {

    private static final String TAG = "db_on_change";
    private FirebaseDatabase database;
    private DatabaseReference coordinates;
    private String coordinateKey;
    private EditText titleField;
    private EditText snippetField;
    private Button updateButt;
    private Button cancelButt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_coordinate);

        database = FirebaseDatabase.getInstance();
        coordinates = database.getReference("coordinates");

        Intent i = getIntent();
        coordinateKey = i.getStringExtra(COORDINATE_KEY);

        titleField = (EditText)findViewById(R.id.titleField);
        snippetField = (EditText)findViewById(R.id.SummaryField);
        updateButt = (Button)findViewById(R.id.updateButt);
        cancelButt = (Button)findViewById(R.id.cancelButt);
        updateButt.setOnClickListener(new MyClickListener());
        cancelButt.setOnClickListener(new MyClickListener());

        updateScreenValues(coordinateKey);
    }

    private class MyClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v.getId() == updateButt.getId()){
                updateDatabase();

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

    private void updateDatabase() {
        coordinates.child(coordinateKey).child("title").setValue(titleField.getText().toString());
        coordinates.child(coordinateKey).child("snippet").setValue(snippetField.getText().toString());
    }

    private void updateScreenValues(final String key){
        coordinates.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> coordinatesMap = (Map<String, Object>)dataSnapshot.getValue();
                if(coordinatesMap == null)
                    return;
                Map<String, Object> cor = ((Map<String, Object>)coordinatesMap.get(key));
                titleField.setText((String)cor.get("title"));
                snippetField.setText((String)cor.get("snippet"));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
