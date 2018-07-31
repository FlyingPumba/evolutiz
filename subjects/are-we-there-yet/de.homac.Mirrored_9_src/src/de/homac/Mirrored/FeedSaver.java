/*
 * FeedSaver.java
 *
 * Part of the Mirrored app for Android
 *
 * Copyright (C) 2010 Holger Macht <holger@homac.de>
 *
 * This file is released under the GPLv3.
 *
 */

package de.homac.Mirrored;

import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class FeedSaver extends Object {

	static public final String SAVE_DIR = "/Android/data/de.homac.Mirrored/";

	private Feed _feed;

	static private String TAG;

	static private final String FILENAME = "articles.xml";

	ArrayList<Article> _articles = new ArrayList();

	public FeedSaver(Mirrored app, Feed feed, DisplayMetrics dm) {
		this._feed = feed;
		TAG = app.APP_NAME + ", " + "FeedSaver";

		ArrayList<Article> existing_articles = _feed.getArticles();
		if (existing_articles == null) {
			if (MDebug.LOG)
				Log.d(TAG, "No existing articles");
			return;
		}
		// add already existing articles
		for (Article article : existing_articles)
			_articles.add(article);
	}

	public void add(Article article) {
		if (MDebug.LOG)
			Log.d(TAG, "Adding article with title: " + article.title);
		_articles.add(article);
	}

	public void remove(Article article) {
		if (MDebug.LOG)
			Log.d(TAG, "Removing article with title: " + article.title);

		int index = _articles.indexOf(article);

		_articles.remove(index);
	}

	public boolean save(DisplayMetrics dm) {
		FileOutputStream fos = null;
		BufferedWriter out;
		String dirname = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + SAVE_DIR;
		File directory = new File(dirname);

		if (MDebug.LOG)
			Log.d(TAG, "Saving");

		if (!directory.exists())
			directory.mkdirs();

		File f = new File(dirname + FILENAME);

		if (storageReady()) {
			if (MDebug.LOG)
				Log.d(TAG, "SD card ready");
		} else {
			if (MDebug.LOG)
				Log.d(TAG, "SD card not ready");
			return false;
		}

		try {
			f.createNewFile();
		} catch (IOException e) {
			if (MDebug.LOG)
				Log.e(TAG, e.toString());
			throw new IllegalStateException("Failed to create " + f.toString());
		}

		try {
			fos = new FileOutputStream(f);

			fos.write(_startXML().getBytes());
			if (_articles != null)
				for (Article article : _articles)
					fos.write(_articleXML(article, dm).getBytes());
			fos.write(_finishXML().getBytes());

		} catch (IOException e) {
			if (MDebug.LOG)
				Log.e(TAG, e.toString());
		} finally {
			if (fos != null) {
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					if (MDebug.LOG)
						Log.e(TAG, e.toString());
				}
			}
		}

		return true;
	}

	public void clear() {
		_articles.clear();
	}

	static public File read() {
		// String data = null;
		String dirname = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + SAVE_DIR;
		File f = new File(dirname + FILENAME);

		if (MDebug.LOG)
			Log.d(TAG, "Reading saved articles");

		if (!f.exists())
			return null;

		return f;
	}

	private String _startXML() {
		String o;

		o = "";
		o += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		o += "<rss version=\"2.0\">\n";
		o += " <channel>\n";
		o += "  <title>Spiegel Online News</title>\n";
		return o;
	}

	private String _articleXML(Article article, DisplayMetrics dm) {
		String o = "";

		o += "\n";
		o += " <item>\n";
		o += "  <title>" + article.title + "</title>\n";
		o += "  <guid>" + article.guid + "</guid>\n";
		o += "  <link>" + article.url.toString() + "</link>\n";
		o += "  <description>" + article.description + "</description>\n";

		if (article.feedCategory == null)
			article.feedCategory = "";

		o += "  <category>" + article.feedCategory + "</category>\n";

		o += "  <content><![CDATA[" + article.getContent(false)
				+ "]]></content>\n";
		o += " </item>\n";

		return o;
	}

	private String _finishXML() {
		String end = "";
		end += " </channel>\n";
		end += "</rss>\n";
		return end;
	}

	public boolean storageReady() {
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state))
			return true;

		return false;
	}
}
