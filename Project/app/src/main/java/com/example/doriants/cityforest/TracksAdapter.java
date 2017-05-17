package com.example.doriants.cityforest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TracksAdapter extends ArrayAdapter<Track> {

    public TracksAdapter(Context context, ArrayList<Track> tracks) {
        super(context, 0, tracks);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        // Get the data item for this position
        Track track = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.track_list_view, parent, false);
        }

        // Lookup view for data population
        TextView trackName = (TextView)convertView.findViewById(R.id.trackName);
        TextView startingPoint = (TextView)convertView.findViewById(R.id.startingPoint);
        TextView endingPoint = (TextView)convertView.findViewById(R.id.endingPoint);
        TextView trackLevel = (TextView)convertView.findViewById(R.id.trackLevel);
        TextView trackDistance = (TextView)convertView.findViewById(R.id.trackDistance);

        if(track != null) {
            trackName.setText(track.getTrack_name());
            startingPoint.setText(track.getStarting_point());
            endingPoint.setText(track.getEnding_point());
            trackLevel.setText(track.getLevel());

            String distance = "" + track.getLength();
            trackDistance.setText(distance);
        }

        // Return the completed view to render on screen
        return convertView;
    }

}
