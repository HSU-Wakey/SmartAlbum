package com.example.smartalbum.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.util.Pair;

import com.example.smartalbum.domain.model.ImageMeta;
import com.example.smartalbum.tflite.ImageClassifier;
import com.example.smartalbum.util.ImageUtils;
import com.example.smartalbum.util.LocationUtils;

import java.util.List;

public class ImageRepository {
    private final ImageClassifier imageClassifier;
    private final Context context;

    public ImageRepository(Context context) {
        this.context = context;
        try {
            this.imageClassifier = new ImageClassifier(context);
        } catch (Exception e) {
            throw new RuntimeException("모델 로드 실패", e);
        }
    }

    public ImageMeta classifyImage(Uri uri, Bitmap bitmap) {
        List<Pair<String, Float>> predictions = imageClassifier.classifyImage(bitmap);
        String region = null;

        Location location = ImageUtils.getExifLocation(context, uri);
        if (location != null) {
            region = LocationUtils.getRegionFromLocation(context, location);
        }

        return new ImageMeta(uri.toString(), region, predictions);
    }

    public void close() {
        imageClassifier.close();
    }
}
