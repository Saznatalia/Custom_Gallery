package com.example.a21005371_assignment_2;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ComponentActivity;

import java.io.IOException;

public class SecondActivity extends AppCompatActivity {
    private ImageView mSelectedImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        mSelectedImg = (ImageView) findViewById(R.id.selectedImage); // init a ImageView
        Intent intent = getIntent(); // get Intent which we set from Previous Activity
        String selectedImgPath = intent.getStringExtra("path");
            // get image from Intent and set it in ImageView
//            String selectedImgPath = intent.getStringExtra("path");
        new AsyncTask<Intent, Void, Bitmap>() {
            private Intent intent = getIntent();
            @Override
            protected Bitmap doInBackground(Intent... intents) {
                intent = intents[0];
                Bitmap bmp;
                if (intent == null) {
                    return null;
                }
                // First decode with inJustDecodeBounds=true to check dimensions
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                bmp = BitmapFactory.decodeFile(selectedImgPath, options);

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, 500, 500);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                bmp = BitmapFactory.decodeFile(selectedImgPath, options);
                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                // only set the imageview if the position hasn't changed.
                if (bmp != null) {
                    mSelectedImg.setImageBitmap(bmp);
                }
            }

        }.execute(intent);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize += 1;
            }
        }

        return inSampleSize;
    }
}
