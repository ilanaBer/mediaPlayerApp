package com.example.newmediaplayer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.transition.Transition;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener{

    MediaPlayer mediaPlayer=new MediaPlayer();
    ArrayList<Song>songs;
    int currentPlaying=0;
    final int NOTIFICATION_ID=1;
    RemoteViews remoteViews;
    NotificationCompat.Builder builder;
    NotificationManager manager;


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.reset();


        manager=(NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        String channelId="channel_id";
        String channelName="music_channel";
        if(Build.VERSION.SDK_INT>=26){
            NotificationChannel channel=new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW);
            manager.createNotificationChannel(channel);
        }

        builder=new NotificationCompat.Builder(this,channelId);
        builder.setSmallIcon(android.R.drawable.ic_media_play);

        remoteViews=new RemoteViews(getPackageName(), R.layout.music_notification);

        Intent playIntent=new Intent(this, MusicService.class);
        playIntent.putExtra("command", "play");
        PendingIntent playPendingIntent=PendingIntent.getService(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.play_btn, playPendingIntent);

        Intent pauseIntent=new Intent(this, MusicService.class);
        pauseIntent.putExtra("command", "pause");
        PendingIntent pausePendingIntent=PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.pause_btn, pausePendingIntent);

        Intent nextIntent=new Intent(this, MusicService.class);
        nextIntent.putExtra("command", "next");
        PendingIntent nextPendingIntent=PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.next_btn, nextPendingIntent);

        Intent prevIntent=new Intent(this, MusicService.class);
        prevIntent.putExtra("command", "prev");
        PendingIntent prevPendingIntent=PendingIntent.getService(this, 3, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.prev_btn, prevPendingIntent);

        Intent closeIntent=new Intent(this, MusicService.class);
        closeIntent.putExtra("command", "close");
        PendingIntent closePendingIntent=PendingIntent.getService(this, 4, closeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.close_btn, closePendingIntent);


        builder.setCustomBigContentView(remoteViews);
        startForeground(NOTIFICATION_ID,builder.build());


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        String command=intent.getStringExtra("command");
        loadData();
        switch (command){
            case "new_instance":
                if(!songs.isEmpty() && !mediaPlayer.isPlaying()) {
                    try {
                        mediaPlayer.setDataSource(songs.get(currentPlaying).getLink());
                        mediaPlayer.prepareAsync();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;

            case "play":
                if(!mediaPlayer.isPlaying())
                    mediaPlayer.start();
                break;

            case "next":
                if(mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                playSong(true);
                break;

            case "prev":
                if(mediaPlayer.isPlaying())
                    mediaPlayer.stop();
                playSong(false);
                break;

            case "pause":
                if(mediaPlayer.isPlaying())
                    mediaPlayer.pause();
                break;
            case "close":
                stopSelf();

        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void playSong(boolean isNext){
        if(isNext){
            currentPlaying++;
            if(currentPlaying==songs.size())
                currentPlaying=0;
        }
        else{
            currentPlaying--;
            if(currentPlaying<0)
                currentPlaying=songs.size()-1;
        }
        mediaPlayer.reset();
        if(!songs.isEmpty()) {
            try {
                mediaPlayer.setDataSource(songs.get(currentPlaying).getLink());
                mediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        playSong(true);
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
        remoteViews.setTextViewText(R.id.songNameNotif, songs.get(currentPlaying).getSongName());
        remoteViews.setTextViewText(R.id.artistNameNotif, songs.get(currentPlaying).getSongArtist());
        Glide.with(this)
                .asBitmap()
                .load(songs.get(currentPlaying).getPhotoResId())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                        remoteViews.setImageViewBitmap(R.id.songImage, resource);
                        builder.setCustomBigContentView(remoteViews);
                        Notification notification = builder.build();
                        manager.notify(NOTIFICATION_ID, notification);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }
                });
/*        builder.setCustomBigContentView(remoteViews);
        manager.notify(NOTIFICATION_ID, builder.build());*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer!=null){
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.release();
        }
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

}
