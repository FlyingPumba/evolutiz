/*
 * ArticleViewer.java
 *
 * Part of the Mirrored app for Android
 *
 * Copyright (C) 2010 Holger Macht <holger@homac.de>
 *
 * This file is released under the GPLv3.
 *
 */

package de.homac.Mirrored;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import java.util.ArrayList;

public class ArticleViewer extends Activity {

    private String TAG;
    private Mirrored app;

    static final int MENU_SAVE_ARTICLE = 0;
    static final int MENU_EXTERNAL_BROWSER = 1;
    static final int MENU_BACK_TO_ARTICLES_LIST = 2;

    private boolean _online;
    private WebView _webview;
    private String _content = "";
    private DisplayMetrics _dm;
    private ArrayList<String> _articleHistory = new ArrayList<String>();

    static final String BASE_URL = "http://m.spiegel.de/";

    @Override
    protected void onCreate(Bundle icicle) {
        app = (Mirrored) getApplication();
        TAG = app.APP_NAME + ", " + "ArticleViewer";

        super.onCreate(icicle);

        _online = app.online();

        if (MDebug.LOG)
            Log.d(TAG, "Setting content view");
        setContentView(R.layout.article_viewer);

        _webview = (WebView) findViewById(R.id.webview);

        Article article = app.getArticle();
        if (MDebug.LOG)
            Log.d(TAG, "Received article from application with title: " + article.title);

        /* Add some debugging */
        if (article.title == null)
            Log.d(TAG, "Article title is null");

        if (article.title != null)
            setTitle(article.title);
        _dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(_dm);

        // check if screen orientation changed and relead the article content
        Mirrored.Orientation newOrientation = _getOrientation(_dm);
        if (app.screenOrientation == null) { //first time
            app.screenOrientation = newOrientation;
        } else if (app.screenOrientation != newOrientation && _online) {
            if (MDebug.LOG)
                Log.d(TAG, "Screen orientation changed, redownloading article content");
            article.resetContent();
//			article.getContent(_dm, _online);
            app.screenOrientation = newOrientation;
        }

        _content = article.getContent(_online);
        _articleHistory.add(_content);
        _webview.loadDataWithBaseURL(BASE_URL, _content, "text/html", "utf-8", null);
    }

    /* On Android 2.1, canGoBack() always returns false when using loadDataWithBaseURL like above. In Android 2.2 it
      * works fine. To make sure it works for all versions, implement own history management with putting the
      * articles in a list and always calling loadData...() */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (MDebug.LOG)
            Log.d(TAG, "onKeyDown(), but canGoBack? " + _webview.canGoBack());
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            int size = _articleHistory.size();
            if (MDebug.LOG)
                Log.d(TAG, "_articleHistory has " + size + " elements");

            if (size == 1) {
                // remove original article, then go back to ArticlesList
                _articleHistory.remove(size - 1);
                return super.onKeyDown(keyCode, event);
            } else {
                // remove currently viewed content
                _articleHistory.remove(size - 1);
                // get the previous history item
                String content = _articleHistory.get(_articleHistory.size() - 1);
                if (MDebug.LOG)
                    Log.d(TAG, "Loading history item: " + content);
                _webview.loadDataWithBaseURL(BASE_URL, content,
                        "text/html", "utf-8", null);
            }

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        if (_online) {
            menu.add(Menu.NONE, MENU_SAVE_ARTICLE, Menu.NONE, R.string.menu_save_article)
                    .setIcon(android.R.drawable.ic_menu_save);
            menu.add(Menu.NONE, MENU_EXTERNAL_BROWSER, Menu.NONE, R.string.menu_external_browser)
                    .setIcon(android.R.drawable.ic_menu_view);
            menu.add(Menu.NONE, MENU_BACK_TO_ARTICLES_LIST, Menu.NONE,
                    R.string.menu_back_to_articles_list).setIcon(R.drawable.ic_menu_back);
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case MENU_SAVE_ARTICLE:
                if (MDebug.LOG)
                    Log.d(TAG, "MENU_SAVE_ARTICLE clicked");

                DisplayMetrics dm = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);

                Article article = app.getArticle();
                // get the content, just to be sure it has been downloaded, give false for internet state to
                // make sure article is trimmed
                article.getContent(false);
                app.getFeedSaver().add(article);
                if (!app.getFeedSaver().save(dm)) {
                    app.showDialog(this, getString(R.string.error_saving));
                }
                return true;

            case MENU_EXTERNAL_BROWSER:
                if (MDebug.LOG) {
                    Log.d(TAG, "MENU_EXTERNAL_BROWSER clicked");
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(app.getArticle().getArticleUrl(0)));
                startActivity(intent);
                return true;

            case MENU_BACK_TO_ARTICLES_LIST:
                if (MDebug.LOG)
                    Log.d(TAG, "MENU_BACK_TO_ARTICLES_LIST clicked");
                this.finish();
        }
        return false;
    }

    private Mirrored.Orientation _getOrientation(DisplayMetrics dm) {
        if (dm.heightPixels < dm.widthPixels) {
            return Mirrored.Orientation.HORIZONTAL;
        } else
            return Mirrored.Orientation.VERTICAL;
    }
}
