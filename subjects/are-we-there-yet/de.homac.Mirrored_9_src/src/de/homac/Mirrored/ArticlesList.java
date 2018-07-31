/*
 * ArticleList.java
 *
 * Part of the Mirrored app for Android
 *
 * Copyright (C) 2010 Holger Macht <holger@homac.de>
 *
 * This file is released under the GPLv3.
 *
 */

package de.homac.Mirrored;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class ArticlesList extends ListActivity implements Runnable {
	static final int CONTEXT_MENU_DELETE_ID = 0;
	static final int CONTEXT_MENU_SAVE_ID = 1;
	static final int MENU_CATEGORIES = 0;
	static final int MENU_PREFERENCES = 1;
	static final int MENU_SAVE_ALL = 2;
	static final int MENU_DELETE_ALL = 3;
	static final int MENU_OFFLINE_MODE = 4;
	static final int MENU_ONLINE_MODE = 5;
	static final int MENU_REFRESH = 6;

	// static final String BASE_FEED =
	// "http://www.spiegel.de/schlagzeilen/index.rss";
	static final String FEED_PREFIX = "http://www.spiegel.de/";
	static final String FEED_SUFFIX = "/index.rss";
	static final String BASE_CATEGORY_FEED = FEED_PREFIX
			+ Mirrored.BASE_CATEGORY + FEED_SUFFIX;

	private ArrayList<Article> _articles = null;
	private ArrayList<Bitmap> _article_images = new ArrayList<Bitmap>();
	private Feed _feed;
	private FeedSaver _saver;
	private URL _url;
	private String _prefStartWithCategory;
	private boolean _internetReady;
	private boolean _saveAllArticles = false;
	private boolean _downloadArticleParts = false;
	private ProgressDialog _pdialog;
	DisplayMetrics _displayMetrics = new DisplayMetrics();

	private String TAG;
	private Mirrored app;

	@Override
	protected void onCreate(Bundle icicle) {
		try {
			app = (Mirrored) getApplication();
			TAG = app.APP_NAME + ", " + "ArticlesList";

			if (MDebug.LOG)
				Log.d(TAG, "onCreate()");

			super.onCreate(icicle);

			_internetReady = app.online();

			// if (_prefDarkBackground)
			// setTheme(android.R.style.Theme_Black);

			if (MDebug.LOG)
				Log.d(TAG, "Setting content view");
			setContentView(R.layout.articles_list);

			String category = app.BASE_CATEGORY;
			// check if we're coming from the feedCategory view and/or have a
			// preference feedCategory
			if (getIntent().hasExtra(app.EXTRA_CATEGORY)) {
				if (MDebug.LOG)
					Log.d(TAG, "hasExtra(app.EXTRA_CATEGORY)");

				category = getIntent().getExtras()
						.getString(app.EXTRA_CATEGORY);

				if (category.equals(app.BASE_CATEGORY))
					_url = new URL(BASE_CATEGORY_FEED);
				else
					_url = new URL(FEED_PREFIX + category + FEED_SUFFIX);
			} else {
				if (MDebug.LOG)
					Log.d(TAG, "!hasExtra(app.EXTRA_CATEGORY)");

				String startWithCategory = app.getStringPreference(
						"PrefStartWithCategory", null);
				if (startWithCategory != null
						&& startWithCategory.length() != 0
						&& !startWithCategory.equals("Schlagzeilen")) {
					if (MDebug.LOG)
						Log.d(TAG, "Got feedCategory from preferences: "
								+ startWithCategory);

					_url = new URL(BASE_CATEGORY_FEED + startWithCategory);
					category = startWithCategory;
				} else {
					if (MDebug.LOG)
						Log.d(TAG, "No feedCategory set, using BASE_FEED: "
								+ BASE_CATEGORY_FEED);
					_url = new URL(BASE_CATEGORY_FEED);
				}
			}
			String title = category.substring(0, 1).toUpperCase()
					+ category.substring(1);
			if (!_internetReady) {
				title += " (" + getString(R.string.caption_offline) + ")";
				if (!app.getBooleanPreference("PrefStartWithOfflineMode", false)) {
					Toast.makeText(getApplicationContext(),
							R.string.switch_to_offline_mode, Toast.LENGTH_LONG)
							.show();
				}
			}
			setTitle(title);

			_pdialog = ProgressDialog.show(this, "",
					getString(R.string.progress_dialog_load), true, false);

			Thread thread = new Thread(this);
			thread.start();

			registerForContextMenu(getListView());

		} catch (MalformedURLException e) {
			if (MDebug.LOG)
				Log.e(TAG, e.toString());
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		if (MDebug.LOG)
			Log.d(TAG, "onConfigurationChanged()");
		setContentView(R.layout.articles_list);
		registerForContextMenu(getListView());
	}

	public void run() {

		if (_saveAllArticles) {
			_saveAllArticles = false;

			ArticleContentDownloader downloader = new ArticleContentDownloader(
					app, _displayMetrics(), _articles, true, false,
					_internetReady);
			downloader.download();

			for (Article article : _articles) {
				// make sure we actually have content
				_saver.add(article);
			}

			_saver.save(_displayMetrics());
			_pdialog.dismiss();

			return;

		} else if (_downloadArticleParts) {
			_downloadArticleParts = false;

			ArticleContentDownloader downloader = new ArticleContentDownloader(
					app, _displayMetrics(), _articles,
					app.getBooleanPreference("PrefDownloadAllArticles", false),
					app.getBooleanPreference("PrefDownloadImages", true)
							&& _internetReady, _internetReady);
			downloader.download();

			_handler.sendEmptyMessage(0);
			return;
		}

		// first thread run, run only once
		_feed = new Feed(app, _url, _internetReady);

		if (getIntent().hasExtra(app.EXTRA_CATEGORY)) {
			String category = getIntent().getExtras().getString(
					app.EXTRA_CATEGORY);

			_articles = _feed.getArticles(category);
		} else
			_articles = _feed.getArticles(app.BASE_CATEGORY);

		if (_articles == null) {
			if (MDebug.LOG)
				Log.d(TAG, "No articles available");
			_handler.sendEmptyMessage(0);
			return;
		}

		// get offline feed also if online
		if (_internetReady) {
			Feed offlineFeed = new Feed(app, _url, false);
			if (offlineFeed.getArticles() != null) {
				if (MDebug.LOG)
					Log.d(TAG, "Offline feed has "
							+ offlineFeed.getArticles().size() + " articles");
			} else {
				if (MDebug.LOG)
					Log.d(TAG, "Offline feed is null");
			}

			_saver = new FeedSaver(app, offlineFeed, _displayMetrics());
		} else
			_saver = new FeedSaver(app, _feed, _displayMetrics());

		if (_internetReady) {
			_downloadArticleParts = true;
			Thread thread = new Thread(this);
			thread.start();
		} else {
			// we're finally done
			if (MDebug.LOG)
				Log.d(TAG, "all articles fetched, sending message");
			_handler.sendEmptyMessage(0);
		}
	}

	private Handler _handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (_articles == null) {
				if (MDebug.LOG)
					Log.d(TAG, "no articles");
				return;
			}

			for (Article article : _articles) {
				_article_images.add(article.getImage(app.getBooleanPreference(
						"PrefDownloadImages", true) && _internetReady));
			}

			setListAdapter(new IconicAdapter());
			_pdialog.dismiss();

			if (_articles.size() == 0)
				app.showDialog(ArticlesList.this,
						getString(R.string.no_articles));
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(Menu.NONE, MENU_CATEGORIES, Menu.NONE,
				R.string.menu_categories).setIcon(
				android.R.drawable.ic_menu_more);
		menu.add(Menu.NONE, MENU_PREFERENCES, Menu.NONE,
				R.string.menu_preferences).setIcon(
				android.R.drawable.ic_menu_preferences);

		if (_internetReady) {
			menu.add(Menu.NONE, MENU_OFFLINE_MODE, Menu.NONE,
					R.string.menu_offline_mode).setIcon(
					android.R.drawable.ic_menu_close_clear_cancel);
			menu.add(Menu.NONE, MENU_REFRESH, Menu.NONE, R.string.menu_refresh)
					.setIcon(R.drawable.ic_menu_refresh);
			menu.add(Menu.NONE, MENU_SAVE_ALL, Menu.NONE,
					R.string.menu_save_all).setIcon(
					android.R.drawable.ic_menu_save);
		} else {
			menu.add(Menu.NONE, MENU_ONLINE_MODE, Menu.NONE,
					R.string.menu_online_mode).setIcon(
					android.R.drawable.ic_menu_upload);
			if (_articles.size() > 0)
				menu.add(Menu.NONE, MENU_DELETE_ALL, Menu.NONE,
						R.string.menu_delete_all).setIcon(
						android.R.drawable.ic_menu_delete);
		}

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;

		switch (item.getItemId()) {
			case MENU_CATEGORIES :
				if (MDebug.LOG)
					Log.d(TAG, "MENU_CATEGORIES clicked");

				intent = new Intent(this, CategoriesList.class);
				// if we're coming from the CategoriesView, don't remember a new
				// CategoriesView,
				// we need only one instance
				if (getIntent().hasExtra(app.EXTRA_CATEGORY)) {
					intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
				}
				intent.setAction(Intent.ACTION_VIEW);
				if (MDebug.LOG)
					Log.d(TAG, "Starting CategoriesView");
				startActivity(intent);
				this.finish();

				return true;

			case MENU_PREFERENCES :
				if (MDebug.LOG)
					Log.d(TAG, "MENU_PREFERENCES clicked");

				Iterator _article_iter = null;
				_article_iter = _articles.iterator();
				for (Article article : _articles) {
					article.resetContent();
				}

				intent = new Intent(this, Preferences.class);
				startActivity(intent);

				return true;
			case MENU_SAVE_ALL :
				if (MDebug.LOG)
					Log.d(TAG, "MENU_SAVE_ALL clicked");

				if (_saver.storageReady()) {
					_pdialog = ProgressDialog.show(this, "",
							getString(R.string.progress_dialog_save_all), true,
							false);
					_saveAllArticles = true;
					Thread thread = new Thread(this);
					thread.start();
				} else {
					app.showDialog(ArticlesList.this,
							getString(R.string.error_saving));
				}

				return true;

			case MENU_DELETE_ALL :
				if (MDebug.LOG)
					Log.d(TAG, "MENU_DELETE_ALL clicked");

				DisplayMetrics dm = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(
						_displayMetrics());

				// delete articles from last to first and don't use for loop or
				// iterator to prevent
				// ConcurrentException
				while (_articles.size() > 0) {
					Article article = _articles.get(_articles.size() - 1);
					if (MDebug.LOG)
						Log.d(TAG, "Removing article with title: "
								+ article.title);
					_saver.remove(article);
					// remove deleted row
					((IconicAdapter) getListView().getAdapter())
							.remove(article);
				}

				if (!_saver.save(_displayMetrics()))
					app.showDialog(this, getString(R.string.error_saving));

				return true;

			case MENU_OFFLINE_MODE :
				if (MDebug.LOG)
					Log.d(TAG, "MENU_OFFLINE_MODE clicked");

				intent = new Intent(this, ArticlesList.class);
				app.setOfflineMode(true);
				intent.setAction(Intent.ACTION_VIEW);
				startActivity(intent);
				this.finish();

				return true;

			case MENU_ONLINE_MODE :
				if (MDebug.LOG)
					Log.d(TAG, "MENU_ONLINE_MODE clicked");

				app.setOfflineMode(false);

				if (!app.online())
					app.showDialog(this,
							getString(R.string.please_check_internet));
				else {
					intent = new Intent(this, ArticlesList.class);
					intent.setAction(Intent.ACTION_VIEW);
					startActivity(intent);
					this.finish();
				}

				return true;

			case MENU_REFRESH :
				if (MDebug.LOG)
					Log.d(TAG, "MENU_REFRESH clicked");

				intent = new Intent(this, ArticlesList.class);

				if (getIntent().hasExtra(app.EXTRA_CATEGORY))
					intent.putExtra(app.EXTRA_CATEGORY, getIntent().getExtras()
							.getString(app.EXTRA_CATEGORY));

				intent.setAction(Intent.ACTION_VIEW);
				startActivity(intent);
				this.finish();
		}

		return false;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (_internetReady)
			menu.add(0, CONTEXT_MENU_SAVE_ID, 0, R.string.context_menu_save);
		else
			menu.add(0, CONTEXT_MENU_DELETE_ID, 0, R.string.context_menu_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		Article article;
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(_displayMetrics());

		switch (item.getItemId()) {
			case CONTEXT_MENU_DELETE_ID :
				if (MDebug.LOG)
					Log.d(TAG, "CONTEXT_MENU_DELETE_ID clicked");

				article = _articles.get(info.position);

				if (MDebug.LOG)
					Log.d(TAG, "Removing article with title: " + article.title);

				_saver.remove(article);
				// remove deleted row
				((IconicAdapter) getListView().getAdapter()).remove(article);

				if (!_saver.save(_displayMetrics()))
					app.showDialog(this, getString(R.string.error_saving));
				else
					Toast.makeText(getApplicationContext(),
							R.string.article_deleted, Toast.LENGTH_LONG).show();

				return true;

			case CONTEXT_MENU_SAVE_ID :
				if (MDebug.LOG)
					Log.d(TAG, "CONTEXT_MENU_SAVE_ID clicked");

				article = _articles.get(info.position);
				// get the content, just to be sure it has been downloaded, give
				// false for internet state to
				// make sure article is trimmed
				_saver.add(article);

				if (!_saver.save(_displayMetrics()))
					app.showDialog(this, getString(R.string.error_saving));

				Toast.makeText(getApplicationContext(), R.string.article_saved,
						Toast.LENGTH_LONG).show();

				// make sure the articles is redownloaded
				if (_internetReady)
					article.resetContent();

				return true;

			default :
				return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Intent intent = new Intent(this, ArticleViewer.class);
		Article article = _articles.get(position);

		app.setArticle(article);
		app.setFeedSaver(_saver);

		startActivity(intent);
	}

	private DisplayMetrics _displayMetrics() {
		getWindowManager().getDefaultDisplay().getMetrics(_displayMetrics);
		return _displayMetrics;
	}

	class IconicAdapter extends ArrayAdapter<Article> {

		IconicAdapter() {
			super(ArticlesList.this, R.layout.article_row, _articles);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View row = convertView;

			if (row == null) {
				if (MDebug.LOG)
					Log.d(TAG, "row is null in getView()");

				LayoutInflater inflater = getLayoutInflater();

				row = inflater.inflate(R.layout.article_row, parent, false);

				/*
				 * row.setTag(R.id.article_headline,
				 * row.findViewById(R.id.article_headline));
				 * row.setTag(R.id.article_description,
				 * row.findViewById(R.id.article_description));
				 * row.setTag(R.id.article_image,
				 * row.findViewById(R.id.article_image));
				 */
			}

			Article article = _articles.get(position);

			TextView headline = (TextView) row
					.findViewById(R.id.article_headline);
			TextView date = (TextView) row.findViewById(R.id.article_date);
			ImageView image = (ImageView) row.findViewById(R.id.article_image);
			TextView description = (TextView) row
					.findViewById(R.id.article_description);

			Bitmap b = _article_images.get(position);

			headline.setText(article.title);
			image.setImageBitmap(b);
			description.setText(Html.fromHtml(article.description));
			date.setText(article.dateString());

			return row;
		}
	}
}
