package com.tinyhydra.botd;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
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
public class TopTenList extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.topten);

        settings = getSharedPreferences(Const.GenPrefs, 0);
        editor = settings.edit();

        shopList = new ArrayList<JavaShop>();
        topTenAdapter = new TopTenAdapter(this, shopList);

        ldList = (ListView) findViewById(R.id.topten_parent);
        ldList.setAdapter(topTenAdapter);
        ldList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                JavaShop javaShop = (JavaShop) view.findViewById(R.id.tt_nametext).getTag();
                if (javaShop.getUrl().contains("http")) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(javaShop.getUrl()));
                    startActivity(browserIntent);
                }
            }
        });
        BotdServerOperations.GetTopTen(this, handler, false);
    }

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    TopTenAdapter topTenAdapter;
    List<JavaShop> shopList;
    ListView ldList;

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.arg1) {
                case Const.CODE_SHOWTOAST:
                    Toast.makeText(getApplicationContext(), message.getData().getString(Const.MessageToastString), message.arg2).show();
                    break;
                case Const.CODE_GETTOPTEN:
                    shopList = BotdServerOperations.ParseShopJSON(settings.getString(Const.LastTopTenQueryResults, ""));
                    topTenAdapter.refreshTopTenList(shopList);
                    break;
            }
        }
    };
}