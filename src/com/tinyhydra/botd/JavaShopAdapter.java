package com.tinyhydra.botd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Copyright © 2012 mercapps.com
 */
public class JavaShopAdapter extends BaseAdapter {

    List<JavaShop> shopList;

    private LayoutInflater mInflater;
    Context context;

    public JavaShopAdapter(Context context, List<JavaShop> shopList) {
        this.context = context;
        this.shopList = shopList;
        mInflater = LayoutInflater.from(context);
    }

    public void refreshShopList(List<JavaShop> shopList) {
        this.shopList = shopList;
        notifyDataSetChanged();
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

        holder.shopName.setTag(getItem(position));
        holder.shopName.setText(getItem(position).getName());
        holder.shopVicinity.setText(getItem(position).getVicinity());

        return convertView;
    }

    static class ViewHolder {
        TextView shopName;
        TextView shopVicinity;
    }
}