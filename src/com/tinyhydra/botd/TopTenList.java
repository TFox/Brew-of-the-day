package com.tinyhydra.botd;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.*;


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
        ListView ldList = (ListView) findViewById(R.layout.topten);
        ldList.setAdapter(new TopTenAdapter(this,BotdServerOperations.GetTopTen(this,handler)));
        ldList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            }
        });
    }

    final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            switch (message.arg1) {
                case Const.CODE_SHOWTOAST:
                    Toast.makeText(Main.this, message.getData().getString(Const.MessageToastString), message.arg2).show();
                    break;
                case Const.CODE_GETTOPTEN:
                    final JavaShop topShop = BotdServerOperations.ParseShopJSON(settings.getString(Const.LastTopTenQueryResults, ""), getResources().getString(R.string.google_api_key)).get(0);
                    brewOfTheDayName.setText(topShop.getName());
                    brewOfTheDayVicinity.setText(topShop.getVicinity());
                    findViewById(R.id.main_currentbotdparent).setTag(topShop);
                    // default url is '---', and may not get set if the http request fails, so just doublecheck before
                    // setting the onClick.
                    if (topShop.getUrl().contains("http")) {
                        findViewById(R.id.main_currentbotdparent).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(topShop.getUrl()));
                                startActivity(browserIntent);
                            }
                        });
                    }
            }
        }
    };
}