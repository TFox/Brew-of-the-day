package com.tinyhydra.botd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Copyright © 2012 tinyhydra.com
 */
public class TopTenAdapter extends BaseAdapter {

    List<JavaShop> topTenList;

    private LayoutInflater mInflater;
    Context context;

    public TopTenAdapter(Context context, List<JavaShop> topTenList) {
        this.context = context;
        this.topTenList = topTenList;
        mInflater = LayoutInflater.from(context);
    }

    public void refreshTopTenList(List<JavaShop> topTenList) {
        this.topTenList = topTenList;
        notifyDataSetInvalidated();
    }

    public int getCount() {
        return topTenList.size();
    }

    public JavaShop getItem(int position) {
        return topTenList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.topten_item, null);
            holder = new ViewHolder();

            holder.shopName = (TextView) convertView.findViewById(R.id.tt_nametext);
            holder.shopVicinity = (TextView) convertView.findViewById(R.id.tt_subtext);
            holder.voteCount = (TextView) convertView.findViewById(R.id.tt_votescount);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        // Bind the JavaShop object to the Name View
        holder.shopName.setTag(getItem(position));
        // Set the name & 'vicinity' which is google-places-speak for 'nearest address'
        // note: address is available, but requires a separate http request, and
        // returns more data than we need. Vicinity should be sufficient
        holder.shopName.setText(getItem(position).getName());
        holder.shopVicinity.setText(getItem(position).getVicinity());
        holder.voteCount.setText(Integer.toString(getItem(position).getVotes()));

        return convertView;
    }

    static class ViewHolder {
        TextView shopName;
        TextView shopVicinity;
        TextView voteCount;
    }
}