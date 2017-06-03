package com.example.doriants.cityforest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static com.example.doriants.cityforest.Constants.Q_ENDING_POINT;
import static com.example.doriants.cityforest.Constants.Q_HAS_WATER;
import static com.example.doriants.cityforest.Constants.Q_IS_ROMANTIC;
import static com.example.doriants.cityforest.Constants.Q_LEVEL;
import static com.example.doriants.cityforest.Constants.Q_SEASON;
import static com.example.doriants.cityforest.Constants.Q_STARTING_POINT;
import static com.example.doriants.cityforest.Constants.Q_SUITABLE_FOR_BIKES;
import static com.example.doriants.cityforest.Constants.Q_SUITABLE_FOR_DOGS;
import static com.example.doriants.cityforest.Constants.Q_SUITABLE_FOR_FAMILIES;

public class AdvancedSearchTracksActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference tracks;

    private Spinner starting_point;
    private Spinner ending_point;
    private Spinner track_level;
    private Spinner season;
    private CheckBox has_water;
    private CheckBox suitable_for_bikes;
    private CheckBox suitable_for_families;
    private CheckBox suitable_for_dogs;
    private CheckBox is_romantic;

    private Button search_button;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_search_tracks);

        database = FirebaseDatabase.getInstance();
        tracks = database.getReference("tracks");

        starting_point = (Spinner)findViewById(R.id.startingPoint);
        ending_point = (Spinner)findViewById(R.id.endingPoint);
        track_level = (Spinner)findViewById((R.id.trackLevel));
        season = (Spinner)findViewById(R.id.season);
        has_water = (CheckBox)findViewById(R.id.hasWaterCheckbox);
        suitable_for_bikes = (CheckBox)findViewById(R.id.suitableForBikesCheckbox);
        suitable_for_families = (CheckBox)findViewById(R.id.suitableForFamiliesCheckbox);
        suitable_for_dogs = (CheckBox)findViewById(R.id.suitableForDogsCheckbox);
        is_romantic = (CheckBox)findViewById(R.id.isRomanticCheckbox);
        search_button = (Button)findViewById(R.id.searchButton);

        initiateSpinner(starting_point, R.array.train_stations);
        initiateSpinner(ending_point, R.array.train_stations);
        initiateSpinner(track_level, R.array.track_level);
        initiateSpinner(season, R.array.season);

        search_button.setOnClickListener(new ClickListener());
    }

    /*In order to be able to sign out from the logged in account, I have to
        * check who is the logged in user.*/
    @Override
    protected void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
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

    private class ClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if(v.getId() == search_button.getId()){

                /*Currently, we know that firebase database is able to do filters about only
                * one parameter of track, that's why we will not have the ability right now to filter
                * tracks with more than one filter.
                *
                * That's why I want to prepare to the time when google and firebase will have
                * software update and then we can make as many filters as we want.*/

                Intent i = new Intent(AdvancedSearchTracksActivity.this, SearchTracksResultsActivity.class);

                if(! starting_point.getSelectedItem().toString().equals(getResources().getString(R.string.choose_a_station)))
                    i.putExtra(Q_STARTING_POINT, starting_point.getSelectedItem().toString());
                if(! ending_point.getSelectedItem().toString().equals(getResources().getString(R.string.choose_a_station)))
                    i.putExtra(Q_ENDING_POINT, ending_point.getSelectedItem().toString());

                if(! track_level.getSelectedItem().toString().equals(getResources().getString(R.string.choose_a_level)))
                    i.putExtra(Q_LEVEL, track_level.getSelectedItem().toString());

                i.putExtra(Q_SEASON, season.getSelectedItem().toString());
                i.putExtra(Q_HAS_WATER, has_water.isChecked());
                i.putExtra(Q_SUITABLE_FOR_BIKES, suitable_for_bikes.isChecked());
                i.putExtra(Q_SUITABLE_FOR_DOGS, suitable_for_dogs.isChecked());
                i.putExtra(Q_SUITABLE_FOR_FAMILIES, suitable_for_families.isChecked());
                i.putExtra(Q_IS_ROMANTIC, is_romantic.isChecked());

                startActivity(i);

            }
        }
    }




















    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i;
        switch(item.getItemId()){
            case R.id.aboutActivity:
                i = new Intent(this, AboutUsActivity.class);
                startActivity(i);
                return true;

            case R.id.contactUsActivity:
                i = new Intent(this, ContactUsActivity.class);
                startActivity(i);
                return true;

            case R.id.homeActivity:
                i = new Intent(this, Home.class);
                startActivity(i);
                return true;

            case R.id.userGuideActivity:
                i = new Intent(this, UserGuideActivity.class);
                startActivity(i);
                return true;

            case R.id.searchTracksActivity:
                return true;

            case R.id.makeOwnTrackActivity:
                i = new Intent(this, MakeOwnTrackActivity.class);
                startActivity(i);
                return true;

            case R.id.tracksActivity:
                i = new Intent(this, TracksActivity.class);
                startActivity(i);
                return true;

            case R.id.signOut:
                signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /*Method signs out user's google account*/
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        Intent i = new Intent(AdvancedSearchTracksActivity.this, SignInActivity.class);
                        startActivity(i);
                    }});
    }
}
