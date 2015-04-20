package com.saugat.arbrowser;

/**
 * Created by DArKLoRD on 4/20/2015.
 */
public class poi {

    private int poiId;
    private String poiName;
    private Double poiLongitude;
    private Double poiLatitude;

    public poi(){

    }

    public poi(int id, String name, Double longitude, Double latitude){
        this.poiId = id;
        this.poiName = name;
        this.poiLatitude = latitude;
        this.poiLongitude = longitude;
    }

    public int getPoiId(){
        return poiId;
    }

    public String getPoiName(){
        return poiName;
    }

    public Double getPoiLongitude(){
        return  poiLongitude;
    }

    public Double getPoiLatitude(){
        return  poiLatitude;
    }

    public void setPoiId(int id){
        this.poiId = id;
    }

    public void setPoiName(String poiName){
        this.poiName = poiName;
    }

    public void setPoiLongitude(Double longitude ){
        this.poiLongitude = longitude;
    }

    public void setPoiLatitude(Double latitude){
        this.poiLatitude = latitude;
    }


}
