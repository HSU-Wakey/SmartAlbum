package com.example.smartalbum;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Pair;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smartalbum.tflite.ImageClassifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private ImageView imageView;
    private TextView resultText;
    private Button btnSelectImage;
    private ImageClassifier imageClassifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        imageView = findViewById(R.id.imageView);
        resultText = findViewById(R.id.resultText);
        btnSelectImage = findViewById(R.id.btnSelectImage);

        try {
            imageClassifier = new ImageClassifier(this);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("GalleryActivity", "모델 로드 실패");
        }

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
                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                imageView.setImageBitmap(bitmap);

                // AI 모델 예측 실행
                List<Pair<String, Float>> predictions = imageClassifier.classifyImage(bitmap);
                StringBuilder resultBuilder = new StringBuilder("Top 5 Predictions:\n\n");

                for (Pair<String, Float> result : predictions) {
                    resultBuilder.append(String.format("- %s: %.2f%%\n", result.first, result.second));
                }

                resultText.setText(resultBuilder.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (imageClassifier != null) {
            imageClassifier.close();
        }
    }
}
