package com.example.newmediaplayer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import com.bumptech.glide.Glide;
import java.io.File;


public class AddSong extends AppCompatActivity  {

    final int CAMERA_REQUEST = 1;
    final int WRITE_PERMISSION_REQUEST=1;
    private final static int IMAGE_PICK_CODE=1000;
    private final static int PERMISSION_CODE=1001;
    File file;
    EditText songNameEt, artistNameEt, linkEt;
    Button openGalleryBtn, openCameraBtn, cancelBtn, saveBtn;
    ImageView photoSongImage;
    String imagePath;
    Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.song_add);



        songNameEt = findViewById(R.id.addSongNameEt);
        artistNameEt = findViewById(R.id.addArtistNameEt);
        linkEt = findViewById(R.id.addLinkEt);
        openGalleryBtn = findViewById(R.id.photoGallery);
        openCameraBtn = findViewById(R.id.photoCamera);
        cancelBtn = findViewById(R.id.cancelBtn);
        saveBtn = findViewById(R.id.saveBtn);
        photoSongImage = findViewById(R.id.songImage);



        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                file = new File(Environment.getExternalStorageDirectory(), "pic.jpg");
                file=new File(getExternalFilesDir(null),"pic.jpg");
                imageUri = FileProvider.getUriForFile(AddSong.this, "com.example.newmediaplayer.provider", file);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, CAMERA_REQUEST);
            }
        });

        if (Build.VERSION.SDK_INT >= 23) {
            int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_PERMISSION_REQUEST);
            } else openCameraBtn.setVisibility(View.VISIBLE);
        } else openCameraBtn.setVisibility(View.VISIBLE);


        openGalleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
                {
                    if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)==PackageManager.PERMISSION_DENIED){
                        String [] permission={Manifest.permission.READ_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    }
                    else{
                        pickImageFromGallery();
                    }
                }
                else{
                    pickImageFromGallery();
                }
            }
        });


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(AddSong.this, MainActivity.class);
                intent.putExtra("submit", false);
                startActivity(intent);
                finish();
            }
        });


        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             Intent intent=new Intent(AddSong.this, MainActivity.class);
             Song song = new Song(songNameEt.getText().toString(), artistNameEt.getText().toString(), imagePath, linkEt.getText().toString());
                intent.putExtra("song", song);
                intent.putExtra("submit", true);
                startActivity(intent);
                finish();
            }
        });



    }


    private void pickImageFromGallery(){
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==WRITE_PERMISSION_REQUEST){
            if(grantResults[0]!=PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this,"can't take pic", Toast.LENGTH_SHORT).show();
            }
            else {
                openCameraBtn.setVisibility(View.VISIBLE);
            }
        }
        else {
            switch (requestCode){
                case PERMISSION_CODE:{
                    if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                        pickImageFromGallery();
                    }
                    else {
                        Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            imagePath = file.getAbsolutePath();
            Glide
                    .with(AddSong.this)
                    .load(imageUri)
                    .centerCrop()
                    .into(photoSongImage);

        }
        else if ( requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK){
            imageUri = data.getData();
            Glide
                    .with(AddSong.this)
                    .load(imageUri)
                    .centerCrop()
                    .into(photoSongImage);
            imagePath = RealPathUtil.getRealPath(this, imageUri);
        }
    }


}
