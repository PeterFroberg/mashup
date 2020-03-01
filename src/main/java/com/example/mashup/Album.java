package com.example.mashup;

public class Album {
    private String title;
    private String id;
    private String image;

    Album(String title){
        this.title = title;
    }

    Album(String title, String id, String image){
        this.title = title;
        this.id = id;
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
