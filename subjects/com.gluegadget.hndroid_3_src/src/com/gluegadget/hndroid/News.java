package com.gluegadget.hndroid;

public class News {
	
	private String title;
	private String author;
	private String score;
	private String comment;
	private String url;
	private String domain;
	private String commentsUrl;
	private String upVoteUrl;

	public News(String _title, String _score, String _comment, String _author, String _domain, String _url, String _commentsUrl, String _upVoteUrl) {
		title = _title;
		score = _score;
		comment = _comment;
		author = _author;
		url = _url;
		if (_commentsUrl.length() > 7)
			commentsUrl = "http://news.ycombinator.com/item?id=" + _commentsUrl.substring(6);
		else
			commentsUrl = _commentsUrl;
		
		if (_domain.length() > 2)
			domain = _domain.substring(1, _domain.length()-1);
		else
			domain = _domain;
		
		if (_upVoteUrl.length() > 1)
			upVoteUrl = "http://news.ycombinator.com/" + _upVoteUrl.replace("&amp", "&");
		else
			upVoteUrl = _upVoteUrl;
	}
	
	public News(String _title) {
		this(_title, "", "", "", "", "", "", "");
	}
	
	public String getCommentsUrl() {
		return commentsUrl;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getScore() {
		return score;
	}
	
	public String getComment() {
		String returnValue = "";
		if (comment.contains("discuss")) {
			returnValue = "0";
		} else {
			String tmp = comment.replaceAll("comments?", "");
			if (tmp.length() == 0)
				returnValue = "?";
			else
				returnValue = tmp;
		}
		
		return returnValue;
	}
	public String getAuthor() {
		return author;
	}
	
	public String getUrl() {
		if (!url.startsWith("http"))
			if (url.startsWith("/"))
				return "http://news.ycombinator.com" + url;
			else
				return "http://news.ycombinator.com/" + url;
		else
			return url;
	}
	
	public String getUpVoteUrl() {
		return upVoteUrl;
	}
	
	public String getDomain() {
		return domain;
	}
	
	@Override
	public String toString() {
		if (author == "")
			return title;
		else
			return title + " by " + author;
	}

}
