package com.example.newmediaplayer;

import android.widget.Button;

import java.io.Serializable;

public class Song implements Serializable {
    private String songName;
    private String songArtist;
    private String photoResId;
    private String link;


    public Song(String songName, String songArtist, String photoResId, String link)  {
        this.songName = songName;
        this.songArtist = songArtist;
        this.photoResId = photoResId;
        this.link=link;
    }


    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    public String getPhotoResId() {
        return photoResId;
    }

    public void setPhotoResId(String photoResId) {
        this.photoResId = photoResId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}
