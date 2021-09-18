package com.example.a21005371_assignment_2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.os.AsyncTask;
import android.util.Log;
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
    private final Context mContext;
    private final int[] mImgIds;
    private LayoutInflater mInflater;
    private final String[] mImgPaths;
    private final ExecutorService mExecutor = Executors.newFixedThreadPool(4);

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

            // Inflate it from xml
            mInflater = LayoutInflater.from(mContext);
            convertView = mInflater.inflate(R.layout.image, null);

            // ConvertView will be a LinearLayout
            vh = new ViewHolder();
            vh.img = convertView.findViewById(R.id.imgView);

            // Set the tag to it
            convertView.setTag(vh);
        } else {

            // Otherwise get the view holder
            vh = (ViewHolder) convertView.getTag();
        }

        // Set vh to its position
        vh.position = i;

        // Erase the image so we don't see old photos
        vh.img.setImageBitmap(null);

        // Make an SyncTask to load the image
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
                    bmp = getBitmap(mImgPaths[i], 160, 160);
/*                    THIS COMMENTED CODE WORKS FASTER
                    if (Build.VERSION.SDK_INT < 29) {
                        bmp = MediaStore.Images.Thumbnails.getThumbnail(mContext.getContentResolver(),
                                mImgIds[i], MediaStore.Images.Thumbnails.MICRO_KIND, null);
                    } else {
                        bmp = mContext.getContentResolver().loadThumbnail(Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                String.valueOf(mImgIds[i])), Size.parseSize("200*+200"), null);
                    }
                    bmp = modifyOrientation(bmp, mImgPaths[i]); */
                } catch (Exception e) {
                    Log.i(TAG, "Error Loading:" + mImgIds[i]);
                    e.printStackTrace();
                }
                // Return the bitmap (might be null)
                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap bmp) {

                // Only set the imageview if the position hasn't changed.
                if (vh.position == i) {
                    vh.img.setImageBitmap(bmp);
                }
            }
        }.execute(vh);
        return convertView;
    }

    public static Bitmap getBitmap(String path, int reqWidth, int reqHeight) {
        Bitmap bmp;

        // First decode with inJustDecodeBounds=true to check dimensions
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        bmp = BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        bmp = BitmapFactory.decodeFile(path, options);
        try {
            bmp = ImageAdapter.modifyOrientation(bmp, path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmp;
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
