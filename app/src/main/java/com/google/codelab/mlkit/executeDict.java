package com.google.codelab.mlkit;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.*;
import com.google.gson.*;
import com.squareup.okhttp.*;

public class executeDict extends AsyncTask<Void , Void, String> {


    private static String subscriptionKey = "4ca7b53995e041cbab318cbde301835e";
    private static String endpoint = "https://dictdetection.cognitiveservices.azure.com/";
    String url = endpoint + "vision/v3.0/ocr?language=unk&detectOrientation=true";

    private static final String imageToAnalyze =
            "https://7esl.com/wp-content/uploads/2019/12/positive-words-2.jpg";

    public OkHttpClient client = new OkHttpClient();

   
    public static String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(json_text);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    @Override
    protected String doInBackground(Void...voids) {
        MediaType mediaType = MediaType.parse("application/json");
        Log.i("dict" , "2");
        RequestBody body = RequestBody.create(mediaType,
                "{\"url\":\"" + imageToAnalyze + "\"}");
        Log.i("dict" , "3");
        Request request = new Request.Builder()
                .url(url).post(body)
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .addHeader("Ocp-Apim-Subscription-Region", "eastus")
                .addHeader("Content-type", "application/json")

                .build();
        Log.i("dict" , "4");
        try{
            client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

            Response response = client.newCall(request).execute();
            Log.i("dict" , "5");
            String reu = response.body().string();
            Log.i("dict", reu);
            return reu;
        }catch (Exception e){
            Log.i("dict", String.valueOf(e));
            Log.i("dict", "error1");

        }

        return null;
    }



}
