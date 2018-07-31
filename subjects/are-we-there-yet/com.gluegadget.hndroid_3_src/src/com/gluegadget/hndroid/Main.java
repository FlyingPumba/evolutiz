package com.gluegadget.hndroid;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
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
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class Main extends Activity {
	static final String PREFS_NAME = "user";
	
	static final private int MENU_UPDATE = Menu.FIRST;
	static final private int MENU_LOGIN = 2;
	static final private int MENU_LOGOUT = 3;
	static final private int MENU_PREFERENCES = 4;
	
	private static final int LIST_MENU_GROUP = 10;
	private static final int LIST_NEWS_ID = 11;
	private static final int LIST_BEST_ID = 12;
	private static final int LIST_ACTIVE_ID = 13;
	private static final int LIST_NOOB_ID = 14;
	
	static final private int CONTEXT_USER_SUBMISSIONS = 2;
	static final private int CONTEXT_COMMENTS = 3;
	static final private int CONTEXT_USER_LINK = 4;
	static final private int CONTEXT_USER_UPVOTE = 5;
	static final private int CONTEXT_GOOGLE_MOBILE = 6;
	
	static final private int NOTIFY_DATASET_CHANGED = 1;
	static final private int LOGIN_FAILED = 2;
	static final private int LOGIN_SUCCESSFULL = 3;
	
	static int DEFAULT_ACTION_PREFERENCES = 0;
	
	String loginUrl = "";
	
	ProgressDialog dialog;
	
	ListView newsListView;
	
	NewsAdapter aa;
	
	ArrayList<News> news = new ArrayList<News>();
	
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
    	dialog = ProgressDialog.show(Main.this, "", "Loading. Please wait...", true);
    	new Thread(new Runnable(){
    		public void run() {
    			refreshNews();
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
    		case LOGIN_FAILED:
    			Toast.makeText(Main.this, "Login failed :(", Toast.LENGTH_LONG).show();
    			break;
    		case LOGIN_SUCCESSFULL:
    			Toast.makeText(Main.this, "Successful login :)", Toast.LENGTH_LONG).show();
    			dialog = ProgressDialog.show(Main.this, "", "Reloading. Please wait...", true);
    	    	new Thread(new Runnable(){
    	    		public void run() {
    	    			refreshNews();
    	    			dialog.dismiss();
    	    			handler.sendEmptyMessage(NOTIFY_DATASET_CHANGED);
    	    		}
    	    	}).start();
    			break;
    		default:
    			break;
    		}
    	}
    };
    
    OnItemClickListener clickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> newsAV, View view, int pos, long id) {
			final News item = (News) newsAV.getAdapter().getItem(pos);
			if (pos < newsAV.getAdapter().getCount() - 1) {
				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				String ListPreference = prefs.getString("PREF_DEFAULT_ACTION", "view-comments");
				if (ListPreference.equalsIgnoreCase("open-in-browser")) {
					Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse((String) item.getUrl()));
					startActivity(viewIntent);
				} else if (ListPreference.equalsIgnoreCase("view-comments")) {
					Intent intent = new Intent(Main.this, Comments.class);
					intent.putExtra("url", item.getCommentsUrl());
					intent.putExtra("title", item.getTitle());
					startActivity(intent);
				} else if (ListPreference.equalsIgnoreCase("mobile-adapted-view")) {
					Intent viewIntent = new Intent("android.intent.action.VIEW",
							Uri.parse((String) "http://www.google.com/gwt/x?u=" + item.getUrl()));
					startActivity(viewIntent);
				}
			} else {
				dialog = ProgressDialog.show(Main.this, "", "Loading. Please wait...", true);
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
    
    private class OnLoginListener implements LoginDialog.ReadyListener {
    	@Override
    	public void ready(final String username, final String password) {
    		try {
    			dialog = ProgressDialog.show(Main.this, "", "Trying to login. Please wait...", true);
    			new Thread(new Runnable(){
    				public void run() {
    					Integer message = 0;
    					try {
    						DefaultHttpClient httpclient = new DefaultHttpClient();
    						HttpGet httpget = new HttpGet("http://news.ycombinator.com" + loginUrl);
    						HttpResponse response;
    						HtmlCleaner cleaner = new HtmlCleaner();
    						response = httpclient.execute(httpget);
    						HttpEntity entity = response.getEntity();
    						TagNode node = cleaner.clean(entity.getContent());
    						Object[] loginForm = node.evaluateXPath("//form[@method='post']/input");
    						TagNode loginNode = (TagNode) loginForm[0];
    						String fnId = loginNode.getAttributeByName("value").toString().trim();    			

    						HttpPost httpost = new HttpPost("http://news.ycombinator.com/y");
    						List <NameValuePair> nvps = new ArrayList <NameValuePair>();
    						nvps.add(new BasicNameValuePair("u", username));
    						nvps.add(new BasicNameValuePair("p", password));
    						nvps.add(new BasicNameValuePair("fnid", fnId));
    						httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
    						response = httpclient.execute(httpost);
    						entity = response.getEntity();
    						if (entity != null) {
    							entity.consumeContent();
    						}
    						List<Cookie> cookies = httpclient.getCookieStore().getCookies();
    						if (cookies.isEmpty()) {
    							message = LOGIN_FAILED;
    						} else {
    							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    							SharedPreferences.Editor editor = settings.edit();
    							editor.putString("cookie", cookies.get(0).getValue());
    							editor.commit();
    							message = LOGIN_SUCCESSFULL;
    						}
    						httpclient.getConnectionManager().shutdown();
    						dialog.dismiss();
    						handler.sendEmptyMessage(message);
    					} catch (Exception e) {
    						dialog.dismiss();
    						handler.sendEmptyMessage(message);
    						e.printStackTrace();
    					}
    				}
    			}).start();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);

    	MenuItem itemRefresh = menu.add(0, MENU_UPDATE, Menu.NONE, R.string.menu_refresh);
    	itemRefresh.setIcon(R.drawable.ic_menu_refresh);
    	itemRefresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    		public boolean onMenuItemClick(MenuItem item) {
    			try {
    				dialog = ProgressDialog.show(Main.this, "", "Reloading. Please wait...", true);
    				new Thread(new Runnable(){
    					public void run() {
    						refreshNews();
    						dialog.dismiss();
    						handler.sendEmptyMessage(NOTIFY_DATASET_CHANGED);
    					}
    				}).start();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    			return true;
    		}
    	});
    	
    	SubMenu subMenu = menu.addSubMenu(R.string.menu_lists);
    	subMenu.add(LIST_MENU_GROUP, LIST_NEWS_ID, 0, "news");
    	subMenu.add(LIST_MENU_GROUP, LIST_BEST_ID, 1, "best");
    	subMenu.add(LIST_MENU_GROUP, LIST_ACTIVE_ID, 2, "active");
    	subMenu.add(LIST_MENU_GROUP, LIST_NOOB_ID, 3, "noobstories");
    	subMenu.setIcon(R.drawable.ic_menu_friendslist);
    	subMenu.setGroupCheckable(LIST_MENU_GROUP, true, true);

    	MenuItem itemLogout = menu.add(0, MENU_LOGOUT, Menu.NONE, R.string.menu_logout);
    	itemLogout.setIcon(R.drawable.ic_menu_logout);
    	itemLogout.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    		public boolean onMenuItemClick(MenuItem item) {
    			try {
    				dialog = ProgressDialog.show(Main.this, "", "Reloading. Please wait...", true);
    				new Thread(new Runnable(){
    					public void run() {
    						SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    						SharedPreferences.Editor editor = settings.edit();
    						editor.remove("cookie");
    						editor.commit();
    						refreshNews();
    						dialog.dismiss();
    						handler.sendEmptyMessage(NOTIFY_DATASET_CHANGED);
    					}
    				}).start();
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    			return true;
    		}
    	});

    	MenuItem itemLogin = menu.add(0, MENU_LOGIN, Menu.NONE, R.string.menu_login);
    	itemLogin.setIcon(R.drawable.ic_menu_login);
    	itemLogin.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    		public boolean onMenuItemClick(MenuItem item) {
    			LoginDialog loginDialog = new LoginDialog(Main.this, "", new OnLoginListener());
    			loginDialog.show();

    			return true;
    		}
    	});
    	
    	MenuItem itemPreferences = menu.add(0, MENU_PREFERENCES, Menu.NONE, R.string.menu_preferences);
    	itemPreferences.setIcon(R.drawable.ic_menu_preferences);
    	itemPreferences.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				Intent intent = new Intent(Main.this, Preferences.class);
				startActivity(intent);
				
				return true;
			}
		});

    	return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

    	if (loginUrl.contains("submit")) {
    		menu.findItem(MENU_LOGIN).setVisible(false);
    		menu.findItem(MENU_LOGIN).setEnabled(false);
    		menu.findItem(MENU_LOGOUT).setVisible(true);
    		menu.findItem(MENU_LOGOUT).setEnabled(true);
    	} else {
    		menu.findItem(MENU_LOGIN).setVisible(true);
    		menu.findItem(MENU_LOGIN).setEnabled(true);
    		menu.findItem(MENU_LOGOUT).setVisible(false);
    		menu.findItem(MENU_LOGOUT).setEnabled(false);
    	}
    	
    	return super.onPrepareOptionsMenu(menu); 
    }
    
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
    	switch (item.getItemId()) {
    	case LIST_ACTIVE_ID:
    	case LIST_BEST_ID:
    	case LIST_NOOB_ID:
    	case LIST_NEWS_ID:
    		try {
				dialog = ProgressDialog.show(Main.this, "", "Reloading. Please wait...", true);
				new Thread(new Runnable(){
					public void run() {
						String hnFeed = getString(R.string.hnfeed);
						refreshNews(hnFeed + item.toString());
						dialog.dismiss();
						handler.sendEmptyMessage(NOTIFY_DATASET_CHANGED);
					}
				}).start();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	return true;
    }
    
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
    					Intent intent = new Intent(Main.this, Comments.class);
    					intent.putExtra("url", newsContexted.getCommentsUrl());
    					intent.putExtra("title", newsContexted.getTitle());
    					startActivity(intent);
    					return true;
    				}
    			});
    		}

    		MenuItem userSubmissions = menu.add(0, CONTEXT_USER_SUBMISSIONS, 0, newsContexted.getAuthor() + " submissions");
    		userSubmissions.setOnMenuItemClickListener(new OnMenuItemClickListener() {		
    			public boolean onMenuItemClick(MenuItem item) {
    				Intent intent = new Intent(Main.this, Submissions.class);
    				intent.putExtra("user", newsContexted.getAuthor());
    				intent.putExtra("title", newsContexted.getAuthor() + " submissions");
    				startActivity(intent);
    				return true;
    			}
    		});

    		if (loginUrl.contains("submit") && newsContexted.getUpVoteUrl() != "") {
    			MenuItem upVote = menu.add(0, CONTEXT_USER_UPVOTE, 0, R.string.context_upvote);
    			upVote.setOnMenuItemClickListener(new OnMenuItemClickListener() {		
    				public boolean onMenuItemClick(MenuItem item) {
    					dialog = ProgressDialog.show(Main.this, "", "Voting. Please wait...", true);
    					new Thread(new Runnable(){
    						public void run() {
    							SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
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
    
    private void refreshNews() {
    	String hnFeed = getString(R.string.hnfeed);
    	refreshNews(hnFeed);
    }
    
    private void refreshNews(String newsUrl) {
    	try {
    		news.clear();
    		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    		String cookie = settings.getString("cookie", "");
    		DefaultHttpClient httpclient = new DefaultHttpClient();
    		HttpGet httpget = new HttpGet(newsUrl);
    		if (cookie != "")
    			httpget.addHeader("Cookie", "user=" + cookie);
    		ResponseHandler<String> responseHandler = new BasicResponseHandler();
    		String responseBody = httpclient.execute(httpget, responseHandler);
    		HtmlCleaner cleaner = new HtmlCleaner();
    		TagNode node = cleaner.clean(responseBody);

    		Object[] newsTitles = node.evaluateXPath("//td[@class='title']/a[1]");
    		Object[] subtexts = node.evaluateXPath("//td[@class='subtext']");
    		Object[] domains = node.evaluateXPath("//span[@class='comhead']");
    		Object[] loginFnid = node.evaluateXPath("//span[@class='pagetop']/a");
    		TagNode loginNode = (TagNode) loginFnid[5];
    		loginUrl = loginNode.getAttributeByName("href").toString().trim();
    		/*CleanerProperties props = cleaner.getProperties();
    		PrettyXmlSerializer xmlSerializer = new PrettyXmlSerializer(props);
    		String fileName = "/mnt/sdcard/hn-loggedin.xml";
    		xmlSerializer.writeXmlToFile(node, fileName);*/

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
						if (scoreSpanNode.length == 0)
							// If there's no span in the subtext it's a job
							// advert. Skip it.
							continue;
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
