package com.gluegadget.hndroid;

public class Comment {
	private String title;
	private String author;
	private String replyToUrl; 
	private String upVoteUrl;
	private Integer padding;

	public Comment(String _title, String _author, Integer _padding, String _replyToUrl, String _upVoteUrl) {
		title = _title;
		author = _author;
		padding = _padding;
		
		if (_replyToUrl.length() > 1)
			replyToUrl = "http://news.ycombinator.com/" + _replyToUrl.replace("&amp", "&");
		else
			replyToUrl = _replyToUrl;
		
		if (_upVoteUrl.length() > 1)
			upVoteUrl = "http://news.ycombinator.com/" + _upVoteUrl.replace("&amp", "&");
		else
			upVoteUrl = _upVoteUrl;
	}

	public Comment(String _title) {
		this(_title, "", 0, "", "");
	}
	
	public Integer getPadding() {
		return padding;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}
	
	public String getReplyToUrl() {
		return replyToUrl;
	}
	
	public String getUpVoteUrl() {
		return upVoteUrl;
	}

	@Override
	public String toString() {
		return author + ": " + title;
	}
}
