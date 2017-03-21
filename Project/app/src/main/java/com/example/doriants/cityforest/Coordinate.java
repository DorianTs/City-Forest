package com.example.doriants.cityforest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mapbox.services.commons.models.Position;


import java.util.HashMap;
import java.util.Map;

public class Coordinate {

    private Position pos = null;
    private String title;
    private String snippet;


    public Coordinate(){
    }

    public Coordinate(double coX, double coY,
                      String title, String snippet){

        this.pos = Position.fromCoordinates(coY, coX);
        this.title = title;
        this.snippet = snippet;
    }

    /*building the JSON branch in the database that will include the coordinate*/
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("position", castPositionToJson());
        result.put("title", this.title);
        result.put("snippet", this.snippet);

        return result;
    }

    //=========================Getters & Setters=========================//
    public Position getPos(){
        return this.pos;
    }
    public String getTitle(){
        return this.title;
    }
    public String getSnippet(){
        return this.snippet;
    }

    public void setPos(double coX, double coY){
        this.pos = Position.fromCoordinates(coY, coX);
    }
    public void setTitle(String title){
        this.title = title;
    }
    public void setSnippet(String snippet){
        this.snippet = snippet;
    }
    //========================= END =========================//

    public String castPositionToJson(){
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.serializeSpecialFloatingPointValues();

        Gson gson = gsonBuilder.create();
        String json = gson.toJson(this.pos, Position.class);
        return json;
    }

}
