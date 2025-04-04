package com.example.smartalbum.util;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import androidx.exifinterface.media.ExifInterface;

import java.io.InputStream;

public class ImageUtils {
    public static Location getExifLocation(Context context, Uri uri) {
        try {
            InputStream stream = context.getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(stream);
            float[] latLong = new float[2];
            if (exif.getLatLong(latLong)) {
                Location loc = new Location("");
                loc.setLatitude(latLong[0]);
                loc.setLongitude(latLong[1]);
                return loc;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getExifDateTaken(Context context, Uri uri) {
        try {
            InputStream stream = context.getContentResolver().openInputStream(uri);
            ExifInterface exif = new ExifInterface(stream);
            return exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
