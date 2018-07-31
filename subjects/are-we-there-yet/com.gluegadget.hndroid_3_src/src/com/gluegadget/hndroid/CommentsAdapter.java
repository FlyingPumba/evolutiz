package com.gluegadget.hndroid;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CommentsAdapter extends ArrayAdapter<Comment> {
	private LayoutInflater mInflater;
	int resource;
	
	public CommentsAdapter(Context _context, int _resource, List<Comment> _items) {
		super(_context, _resource, _items);
		mInflater = (LayoutInflater)_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		resource = _resource;
	}
	
	static class ViewHolder {
		TextView title;
		TextView author;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		Comment item = getItem(position);
		
		if (convertView == null) {
			convertView = mInflater.inflate(resource, parent, false);
			holder = new ViewHolder();
			holder.title = (TextView)convertView.findViewById(R.id.title);
			holder.author = (TextView)convertView.findViewById(R.id.author);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder)convertView.getTag();
		}
		
		holder.title.setPadding(item.getPadding() + 1, 10, 10, 10);
		holder.title.setText(item.getTitle());
		
		if (item.getAuthor() == "")
			holder.author.setText(item.getAuthor());
		else
			holder.author.setText("by " + item.getAuthor());

		return convertView;
	}
}
