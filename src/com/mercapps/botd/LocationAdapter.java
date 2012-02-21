package com.mercapps.botd;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class LocationAdapter extends BaseAdapter {

    List<Location> locationList;

    private LayoutInflater mInflater;
    Context context;

    public LocationAdapter(Context context, List<Location> locationList) {
        this.context = context;
        this.locationList = locationList;
        mInflater = LayoutInflater.from(context);
    }

    public void refreshLocationList(List<Location> locationList) {
        this.locationList = locationList;
        notifyDataSetChanged();
    }

    public int getCount() {
        return locationList.size();
    }

    public Location getItem(int position) {
        return locationList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = mInflater.inflate(R.layout.location_item, null);
            holder = new ViewHolder();

            holder.locationName = (TextView) convertView.findViewById(R.id.li_location);
            holder.locationVicinity = (TextView) convertView.findViewById(R.id.li_vicinity);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.locationName.setTag(getItem(position).getId());
        holder.locationName.setText(getItem(position).getName());
        holder.locationVicinity.setText(getItem(position).getVicinity());

        return convertView;
    }

    static class ViewHolder {
        TextView locationName;
        TextView locationVicinity;
    }
}