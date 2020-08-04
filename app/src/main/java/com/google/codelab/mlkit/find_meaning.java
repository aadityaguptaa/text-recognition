package com.google.codelab.mlkit;

import android.app.ProgressDialog;
import android.content.Context;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.ExecutionException;

import edmt.dev.edmtdevcognitivevision.Contract.AnalysisResult;
import edmt.dev.edmtdevcognitivevision.Rest.VisionServiceException;
import edmt.dev.edmtdevcognitivevision.VisionServiceClient;
import edmt.dev.edmtdevcognitivevision.VisionServiceRestClient;

public class find_meaning extends AppCompatActivity {

    private static String subscriptionKey = "4ca7b53995e041cbab318cbde301835e";
    private static String endpoint = "https://dictdetection.cognitiveservices.azure.com/";
    String url = endpoint + "vision/v3.0/ocr?language=unk&detectOrientation=true";

    private static final String imageToAnalyze =
            "https://7esl.com/wp-content/uploads/2019/12/positive-words-2.jpg";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_meaning);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ImageView imageView = findViewById(R.id.image_view);
        imageView.setImageBitmap(getBitmapFromAsset(this, "shark_sighted.jpg"));
        Button button = findViewById(R.id.button100);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Bitmap bmp = getBitmapFromAsset(getBaseContext(), "shark_sighted.jpg");
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                final VisionServiceClient visionServiceClient = new VisionServiceRestClient(subscriptionKey, url);

                AsyncTask<InputStream, String, String> visionTask = new AsyncTask<InputStream, String, String>() {
                    ProgressDialog progressDialog = new ProgressDialog(find_meaning.this);

                    @Override
                    protected void onPreExecute() {
                        progressDialog.show();
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        progressDialog.dismiss();

                        AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                        StringBuilder result_text = new StringBuilder();

                    }

                    @Override
                    protected String doInBackground(InputStream... inputStreams) {
                        try{
                            publishProgress("Recognizing...");
                            String[] features = {"Description"};
                            String[] details={};

                            AnalysisResult result = visionServiceClient.analyzeImage(inputStreams[0], features, details);
                            String jsonResult = new Gson().toJson(result);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (VisionServiceException e) {
                            e.printStackTrace();
                        }
                    }
                };



                try {
                    String xx = new executeDict().execute(inputStream).get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    Log.i("dict", e.toString());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.i("dict", e.toString());

                }

            }
        });
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
