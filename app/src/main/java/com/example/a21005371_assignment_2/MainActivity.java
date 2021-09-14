package com.example.a21005371_assignment_2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.ExifInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.GridView;


public class MainActivity extends AppCompatActivity {
    private int[] ids;
    GridView mGrid;
    Cursor mCursor;
    int mPosition = 0;

    void init() {

        // get images
        final String[] columns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.ORIENTATION, MediaStore.Images.Media.DATA };
        final String orderBy = MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC";
        mCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
        this.ids = getImageIDs(mCursor);

        // Create an object of my ImgAdapter and set adapter to grid view
        ImageAdapter imgAdapter = new ImageAdapter(getApplicationContext(), ids);
        mGrid.setAdapter(imgAdapter);

        // OnClick
        mGrid.setOnItemClickListener((parent, view, position, id) -> {
            mCursor.moveToPosition(position);
            String imgPath = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.Media.DATA));
            Log.i("Image's id: ", String.valueOf(ids[position]));
            // set an Intent to Another Activity
            Intent intent = new Intent(MainActivity.this, SecondActivity.class);
            intent.putExtra("path", imgPath); // put image path data in Intent
            startActivity(intent); // start Intent
        });
    }

    @NonNull
    private int[] getImageIDs(Cursor cursor) {
//        final String[] columns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.ORIENTATION, MediaStore.Images.Media.DATA };
//        final String orderBy = MediaStore.Images.ImageColumns.DATE_MODIFIED + " DESC";
//
//        Cursor imgCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null, null, orderBy);
        int image_column_index = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        int[] ids = new int[cursor.getCount()];
        String[] paths = new String[cursor.getCount()];
        for (int i = 0; i < cursor.getCount(); i++) {
            cursor.moveToPosition(i);
            ids[i] = cursor.getInt(image_column_index);
        }
        return ids;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGrid = findViewById(R.id.gridView); // init GridView

        // Check if we have necessary permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // If not, request the permission
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            init();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        // save the list position
        mPosition = mGrid.getFirstVisiblePosition();
        // close the cursor (will be opened again in init() during onResume())
        mCursor.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        // re-init in case things have changed
        init();
        // set the list position
        mGrid.setSelection(mPosition);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                init();
            } else {
                // User refused to grant permission
                finish();
            }
        }
    }

}