package com.example.smartalbum.tflite;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class ImageClassifier {
    private static final String MODEL_PATH = "mobilenet_v3_large_quantized.tflite";
    private static final String LABELS_PATH = "labels.txt";
    private static final int IMAGE_SIZE = 224;
    private static final int NUM_CLASSES = 1000;

    private Interpreter tflite;
    private List<String> labels;

    public ImageClassifier(Context context) throws IOException {
        MappedByteBuffer tfliteModel = FileUtil.loadMappedFile(context, MODEL_PATH);
        tflite = new Interpreter(tfliteModel);
        labels = FileUtil.loadLabels(context, LABELS_PATH);
    }

    public String classifyImage(Bitmap bitmap) {
        // 이미지 크기 조정
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, IMAGE_SIZE, IMAGE_SIZE, true);

        // ByteBuffer에 이미지 데이터 변환 (float 대신 uint8 사용)
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(IMAGE_SIZE * IMAGE_SIZE * 3);
        inputBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        resizedBitmap.getPixels(intValues, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);

        for (int i = 0; i < intValues.length; i++) {
            int pixel = intValues[i];
            inputBuffer.put((byte) ((pixel >> 16) & 0xFF)); // Red
            inputBuffer.put((byte) ((pixel >> 8) & 0xFF));  // Green
            inputBuffer.put((byte) (pixel & 0xFF));         // Blue
        }

        // 모델 실행
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, NUM_CLASSES}, 1);
        tflite.run(inputBuffer, outputBuffer.getBuffer());

        // 가장 높은 확률을 가진 클래스 찾기
        int maxIndex = 0;
        float[] probabilities = outputBuffer.getFloatArray();
        for (int i = 1; i < probabilities.length; i++) {
            if (probabilities[i] > probabilities[maxIndex]) {
                maxIndex = i;
            }
        }

        return labels.get(maxIndex);  // 가장 높은 확률을 가진 클래스를 반환
    }

    public void close() {
        tflite.close();
    }
}
