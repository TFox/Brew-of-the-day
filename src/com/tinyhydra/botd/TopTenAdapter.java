package com.tinyhydra.botd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Brew of the day
 * Copyright (C) 2012  tinyhydra.com
 * *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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