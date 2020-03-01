package com.example.mashup;

import java.util.ArrayList;

public class WsResponse {
    private String mbid;
    private String name;
    private String Description = "";
    private ArrayList<Album> albums = new ArrayList<Album>();

    public WsResponse(String mbid) {
        this.mbid = mbid;
    }

    public WsResponse(String mbid, String name, ArrayList<Album> albums, String description){
        this.mbid = mbid;
    }

    public String getMbid() {
        return mbid;
    }

    public void setMbid(String mbid){
        this.mbid = mbid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public ArrayList<Album> getAlbums() {
        return albums;
    }
}
