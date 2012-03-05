package com.tinyhydra.botd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

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
// The Vote() dialog ListView adapter.
public class JavaShopAdapter extends BaseAdapter {

    List<JavaShop> shopList;

    private LayoutInflater mInflater;
    Context context;

    // default constructor. Sets the list of cafe's we got back from google places
    // so we can turn them into ListView display objects
    public JavaShopAdapter(Context context, List<JavaShop> shopList) {
        this.context = context;
        this.shopList = shopList;
        mInflater = LayoutInflater.from(context);
    }

    public void refreshShopList(List<JavaShop> shopList) {
        this.shopList = shopList;
    }

    public int getCount() {
        return shopList.size();
    }

    public JavaShop getItem(int position) {
        return shopList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.javashop_item, null);
            holder = new ViewHolder();

            holder.shopName = (TextView) convertView.findViewById(R.id.si_name);
            holder.shopVicinity = (TextView) convertView.findViewById(R.id.si_vicinity);

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

        return convertView;
    }

    static class ViewHolder {
        TextView shopName;
        TextView shopVicinity;
    }
}