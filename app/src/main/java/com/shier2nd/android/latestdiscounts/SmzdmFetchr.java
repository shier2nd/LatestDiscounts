package com.shier2nd.android.latestdiscounts;

import android.net.Uri;
import android.util.Log;

import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Woodinner on 5/28/16.
 */
public class SmzdmFetchr {

    private static final String TAG = "SmzdmFetchr";

    private static final String API_S_PARAM = "MJS4lEe6fybJ33KUSAc0FYlCG7o0wJFk";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() +
                        ": with " +
                        urlSpec);
            }

            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<DiscountItem> fetchItems(int page, String timeSort) {

        List<DiscountItem> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.smzdm.com/v1/home/articles/")
                    .buildUpon()
                    .appendQueryParameter("limit", "20")
                    .appendQueryParameter("have_zhuanti", "1")
                    .appendQueryParameter("time_sort", timeSort)
                    .appendQueryParameter("page", String.valueOf(page))
                    .appendQueryParameter("f", "android")
                    .appendQueryParameter("s", API_S_PARAM)
                    .appendQueryParameter("v", "317")
                    .appendQueryParameter("weixin", "0")
                    .build().toString();
            String jsonString = getUrlString(url);
            parseItems(items, jsonString);
        } catch (IOException ioe) {
            Log.i(TAG, "Failed to fetch items", ioe);
        }

        return items;
    }

    /*
    Simplify JSON parsing code by Gson
     */
    private void parseItems(List<DiscountItem> items, String jsonString) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(DiscountItem[].class, new SmzdmDeserializer())
                .create();
        DiscountItem[] discountList = gson.fromJson(jsonString, DiscountItem[].class);

        // Scan discountList
        for (DiscountItem item : discountList) {
            if (item.getWorthyNum() != null) {
                items.add(item);
            }
        }
    }

    private class SmzdmDeserializer implements JsonDeserializer<DiscountItem[]> {

        @Override
        public DiscountItem[] deserialize(JsonElement je, Type type, JsonDeserializationContext jdc)
                throws JsonParseException
        {
            // Get the "discounts" element from the parsed JSON
            JsonElement discounts = je.getAsJsonObject().get("data");
            JsonElement discountArray = discounts.getAsJsonObject().get("rows");

            // Deserialize it. You use a new instance of Gson to avoid infinite recursion
            // to this deserializer
            Gson gson = new GsonBuilder()
                    .setFieldNamingStrategy(new SmzdmFieldNamingStrategy())
                    .create();
            return gson.fromJson(discountArray, DiscountItem[].class);
        }
    }

    private class SmzdmFieldNamingStrategy implements FieldNamingStrategy {

        @Override
        public String translateName(Field f) {
            switch (f.getName()) {
                case "mChannelId":
                    return "article_channel_id";
                case "mDiscountId":
                    return "article_id";
                case "mPicUrl":
                    return "article_pic";
                case "mTitle":
                    return "article_title";
                case "mPrice":
                    return "article_price";
                case "mDate":
                    return "article_format_date";
                case "mMall":
                    return "article_mall";
                case "mCommentNum":
                    return "article_comment";
                case "mWorthyNum":
                    return "article_worthy";
                case "mUnworthyNum":
                    return "article_unworthy";
                case "mTimeSort":
                    return "time_sort";
                default:
                    return f.getName();
            }
        }
    }

}
