package com.example.a21005371_assignment_2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {
    private ImageView mSelectedImg;
    private float mScaleFactor = 1.f;
    private ScaleGestureDetector mScaleGestureDetector;

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initiate layout, view, and gesture detector
        setContentView(R.layout.activity_second);
        mSelectedImg = findViewById(R.id.selectedImage);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        Intent intent = getIntent(); // get intent which was set from main activity
        String selectedImgPath = intent.getStringExtra("path"); //get extra from intent

        // Load a picture on the background thread
        new AsyncTask<Intent, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Intent... intents) {
                Intent intent = intents[0];

                if (intent == null) {
                    return null;
                }
                return ImageAdapter.getBitmap(selectedImgPath, 500, 500);
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                if (bmp != null) {
                    mSelectedImg.setImageBitmap(bmp);
                }
            }
        }.execute(intent);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScaleGestureDetector.onTouchEvent(event);
    }

    // Pitch zoom for the picture
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();
            mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 10.0f)); // set max and min borders
            mSelectedImg.setScaleX(mScaleFactor);
            mSelectedImg.setScaleY(mScaleFactor);
            return true;
        }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }
    }
}
