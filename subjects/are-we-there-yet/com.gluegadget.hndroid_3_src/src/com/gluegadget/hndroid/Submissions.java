package com.gluegadget.hndroid;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class Submissions extends Activity {
	static final private int CONTEXT_COMMENTS = 3;
	static final private int CONTEXT_USER_LINK = 4;
	static final private int CONTEXT_USER_UPVOTE = 5;
	static final private int CONTEXT_GOOGLE_MOBILE = 6;
	
	static final private int NOTIFY_DATASET_CHANGED = 1;
	
	ProgressDialog dialog;
	
	ListView newsListView;
	
	NewsAdapter aa;
	
	ArrayList<News> news = new ArrayList<News>();
	
	String loginUrl;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);

    	newsListView = (ListView)this.findViewById(R.id.hnListView);
    	registerForContextMenu(newsListView);
    	int layoutID = R.layout.news_list_item;
    	aa = new NewsAdapter(this, layoutID , news);
    	newsListView.setAdapter(aa);
    	newsListView.setOnItemClickListener(clickListener);

    	final Bundle extras = getIntent().getExtras();
    	TextView hnTopDesc = (TextView)this.findViewById(R.id.hnTopDesc);
    	hnTopDesc.setText(extras.getString("title"));
    	dialog = ProgressDialog.show(Submissions.this, "", "Loading. Please wait...", true);
    	new Thread(new Runnable(){
    		public void run() {
    			refreshNews("http://news.ycombinator.com/submitted?id=" + extras.getString("user"));
    			dialog.dismiss();
    			handler.sendEmptyMessage(NOTIFY_DATASET_CHANGED);
    		}
    	}).start();
    }

    Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.what){
    		case NOTIFY_DATASET_CHANGED:
    			aa.notifyDataSetChanged();
    			newsListView.setSelection(0);
    			break;
    		}
    	}
    };
    
    OnItemClickListener clickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> newsAV, View view, int pos, long id) {
			final News item = (News) newsAV.getAdapter().getItem(pos);
			if (pos < 30) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				String ListPreference = prefs.getString("PREF_DEFAULT_ACTION", "view-comments");
				if (ListPreference.equalsIgnoreCase("open-in-browser")) {
					Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse((String) item.getUrl()));
					startActivity(viewIntent);
				} else if (ListPreference.equalsIgnoreCase("view-comments")) {
					Intent intent = new Intent(Submissions.this, Comments.class);
					intent.putExtra("url", item.getCommentsUrl());
					intent.putExtra("title", item.getTitle());
					startActivity(intent);
				} else if (ListPreference.equalsIgnoreCase("mobile-adapted-view")) {
					Intent viewIntent = new Intent("android.intent.action.VIEW",
							Uri.parse((String) "http://www.google.com/gwt/x?u=" + item.getUrl()));
					startActivity(viewIntent);
				}
			} else {
				dialog = ProgressDialog.show(Submissions.this, "", "Loading. Please wait...", true);
    	    	new Thread(new Runnable(){
    	    		public void run() {
    	    			refreshNews(item.getUrl());
    	    			dialog.dismiss();
    	    			handler.sendEmptyMessage(NOTIFY_DATASET_CHANGED);
    	    		}
    	    	}).start();
			}
		}
	};
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
    	if (info.position < 30) {
    		final News newsContexted = (News) newsListView.getAdapter().getItem(info.position);

    		menu.setHeaderTitle(newsContexted.getTitle());

    		MenuItem originalLink = menu.add(0, CONTEXT_USER_LINK, 0, newsContexted.getUrl()); 
    		originalLink.setOnMenuItemClickListener(new OnMenuItemClickListener() {		
    			public boolean onMenuItemClick(MenuItem item) {
    				Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse((String) item.getTitle()));
    				startActivity(viewIntent);
    				return true;
    			}
    		});

    		MenuItem googleMobileLink = menu.add(0, CONTEXT_GOOGLE_MOBILE, 0, R.string.context_google_mobile);
    		googleMobileLink.setOnMenuItemClickListener(new OnMenuItemClickListener() {		
    			public boolean onMenuItemClick(MenuItem item) {
    				Intent viewIntent = new Intent("android.intent.action.VIEW",
    						Uri.parse((String) "http://www.google.com/gwt/x?u=" + newsContexted.getUrl()));
    				startActivity(viewIntent);
    				return true;
    			}
    		});

    		if (newsContexted.getCommentsUrl() != "") {
    			MenuItem comments = menu.add(0, CONTEXT_COMMENTS, 0, R.string.menu_comments); 
    			comments.setOnMenuItemClickListener(new OnMenuItemClickListener() {		
    				public boolean onMenuItemClick(MenuItem item) {
    					Intent intent = new Intent(Submissions.this, Comments.class);
    					intent.putExtra("url", newsContexted.getCommentsUrl());
    					intent.putExtra("title", newsContexted.getTitle());
    					startActivity(intent);
    					return true;
    				}
    			});
    		}

    		if (loginUrl.contains("submit") && newsContexted.getUpVoteUrl() != "") {
    			MenuItem upVote = menu.add(0, CONTEXT_USER_UPVOTE, 0, R.string.context_upvote);
    			upVote.setOnMenuItemClickListener(new OnMenuItemClickListener() {		
    				public boolean onMenuItemClick(MenuItem item) {
    					dialog = ProgressDialog.show(Submissions.this, "", "Voting. Please wait...", true);
    					new Thread(new Runnable(){
    						public void run() {
    							SharedPreferences settings = getSharedPreferences(Main.PREFS_NAME, 0);
    							String cookie = settings.getString("cookie", "");
    							DefaultHttpClient httpclient = new DefaultHttpClient();
    							HttpGet httpget = new HttpGet(newsContexted.getUpVoteUrl());
    							httpget.addHeader("Cookie", "user=" + cookie);
    							ResponseHandler<String> responseHandler = new BasicResponseHandler();
    							try {
    								httpclient.execute(httpget, responseHandler);
    							} catch (ClientProtocolException e) {
    								// TODO Auto-generated catch block
    								e.printStackTrace();
    							} catch (IOException e) {
    								// TODO Auto-generated catch block
    								e.printStackTrace();
    							}
    							dialog.dismiss();
    							handler.sendEmptyMessage(NOTIFY_DATASET_CHANGED);
    						}
    					}).start();
    					return true;
    				}
    			});
    		}
    	}
    }
    
    private void refreshNews(String newsUrl) {
    	try {
    		news.clear();
    		SharedPreferences settings = getSharedPreferences(Main.PREFS_NAME, 0);
    		String cookie = settings.getString("cookie", "");
    		DefaultHttpClient httpclient = new DefaultHttpClient();
    		HttpGet httpget = new HttpGet(newsUrl);
    		if (cookie != "")
    			httpget.addHeader("Cookie", "user=" + cookie);
    		ResponseHandler<String> responseHandler = new BasicResponseHandler();
    		String responseBody = httpclient.execute(httpget, responseHandler);
    		HtmlCleaner cleaner = new HtmlCleaner();
    		TagNode node = cleaner.clean(responseBody);

    		Object[] newsTitles = node.evaluateXPath("//td[@class='title']/a");
    		Object[] subtexts = node.evaluateXPath("//td[@class='subtext']");
    		Object[] domains = node.evaluateXPath("//span[@class='comhead']");
    		Object[] loginFnid = node.evaluateXPath("//span[@class='pagetop']/a");
    		TagNode loginNode = (TagNode) loginFnid[5];
    		loginUrl = loginNode.getAttributeByName("href").toString().trim();

    		if (newsTitles.length > 0) {
    			int j = 0;
    			int iterateFor = newsTitles.length;
    			for (int i = 0; i < iterateFor; i++) {
    				String scoreValue = "";
    				String authorValue = "";
    				String commentValue = "";
    				String domainValue = "";
    				String commentsUrl = "";
    				String upVoteUrl = "";
    				TagNode newsTitle = (TagNode) newsTitles[i];

    				String title = newsTitle.getChildren().iterator().next().toString().trim();
    				String href = newsTitle.getAttributeByName("href").toString().trim();

    				if (i < subtexts.length) {
    					TagNode subtext = (TagNode) subtexts[i];
    					Object[] scoreSpanNode = subtext.evaluateXPath("/span");
    					TagNode score = (TagNode) scoreSpanNode[0];
    					
    					Object[] scoreAnchorNodes = subtext.evaluateXPath("/a");
    					TagNode author = (TagNode) scoreAnchorNodes[0];
    					authorValue = author.getChildren().iterator().next().toString().trim();
    					if (scoreAnchorNodes.length == 2) {
    						TagNode comment = (TagNode) scoreAnchorNodes[1];
    						commentValue = comment.getChildren().iterator().next().toString().trim();
    					}

    					TagNode userNode = newsTitle.getParent().getParent();
    					Object[] upVotes = userNode.evaluateXPath("//td/center/a[1]");
    					if (upVotes.length > 0) {
    						TagNode upVote = (TagNode) upVotes[0];
    						upVoteUrl = upVote.getAttributeByName("href").toString().trim();
    					}
    					
    					Object[] commentsTag = author.getParent().evaluateXPath("/a");
    					if (commentsTag.length == 2)
    						commentsUrl = score.getAttributeByName("id").toString().trim();
    					
    					scoreValue = score.getChildren().iterator().next().toString().trim();
    					authorValue = author.getChildren().iterator().next().toString().trim();
    					
    					if (href.startsWith("http")) {
    						TagNode domain = (TagNode)domains[j];
    						domainValue = domain.getChildren().iterator().next().toString().trim();
    						j++;
    					}
    				}

    				News newsEntry = new News(title, scoreValue, commentValue, authorValue, domainValue, href, commentsUrl, upVoteUrl);
    				news.add(newsEntry);
    			}
    		}
    	} catch (MalformedURLException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (XPatherException e) {
    		e.printStackTrace();
    	} finally {

    	}
    }
}
