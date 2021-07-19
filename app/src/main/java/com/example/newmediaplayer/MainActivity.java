package com.example.newmediaplayer;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.app.Dialog;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity  {

    private boolean isPlay = false;
    ArrayList<Song> songs;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FloatingActionButton startButton = findViewById(R.id.play_btn);
        FloatingActionButton pauseButton = findViewById(R.id.pause_btn);
        final FloatingActionButton nextButton = findViewById(R.id.next_btn);
        FloatingActionButton prevButton = findViewById(R.id.prev_btn);
        FloatingActionButton addButton=findViewById(R.id.add_btn);


        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        songs = new ArrayList<>();
        sharedPreferences=getSharedPreferences("song_first", MODE_PRIVATE);
        Boolean firstTime=sharedPreferences.getBoolean("firstTime", true);
        if(firstTime) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            songs.add(new Song("one More Cup Of Coffee", "Bob dylan", "https://i.pinimg.com/originals/e3/d9/c5/e3d9c5ab7347f1eb3c2376acb6a2bf5b.jpg", "http://www.syntax.org.il/xtra/bob.m4a"));
            songs.add(new Song("Sara", "Bob Dylan", "https://cdn11.bigcommerce.com/s-n6h3dlxzq9/images/stencil/1200x1200/products/37216/94691/SLPTDVNYL1138__58917.1577696868.jpg?c=2", "http://www.syntax.org.il/xtra/bob1.m4a"));
            songs.add(new Song("The Man In Me", "Bob Dylan", "https://f4.bcbits.com/img/a4152887838_10.jpg", "http://www.syntax.org.il/xtra/bob2.mp3"));
            saveData();
            editor.putBoolean("firstTime", false);
            editor.apply();
        }
        else
            loadData();


        //הוספת השיר לתוך הריסייקלר
        if(getIntent().getBooleanExtra("submit", false))
        {
            Song song = (Song) getIntent().getSerializableExtra("song");
            songs.add(song);
            saveData();
        }


        final SongAdapter songAdapter = new SongAdapter(songs);

        songAdapter.setActionsListener(new ItemTouchHelperAdapter() {
            @Override
            public void onItemMove(int fromPosition, int toPosition) {
                Song fromSong = songs.get(fromPosition);
                songs.remove(fromSong);
                songs.add(toPosition, fromSong);
                songAdapter.notifyItemMoved(fromPosition, toPosition);
                saveData();
            }

            @Override
            public void onItemSwiped(final int position) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.delete_dialog);
                Button continueBtn = (Button)dialog.findViewById(R.id.continue_btn);
                Button exitBtn = (Button)dialog.findViewById(R.id.exit_btn);
                continueBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        songs.remove(position);
                        songAdapter.notifyDataSetChanged();
                        dialog.cancel();
                        saveData();
                    }
                });
                exitBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        songAdapter.notifyItemChanged(position);
                        dialog.cancel();
                    }
                });
                dialog.show();
            }

        });

        songAdapter.setSongListener(new SongAdapter.MySongListener() {
            @Override
            public void onSongClick(int position, View view) {
                Intent intent= new Intent(MainActivity.this,SongInfo.class);
//                Song infoSong=new Song(songs.get(position).getSongName(), songs.get(position).getSongArtist(), songs.get(position).getPhotoResId(), null);
//                intent.putExtra("songInfo", infoSong);
                intent.putExtra("songName",songs.get(position).getSongName());
                intent.putExtra("songArtist", songs.get(position).getSongArtist());
                intent.putExtra("songImage", songs.get(position).getPhotoResId());
                startActivity(intent);
            }
        });


        ItemTouchHelper.Callback callback = new MyItemTouchHelper(songAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        songAdapter.setTouchHelper(itemTouchHelper);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.setAdapter(songAdapter);




        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isPlay) {
                    Intent intent = new Intent(MainActivity.this, MusicService.class);
                    intent.putExtra("command", "new_instance");
                    startService(intent);
                    isPlay = true;
                } else {
                    Intent intent = new Intent(MainActivity.this, MusicService.class);
                    intent.putExtra("command", "play");
                    startService(intent);
                }

            }
        });

        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.putExtra("list", songs);
                intent.putExtra("command", "pause");
                startService(intent);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.putExtra("list", songs);
                intent.putExtra("command", "next");
                startService(intent);
            }
        });

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MusicService.class);
                intent.putExtra("list", songs);
                intent.putExtra("command", "prev");
                startService(intent);
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this, AddSong.class);
                startActivity(intent);
                finish();
            }
        });

    }



    private void loadData() {
        try {
            FileInputStream fis = openFileInput("songList.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            songs = (ArrayList<Song>) ois.readObject();
            ois.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        try {
            FileOutputStream fos = openFileOutput("songList.dat", MODE_PRIVATE);
            ObjectOutputStream oow = new ObjectOutputStream(fos);
            oow.writeObject(songs);
            oow.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}