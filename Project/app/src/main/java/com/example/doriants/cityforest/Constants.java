package com.example.doriants.cityforest;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.services.commons.models.Position;

public final class Constants {

    public static final Position DEFAULT_JERUSALEM_COORDINATE
            = Position.fromCoordinates(35.207089, 31.771559);

    public static final String CHOSEN_COORDINATE = "Chosen Coordinate";
    public static final String NEW_COORDINATE_ID = "Coordinate ID";
    public static final String IS_COORDINATE_MAP_CREATED = "Is coordinate map created";
    public static final String COORDINATE_KEY = "Coordinate key";

    public static final int NEW_COORDINATE = 100;
    public static final int COORDINATE_CREATED = 101;
    public static final int MAX_NUM_OF_TRACK_COORDINATES = 25;

    public static boolean ADD_COORDINATE_MODE = false;
    public static boolean DELETE_COORDINATE_MODE = false;
    public static boolean EDIT_COORDINATE_MODE = false;
    public static boolean ADD_TRACK_MODE = false;
    public static boolean FINISH_EDIT_TRACK_MODE = false;

}
