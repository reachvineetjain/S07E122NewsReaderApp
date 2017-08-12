package com.nehvin.s07e122newsreaderapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ArticleActivity extends AppCompatActivity {

    private static String TAG = ArticleActivity.class.getSimpleName();
    private ShareActionProvider mShareActionProvider;
    private String url_to_share;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        WebView webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());

        Intent i = getIntent();
        url_to_share = i.getStringExtra("articleURL");
        webView.loadUrl(url_to_share);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.hn_share, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.menu_item_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        Log.i(TAG, "onOptionsItemSelected: ");
        int itemID = item.getItemId();
        switch (itemID){
            case R.id.menu_item_share :
                shareIntent();
                return true;
            default:
                return false;
        }
    }

    private void shareIntent() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url_to_share);
        Intent chooser = Intent.createChooser(intent, "Choose your target");

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        }
    }

}