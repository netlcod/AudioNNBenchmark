package com.netlcod.audionnbenchmark;

import android.content.Intent;
import android.database.Cursor;
import android.provider.OpenableColumns;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int PICK_MODEL_REQUEST = 1;
    private AudioProcessorConfiguration processorConfiguration;
    private AudioProcessor audioProcessor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button selectModelButton = findViewById(R.id.select_model);
        Button startTestButton = findViewById(R.id.start_test);
        EditText numberOfCyclesInput = findViewById(R.id.number_of_cycles);
        TextView fpsLabel = findViewById(R.id.label_fps);

        selectModelButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICK_MODEL_REQUEST);
        });

        startTestButton.setOnClickListener(v -> {
            try {
                int cycles = Integer.parseInt(numberOfCyclesInput.getText().toString());
                showToast("Запуск цикла");
                fpsLabel.setText("FPS:");
                runPerformanceTask(cycles, fps -> fpsLabel.setText(String.format("FPS: %.2f", fps)));
            } catch (NumberFormatException e) {
                showToast("Количество итераций задано неверно!");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_MODEL_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                File modelFile = resolveModelFile(fileUri);
                if (modelFile != null) {
                    processorConfiguration = new AudioProcessorConfiguration();
                    initializeProcessorConfiguration(modelFile.getPath());

                    audioProcessor = new AudioProcessor(processorConfiguration);
                }
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private File resolveModelFile(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            String fileName = getFileNameFromUri(uri);
            File modelFile = new File(getCacheDir(), fileName);
            try (FileOutputStream outputStream = new FileOutputStream(modelFile)) {
                byte[] buffer = new byte[4 * 1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            return modelFile;
        } catch (IOException e) {
            Log.e("ModelPicker", "Ошибка загрузки модели", e);
            showToast("Ошибка загрузки модели");
        }
        return null;
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            if (nameIndex != -1) {
                fileName = cursor.getString(nameIndex);
            }
            cursor.close();
        }
        return fileName;
    }

    private AudioProcessorConfiguration initializeProcessorConfiguration(String filePath) {
        String baseName = filePath.replace(".tflite", "");

        String[] parts = new File(baseName).getName().split("-");
        if (parts.length != 8) {
            Log.e("ModelParser", "Неверный формат имени файла модели: " + filePath);
            return new AudioProcessorConfiguration();
        }

        String nnType = parts[0];
        String dataset = parts[1];
        double featureDuration = Double.parseDouble(parts[2]);
        double featureOverlap = Double.parseDouble(parts[3]);
        String featureType = parts[4];
        int featureSize = Integer.parseInt(parts[5]);
        int nFFT = Integer.parseInt(parts[6]);
        int hopLength = Integer.parseInt(parts[7]);

        processorConfiguration.setSampleRate(16000.0);
        processorConfiguration.setModelPath(filePath);
        processorConfiguration.setNNType(nnType);
        processorConfiguration.setFeatureDuration(featureDuration);
        processorConfiguration.setFeatureOverlapping(featureOverlap);
        processorConfiguration.setFeatureType(featureType);
        processorConfiguration.setFeatureSize(featureSize);
        processorConfiguration.setNFFT(nFFT);
        processorConfiguration.setHopLength(hopLength);

        // Логирование параметров
        Log.d("ModelParser", "Тип сети: " + nnType);
        Log.d("ModelParser", "Датасет: " + dataset);
        Log.d("ModelParser", "Длительность: " + featureDuration + " сек");
        Log.d("ModelParser", "Перекрытие: " + featureOverlap);
        Log.d("ModelParser", "Признак: " + featureType);
        Log.d("ModelParser", "Количество мел-фильтров: " + featureSize);
        Log.d("ModelParser", "Размер FFT: " + nFFT);
        Log.d("ModelParser", "Шаг окна: " + hopLength);

        return processorConfiguration;
    }

    private void runPerformanceTask(int cycles, Callback<Double> callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            double totalTime = 0.0;
            for (int i = 0; i < cycles; i++) {
                int samples = (int) (processorConfiguration.getSampleRate() * processorConfiguration.getFeatureDuration());
                float[] audioSignal = generateRandomData(samples);
                long startTime = System.currentTimeMillis();
                audioSignal = audioProcessor.normalize(audioSignal);
                audioProcessor.setAudioBuffer(audioSignal);
                float[][] features = audioProcessor.extract(audioSignal);
                float[][] pred = audioProcessor.predict(features);
                long endTime = System.currentTimeMillis();
                totalTime += (endTime - startTime) / 1000.0;
            }
            double fps = cycles / totalTime;
            runOnUiThread(() -> callback.onResult(fps)); // Вызываем колбэк с результатом
        });
    }

    private float[] generateRandomData(int size) {
        Random random = new Random();
        float[] data = new float[size];
        for (int i = 0; i < size; i++) {
            data[i] = random.nextFloat() * 2 - 1;
        }
        return data;
    }

    public interface Callback<T> {
        void onResult(T result);
    }
}