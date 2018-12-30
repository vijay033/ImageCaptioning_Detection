package com.example.vijay.image_captionanddetection_tensorflow;

import android.content.Context;
import android.graphics.Bitmap;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CaptionGenerator {

    private static final String MODEL_FILE = "file:///android_asset/merged_frozen_graph.pb";
    private static final String INPUT1 = "encoder/import/InputImage:0";
    private static final String OUTPUT_NODES = "DecoderOutputs.txt";
    private static final int NUM_TIMESTEPS = 22;
    private static final int IMAGE_SIZE = 299;
    private static final int IMAGE_CHANNELS = 3;
    private static final int[] DIM_IMAGE=new int[]{1, IMAGE_SIZE, IMAGE_SIZE, IMAGE_CHANNELS};
    private TensorFlowInferenceInterface inferenceInterface;
    private String[] OutputNodes = null;
    private String[] WORD_MAP = null;

    Context context;

    CaptionGenerator(Context context){
        this.context=context;
        inferenceInterface = InitSession();

    }

    String[] LoadFile(String fileName){
        InputStream is = null;
        try {
            is = context.getAssets().open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        StringBuilder total = new StringBuilder();
        String line;
        try {
            while ((line = r.readLine()) != null) {
                total.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return total.toString().split("\n");
    }

    TensorFlowInferenceInterface InitSession(){
        inferenceInterface = new TensorFlowInferenceInterface();
        inferenceInterface.initializeTensorFlow(context.getAssets(),MODEL_FILE);
        OutputNodes = LoadFile(OUTPUT_NODES);
        WORD_MAP = LoadFile("idmap");
        return inferenceInterface;
    }

    String runModel(Bitmap imBitmap){
        return  GenerateCaptions(Preprocess(imBitmap));
    }

    float[] Preprocess(Bitmap imBitmap){
        imBitmap = Bitmap.createScaledBitmap(imBitmap, IMAGE_SIZE, IMAGE_SIZE, true);
        int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
        float[] floatValues = new float[IMAGE_SIZE * IMAGE_SIZE * 3];

        imBitmap.getPixels(intValues, 0, IMAGE_SIZE, 0, 0, IMAGE_SIZE, IMAGE_SIZE);

        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((float)((val >> 16) & 0xFF))/255;//R
            floatValues[i * 3 + 1] = ((float)((val >> 8) & 0xFF))/255;//G
            floatValues[i * 3 + 2] = ((float)((val & 0xFF)))/255;//B
        }
        return floatValues;
    }

    String GenerateCaptions(float[] imRGBMatrix){
        inferenceInterface.fillNodeFloat(INPUT1, DIM_IMAGE, imRGBMatrix);
        inferenceInterface.runInference(OutputNodes);

        String result = "";
        int temp[][]= new int[NUM_TIMESTEPS][1];
        for(int i = 0; i<NUM_TIMESTEPS; ++i) {
            inferenceInterface.readNodeInt(OutputNodes[i], temp[i]);
            if(temp[i][0] == 2/*</S>*/){
                return result;
            }
            result += WORD_MAP[temp[i][0]]+" ";
        }
        return null;
    }

}
