// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.codelab.mlkit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final String TAG = "MainActivity";
    private ImageView mImageView;
    private Button mTextButton;
    private Bitmap mSelectedImage;
    private GraphicOverlay mGraphicOverlay;
    // Max width (portrait mode)
    private Integer mImageMaxWidth;
    // Max height (portrait mode)
    private Integer mImageMaxHeight;

    /**
     * Number of results to show in the UI.
     */
    private static final int RESULTS_TO_SHOW = 3;
    /**
     * Dimensions of inputs.
     */
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE = 3;
    private static final int DIM_IMG_SIZE_X = 224;
    private static final int DIM_IMG_SIZE_Y = 224;

    private final PriorityQueue<Map.Entry<String, Float>> sortedLabels =
            new PriorityQueue<>(
                    RESULTS_TO_SHOW,
                    new Comparator<Map.Entry<String, Float>>() {
                        @Override
                        public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float>
                                o2) {
                            return (o1.getValue()).compareTo(o2.getValue());
                        }
                    });
    /* Preallocated buffers for storing image data. */
    private final int[] intValues = new int[DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mImageView = findViewById(R.id.image_view);

        mTextButton = findViewById(R.id.button_text);

        mGraphicOverlay = findViewById(R.id.graphic_overlay);
        mTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTextRecognition();
            }
        });

        //Request For Camera Permission
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA
            }, 100);
        }

        ImageView cameraClick = findViewById(R.id.imageView2);
        cameraClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open camera
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 100);
            }
        });



        Spinner dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"Image1", "Image2", "Image3", "Image4", "Image5", "Image6", "Image7", "Image8", "Image9", "CapturefromCamera"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout
                .simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        dropdown.setOnItemSelectedListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            Bitmap captureImage = (Bitmap) data.getExtras().get("data");
            mSelectedImage = captureImage;
            ImageView imageView = findViewById(R.id.image_view);

            imageView.setImageBitmap(mSelectedImage);

            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            float scaleFactor =
                    Math.max(
                            (float) mSelectedImage.getWidth() / (float) targetWidth,
                            (float) mSelectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            mSelectedImage,
                            (int) (mSelectedImage.getWidth() / scaleFactor),
                            (int) (mSelectedImage.getHeight() / scaleFactor),
                            true);

            mImageView.setImageBitmap(resizedBitmap);
            mSelectedImage = resizedBitmap;
            runTextRecognition();

        }
    }


    private void runTextRecognition() {
        InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
        TextRecognizer recognizer = TextRecognition.getClient();
        mTextButton.setEnabled(false);
        recognizer.process(image).addOnSuccessListener(new OnSuccessListener<Text>() {
            @Override
            public void onSuccess(Text text) {
                mTextButton.setEnabled(true);
                processTextRecognitionResult(text);
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mTextButton.setEnabled(true);
                        e.printStackTrace();
                    }
                });

    }

    private void processTextRecognitionResult(Text texts) {
        List<Text.TextBlock> blocks = texts.getTextBlocks();
        if (blocks.size() == 0) {
            showToast("No text found");
            return;
        }
        mGraphicOverlay.clear();
        TextView textView2 = findViewById(R.id.input);
        String res = new String();
        int count = 0;
        String aa;
        for (int i = 0; i < blocks.size(); i++) {
            aa = blocks.get(i).getRecognizedLanguage();
            List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                List<Text.Element> elements = lines.get(j).getElements();
                textView2.setVisibility(View.VISIBLE);
                for (int k = 0; k < elements.size(); k++) {
                    GraphicOverlay.Graphic textGraphic = new TextGraphic(mGraphicOverlay, elements.get(k));
                    mGraphicOverlay.add(textGraphic);
                    res += elements.get(k).getText();
                    res += " ";
                }
                count +=1;
                res+="\n";
            }
        }

        try {
            String xx = new Translate().execute(res).get();
            Log.i("result2", xx);
            JSONArray jsonArray = new JSONArray(xx);
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            JSONObject detectedLanguage1 = jsonObject.getJSONObject("detectedLanguage");
            String w = detectedLanguage1.getString("language");
            JSONArray translation = jsonObject.getJSONArray("translations");
            JSONObject op = translation.getJSONObject(0);
            String resss = op.getString("text");
            Log.i("qwerty", resss);
            textView2.setText(res);
            TextView translationTextView = findViewById(R.id.translated);
            translationTextView.setVisibility(View.VISIBLE);
            translationTextView.setText(resss);
            ImageView translateIcon = findViewById(R.id.imageView8);
            translateIcon.setVisibility(View.VISIBLE);
            TextView detectedLanguage = findViewById(R.id.textView4);
            detectedLanguage.setVisibility(View.VISIBLE);
            TextView translatedLanguage = findViewById(R.id.textView5);
            translatedLanguage.setVisibility(View.VISIBLE);
            translatedLanguage.setText("ENGLISH");
            translatedLanguage.setVisibility(View.VISIBLE);

            Dictionary languages = new Hashtable();
            languages.put("am", "Amharic");
            languages.put("ar", "Arabic");
            languages.put("eu", "Basque");
            languages.put("bn", "Bengali");
            languages.put("en-GB", "English (UK)");
            languages.put("pt-BR", "Portuguese");
            languages.put("bg", "Bulgarian");
            languages.put("ca", "Catalan");
            languages.put("chr", "Cherokee");
            languages.put("hr", "Croatian");
            languages.put("cs", "Czech");
            languages.put("da", "Danish");
            languages.put("nl", "Dutch");
            languages.put("en", "English (US)");

            languages.put("et", "Estonian");
            languages.put("fil", "Filipino");
            languages.put("fi", "Finnish");
            languages.put("fr", "French");
            languages.put("de", "German");
            languages.put("el", "Greek");
            languages.put("gu", "Gujarati");
            languages.put("iw", "Hebrew");
            languages.put("hi", "Hindi");
            languages.put("ru", "Russian");
            languages.put("es", "Spanish");
            languages.put("it", "Italian");
            w = (String) languages.get(w);
            Log.i("lang", w);
            detectedLanguage.setText(w.toUpperCase());
        } catch (Exception e) {
            Log.e("e2", String.valueOf(e));
        }

    }



    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private Integer getImageMaxWidth() {
        if (mImageMaxWidth == null) {
            mImageMaxWidth = mImageView.getWidth();
        }
        return mImageMaxWidth;
    }


    private Integer getImageMaxHeight() {
        if (mImageMaxHeight == null) {
            mImageMaxHeight =
                    mImageView.getHeight();
        }
        return mImageMaxHeight;
    }

    private Pair<Integer, Integer> getTargetedWidthHeight() {
        int targetWidth;
        int targetHeight;
        int maxWidthForPortraitMode = getImageMaxWidth();
        int maxHeightForPortraitMode = getImageMaxHeight();
        targetWidth = maxWidthForPortraitMode;
        targetHeight = maxHeightForPortraitMode;
        return new Pair<>(targetWidth, targetHeight);
    }

    public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
        mGraphicOverlay.clear();
        switch (position) {
            case 4:
                mSelectedImage = getBitmapFromAsset(this, "spanishWarningcp.jpg");
                break;
            case 8:
                mSelectedImage = getBitmapFromAsset(this, "warningRussin.jpg");
                break;
            case 2:
                mSelectedImage = getBitmapFromAsset(this, "professor.jpg");
                break;
            case 0:
                mSelectedImage = getBitmapFromAsset(this, "span2.jpg");
                break;
            case 7:
                mSelectedImage = getBitmapFromAsset(this, "shark_sighted.jpg");
                break;
            case 6:
                mSelectedImage = getBitmapFromAsset(this, "german1.jpg");
                break;
            case 1:
                mSelectedImage = getBitmapFromAsset(this, "german2.jpg");
                break;
            case 3:
                mSelectedImage = getBitmapFromAsset(this, "frenchWarning.jpg");
                break;
            case 5:
                mSelectedImage = getBitmapFromAsset(this, "italianBoard.jpg");
                break;


        }
        if (mSelectedImage != null) {
            Pair<Integer, Integer> targetedSize = getTargetedWidthHeight();

            int targetWidth = targetedSize.first;
            int maxHeight = targetedSize.second;

            float scaleFactor =
                    Math.max(
                            (float) mSelectedImage.getWidth() / (float) targetWidth,
                            (float) mSelectedImage.getHeight() / (float) maxHeight);

            Bitmap resizedBitmap =
                    Bitmap.createScaledBitmap(
                            mSelectedImage,
                            (int) (mSelectedImage.getWidth() / scaleFactor),
                            (int) (mSelectedImage.getHeight() / scaleFactor),
                            true);

            mImageView.setImageBitmap(resizedBitmap);
            mSelectedImage = resizedBitmap;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream is;
        Bitmap bitmap = null;
        try {
            is = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }
}
