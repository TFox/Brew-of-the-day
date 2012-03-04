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
 * Property of Groupsy Mobile, inc.
 * <p/>
 * THIS FILE AND ITS CONTENTS ARE THE SOLE PROPERTY
 * OF GROUPSY MOBILE, INC. EXCEPT AS REQUIRED BY
 * ALTERNATE LICENSES OR LAW. IT MAY NOT BE DUPLICATED OR
 * USED IN ANY FASHION WITHOUT EXPRESS PERMISSION FROM AN
 * AUTHORIZED REPRESENTATIVE OF GROUPSY MOBILE, INC.
 * <p/>
 * User: TFox
 * Date: 3/2/12
 * Time: 5:06 PM
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