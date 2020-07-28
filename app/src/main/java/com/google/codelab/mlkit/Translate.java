package com.google.codelab.mlkit;

import android.os.AsyncTask;
import android.util.Log;

import java.util.*;
import com.google.gson.*;
import com.squareup.okhttp.*;

public class Translate extends AsyncTask<String , Void, String> {


    private static String subscriptionKey = "ba73698f4023433a96bcb38324f21f33";
    private static String endpoint = "https://api.cognitive.microsofttranslator.com/";
    String url = endpoint + "/translate?api-version=3.0&to=en";
    public OkHttpClient client = new OkHttpClient();


    public static String prettify(String json_text) {
        JsonParser parser = new JsonParser();
        JsonElement json = parser.parse(json_text);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(json);
    }

    @Override
    protected String doInBackground(String...string) {
        MediaType mediaType = MediaType.parse("application/json");
        Log.i("err" , "2");
        String inp = " \"" + string[0] + "\"";
        RequestBody body = RequestBody.create(mediaType,
                "[{\n\t\"Text\":"+inp +  "\n}]");
        Log.i("err" , "3");
        Request request = new Request.Builder()
                .url(url).post(body)
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .addHeader("Ocp-Apim-Subscription-Region", "eastasia")
                .addHeader("Content-type", "application/json").build();
        Log.i("err" , "4");
        try{
            client.setProtocols(Arrays.asList(Protocol.HTTP_1_1));

            Response response = client.newCall(request).execute();
            Log.i("err" , "5");
            String reu = response.body().string();
            Log.i("result", reu);
            return reu;
        }catch (Exception e){
            Log.i("result", String.valueOf(e));
            Log.i("result", "error1");

        }

        return null;
    }



}
