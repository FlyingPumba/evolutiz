package com.gluegadget.hndroid;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem.OnMenuItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class Comments extends Activity {
	
	static final private int MENU_UPDATE = Menu.FIRST;
	static final private int MENU_COMMENT = Menu.FIRST + 1;
	
	static final private int NOTIFY_DATASET_CHANGED = 1;
	static final private int NOTIFY_COMMENT_ADDED = 2;
	
	static final private int CONTEXT_REPLY = 1;
	static final private int CONTEXT_UPVOTE = 2;
	
	ProgressDialog dialog;
	
	ListView newsListView;
	
	CommentsAdapter aa;
	
	ArrayList<Comment> commentsList = new ArrayList<Comment>();
	
	String extrasCommentsUrl;
	
	String fnId = "";
	
	Boolean loggedIn = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        newsListView = (ListView)this.findViewById(R.id.hnListView);
        registerForContextMenu(newsListView);
        int layoutID = R.layout.comments_list_item;
        aa = new CommentsAdapter(this, layoutID , commentsList);
        newsListView.setAdapter(aa);
        
    	final Bundle extras = getIntent().getExtras();
    	extrasCommentsUrl = extras.getString("url");
    	TextView hnTopDesc = (TextView)this.findViewById(R.id.hnTopDesc);
    	hnTopDesc.setText(extras.getString("title"));
    	dialog = ProgressDialog.show(Comments.this, "", "Loading. Please wait...", true);
    	new Thread(new Runnable(){
    		public void run() {
    			refreshComments(extrasCommentsUrl);
    			dialog.dismiss();
    			handler.sendEmptyMessage(NOTIFY_DATASET_CHANGED);
    		}
    	}).start();
        
    }
    
    Handler handler = new Handler(){
    	@Override
    	public void handleMessage(Message msg) {
    		switch(msg.what) {
    		case NOTIFY_DATASET_CHANGED:
    			aa.notifyDataSetChanged();
    			break;
    		case NOTIFY_COMMENT_ADDED:
    			dialog = ProgressDialog.show(Comments.this, "", "Reloading. Please wait...", true);
    	    	new Thread(new Runnable(){
    	    		public void run() {
    	    			refreshComments(extrasCommentsUrl);
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
    
    private class OnCommentListener implements CommentDialog.ReadyListener {
    	@Override
    	public void ready(final String text) {
    		try {
    			dialog = ProgressDialog.show(Comments.this, "", "Trying to comment. Please wait...", true);
    			new Thread(new Runnable(){
    				public void run() {
    					try {
    						DefaultHttpClient httpclient = new DefaultHttpClient();
    						HttpPost httpost = new HttpPost("http://news.ycombinator.com/r");
    						List <NameValuePair> nvps = new ArrayList <NameValuePair>();
    						nvps.add(new BasicNameValuePair("text", text));
    						nvps.add(new BasicNameValuePair("fnid", fnId));
    						httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
    						SharedPreferences settings = getSharedPreferences(Main.PREFS_NAME, 0);
    			    		String cookie = settings.getString("cookie", "");
    			    		httpost.addHeader("Cookie", "user=" + cookie);
    						httpclient.execute(httpost);
    						httpclient.getConnectionManager().shutdown();
    						dialog.dismiss();
    						handler.sendEmptyMessage(NOTIFY_COMMENT_ADDED);
    					} catch (Exception e) {
    						dialog.dismiss();
    						handler.sendEmptyMessage(0);
    						e.printStackTrace();
    					}
    				}
    			}).start();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	public void ready(final String text, String url) {}
    }
    
    private class OnReplyListener implements CommentDialog.ReadyListener {
    	@Override
    	public void ready(final String text, final String replyUrl) {
    		try {
    			dialog = ProgressDialog.show(Comments.this, "", "Trying to reply. Please wait...", true);
    			new Thread(new Runnable(){
    				public void run() {
    					try {
    						DefaultHttpClient httpclient = new DefaultHttpClient();
    						SharedPreferences settings = getSharedPreferences(Main.PREFS_NAME, 0);
    			    		String cookie = settings.getString("cookie", "");
    						HttpGet httpget = new HttpGet(replyUrl);
    			    		httpget.addHeader("Cookie", "user=" + cookie);
    			    		ResponseHandler<String> responseHandler = new BasicResponseHandler();
    			    		String responseBody = httpclient.execute(httpget, responseHandler);
    			    		HtmlCleaner cleaner = new HtmlCleaner();
    			    		TagNode node = cleaner.clean(responseBody);
    			    		Object[] forms = node.evaluateXPath("//form[@method='post']/input[@name='fnid']");
    			    		if (forms.length == 1) {
    			    			TagNode formNode = (TagNode)forms[0];
    			    			String replyToFnId = formNode.getAttributeByName("value").toString().trim();
    			    			HttpPost httpost = new HttpPost("http://news.ycombinator.com/r");
        						List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        						nvps.add(new BasicNameValuePair("text", text));
        						nvps.add(new BasicNameValuePair("fnid", replyToFnId));
        						httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        			    		httpost.addHeader("Cookie", "user=" + cookie);
        						httpclient.execute(httpost);
        						httpclient.getConnectionManager().shutdown();
    			    		}
    						dialog.dismiss();
    						handler.sendEmptyMessage(NOTIFY_COMMENT_ADDED);
    					} catch (Exception e) {
    						dialog.dismiss();
    						handler.sendEmptyMessage(0);
    						e.printStackTrace();
    					}
    				}
    			}).start();
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    	public void ready(final String text) {}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);

    	MenuItem itemRefresh = menu.add(0, MENU_UPDATE, Menu.NONE, R.string.menu_refresh);
    	itemRefresh.setIcon(R.drawable.ic_menu_refresh);
    	itemRefresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    		public boolean onMenuItemClick(MenuItem item) {
    			try {
    				dialog = ProgressDialog.show(Comments.this, "", "Reloading. Please wait...", true);
    				new Thread(new Runnable(){
    					public void run() {
    						refreshComments(extrasCommentsUrl);
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

    	MenuItem itemComment = menu.add(0, MENU_COMMENT, Menu.NONE, R.string.menu_comment);
    	itemComment.setIcon(R.drawable.ic_menu_compose);
    	itemComment.setOnMenuItemClickListener(new OnMenuItemClickListener() {
    		@Override
    		public boolean onMenuItemClick(MenuItem item) {
    			CommentDialog commentDialog = new CommentDialog(Comments.this, "Comment on submission", new OnCommentListener());
    			commentDialog.show();

    			return true;
    		}
    	});

    	return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
    	if (!loggedIn || fnId == "") {
    		menu.findItem(MENU_COMMENT).setVisible(false);
    		menu.findItem(MENU_COMMENT).setEnabled(false);
    	}
    	
    	return super.onPrepareOptionsMenu(menu); 
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
    	final Comment newsContexted = (Comment) newsListView.getAdapter().getItem(info.position);
    	
    	menu.setHeaderTitle(newsContexted.getTitle()); 	
    	if (fnId != "" && newsContexted.getReplyToUrl() != "" && loggedIn) {
    		MenuItem originalLink = menu.add(0, CONTEXT_REPLY, 0, R.string.context_reply); 
    		originalLink.setOnMenuItemClickListener(new OnMenuItemClickListener() {		
    			public boolean onMenuItemClick(MenuItem item) {
    				CommentDialog commentDialog = new CommentDialog(
    						Comments.this, "Reply to " + newsContexted.getAuthor(),
    						newsContexted.getReplyToUrl(), new OnReplyListener()
    				);
    				commentDialog.show();

    				return true;
    			}
    		});
    	}
    	
    	if (newsContexted.getUpVoteUrl() != "" && loggedIn) {
    		MenuItem upVote = menu.add(0, CONTEXT_UPVOTE, 0, R.string.context_upvote);
        	upVote.setOnMenuItemClickListener(new OnMenuItemClickListener() {		
        		public boolean onMenuItemClick(MenuItem item) {
        			dialog = ProgressDialog.show(Comments.this, "", "Voting. Please wait...", true);
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
			    			handler.sendEmptyMessage(NOTIFY_COMMENT_ADDED);
			    		}
			    	}).start();
        			return true;
        		}
        	});
    	}
    }
    
    private void refreshComments(String uri) {
    	try {
    		commentsList.clear();
    		SharedPreferences settings = getSharedPreferences(Main.PREFS_NAME, 0);
    		String cookie = settings.getString("cookie", "");
    		DefaultHttpClient httpclient = new DefaultHttpClient();
    		HttpGet httpget = new HttpGet(uri);
    		if (cookie != "")
    			httpget.addHeader("Cookie", "user=" + cookie);
    		ResponseHandler<String> responseHandler = new BasicResponseHandler();
    		String responseBody = httpclient.execute(httpget, responseHandler);
    		HtmlCleaner cleaner = new HtmlCleaner();
    		TagNode node = cleaner.clean(responseBody);

    		Object[] loginFnid = node.evaluateXPath("//span[@class='pagetop']/a");
    		TagNode loginNode = (TagNode) loginFnid[5];
    		if (loginNode.getAttributeByName("href").toString().trim().equalsIgnoreCase("submit"))
    			loggedIn = true;
    		Object[] forms = node.evaluateXPath("//form[@method='post']/input[@name='fnid']");
    		if (forms.length == 1) {
    			TagNode formNode = (TagNode)forms[0];
    			fnId = formNode.getAttributeByName("value").toString().trim();	
    		}
    		Object[] comments = node.evaluateXPath("//table[@border='0']/tbody/tr/td/img[@src='http://ycombinator.com/images/s.gif']");

    		if (comments.length > 1) {
    			for (int i = 0; i < comments.length; i++) {
    				TagNode commentNode = (TagNode)comments[i];
    				String depth = commentNode.getAttributeByName("width").toString().trim();
    				Integer depthValue = Integer.parseInt(depth) / 2;
    				TagNode nodeParent = commentNode.getParent().getParent();
    				Object[] comment = nodeParent.evaluateXPath("//span[@class='comment']");
    				Comment commentEntry;
    				if (comment.length > 0) {
    					TagNode commentSpan = (TagNode) comment[0];
    					StringBuffer commentText = commentSpan.getText();
    					if (!commentText.toString().equalsIgnoreCase("[deleted]")) {
    						Object[] author = nodeParent.evaluateXPath("//span[@class='comhead']/a[1]");
    						Object[] replyTo = nodeParent.evaluateXPath("//p/font[@size='1']/u/a");
    						Object[] upVotes = nodeParent.getParent().evaluateXPath("//td[@valign='top']/center/a[1]");

    						TagNode authorNode = (TagNode) author[0];

    						String upVoteUrl = "";
    						String replyToValue = "";
    						String authorValue = authorNode.getChildren().iterator().next().toString().trim();
    						if (upVotes.length > 0) {
    							TagNode upVote = (TagNode) upVotes[0];
    							upVoteUrl = upVote.getAttributeByName("href").toString().trim();
    						}
    						if (replyTo.length > 0) {
    							TagNode replyToNode = (TagNode) replyTo[0];
    							replyToValue = replyToNode.getAttributeByName("href").toString().trim();
    						}

                            TagNode font = commentSpan.findElementByName("font", true);
                            String commentBody = font.getText().toString();

                            TagNode[] ps = commentSpan.getElementsByName("p", true);

                            for (TagNode p : ps) {
                                commentBody += "\n\n" + Html.fromHtml(p.getText().toString()).toString();
                            }

    						commentEntry = new Comment(commentBody, authorValue, depthValue, replyToValue, upVoteUrl);
    					} else {
    						commentEntry = new Comment("[deleted]");
    					}
    					commentsList.add(commentEntry);
    				}
    			}
    		} else {
    			Comment commentEntry = new Comment("No comments.");
    			commentsList.add(commentEntry);
    		}
    	} catch (MalformedURLException e) {
    		e.printStackTrace();
    	} catch (IOException e) {
    		e.printStackTrace();
    	} catch (XPatherException e) {
    		e.printStackTrace();
    	} catch (IllegalStateException e) {
    		finish();
    	} finally {

    	}
    }
}
