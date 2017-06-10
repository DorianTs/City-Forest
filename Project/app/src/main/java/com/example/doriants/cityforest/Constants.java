package com.example.doriants.cityforest;

import com.mapbox.services.commons.models.Position;

public final class Constants {

    public static final Position DEFAULT_JERUSALEM_COORDINATE
            = Position.fromCoordinates(35.207089, 31.771559);

    public static final String CHOSEN_COORDINATE = "Chosen Coordinate";
    public static final String NEW_COORDINATE_ID = "Coordinate ID";
    public static final String IS_COORDINATE_MAP_CREATED = "Is coordinate map created";
    public static final String COORDINATE_KEY = "Coordinate key";
    public static final String CHOSEN_TRACK = "Chosen Track";
    public static final String TRACK_EDIT = "Track Edit";
    public static final String SELECTED_TRACK = "Selected Track";
    public static final String CREATED_COORDINATE_FOR_ZOOM = "Created coordinate for zoom";
    public static final String EDITED_COORDINATE_FOR_ZOOM = "Edited coordinate for zoom";

    public static final String SEARCH_RESULT_QUERY = "Search Result query";
    public static final String Q_STARTING_POINT = "Starting point";
    public static final String Q_ENDING_POINT = "Ending point";
    public static final String Q_LEVEL = "Level";
    public static final String Q_SEASON = "Season";
    public static final String Q_HAS_WATER = "Has water";
    public static final String Q_SUITABLE_FOR_BIKES = "Suitable for bikes";
    public static final String Q_SUITABLE_FOR_DOGS = "Suitable for dogs";
    public static final String Q_SUITABLE_FOR_FAMILIES = "Suitable for families";
    public static final String Q_IS_ROMANTIC = "Is romantic";


    public static final int NEW_COORDINATE = 100;
    public static final int COORDINATE_CREATED = 101;
    public static final int EDIT_COORDINATE = 102;
    public static final int COORDINATE_EDITED = 103;
    public static final int MAX_NUM_OF_TRACK_COORDINATES = 25;
    public static final int NEW_TRACK = 200;
    public static final int TRACK_CREATED = 201;
    public static final int TRACK_EDITED = 202;

    public static boolean ADD_COORDINATE_MODE = false;
    public static boolean DELETE_COORDINATE_MODE = false;
    public static boolean EDIT_COORDINATE_MODE = false;
    public static boolean ADD_TRACK_MODE = false;
    public static boolean FINISH_EDIT_TRACK_MODE = false;

    public static int ROUTE_LINE_WIDTH = 4;
    public static int ZOOM_LEVEL_CURRENT_LOCATION = 13;
    public static int ZOOM_LEVEL_MARKER_CLICK = 16;
    public static int RC_SIGN_IN = 1;

}
