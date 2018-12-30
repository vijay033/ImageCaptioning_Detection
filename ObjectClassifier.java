package com.example.vijay.image_captionanddetection_tensorflow;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;

import org.tensorflow.Operation;
import org.tensorflow.Tensor;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ObjectClassifier {

    private static final String TAG = "ObjectClassifier";
    // Only return this many results with at least this confidence.
    private static final int MAX_RESULTS = 3;
    private static final float THRESHOLD = 0.1f;
    private static final int INPUT_CHANNELS = 3;
    private static final int NUM_CLASSES = 1001;
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";
    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";
    private static final int[] DIM_IMAGE=new int[]{1, INPUT_SIZE, INPUT_SIZE, INPUT_CHANNELS};
    private TensorFlowInferenceInterface inferenceInterface;
    int numClasses;
    float []outputs;
    Context context;
    private List<String> labels;

    ObjectClassifier(Context context) throws IOException {
        this.context = context;
        inferenceInterface = InitDetectionSession();
    }

    private List<String> readLabels(AssetManager assets, String file) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(assets.open(file)));

        String line;
        List<String> labels = new ArrayList<>();
        while((line = br.readLine()) != null){
            labels.add(line);
        }
        br.close();

        return labels;
    }


    TensorFlowInferenceInterface InitDetectionSession ()throws IOException{
        inferenceInterface = new TensorFlowInferenceInterface();
        inferenceInterface.initializeTensorFlow(context.getAssets(),MODEL_FILE);
        labels = readLabels(context.getAssets(),"imagenet_comp_graph_label_strings.txt");

        final Operation operation = inferenceInterface.graph().operation(OUTPUT_NAME);
        numClasses = (int) operation.output(0).shape().size(1);
        outputs = new float[numClasses];
        return inferenceInterface;
    }


    float [] Preprocess(Bitmap imBitmap){
        imBitmap = Bitmap.createScaledBitmap(imBitmap, INPUT_SIZE, INPUT_SIZE, true);
        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        float[] floatValues = new float[INPUT_SIZE * INPUT_SIZE * 3];

        imBitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE);

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = (((val >> 16) & 0xFF) - IMAGE_MEAN)/IMAGE_STD;//R
            floatValues[i * 3 + 1] = (((val >> 8) & 0xFF)- IMAGE_MEAN)/IMAGE_STD;//G
            floatValues[i * 3 + 2] = ((val & 0xFF)-IMAGE_MEAN)/IMAGE_STD;//B
        }
        return floatValues;
    }

    String []ClassifyObjectLabels(float [] imRGBMatrix){
        String []toplabels = new String[5]; //top 5 prediction
        int top = 0;
        inferenceInterface.fillNodeFloat(INPUT_NAME,DIM_IMAGE,imRGBMatrix);
        inferenceInterface.runInference(new String[]{OUTPUT_NAME});
        inferenceInterface.readNodeFloat(OUTPUT_NAME,outputs);
        for(int i = 0; i < outputs.length ; i++){
            if(outputs[i] > THRESHOLD && top < 5) {
                toplabels[++top] = labels.get(i);
            }
        }
        return  toplabels;
    }

    String [] runModel(Bitmap bitmap){
        return ClassifyObjectLabels(Preprocess(bitmap));
    }

}
