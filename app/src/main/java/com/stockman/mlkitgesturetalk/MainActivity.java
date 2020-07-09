package com.stockman.mlkitgesturetalk;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
//import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
//import androidx.camera.core.PreviewConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.util.Rational;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;
//import com.google.firebase.ml.common.FirebaseMLException;
//import com.google.firebase.ml.vision.FirebaseVision;
//import com.google.firebase.ml.vision.automl.FirebaseAutoMLLocalModel;
//import com.google.firebase.ml.vision.common.FirebaseVisionImage;
//import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
//import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
//import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;
//import com.google.firebase.ml.vision.label.FirebaseVisionOnDeviceAutoMLImageLabelerOptions;
import com.google.mlkit.common.model.LocalModel;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerLocalModel;
import com.google.mlkit.vision.label.automl.AutoMLImageLabelerOptions;
import com.google.mlkit.vision.objects.DetectedObject;
import com.google.mlkit.vision.objects.ObjectDetection;
import com.google.mlkit.vision.objects.ObjectDetector;
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions;
import com.google.mlkit.vision.objects.defaults.PredefinedCategory;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    //varibles needed eg text to speach object, word strings etc
    private TextToSpeech mTextToSpeech;
    private ArrayList<MyModel> myModel;
    private int mPosition = 0;
    private int REQUEST_CODE_PERMISSIONS =101;
    private String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA",
            "android.permission.WRITE_EXTERNAL_STORAGE"};
    private ImageView mImageView;
    private ImageButton mLeftButton;
    private ImageButton mRightButton;
    private PreviewView mTextureView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private Executor executor = Executors.newSingleThreadExecutor();
    final String TAG="I'm Here"; //for log statements and debugging
    private  int indexCheck=-1; //set to an unreachable index to ensure no future additional words dont go missed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextureView = findViewById(R.id.textureView);
        //set up text to speach object and overwrite methods
        mTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTextToSpeech.setLanguage(Locale.ENGLISH);
                    //checking for error setting up language
                    if (result == TextToSpeech.LANG_MISSING_DATA
                            || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.d("TextToSpech", "Language set up error");
                    }
                } else {
                    Log.d("TextToSpech", "set up fail");
                }
            }
        });
        //Int array to hold the drawables
        Integer[] icons = {R.drawable.toilet_icon,R.drawable.help_icon,
                R.drawable.ni_icon,R.drawable.food_icon,R.drawable.yes_icon};
        //String ArrayFor words
        String[] words = {"Toilet", "Help","No", "Food", "Yes"};
        //setupArrayList
        myModel = new ArrayList<>();
        for (int i =0; i<icons.length; i++){
            MyModel model = new MyModel(icons[i], words[i]);
            myModel.add(model);
        }
        //declaring on screen attributes and associating them
        mImageView = findViewById(R.id.word_icon);
        mRightButton =  findViewById(R.id.right_button);
        mLeftButton =  findViewById(R.id.left_button);
        //ensuring all nessesary permissions are granted
        if(allPermissionGranted()){
            startCamera();
        }else{
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS);
        }
        //to use on screen button controls
        mRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               moveRight();
            }
        });
        mLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               moveLeft();
            }
        });
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String word = myModel.get(mPosition).getWords();
                playWord(word);
            }

        });
    }

    private boolean allPermissionGranted() {
//request permissions needed
        for(String permission : REQUIRED_PERMISSIONS){
            if(ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private void startCamera() {
        Log.d(TAG, "startCamera: ");
        //open and bind camera for cameraX implimentation
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider); //also has analisie
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Log.d(TAG, "bindPreview: ");
        //set up depemdancies for camera, preview and analysis in cameraX
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                //hard coded so there is no option to change and face child user
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1280, 720))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        preview.setSurfaceProvider(mTextureView.createSurfaceProvider());
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            //could change to lamda expression howeve supress warning is required
            @SuppressLint("UnsafeExperimentalUsageError")
              @Override
            //labeller
                    public void analyze(@NonNull ImageProxy imageProxy) {
                        if ((imageProxy == null) || (imageProxy.getImage() == null)) {
                            return;
                        } else {
                            @SuppressLint("UnsafeExperimentalUsageError") Image mediaImage = imageProxy.getImage();
                            Log.d(TAG, "analyze: ");
                            InputImage image =
                                    InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                            //load and create local model for ml kit
                            AutoMLImageLabelerLocalModel localModel =
                                    new AutoMLImageLabelerLocalModel.Builder()
                                            .setAssetFilePath("manifest.json")
                                            // or .setAbsoluteFilePath(absolute file path to manifest file)
                                            .build();
                            Log.d(TAG, "analyze: local modle loaded");
                            //set the confidence thresehold
                            // try 0.45 with new check!
                            AutoMLImageLabelerOptions autoMLImageLabelerOptions =
                                    new AutoMLImageLabelerOptions.Builder(localModel)
                                            .setConfidenceThreshold(0.5f)
                                            .build();

                            ImageLabeler labeler = ImageLabeling.getClient(autoMLImageLabelerOptions);
                            Log.d(TAG, "analyze: labeler made");

                            labeler.process(image)
                                    .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                                        @Override
                                        public void onSuccess(List<ImageLabel> labels) {
                                            Log.d(TAG, "onSuccess: making labels");

                                            for (ImageLabel label : labels) {

                                                String text = label.getText();
                                                float confidence = label.getConfidence();
                                                int index = label.getIndex();
                                                Log.d("kinda works", "onSuccess: " + text + " : " + index);
                                                if (index != indexCheck) {
                                                    mPosition = index;
                                                    playWord(text);
                                                }
                                                indexCheck = index;
                                                //set to false to avoid irratating "yes,yes,yes,yes,yes"
                                                //hand needs to be removed from image
                                                //still some false positives registered

                                            }

                                            imageProxy.close(); //needed to set up pipeline for next image
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            Log.d("sorta works", "onFailure: ... idiot"); //get rid of swear mark
                                            imageProxy.close();
                                        }
                                    });
                            //try making an object detector to improve accuracy??

                        }
                    }
            //object dectero
          //  @Override
