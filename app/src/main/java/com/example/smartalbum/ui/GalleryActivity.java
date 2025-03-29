package com.example.smartalbum.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartalbum.R;
import com.example.smartalbum.data.ImageRepository;
import com.example.smartalbum.domain.model.ImageMeta;

import java.io.InputStream;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;

    private ImageView imageView;
    private TextView resultText;
    private Button btnSelectImage;

    private ImageRepository imageRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        imageRepository = new ImageRepository(this);

        btnSelectImage.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(bitmap);

                ImageMeta imageMeta = imageRepository.classifyImage(imageUri, bitmap);

                StringBuilder sb = new StringBuilder();
                sb.append("📍지역: ").append(imageMeta.getRegion()).append("\n\n");
                sb.append("Top 5 Predictions:\n\n");
                List<Pair<String, Float>> predictions = imageMeta.getPredictions();
                for (Pair<String, Float> prediction : predictions) {
                    sb.append("- ").append(prediction.first).append(": ")
                            .append(String.format("%.2f", prediction.second)).append("%\n");
                }

                resultText.setText(sb.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageRepository.close();
    }
}
