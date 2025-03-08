package com.netlcod.audionnbenchmark;

import android.util.Log;

import org.tensorflow.lite.Interpreter;
import com.jlibrosa.audio.process.AudioFeatureExtraction;

public class AudioProcessor {
    AudioProcessorConfiguration processorConfiguration;
    private Interpreter tfInterpreter;
    private AudioFeatureExtraction featureExtractor;
    private float[] audioBuffer;

    public AudioProcessor(AudioProcessorConfiguration configuration) {
        this.processorConfiguration = configuration;
        this.updateConfiguration();
    }

    public void updateConfiguration() {
        this.featureExtractor = new AudioFeatureExtraction();
        this.featureExtractor.setSampleRate(processorConfiguration.getSampleRate());
        this.featureExtractor.setLength((int) (processorConfiguration.getSampleRate() * processorConfiguration.getFeatureDuration()));
        this.featureExtractor.setN_fft(processorConfiguration.getNFFT());
        this.featureExtractor.setHop_length(processorConfiguration.getHopLength());

        switch (processorConfiguration.getFeatureType()) {
            case "mel":
                this.featureExtractor.setN_mels(processorConfiguration.getFeatureSize());
                break;
            case "mfcc":
                this.featureExtractor.setN_mfcc(processorConfiguration.getFeatureSize());
                break;
            default:
                Log.e("AudioProcessor", "Incorrect featureType: " + processorConfiguration.getFeatureType());
        }


        try {
            tfInterpreter = new Interpreter(ModelLoader.loadModelFile(processorConfiguration.getModelPath()));
        } catch (Exception e) {
            Log.e("AudioProcessor", "ERROR_LOADING_MODEL", e);
        }
    }

    public float[] getAudioBuffer() {
        return audioBuffer;
    }

    public void setAudioBuffer(float[] audioBuffer) {
        this.audioBuffer = audioBuffer;
    }

    public float[] normalize(float[] signal) {
        float mean = 0, std = 0;
        for (float value : signal) {
            mean += value;
        }
        mean /= signal.length;

        for (float value : signal) {
            std += (float) Math.pow(value - mean, 2);
        }
        std = (float) Math.sqrt(std / signal.length);

        float[] normalized = new float[signal.length];
        for (int i = 0; i < signal.length; i++) {
            normalized[i] = (signal[i] - mean) / std;
        }
        return normalized;
    }

    public float[][] extract(float[] signal) {
        double[][] feature = null;

        switch (processorConfiguration.getFeatureType()) {
            case "mel":
                feature = featureExtractor.melSpectrogram(signal);
                break;
            case "mfcc":
//                feature = featureExtractor.extractMFCCFeatures(signal);
                break;
            default:
                Log.e("AudioProcessor", "Incorrect featureType: " + processorConfiguration.getFeatureType());
        }

        float[][] convertedFeature = new float[0][];
        convertedFeature = new float[feature.length][feature[0].length];
        for (int i = 0; i < feature.length; i++) {
            for (int j = 0; j < feature[i].length; j++) {
                convertedFeature[i][j] = (float) feature[i][j];
            }
        }

        return convertedFeature;
    }

    public float[][] predict(float[][] feature) {
        // Преобразование в четырехмерный массив [1, H, W, C]
        int height = feature.length;
        int width = feature[0].length;
        int channels = 1;

        float[][][][] paddedFeature = new float[1][height][width][channels];
        for (int ii = 0; ii < height; ii++) {
            for (int jj = 0; jj < width; jj++) {
                paddedFeature[0][ii][jj][0] = feature[ii][jj];
            }
        }

        float[][] output = new float[1][2];
        tfInterpreter.run(paddedFeature, output);
        return output;
    }
}