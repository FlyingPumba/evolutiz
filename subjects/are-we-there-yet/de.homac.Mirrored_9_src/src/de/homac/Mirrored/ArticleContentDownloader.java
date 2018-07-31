/*
 * ArticleContentDownloader.java
 *
 * Part of the Mirrored app for Android
 *
 * Copyright (C) 2010 Holger Macht <holger@homac.de>
 *
 * This file is released under the GPLv3.
 *
 */

package de.homac.Mirrored;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;
import android.util.DisplayMetrics;

public class ArticleContentDownloader extends Object implements Runnable {

	private Iterator _article_iter = null;
	private final Lock _lock = new ReentrantLock();
	private ArrayList<Article> _articles = null;

	private String TAG;
	private Mirrored app;

	private final boolean _downloadImages;
	private final boolean _downloadContent;
	private final boolean _internetReady;

    public ArticleContentDownloader(Mirrored app, DisplayMetrics dm, ArrayList<Article> articles,
					boolean downloadContent, boolean downloadImages, boolean internetReady) {

		_articles = articles;
		_downloadImages = downloadImages;
		_downloadContent = downloadContent;
		_internetReady = internetReady;

        this.app = app;
		TAG = app.APP_NAME + ", " + "ArticleContentDownloader";
	}
 
	public void download() {
		ArrayList<Thread> threads = new ArrayList<Thread>();
		_article_iter = _articles.iterator();


		for (Article article : _articles) {
			Thread thread = new Thread(this);
			thread.start();
			threads.add(thread);
		}

		// wait for all threads to finish
		try {
			for (Thread thread : threads) {
				thread.join();
			}
		} catch (InterruptedException e) {
			if (MDebug.LOG)
				Log.e(TAG, e.toString());
		}
	}

	public void run() {
		_lock.lock();
		if (_article_iter.hasNext()) {
			Article a = (Article)_article_iter.next();
			_lock.unlock();
			if (_downloadContent)
				a.getContent( _internetReady);
			if (_downloadImages)
				a.getImage(_internetReady);
		}
	}
}
