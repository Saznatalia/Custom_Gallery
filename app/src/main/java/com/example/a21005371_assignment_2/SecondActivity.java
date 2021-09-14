package com.example.a21005371_assignment_2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ComponentActivity;

public class SecondActivity extends AppCompatActivity {
    private ImageView mSelectedImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        mSelectedImg = (ImageView) findViewById(R.id.selectedImage); // init a ImageView
        Intent intent = getIntent(); // get Intent which we set from Previous Activity
        if (intent != null) {
            // get image from Intent and set it in ImageView
            int selectedImgId = intent.getIntExtra("image", 0);
            Log.i("ID: ", String.valueOf(this.getBaseContext().getResources()));
            Bitmap bmp = BitmapFactory.decodeResource(this.getBaseContext().getResources(), selectedImgId);
            if (bmp == null) {
                Log.i("NULL", "NUll");
            }
            mSelectedImg.setImageBitmap(bmp);
        }
    }
}
