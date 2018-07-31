/*
 * Feed.java
 *
 * Part of the Mirrored app for Android
 *
 * Copyright (C) 2010 Holger Macht <holger@homac.de>
 *
 * This file is released under the GPLv3.
 *
 */

package de.homac.Mirrored;

import android.util.Log;

import java.net.URL;
import java.util.ArrayList;

class Feed extends RSSHandler {

	public Feed(Mirrored m, URL url, boolean online) {
		super(m, url, online);
	}

	// only return those articles with a specific feedCategory
	public ArrayList getArticles(String category) {
		ArrayList<Article> articles;
		ArrayList<Article> all_articles = getArticles();

		if (category.equals(app.BASE_CATEGORY))
			return all_articles;

		if (all_articles == null) {
			if (MDebug.LOG)
				Log.d(TAG, "No articles");
			return null;
		}

		articles = new ArrayList();

		for (Article article : all_articles)
			if (article.feedCategory.equals(category))
				articles.add(article);

		return articles;
	}
}
