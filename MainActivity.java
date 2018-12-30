package com.example.vijay.image_captionanddetection_tensorflow;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.tensorflow.Session;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    private ProgressBar progress;
    ImageView imageView = null;
    Button changeButton = null;
    Button captionButton = null;
    Button detectionButton = null;
    TextView captionText = null;
    TextView detectionText = null;
    private String captext;
    private String[]detectiontext1;
    private int currImage = 0;
    Bitmap bitmap = null;
    Context context = null;
    CaptionGenerator captionGenerator = null;
    ObjectClassifier objectClassifier = null;
    String combineDetectionText = null;

    static{
        System.loadLibrary("tensorflow_inference");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageView = (ImageView)findViewById(R.id.imgId);
        changeButton = (Button)findViewById(R.id.changeImg);
        captionButton = (Button)findViewById(R.id.capImage);
        detectionButton = (Button)findViewById(R.id.detObject);
        captionText = (TextView)findViewById(R.id.capText);
        detectionText = (TextView)findViewById(R.id.detText);
        progress = (ProgressBar)findViewById(R.id.progressBar);
        progress.setVisibility(View.INVISIBLE);
        context= this.getApplicationContext();

        captionButton.setEnabled(true);
        detectionButton.setEnabled(true);

        final int[] images = {R.drawable.indian_cobra,
                R.drawable.komodo_dragon,
                R.drawable.hat,
                R.drawable.dog_beach,
                R.drawable.man_in_guitar
        };

        bitmap = BitmapFactory.decodeResource(getResources(),images[currImage]);
        imageView.setImageBitmap(bitmap);

/*Change Image */
        changeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currImage++;
                currImage = currImage % images.length;
                bitmap = BitmapFactory.decodeResource(getResources(),images[currImage]);
                imageView.setImageBitmap(bitmap);
            }
        });

/*Initialize Models*/

        Runnable runnable1 = new Runnable() {
            @Override
            public void run() {
                progress.setVisibility(View.VISIBLE);
                try {
                    objectClassifier = new ObjectClassifier(context);
                    captionGenerator = new CaptionGenerator(context);
                }catch (IOException e){
                    e.printStackTrace();
                }
                progress.post(new Runnable() {
                    @Override
                    public void run() {
                      progress.setVisibility(View.INVISIBLE);
                    }
                });
            }
        };
        new Thread(runnable1).start();

        /*Caption Image*/

        captionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                captionButton.setEnabled(false);
                detectionButton.setEnabled(false);
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte [] imageInByte = baos.toByteArray();

                try{
                    baos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

                final Bitmap bitmapnew = BitmapFactory.decodeByteArray(imageInByte,0,imageInByte.length);

                Runnable runCaption = new Runnable() {
                    @Override
                    public void run() {
                      captext = captionGenerator.runModel(bitmapnew);
                      progress.post(new Runnable() {
                          @Override
                          public void run() {
                              captionButton.setEnabled(true);
                              detectionButton.setEnabled(true);
                              captionText.setText(captext);
                              progress.setVisibility(View.INVISIBLE);
                          }
                      });
                    }
                };
                new Thread(runCaption).start();
            }
        });


        /*Object detection in a image*/

        detectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                captionButton.setEnabled(false);
                detectionButton.setEnabled(false);
                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte [] imageInByte = baos.toByteArray();

                try{
                    baos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }

                final Bitmap bitmapnew = BitmapFactory.decodeByteArray(imageInByte,0,imageInByte.length);

                Runnable runDetection = new Runnable() {
                    @Override
                    public void run() {
                        detectiontext1 = objectClassifier.runModel(bitmapnew);
                        progress.post(new Runnable() {
                            @Override
                            public void run() {

                                for(int i = 0 ; i < detectiontext1.length; i++){
                                    if(detectiontext1[i]!= null){
                                        combineDetectionText = detectiontext1[i];
                                        combineDetectionText += ' ';
                                    }
                                }
                                captionButton.setEnabled(true);
                                detectionButton.setEnabled(true);
                                detectionText.setText(combineDetectionText);
                                progress.setVisibility(View.INVISIBLE);
                            }
                        });
                    }
                };
                new Thread(runDetection).start();

            }
        });


    }





}
