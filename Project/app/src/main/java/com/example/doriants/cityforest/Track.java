package com.example.doriants.cityforest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.services.commons.models.Position;
import com.mapbox.services.directions.v5.models.DirectionsRoute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Track {

    private String route;
    private String db_key;
    private String track_name;
    private String starting_point;
    private String ending_point;
    private double duration;
    private double length;
    private String level;
    private String season;
    private boolean has_water;
    private boolean suitable_for_bikes;
    private boolean suitable_for_dogs;
    private boolean suitable_for_families;
    private boolean is_romantic;
    private ArrayList<Marker> points_of_interest;
    private String additional_info;

    public Track(){

    }

    public Track(String route, String db_key, String track_name, String starting_point,
                 String ending_point, double duration, double length, String level,
                 String season, boolean has_water, boolean suitable_for_bikes,
                 boolean suitable_for_dogs, boolean suitable_for_families, boolean is_romantic,
                 String additional_info){

        this.route = route;
        this.db_key = db_key;
        this.track_name = track_name;
        this.starting_point = starting_point;
        this.ending_point = ending_point;
        this.duration = duration;
        this.length = length;
        this.level = level;
        this.season = season;
        this.has_water = has_water;
        this.suitable_for_bikes = suitable_for_bikes;
        this.suitable_for_dogs = suitable_for_dogs;
        this.suitable_for_families = suitable_for_families;
        this.is_romantic = is_romantic;
        this.additional_info = additional_info;
    }

    /*building the JSON branch in the database that will include the track*/
    public Map<String, Object> toMap(){

        HashMap<String, Object> result = new HashMap<>();
        result.put("route", this.route);
        result.put("key", this.db_key);
        result.put("track_name", this.track_name);
        result.put("starting_point", this.starting_point);
        result.put("ending_point", this.ending_point);
        result.put("duration", this.duration);
        result.put("length", this.length);
        result.put("level", this.level);
        result.put("season", this.season);
        result.put("has_water", this.has_water);
        result.put("suitable_for_bikes", this.suitable_for_bikes);
        result.put("suitable_for_dogs", this.suitable_for_dogs);
        result.put("suitable_for_families", this.suitable_for_families);
        result.put("is_romantic", this.is_romantic);
        result.put("additional_info", this.additional_info);

        return result;
    }

    /*public String castRouteToJson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        String json = gson.toJson(this.route, DirectionsRoute.class);
        return json;
    }*/

    //=========================Getters & Setters=========================//

    public String getRoute(){
        return route;
    }
    public String getDb_key(){
        return db_key;
    }
    public String getTrack_name(){
        return track_name;
    }
    public String getStarting_point(){
        return starting_point;
    }
    public String getEnding_point(){
        return ending_point;
    }
    public double getDuration(){
        return duration;
    }
    public double getLength(){
        return length;
    }
    public String getLevel(){
        return level;
    }
    public String getSeason(){
        return season;
    }
    public boolean getHas_water(){
        return has_water;
    }
    public boolean getSuitable_for_bikes(){
        return suitable_for_bikes;
    }
    public boolean getSuitable_for_dogs(){
        return suitable_for_dogs;
    }
    public boolean getSuitable_for_families(){
        return suitable_for_families;
    }
    public boolean getIs_romantic(){
        return is_romantic;
    }
    public String getAdditional_info(){
        return additional_info;
    }

    public void setRoute(String route){
        this.route = route;
    }
    public void setDb_key(String db_key){
        this.db_key = db_key;
    }
    public void setTrack_name(String track_name){
        this.track_name = track_name;
    }
    public void setStarting_point(String starting_point){
        this.starting_point = starting_point;
    }
    public void setEnding_point(String ending_point){
        this.ending_point = ending_point;
    }
    public void setDuration(double duration){
        this.duration = duration;
    }
    public void setLength(double length){
        this.length = length;
    }
    public void setLevel(String level){
        this.level = level;
    }
    public void setSeason(String season){
        this.season = season;
    }
    public void setHas_water(boolean has_water){
        this.has_water = has_water;
    }
    public void setSuitable_for_bikes(boolean suitable_for_bikes){
        this.suitable_for_bikes = suitable_for_bikes;
    }
    public void setSuitable_for_dogs(boolean suitable_for_dogs){
        this.suitable_for_dogs = suitable_for_dogs;
    }
    public void setSuitable_for_families(boolean suitable_for_families){
        this.suitable_for_families = suitable_for_families;
    }
    public void setIs_romantic(boolean is_romantic){
        this.is_romantic = is_romantic;
    }
    public void setAdditional_info(String additional_info){
        this.additional_info = additional_info;
    }
    
    //========================= END =========================//
}
