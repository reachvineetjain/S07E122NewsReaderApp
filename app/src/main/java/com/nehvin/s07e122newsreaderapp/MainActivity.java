package com.nehvin.s07e122newsreaderapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks, NewsAdapter.ListItemClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String TAG = "News Reader App";
    public static SQLiteDatabase articlesDB;
    List<NewsDetailsView> ndtv = new ArrayList<>();
    private NewsAdapter mAdapter;
    private RecyclerView newsView;
    private int results_from_sharedPref;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //create a db and store the article details in db
        articlesDB = this.openOrCreateDatabase("Articles",MODE_PRIVATE,null);
        articlesDB.execSQL("CREATE TABLE IF NOT EXISTS articles (id INTEGER PRIMARY KEY, articleID INTEGER, " +
                "url VARCHAR, title VARCHAR, content VARCHAR)");

        newsView = (RecyclerView) findViewById(R.id.listOfNews);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        newsView.setLayoutManager(layoutManager);
        newsView.setHasFixedSize(true);
        mAdapter = new NewsAdapter(this, ndtv, this);
        newsView.setAdapter(mAdapter);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        results_from_sharedPref = getNoOfNewsToFetch();
        updateListView();

        //connect to the internet and get the list of news
        getSupportLoaderManager().initLoader(1504, null, this);

    }

    /**
     * get the data from the DB via a cursor and add it to the list of NewsDetailsView
     * */
    public void updateListView() {

        Log.i(TAG, "updateListView: List Updated");
        try {
            Cursor cursor = articlesDB.rawQuery("SELECT * FROM articles ORDER BY articleID DESC", null);
            int articleURLIdx = cursor.getColumnIndex("url");
            int articleTitleIdx = cursor.getColumnIndex("title");
            int countOfArticlesToShow;
            if(results_from_sharedPref < cursor.getCount())
                countOfArticlesToShow = results_from_sharedPref;
            else
                countOfArticlesToShow = cursor.getCount();
            int count = 0;
            Log.i(TAG, "updateListView: "+results_from_sharedPref);
            Log.i(TAG, "updateListView: "+countOfArticlesToShow);
            Log.i(TAG, "updateListView: "+cursor.getCount());
            ndtv.clear();
            if(cursor.moveToFirst())
            {
                do {
                    ndtv.add(new NewsDetailsView(cursor.getString(articleTitleIdx),
                            cursor.getString(articleURLIdx)));
                }while (cursor.moveToNext() && ++count<countOfArticlesToShow );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAdapter.notifyDataSetChanged();
    }

    /*
    * This callback method will get the latest news from the HN website
    * */
    @Override
    public Loader onCreateLoader(int id, Bundle args) {
        Log.i(TAG, "onCreateLoader: ");
        return new NewsLoader(MainActivity.this);
    }

    /*
    * This callback method will be called once the operations of getting the news are completed
    * */
    @Override
    public void onLoadFinished(Loader loader, Object data) {
        Log.i(TAG, "onLoadFinished: ");
        updateListView();
    }

    @Override
    public void onLoaderReset(Loader loader) {

        Log.i(TAG, "onLoaderReset: ");
    }

    /*
    *
    * */
    @Override
    public void onListItemClick(int clickedItemIndex) {
        String url = ndtv.get(clickedItemIndex).url;
        if (! ("".equals(url))) {
            Intent i = new Intent(getApplicationContext(), ArticleActivity.class);
            i.putExtra("articleURL",ndtv.get(clickedItemIndex).url);
            startActivity(i);
        } else {
            Toast.makeText(MainActivity.this, "No URL", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.hn_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int itemID = item.getItemId();
        switch (itemID)
        {
            case R.id.action_settings:
                Intent settingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(settingsActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equalsIgnoreCase("no_of_results_to_show")){
            results_from_sharedPref = Integer.parseInt(sharedPreferences.getString("no_of_results_to_show", "1"));
        }
        updateListView();
    }

    private int getNoOfNewsToFetch()
    {
        int result=100;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        result = Integer.parseInt(sharedPreferences.getString("no_of_results_to_show", "200"));
        Log.i(TAG, "getNoOfNewsToFetch: "+sharedPreferences.getString("no_of_results_to_show", "200"));
        return result;
    }
}