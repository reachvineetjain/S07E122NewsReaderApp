package com.nehvin.s07e122newsreaderapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks{

    private static final String TAG = "News Reader App";
    public static SQLiteDatabase articlesDB;
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
//        getArticles();
        getSupportLoaderManager().initLoader(1504, null, this);
    }

//    private void getArticles() {
//
//        DownLoadTask task = new DownLoadTask();
//        task.execute("https://hacker-news.firebaseio.com/v0/topstories.json?print=pretty");
//
//    }

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

    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "onCreateLoader: ");
        return new NewsLoader(MainActivity.this);
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Log.i(TAG, "onLoadFinished: ");
        updateListView();
    }

    @Override
    public void onLoaderReset(Loader loader) {
        Log.i(TAG, "onLoaderReset: ");
    }


//    private class DownLoadTask extends AsyncTask<String, Void, String>{
//
//
//
//
//        @Override
//        protected String doInBackground(String... urls) {
//
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            updateListView();
//        }
//    }
}