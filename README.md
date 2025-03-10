# Audio NN Benchmark

This application is designed to benchmark the performance of TFLite neural network on mobile device. User can select a `.tflite` model file via a file picker dialog. Only models with a specific naming convention are supported.

---

## Requirements

To run the application, the following dependencies are required:

- Android API 34 or higher.
- Libraries:
  - TensorFlow Lite (`org.tensorflow:tensorflow-lite:2.10.0`).
  - JLibrosa (`com.litongjava:jlibrosa:1.1.8`).

## Project structure

```
audio-nn-benchmark/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/netlcod/audionnbenchmark/
│   │   │   │   ├── MainActivity.java          			# Main activity
│   │   │   │   ├── AudioProcessor.java        			# Audio data processing
│   │   │   │   ├── AudioProcessorConfiguration.java 	# Model configuration
│   │   │   ├── res/
│   │   │   │   ├── layout/activity_main.xml  
│   ├── build.gradle.kts                       
```

## License

This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.
