package com.example.doriants.cityforest;

import java.util.HashMap;
import java.util.Map;



public class PointOfInterest extends Coordinate {

    private String type;
    private long logo;

    public PointOfInterest(){

    }

    public PointOfInterest(double coX, double coY,
                           String title, String snippet, String type){
        super(coX, coY, title, snippet);

        this.type = type;

        setLogo(type);
    }

    /*building the JSON branch in the database that will include the coordinate*/
    public Map<String, Object> toMap(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("position", castPositionToJson());
        result.put("title", this.title);
        result.put("snippet", this.snippet);
        result.put("type", this.type);
        result.put("logo", this.logo);

        return result;
    }

    public String getType(){
        return type;
    }

    public void setType(String type){
        this.type = type;
        setLogo(type);
    }

    public long getLogo(){
        return this.logo;
    }

    public void setLogo(int logo){
        this.logo = logo;
    }
    public void setLogo(String type){
        if(type.equals("תחנת רכבת")){
            logo = R.mipmap.ic_tram_black_24dp;
        }
        else if(type.equals("תחנת אוטובוס")){
            logo = R.mipmap.ic_directions_bus_black_24dp;
        }
        else if(type.equals("אתר היסטורי")){
            logo = R.mipmap.ic_change_history_black_24dp;
        }
        else if(type.equals("בית קפה")){
            logo = R.mipmap.ic_local_cafe_black_24dp;
        }
        else logo = -1;
    }

    public static Long whatIsTheLogoForType(String type){
        if(type.equals("תחנת רכבת")){
            return (long)R.mipmap.ic_tram_black_24dp;
        }
        else if(type.equals("תחנת אוטובוס")){
            return (long)R.mipmap.ic_directions_bus_black_24dp;
        }
        else if(type.equals("אתר היסטורי")){
            return (long)R.mipmap.ic_change_history_black_24dp;
        }
        else if(type.equals("בית קפה")){
            return (long)R.mipmap.ic_local_cafe_black_24dp;
        }
        else return (long) -1;
    }


}
