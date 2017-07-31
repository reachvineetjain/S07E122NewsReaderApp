package com.nehvin.s07e122newsreaderapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "News Reader App";
    private Map<Integer, String> articleTitles = new HashMap<Integer, String>();
    private Map<Integer, String> articleURLs = new HashMap<Integer, String>();
    private ArrayList<Integer> articleIDs = new ArrayList<Integer>();
    SQLiteDatabase articlesDB;
    ArrayList<String> newsTitles = new ArrayList<String>();
    ArrayAdapter arrayAdapter;
    ArrayList<String> newsURLs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView listOfNews = (ListView)findViewById(R.id.listOfNews);
        arrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,newsTitles);
        listOfNews.setAdapter(arrayAdapter);

        listOfNews.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(!newsURLs.get(position).equals("")) {
                    Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
                    i.putExtra("articleURL",newsURLs.get(position));
                    startActivity(i);
                }
                else
                    Toast.makeText(MainActivity.this, "Invalid URL", Toast.LENGTH_SHORT).show();
            }
        });

        //create a db and store the article details in db
        articlesDB = this.openOrCreateDatabase("Articles",MODE_PRIVATE,null);
        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleID INTEGER, " +
                "url VARCHAR, title VARCHAR, content VARCHAR)");
        //connect to the internet and get the list of news
        updateListView();
        getArticles();
    }

    private void getArticles() {

        DownLoadTask task = new DownLoadTask();
        task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");

    }

    private void updateListView() {

        Log.i(TAG, "updateListView: List Updated");
        try {
            Cursor cursor = articlesDB.rawQuery("SELECT * FROM articles ORDER BY articleID DESC", null);
//            int idIdx = cursor.getColumnIndex("id");
//            int articleIDIdx = cursor.getColumnIndex("articleID");
            int articleURLIdx = cursor.getColumnIndex("url");
            int articleTitleIdx = cursor.getColumnIndex("title");
            newsTitles.clear();
            newsURLs.clear();
            if(cursor.moveToFirst())
            {
                do {
                    newsTitles.add(cursor.getString(articleTitleIdx));
                    newsURLs.add(cursor.getString(articleURLIdx));
                }while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        arrayAdapter.notifyDataSetChanged();
    }

    private class DownLoadTask extends AsyncTask<String, Void, String>{

        /**
         * Uses a buffered reader to read a complete line rather than a single character at a time.
         * @param inpStream
         * @return
         */
        private String readFromStream(InputStream inpStream){
            StringBuilder result = new StringBuilder();
            try {
                if(inpStream != null) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inpStream, Charset.forName("UTF-8"));
                    BufferedReader reader = new BufferedReader(inputStreamReader);
                    String line = reader.readLine();
                    while (line != null)
                    {
                        result.append(line);
                        line = reader.readLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return result.toString();
        }



        @Override
        protected String doInBackground(String... urls) {

            String result = "";
            URL url = null;
            HttpURLConnection urlConnection = null;
            InputStream in = null;
            InputStreamReader reader = null;
            int data;
            char current;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                in = urlConnection.getInputStream();
                result = readFromStream(in);
//                reader = new InputStreamReader(in);
//                data = reader.read();
//                while (data != -1)
//                {
//                    current = (char) data;
//                    result += current;
//                    data = reader.read();
//                }

/***/

                JSONObject jsonObject;
                String articleTitle="";
                String articleURL="";
                Integer articleID = null;
                String sqlStatement = "";
                SQLiteStatement sqlPrepStmt;

                JSONArray jsonArray = new JSONArray(result);
                articlesDB.execSQL("DELETE FROM articles");
                for(int i = 0; i < 20; i++)
                {
                    articleTitle="";
                    articleURL="";
                    try {
                        String articleInfo="";
                        articleID = jsonArray.getInt(i);
                        url = new URL("https://hacker-news.firebaseio.com/v0/item/"+articleID+".json?print=pretty");

                        urlConnection = (HttpURLConnection) url.openConnection();
                        in = urlConnection.getInputStream();
                        articleInfo = readFromStream(in);
//                        reader = new InputStreamReader(in);
//                        data = reader.read();
//                        String articleInfo="";
//                        while (data != -1)
//                        {
//                            current = (char) data;
//                            articleInfo += current;
//                            data = reader.read();
//                        }

                        jsonObject = new JSONObject(articleInfo);
                        articleTitle = jsonObject.getString("title");
                        articleURL = jsonObject.getString("url");
                        articleIDs.add(articleID);
                        articleTitles.put(articleID, articleTitle);
                        articleURLs.put(articleID, articleURL);

//                    sqlStatement = "INSERT INTO articles (id, articleID, url, title) values (?, ?, ?, ?)";
//                    sqlPrepStmt = articlesDB.compileStatement(sqlStatement);
//                    sqlPrepStmt.bindString(1, String.valueOf(i+1));
//                    sqlPrepStmt.bindString(2, String.valueOf(articleID));
//                    sqlPrepStmt.bindString(3, articleURL);
//                    sqlPrepStmt.bindString(4, articleTitle);
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


/***/
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } finally{
                if(urlConnection != null)
                {
                    urlConnection.disconnect();
                }
                if(reader != null){
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
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

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            updateListView();
        }
    }
}