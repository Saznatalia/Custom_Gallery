package com.example.a21005371_assignment_2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// The adapter class which supplies data to the GridView,
// it grabs an image in the background using an AsyncTask
public class ImageAdapter extends BaseAdapter {
    private static final String TAG = "Gallery";
    private Context mContext;
    private int[] mImgIds;
    private LayoutInflater mInflater;
    private ExecutorService mExecutor = Executors.newFixedThreadPool(4);

    // Constructor
    public ImageAdapter(Context appContext, int[] images) {
        this.mContext = appContext;
        this.mImgIds = images;
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
                    // otherwise, decode the jpeg into a bitmap
//                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.inJustDecodeBounds = true;
//                    bmp = BitmapFactory.decodeResource(mContext.getResources(),
//                            mImgIds[i], options);
//                    int imageHeight = options.outHeight;
//                    int imageWidth = options.outWidth;
//                    String imageType = options.outMimeType;
//                    bmp = decodeSampledBitmapFromResource(mContext.getResources(), mImgIds[i], 200, 200);
//                    bmp = mContext.getContentResolver().load(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(mImgIds[i])));


                    if (Build.VERSION.SDK_INT < 29) {
                        bmp = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(),
                                mImgIds[i], MediaStore.Images.Thumbnails.MICRO_KIND, null);
                    } else {
                        bmp = mContext.getContentResolver().loadThumbnail(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                String.valueOf(mImgIds[i])), Size.parseSize("200*+200"), null);
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

    public static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }

        cursor.moveToFirst();
        return cursor.getInt(0);
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
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

}
