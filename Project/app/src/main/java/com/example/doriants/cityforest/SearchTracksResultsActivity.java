package com.example.doriants.cityforest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

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
import static com.example.doriants.cityforest.Constants.SELECTED_TRACK;

public class SearchTracksResultsActivity extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference tracks;
    private ListView tracks_list;

    private String q_starting_point;
    private String q_ending_point;
    private String q_track_level;
    private String q_season;
    private boolean q_has_water;
    private boolean q_suitable_for_bikes;
    private boolean q_suitable_for_dogs;
    private boolean q_suitable_for_families;
    private boolean q_is_romantic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_tracks_results);

        database = FirebaseDatabase.getInstance();
        tracks = database.getReference("tracks");

        tracks_list = (ListView)findViewById(R.id.tracks_list);

        Intent i = getIntent();
        q_starting_point = i.getStringExtra(Q_STARTING_POINT);
        q_ending_point = i.getStringExtra(Q_ENDING_POINT);
        q_track_level = i.getStringExtra(Q_LEVEL);
        q_season = i.getStringExtra(Q_SEASON);
        q_has_water = i.getBooleanExtra(Q_HAS_WATER, false);
        q_suitable_for_bikes = i.getBooleanExtra(Q_SUITABLE_FOR_BIKES, false);
        q_suitable_for_dogs = i.getBooleanExtra(Q_SUITABLE_FOR_DOGS, false);
        q_suitable_for_families = i.getBooleanExtra(Q_SUITABLE_FOR_FAMILIES, false);
        q_is_romantic = i.getBooleanExtra(Q_IS_ROMANTIC, false);

        //Query query = tracks.orderByChild("starting_point").equalTo("הר הרצל");
        //Query q2 = tracks.orderByChild("suitable_for_dogs").equalTo(true);

        MyFirebaseListAdapter adapter = new MyFirebaseListAdapter(this,Track.class,
                R.layout.track_list_view, tracks);
        tracks_list.setAdapter(adapter);

        tracks_list.setOnItemClickListener(new ItemClickListener());

    }


    private class ItemClickListener implements android.widget.AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Track track = (Track) parent.getItemAtPosition(position);

            Intent i = new Intent(SearchTracksResultsActivity.this, SelectedTrack.class);
            i.putExtra(SELECTED_TRACK, track.getDb_key());
            startActivity(i);
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
                //i = new Intent(this, AboutUs.class);
                //startActivity(i);
                return true;

            case R.id.contactUsActivity:
                //i = new Intent(this, ContactUs.class);
                //startActivity(i);
                return true;

            case R.id.homeActivity:
                //i = new Intent(this, Home.class);
                //startActivity(i);
                return true;

            case R.id.tracksActivity:
                //i = new Intent(this, Tracks.class);
                //startActivity(i);
                return true;

            case R.id.userGuideActivity:
                //i = new Intent(this, UserGuide.class);
                //startActivity(i);
                return true;

            case R.id.searchTracksActivity:
                //i = new Intent(this, SearchTracksActivity.class);
                //startActivity(i);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
