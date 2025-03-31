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
import java.util.ArrayList;
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

        imageRepository.printAllPhotos();
        imageRepository.clearAllPhotos();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true); // ✅ 다중 선택
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            List<ImageMeta> metaList = new ArrayList<>();
            try {
                if (data.getClipData() != null) { // 여러 장 선택
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        ImageMeta meta = processImage(imageUri);
                        metaList.add(meta);
                        imageRepository.savePhotoToDB(imageUri, meta);
                    }
                } else if (data.getData() != null) { // 한 장 선택
                    Uri imageUri = data.getData();
                    ImageMeta meta = processImage(imageUri);
                    metaList.add(meta);
                    imageRepository.savePhotoToDB(imageUri, meta);
                }

                // 화면에 가장 첫 번째 이미지 표시 (샘플)
                if (!metaList.isEmpty()) {
                    Bitmap bitmap = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(Uri.parse(metaList.get(0).getUri())));
                    imageView.setImageBitmap(bitmap);

                    StringBuilder sb = new StringBuilder();
                    for (ImageMeta meta : metaList) {
                        sb.append("📍지역: ").append(meta.getRegion()).append("\n");
                        for (Pair<String, Float> pred : meta.getPredictions()) {
                            sb.append("- ").append(pred.first).append(": ")
                                    .append(String.format("%.2f", pred.second)).append("%\n");
                        }
                        sb.append("\n");
                    }
                    resultText.setText(sb.toString());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ImageMeta processImage(Uri imageUri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
        return imageRepository.classifyImage(imageUri, bitmap);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imageRepository.close();
    }
}
