package com.shier2nd.android.latestdiscounts;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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

    public List<DiscountItem> fetchItems(int page) {

        List<DiscountItem> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.smzdm.com/v1/home/articles/")
                    .buildUpon()
                    .appendQueryParameter("limit", "20")
                    .appendQueryParameter("have_zhuanti", "1")
                    .appendQueryParameter("time_sort", "")
                    .appendQueryParameter("page", String.valueOf(page))
                    .appendQueryParameter("f", "android")
                    .appendQueryParameter("s", API_S_PARAM)
                    .appendQueryParameter("v", "317")
                    .appendQueryParameter("weixin", "0")
                    .build().toString();
            String jsonString = getUrlString(url);
//            Log.i(TAG, "Received JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items, jsonBody);
        } catch (JSONException je) {
            Log.e(TAG, "Failed to parse JSON", je);
        } catch (IOException ioe) {
            Log.i(TAG, "Failed to fetch items", ioe);
        }

        return items;
    }

    private void parseItems(List<DiscountItem> items, JSONObject jsonBody)
            throws IOException, JSONException {

        JSONObject discountsJsonObject = jsonBody.getJSONObject("data");
        JSONArray discountJsonArray = discountsJsonObject.getJSONArray("rows");

        for (int i = 0; i < discountJsonArray.length(); i++) {
            JSONObject discountJsonObject = discountJsonArray.getJSONObject(i);

            if (!discountJsonObject.has("article_price") ||
                    !discountJsonObject.has("article_worthy")) {
                continue;
            }

            DiscountItem item = new DiscountItem();
            item.setChannelId(discountJsonObject.getString("article_channel_id"));
            item.setDiscountId(discountJsonObject.getString("article_id"));
            item.setPicUrl(discountJsonObject.getString("article_pic"));
            item.setTitle(discountJsonObject.getString("article_title"));
            item.setPrice(discountJsonObject.getString("article_price"));
            item.setDate(discountJsonObject.getString("article_format_date"));
            if (discountJsonObject.has("article_mall")) {
                item.setMall(discountJsonObject.getString("article_mall"));
            }
            item.setCommentNum(discountJsonObject.getString("article_comment"));
            item.setWorthyNum(discountJsonObject.getString("article_worthy"));
            item.setUnworthyNum(discountJsonObject.getString("article_unworthy"));

            items.add(item);
        }
    }
}
