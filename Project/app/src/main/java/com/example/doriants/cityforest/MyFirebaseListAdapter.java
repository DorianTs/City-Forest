package com.example.doriants.cityforest;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.database.Query;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


public class MyFirebaseListAdapter extends FirebaseListAdapter<Track> {

    /**
     * @param activity    The activity containing the ListView
     * @param modelClass  Firebase will marshall the data at a location into
     *                    an instance of a class that you provide
     * @param modelLayout This is the layout used to represent a single list item.
     *                    You will be responsible for populating an instance of the corresponding
     *                    view with the data from an instance of modelClass.
     * @param ref         The Firebase location to watch for data changes. Can also be a slice of a location,
     *                    using some combination of {@code limit()}, {@code startAt()}, and {@code endAt()}.
     */
    public MyFirebaseListAdapter(Activity activity, Class<Track> modelClass, int modelLayout, Query ref) {
        super(activity, modelClass, modelLayout, ref);
    }

    @Override
    protected void populateView(View v, Track track, int position) {

        // Lookup view for data population
        TextView trackName = (TextView)v.findViewById(R.id.trackName);
        TextView startingPoint = (TextView)v.findViewById(R.id.startingPoint);
        TextView endingPoint = (TextView)v.findViewById(R.id.endingPoint);
        TextView trackLevel = (TextView)v.findViewById(R.id.trackLevel);
        TextView trackDistance = (TextView)v.findViewById(R.id.trackDistance);

        String track_name = track.getTrack_name();
        if(track_name.equals(""))
            trackName.setText(R.string.no_track_name);
        else trackName.setText(track.getTrack_name());

        startingPoint.setText(track.getStarting_point());
        endingPoint.setText(track.getEnding_point());
        trackLevel.setText(track.getLevel());

        String distance = "" + track.getLength();
        trackDistance.setText(distance);
    }
}
