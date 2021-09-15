package com.example.a21005371_assignment_2;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;


public class SecondActivity extends AppCompatActivity {
    private ImageView mSelectedImg;
//    private ScaleGestureDetector mScaleGestureDetector;
    private static final String TAG = "Second Activity";

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        mSelectedImg = findViewById(R.id.selectedImage); // init a ImageView
        Intent intent = getIntent(); // get intent which was set from main activity
        String selectedImgPath = intent.getStringExtra("path"); //get extra from intent

        // Load a picture on the background thread
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
                try {
                    bmp = modifyOrientation(bmp, selectedImgPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                // only set the imageview if the position hasn't changed.
                if (bmp != null) {
                    mSelectedImg.setImageBitmap(bmp);
                    // call the ScaleGestureDetector when the image is touched
//                    mSelectedImg.setOnTouchListener(View this, MotionEvent motionEvent -> {
//                        Log.i(TAG,"onTouch:"+ motionEvent.getAction()+","+ motionEvent.getX()+","+ motionEvent.getY());
//                        mScaleGestureDetector.onTouchEvent(motionEvent);
//                        return false;
//                    });
                }
            }
        }.execute(intent);
    }

    /* Function that calculates inSampleSize (from android documentation,
     but altered scale factor so its incrementing by 1 */
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

    public static Bitmap modifyOrientation(Bitmap bitmap, String image_absolute_path) throws IOException {
        ExifInterface ei = new ExifInterface(image_absolute_path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotate(bitmap, 90);

            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotate(bitmap, 180);

            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotate(bitmap, 270);

            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return flip(bitmap, true, false);

            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return flip(bitmap, false, true);

            default:
                return bitmap;
        }
    }

    public static Bitmap rotate(Bitmap bitmap, float degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
        Matrix matrix = new Matrix();
        matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
