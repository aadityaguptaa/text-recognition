package com.google.codelab.mlkit;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.*;
import com.google.gson.*;
import com.squareup.okhttp.*;

import edmt.dev.edmtdevcognitivevision.Utils.Utils;

import static com.google.codelab.mlkit.MainActivity.*;
import static edmt.dev.edmtdevcognitivevision.Utils.Utils.EOF;

public class executeDict extends AsyncTask<InputStream , Void, String> {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    public static final int EOF = -1;
    private static final int STRING_BUILDER_SIZE = 256;
    public static final String EMPTY = "";


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

    public static int copy(final InputStream input, final OutputStream output) throws IOException {
        final long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static long copy(final InputStream input, final OutputStream output, final int bufferSize)
            throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }

    public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer)
            throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static long copyLarge(final InputStream input, final OutputStream output)
            throws IOException {
        return copy(input, output, DEFAULT_BUFFER_SIZE);
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected String doInBackground(InputStream...inputStreams) {
        MediaType mediaType = MediaType.parse("application/octet-stream");
        Log.i("dict" , "2");
        byte[] data;
        try {
            data = Utils.toByteArray(inputStreams[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String sb = new String(data);

        RequestBody body = RequestBody.create(mediaType,
                sb.toString());
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