//            public void analyze(ImageProxy imageProxy) {
//                Image mediaImage = imageProxy.getImage();
//                if (mediaImage != null) {
//                    InputImage image =
//                            InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
//                    LocalModel localModel =
//                            new LocalModel.Builder()
//                                    .setAssetFilePath("model.tflite") //try model.tflite manifest.json
//                                    .build();
//                    Log.d(TAG, "analyze: model loaded");
//                    CustomObjectDetectorOptions customObjectDetectorOptions =
//                            new CustomObjectDetectorOptions.Builder(localModel)
//                                    .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
//                                    .enableClassification()
//                                    .setClassificationConfidenceThreshold(0.5f)
//                                    .setMaxPerObjectLabelCount(3)
//                                    .build();
//                    ObjectDetector objectDetector =
//                            ObjectDetection.getClient(customObjectDetectorOptions);
//                    objectDetector
//                            .process(image)
//                            .addOnFailureListener(e -> {
//                                e.printStackTrace();
//                            })
//                            .addOnSuccessListener(results -> {
//                                Log.d(TAG, "analyze: in on success");
//                                for (DetectedObject detectedObject : results) {
//                                    Rect boundingBox = detectedObject.getBoundingBox();
//                                    Integer trackingId = detectedObject.getTrackingId();
//                                    Log.d(TAG, "analyze: tracking id : "+trackingId);
//                                    Log.d(TAG, "analyze: in for loop");
//                                    for (DetectedObject.Label label : detectedObject.getLabels()) {
//                                        String text = label.getText();
//                                        int index = label.getIndex();
//                                        float confidence = label.getConfidence();
//                                        Log.d(TAG, "analyze: "+ text);
//                                    }
//
//                                }
//
//                            });
//                }
//            }
            });



                //bind to lifecycle of app, means all other threads close with app closure auoit
                Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this,
                        cameraSelector, preview, imageAnalysis);

            }

            public void moveRight() {
                if (mPosition == (myModel.size() - 1)) {
                    mPosition = 0;
                } else {
                    mPosition = mPosition + 1;
                }
                Log.d("rightarrow", "onClick: " + mPosition);
                Integer image = myModel.get(mPosition).getIcons();
                mImageView.setImageResource(image);
            }

            public void moveLeft() {
                if (mPosition == 0) {
                    mPosition = myModel.size() - 1;
                } else {
                    mPosition = mPosition - 1;
                }
                Log.d("lefttarrow", "onClick: " + mPosition);
                Integer image = myModel.get(mPosition).getIcons();
                mImageView.setImageResource(image);
            }

            public void playWord(String word) {

                mTextToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null, "word spoken");
                Integer image = myModel.get(mPosition).getIcons();
                Toast toastImage = new Toast(MainActivity.this);
                toastImage.setGravity(Gravity.CENTER, 0, 0);
                ImageView symbolView = new ImageView(MainActivity.this);
                symbolView.setImageResource(image);
                toastImage.setView(symbolView);
                toastImage.show();
            }


    }