/*
 * Article.java
 *
 * Part of the Mirrored app for Android
 *
 * Copyright (C) 2010 Holger Macht <holger@homac.de>
 *
 * Support for multiple page articles by Christoph Robbert <crobbert@mail.upb.de>
 *
 * This file is released under the GPLv3.
 *
 */

package de.homac.Mirrored;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

class Article extends Object {

	public String title = "";
	public String description = "";
	public URL image_url;
	public Bitmap image = null;
	public URL url;
	public String content = "";
	public String feedCategory = "";
	public String guid = "";
	public String pubDate = "";

	public Mirrored app;

	static private final String ARTICLE_URL = "http://m.spiegel.de/";
	static private final String TAG = "Mirrored," + "Article";
	private static final String TEASER = "<p id=\"spIntroTeaser\">";
	private static final String CONTENT = "<div class=\"spArticleContent\"";

	public Article(Mirrored app) {
		this.app = app;
	}

	public Article(Mirrored app, String urlString) {
		try {
			this.app = app;
			this.url = new URL(urlString);

		} catch (MalformedURLException e) {
			if (MDebug.LOG)
				Log.e("Mirrored", e.toString());
		}

	}

	public Article(Article a) {
		title = a.title;
		url = a.url;
		image_url = a.image_url;
		description = a.description;
		content = a.content;
		feedCategory = a.feedCategory;
		image = a.image;
		guid = a.guid;
		pubDate = a.pubDate;
		app = a.app;
	}

	public String dateString() {
		if (pubDate == null || pubDate.length() == 0) {
			return null;
		}

		if (MDebug.LOG)
			Log.d(TAG, "dateString()");

		SimpleDateFormat format = new SimpleDateFormat(
					       "EEE, dd MMM yyyy HH:mm:ss +0200", Locale.ENGLISH);
		format.setTimeZone(TimeZone.getDefault());

		Date d = format.parse(pubDate, new ParsePosition(0));
		if (d == null)
			return "";

		SimpleDateFormat format2 = new SimpleDateFormat("d. MMMM yyyy, HH:mm",
								Locale.getDefault());
		return format2.format(d);
	}

	private String _id() {
		if (guid == null || guid.length() == 0) {
			return null;
		}

		String id = "";
		int start = url.toString().indexOf("-a-");
		if (start == -1) {
			if (MDebug.LOG)
				Log.e(TAG, "Couldn't calculate article id");
			return null;
		}

		int end = url.toString().indexOf(".html");
		id = url.toString().substring(start + 3, end);

		if (MDebug.LOG)
			Log.e(TAG, "Article id is " + id);

		return id;
	}

	private String _downloadContent(boolean online, int page) {
		StringBuilder sb = new StringBuilder();
		try {

			URL url = new URL(getArticleUrl(page));
			if (MDebug.LOG)
				Log.d(TAG, "Downloading " + url.toString());

			InputStream is = url.openStream();

			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "ISO-8859-1"), 8 * 1024);

