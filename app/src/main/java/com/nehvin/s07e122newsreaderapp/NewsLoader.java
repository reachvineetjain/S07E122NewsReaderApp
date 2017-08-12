package com.nehvin.s07e122newsreaderapp;

import android.content.Context;
import android.database.sqlite.SQLiteStatement;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static android.content.ContentValues.TAG;
import static com.nehvin.s07e122newsreaderapp.MainActivity.articlesDB;


/**
 * Created by Vineet K Jain on 01-Aug-17.
 */

public class NewsLoader extends AsyncTaskLoader<String> {

    private Map<Integer, String> articleTitles = new HashMap<Integer, String>();
    private Map<Integer, String> articleURLs = new HashMap<Integer, String>();
    private ArrayList<Integer> articleIDs = new ArrayList<Integer>();
    private Context context;


    public NewsLoader(Context context) {

        super(context);
        this.context = context;
    }


    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        String result = "";
        URL url ;
        HttpURLConnection urlConnection = null;
        InputStream in = null;
//        InputStreamReader reader = null;

        try {
            url = new URL("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
            urlConnection = getHttpURLConnection(url);

            if (urlConnection.getResponseCode() == 200) {
                in = urlConnection.getInputStream();
                result = readFromStream(in);
            }
            else
            {
                Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
            }

            JSONObject jsonObject;
            String articleTitle="";
            String articleURL="";
            Integer articleID = null;
            String sqlStatement = "";
            SQLiteStatement sqlPrepStmt;

            JSONArray jsonArray = new JSONArray(result);
            articlesDB.execSQL("DELETE FROM articles");
            for(int i = 0; i < 100; i++)
            {
                articleTitle="";
                articleURL="";
                try {
                    String articleInfo="";
                    articleID = jsonArray.getInt(i);
                    url = new URL("https://hacker-news.firebaseio.com/v0/item/"+articleID+".json?print=pretty");

                    urlConnection = getHttpURLConnection(url);

                    if (urlConnection.getResponseCode() == 200) {
                        in = urlConnection.getInputStream();
                        articleInfo = readFromStream(in);
                    }
                    else
                    {
                        Log.e(TAG, "Error response code: " + urlConnection.getResponseCode());
                    }

                    jsonObject = new JSONObject(articleInfo);
                    articleTitle = jsonObject.getString("title");
                    articleURL = jsonObject.getString("url");
                    articleIDs.add(articleID);
                    articleTitles.put(articleID, articleTitle);
                    articleURLs.put(articleID, articleURL);

                    sqlStatement = "INSERT INTO articles (articleID, url, title) values (?, ?, ?)";
                    sqlPrepStmt = articlesDB.compileStatement(sqlStatement);
                    sqlPrepStmt.bindString(1, String.valueOf(articleID));
                    sqlPrepStmt.bindString(2, articleURL);
                    sqlPrepStmt.bindString(3, articleTitle);
                    sqlPrepStmt.execute();

                } catch (JSONException e) {
                    Log.i(TAG, "onCreate: article id : "+articleID.toString());
                    e.printStackTrace();
                }
            }
        } catch (IOException | JSONException e ) {
            e.printStackTrace();
        } finally {
            if(urlConnection != null)
            {
                urlConnection.disconnect();
            }
            if(in != null)
            {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    @NonNull
    private HttpURLConnection getHttpURLConnection(URL url) throws IOException {

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setReadTimeout(10000 /* milliseconds */);
        urlConnection.setConnectTimeout(15000 /* milliseconds */);
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();
        return urlConnection;
    }

    /**
     * Uses a buffered reader to read a complete line rather than a single character at a time.
     * @param inpStream
     * @return
     */
    private String readFromStream(InputStream inpStream){

        Scanner s = new Scanner(inpStream, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}