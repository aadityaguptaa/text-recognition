package com.google.codelab.mlkit;

import androidx.appcompat.app.AppCompatActivity;

import com.google.common.io.ByteStreams;
import com.microsoft.azure.cognitiveservices.vision.computervision.*;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class findMeaning extends AppCompatActivity {

    String subscriptionKey = "4ca7b53995e041cbab318cbde301835e";
    String endpoint ="https://dictdetection.cognitiveservices.azure.com/";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_meaning);
        Button button = findViewById(R.id.button98);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... voids) {
                        ComputerVisionClient compVisClient = ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
                        Log.i("a", "\nAzure Cognitive Services Computer Vision - Java Quickstart Sample");
                        RecognizeTextOCRLocal(compVisClient);
                        return null;
                    }
                };


            }
        });

    }

    public void RecognizeTextOCRLocal(ComputerVisionClient client) {
        Log.i("a", "-----------------------------------------------");
        Log.i("a", "RECOGNIZE PRINTED TEXT");

        // Replace this string with the path to your own image.
        String localTextImagePath = getURLForResource(R.drawable.smile);
        Log.i("a", localTextImagePath);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.smile);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        icon.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        try {
            File rawImage = new File(localTextImagePath);
            byte[] localImageBytes = ByteStreams.toByteArray(inputStream);

            // Recognize printed text in local image
            OcrResult ocrResultLocal = client.computerVision().recognizePrintedTextInStream()
                    .withDetectOrientation(true).withImage(localImageBytes).withLanguage(OcrLanguages.EN).execute();
            Log.i("a", "\n");
            Log.i("a", "Recognizing printed text from a local image with OCR ...");
            Log.i("a", "\nLanguage: " + ocrResultLocal.language());
            Log.i("a", "Text angle: %1.3f\n"+ ocrResultLocal.textAngle());
            Log.i("a", "Orientation: " + ocrResultLocal.orientation());

            boolean firstWord = true;

            for (OcrRegion reg : ocrResultLocal.regions()) {

                for (OcrLine line : reg.lines()) {
                    for (OcrWord word : line.words()) {
                        if (firstWord) {
                            Log.i("a", "\nFirst word in first line is \"" + word.text()
                                    + "\" with  bounding box: " + word.boundingBox());
                            firstWord = false;
                            Log.i("a", "\n");
                        }
                        Log.i("a", word.text() + " ");
                    }
                    Log.i("a", "\n");
                }
            }
        }catch (Exception e){
            Log.i("a", e.toString());
        }
    }

    public static String getURLForResource(int resourceId) {
        //use BuildConfig.APPLICATION_ID instead of R.class.getPackage().getName() if both are not same
        return Uri.parse("android.resource://"+R.class.getPackage().getName()+"/" +resourceId).toString();
    }
}
