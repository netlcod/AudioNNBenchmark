package com.netlcod.audionnbenchmark;

public class AudioProcessorConfiguration {
    private double sampleRate;
    private String modelPath;
    private String nnType;
    private double featureDuration;
    private double featureOverlapping;
    private String featureType;
    private int featureSize;
    private int nFFT;
    private int hopLength;

    public double getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(double sampleRate) {
        this.sampleRate = sampleRate;
    }

    public String getModelPath() {
        return modelPath;
    }

    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }

    public String getNNType() {
        return nnType;
    }

    public void setNNType(String nnType) {
        this.nnType = nnType;
    }

    public double getFeatureDuration() {
        return featureDuration;
    }

    public void setFeatureDuration(double featureDuration) {
        this.featureDuration = featureDuration;
    }

    public double getFeatureOverlapping() {
        return featureOverlapping;
    }

    public void setFeatureOverlapping(double featureOverlapping) {
        this.featureOverlapping = featureOverlapping;
    }

    public String getFeatureType() {
        return featureType;
    }

    public void setFeatureType(String featureType) {
        this.featureType = featureType;
    }

    public int getFeatureSize() {
        return featureSize;
    }

    public void setFeatureSize(int featureSize) {
        this.featureSize = featureSize;
    }

    public int getNFFT() {
        return nFFT;
    }

    public void setNFFT(int nFFT) {
        this.nFFT = nFFT;
    }

    public int getHopLength() {
        return hopLength;
    }

    public void setHopLength(int hopLength) {
        this.hopLength = hopLength;
    }
}