			sb.append(getArticleContent(reader, page > 1));
			String line;
			boolean couldHasNext = false;
			while ((line = reader.readLine()) != null) {
				if (line.contains("<li class=\"spMultiPagerLink\">")) {
					couldHasNext = true;
				} else if (couldHasNext && line.contains(">WEITER</a>")) {
					Log.d(TAG, "Downloading next page");
					sb.append(this.getContent(online, page + 1));
				}
			}
			is.close();
		} catch (MalformedURLException e) {
			if (MDebug.LOG)
				Log.e("Mirrored", e.toString());
		} catch (IOException e) {
			if (MDebug.LOG)
				Log.e("Mirrored", e.toString());
		}
		if (page == 1) {
			sb.append("</body></html>");
		}

		return sb.toString();
	}

	public String getArticleUrl(int page) {
		return ARTICLE_URL + _categories() + "/a-" + _id()
				+ (page > 1 ? "-" + page : "") + ".html";
	}

	private String _categories() {
		if (guid == null || guid.length() == 0) {
			return "";
		}
		String split[] = guid.toString().split("/");
		if (split.length == 6) {
			return split[3] + "/" + split[4];
		} else if (split.length == 5) {
			return split[3];
		}
		if (MDebug.LOG)
			Log.e(TAG, "Couldn't calculate category");
		return "";
	}

	private Bitmap _downloadImage() {
		Bitmap bitmap = null;

		try {
			bitmap = BitmapFactory.decodeStream(image_url.openStream());
		} catch (IOException e) {
			if (MDebug.LOG)
				Log.e(TAG, e.toString());
		}

		return bitmap;
	}

	private String getArticleContent(BufferedReader reader, boolean skipTeaser)
			throws IOException {
		StringBuilder text = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null && !(line.contains(CONTENT))) {
			line = line.trim();
			if (!skipTeaser) {
				if (line.contains("<head>") || line.startsWith("<link")
						|| line.contains("<meta")) {
					text.append(line);
				}
			}
			continue;
		}
		text.append(line.substring(line.indexOf(CONTENT)));

		while (((line = reader.readLine()) != null) && !(line.contains(TEASER))) {
			if (!skipTeaser) {
				text.append(line);
			}
			continue;
		}
		text.append(line.substring(line.indexOf(TEASER)));

		int diffCount = 1;
		while (((line = reader.readLine()) != null) && diffCount > 0) {
			diffCount -= countTag(line, "</div>");
			if (diffCount == 1) {
				// skip inner diffs -> fotostrecke, etc
				text.append(line);
			}
			if (diffCount > 0) {
				diffCount += countTag(line, "<div");
			}
		}
		if (line.contains("</div>")) {
			text.append(line.substring(0, line.lastIndexOf("</div>")));
		}
		text.append("</div>");
		return text.toString();
	}

	private int countTag(String line, String tag) {
		int tagCount = 0;
		String tLine = line.trim();
		while (tLine.length() > 0 && tLine.contains(tag)) {
			tagCount++;
			tLine = tLine.substring(tLine.indexOf(tag) + tag.length());
		}
		return tagCount;
	}

	public void trimContent(boolean online) {
		if (MDebug.LOG)
			Log.d(TAG, "Trimming article content");

		int start, end;

		// cut everything starting with "Zum Thema" at the bottom of most
		// articles...
		start = content.indexOf("<div>Zum Thema:");
		if (start != -1) {
			end = content.indexOf("</div></div></div></body></html>");
			content = content.substring(0, start - 1)
					+ content.substring(end, content.length());
		}
		// cut everything until '<div class="text mode1"', mostly ads
		start = content.indexOf("<p align=\"center\">");
		if (start != -1) {
			end = content.indexOf("</p>");
			content = content.substring(0, start - 1)
					+ content.substring(end + 4, content.length());
		}
		// //////////
		start = content.indexOf("<strong>MEHR ");
		if (start != -1) {
			end = content.indexOf("</div></div></div></body></html>");
			content = content.substring(0, start - 1)
					+ content.substring(end, content.length());
		}
		// Multiple page articles, remove the links to next/prev page
		start = content.indexOf("<strong>1</strong>");
		if (start != -1) {
			end = content.indexOf("</div></div></div></body></html>");
			content = content.substring(0, start - 1)
					+ content.substring(end, content.length());
		}
		start = content.indexOf("ZUR&#xdc;CK</span>");
		if (start != -1) {
			end = content.indexOf("</div></div></div></body></html>");
			content = content.substring(0, start - 1)
					+ content.substring(end, content.length());
		}

		// ////////////
		// only do the following when not connected to the internet
		if (!online) {
			int i = 0;
			while ((start = content.indexOf("<img")) != -1) {
				end = content.indexOf('>', start);
				content = content.substring(0, start - 1)
						+ content.substring(end, content.length());
				i++;
			}
			if (MDebug.LOG)
				Log.d(TAG, "Replaced " + i + " occurences of <img...>");
		}
		// /////////////////////

		// content = content.replaceAll("ddp", "");
		content = content.replaceAll("FOTOSTRECKE", "");
		content = content
				.replaceAll(
						"padding-top: .px;padding-bottom: .px;background-color: #ececec;",
						"");
		content = content.replaceAll(
				"padding-top: 8px;background-color: #ececec;", "");
		content = content.replaceAll("background-color: #ececec;", "");
		content = content.replaceAll("Video abspielen", "");
		content = content.replaceAll("separator mode1 ", "");
		// content = content.replaceAll("global.css", "sss");
		content = content
				.replaceAll(
						"border-color: #ececec;border-style: solid;border-width: ..px;",
						"");

		/* font substitutions */
		int prefFontSize = app.getIntPreference("PrefFontSize", 6);
		int newsize = 19 + (prefFontSize - 6);
		int newsize_large = (int) ((22.0 / 19.0) * (double) newsize);
		int newsize_large_large = (int) ((26.0 / 19.0) * (double) newsize);
		int newsize_misc = (int) ((15.0 / 19.0) * (double) newsize);
		if (MDebug.LOG)
			Log.d(TAG, "Font sizes for this article: " + newsize + ", "
					+ newsize_large + ", " + newsize_large_large + ", "
					+ newsize_misc);

		content = content.replaceAll("font-size:19px", "font-size:" + newsize
				+ "px");
		content = content.replaceAll("font-size:22px", "font-size:"
				+ newsize_large + "px");
		content = content.replaceAll("font-size:26px", "font-size:"
				+ newsize_large_large + "px");
		content = content.replaceAll("font-size:15px", "font-size:"
				+ newsize_misc + "px");
	}

	public String getContent(boolean online, int page) {
		if (content != null && content.length() != 0) {
			if (MDebug.LOG)
				Log.d(TAG, "Article already has content, returning it");
			return content;
		} else {
			if (MDebug.LOG)
				Log.d(TAG,
						"Article doesn't have content, downloading and returning it");
			content = _downloadContent(online, page);
			// trimContent(online);

			return content;
		}
	}

	public String getContent(boolean online) {
		return getContent(online, 1);
	}

	public Bitmap getImage(boolean online) {
		if (!online)
			return image;

		if (image != null) {
			if (MDebug.LOG)
				Log.d(TAG, "Article already has image, returning it");

			return image;
		} else {
			if (MDebug.LOG)
				Log.d(TAG,
						"Article doesn't have image, downloading and returning it");

			if (image_url != null)
				image = _downloadImage();

			return image;
		}
	}

	public void resetContent() {
		content = "";
	}

}
