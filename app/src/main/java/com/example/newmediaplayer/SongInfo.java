package com.example.newmediaplayer;


import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SongInfo extends AppCompatActivity {

    ImageView songImage;
    TextView songName, songArtist;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_info);

        songImage=findViewById(R.id.songImageInfo);
        songName=findViewById(R.id.songNameInfo);
        songArtist=findViewById(R.id.songArtistInfo);

        Bundle bundle=getIntent().getExtras();
        if(bundle!=null)
        {
            String resId=bundle.getString("songImage");
            String nameId=bundle.getString("songName");
            String artistId=bundle.getString("songArtist");
            Glide
                    .with(this)
                    .load(resId)
                    .centerCrop()
                    .into(songImage);
            songName.setText(nameId);
            songArtist.setText(artistId);
        }


//        try {
//            FileInputStream fileInputStream=openFileInput("songInfo");
//            ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream);
//            Song song=(Song)objectInputStream.readObject();
//            objectInputStream.close();
//
//            songName.setText(song.getSongName());
//            songArtist.setText(song.getSongArtist());
//            Glide
//                    .with(this)
//                    .load(song.getPhotoResId())
//                    .centerCrop()
//                    .into(songImage);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }


    }
}
