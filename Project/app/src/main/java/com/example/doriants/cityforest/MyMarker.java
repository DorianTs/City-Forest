package com.example.doriants.cityforest;

import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;

public class MyMarker {
    private String key;
    private MarkerViewOptions marker;


    public MyMarker(String key, MarkerViewOptions marker) {
        this.key = key;
        this.marker = marker;
    }
}
