package com.example.a21005371_assignment_2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// The adapter class which supplies data to the GridView,
// it grabs an image in the background using an AsyncTask
public class ImageAdapter extends BaseAdapter {
    private static final String TAG = "Gallery";
    private Context mContext;
    private int[] mImgIds;
    private LayoutInflater mInflater;
    private String[] mImgPaths;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(4);

    // Constructor
    public ImageAdapter(Context appContext, int[] ids, String[] paths) {
        this.mContext = appContext;
        this.mImgIds = ids;
        this.mImgPaths = paths;
        this.mInflater = (LayoutInflater.from(appContext));
    }

    // ViewHolder holds the image's imageview and it's position in the list
    public class ViewHolder {
        int position;
        ImageView img;
    }

    // Return number of images in drawable
    @Override
    public int getCount() {
        return mImgIds.length;
    }

    @Override
    public Object getItem(int position) {
        return mImgIds[position];
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @SuppressLint("StaticFieldLeak")
    @Override
    public View getView(final int i, View convertView, ViewGroup viewGroup) {
        Log.i(TAG, "getView: " + i + convertView);
        ViewHolder vh;
        if (convertView == null) {
            // if it's not recycled, inflate it from xml
            mInflater = LayoutInflater.from(mContext);
            convertView = mInflater.inflate(R.layout.image, null);

            // convertView will be a LinearLayout
            vh = new ViewHolder();
            vh.img = convertView.findViewById(R.id.imgView);

            // set the tag to it
            convertView.setTag(vh);
        } else {
            // otherwise get the view holder
            vh = (ViewHolder) convertView.getTag();
        }

        // Set vh to its position
        vh.position = i;
        // and erase the image so we don't see old photos
        vh.img.setImageBitmap(null);

        // make an SyncTask to load the image
        View finalConvertView = convertView;
        new AsyncTask<ViewHolder, Void, Bitmap>() {
            private ViewHolder vh;

            @Override
            protected Bitmap doInBackground(ViewHolder... params) {
                vh = params[0];
                Bitmap bmp = null;
                try {
                    Log.i(TAG, "Loading:" + mImgIds[i]);
                    // vh position might change then return null
                    if (vh.position != i) {
                        return null;
                    }
                    // otherwise load thumbnails
                    if (Build.VERSION.SDK_INT < 29) {
                        bmp = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(),
                                mImgIds[i], MediaStore.Images.Thumbnails.MICRO_KIND, null);
                        bmp = modifyOrientation(bmp, mImgPaths[i]);
                    } else {
                        bmp = mContext.getContentResolver().loadThumbnail(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                String.valueOf(mImgIds[i])), Size.parseSize("200*+200"), null);
                        bmp = modifyOrientation(bmp, mImgPaths[i]);
                    }

                } catch (Exception e) {
                    Log.i(TAG, "Error Loading:" + mImgIds[i]);
                    e.printStackTrace();
                }
                // return the bitmap (might be null)
                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {
                // only set the imageview if the position hasn't changed.
                if (vh.position == i) {
                    vh.img.setImageBitmap(bmp);
                }
            }
        }.execute(vh);
        return convertView;
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
