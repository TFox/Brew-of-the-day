package com.tinyhydra.botd;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
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
// Server operations. For now, REST 'Get' returns top 5 shops, and REST 'Put' will cast the user's vote.
public class BotdServerOperations {
    // Casts a vote for a nearby shop returned from Google places. Server will return an int for any problems.
    // Right now the int is either 1 for problem or 0 for ok.
    //TODO: implement more error codes once the server supports them
    public static void CastVote(final Activity activity, final Handler handler, final String email, final String shopId, final String shopRef) {
        new Thread() {
            @Override
            public void run() {
                try {
                    URI uri = new URI(activity.getResources().getString(R.string.server_url));
                    HttpClient client = new DefaultHttpClient();
                    HttpPut put = new HttpPut(uri);

                    JSONObject voteObj = new JSONObject();

                    // user's phone-account-email-address is used to prevent multiple votes
                    // the server will validate. 'shopId' is a consistent id for a specific location
                    // but can't be used to get more data. 'shopRef' is an id that changes based on
                    // some criteria that google places has imposed, but will let us grab data later on
                    // and various Ref codes with the same id will always resolve to the same location.
                    voteObj.put(JSONvalues.email.toString(), email);
                    voteObj.put(JSONvalues.shopId.toString(), shopId);
                    voteObj.put(JSONvalues.shopRef.toString(), shopRef);
                    put.setEntity(new StringEntity(voteObj.toString()));

                    HttpResponse response = client.execute(put);
                    InputStream is = response.getEntity().getContent();
                    int ch;
                    StringBuffer sb = new StringBuffer();
                    while ((ch = is.read()) != -1) {
                        sb.append((char) ch);
                    }
                    if (sb.toString().equals("0")) {
                        Utils.PostToastMessageToHandler(handler, "Vote cast!", Toast.LENGTH_SHORT);
                        // Set a local flag to prevent duplicate voting
                        SharedPreferences settings = activity.getSharedPreferences(Const.GenPrefs, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putLong(Const.LastVoteDate, Utils.GetDate());
                        editor.commit();
                    } else {
                        // The user shouldn't see this. The above SharedPreferences code will be evaluated
                        // when the user hits the Vote button. If the user gets sneaky and deletes local data though,
                        // the server will catch the duplicate vote based on the user's email address and send back a '1'.
                        Utils.PostToastMessageToHandler(handler, "Vote refused. You've probably already voted today.", Toast.LENGTH_LONG);
                    }
                    GetTopTen(activity, handler, true);
                    // Catch blocks. Return a generic error if anything goes wrong.
                    //TODO: implement some better/more appropriate error handling.
                } catch (URISyntaxException usex) {
                    usex.printStackTrace();
                    Utils.PostToastMessageToHandler(handler, "There was a problem submitting your vote. Poor signal? Please try again.", Toast.LENGTH_LONG);
                } catch (UnsupportedEncodingException ueex) {
                    ueex.printStackTrace();
                    Utils.PostToastMessageToHandler(handler, "There was a problem submitting your vote. Poor signal? Please try again.", Toast.LENGTH_LONG);
                } catch (ClientProtocolException cpex) {
                    cpex.printStackTrace();
                    Utils.PostToastMessageToHandler(handler, "There was a problem submitting your vote. Poor signal? Please try again.", Toast.LENGTH_LONG);
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                    Utils.PostToastMessageToHandler(handler, "There was a problem submitting your vote. Poor signal? Please try again.", Toast.LENGTH_LONG);
                } catch (JSONException jex) {
                    jex.printStackTrace();
                    Utils.PostToastMessageToHandler(handler, "There was a problem submitting your vote. Poor signal? Please try again.", Toast.LENGTH_LONG);
                }
            }
        }.start();
    }

    public static List<JavaShop> ParseShopJSON(String shopJson) {
        List<JavaShop> shopList = new ArrayList<JavaShop>();
        for (int i = 0; i < 10; i++)
            shopList.add(new JavaShop());
        try {
            JSONArray results = new JSONArray(shopJson);
            for (int i = 0; i < 10; i++) {
                // get JavaShop object and add it to the array with a rank indicator.
                //TODO: make a cleaner ranking process. This seems sloppy
                if (i < results.length()) {
                    shopList.get(i).setId(results.getJSONObject(i).getString(JSONvalues.shopId.toString()));
                    shopList.get(i).setName(results.getJSONObject(i).getString(JSONvalues.shopName.toString()));
                    shopList.get(i).setUrl(results.getJSONObject(i).getString(JSONvalues.shopUrl.toString()));
                    shopList.get(i).setVicinity(results.getJSONObject(i).getString(JSONvalues.shopVicinity.toString()));
                    shopList.get(i).setReference(results.getJSONObject(i).getString(JSONvalues.shopRef.toString()));
                    shopList.get(i).setVotes(results.getJSONObject(i).getInt(JSONvalues.shopVotes.toString()));
                }
            }
            SortShopList(shopList);
        } catch (JSONException jex) {
            jex.printStackTrace();
        }
        return shopList;
    }

    public static void SortShopList(List<JavaShop> shopList) {
        SortShopList(shopList, 0, shopList.size() - 1);
    }

    private static void SortShopList(List<JavaShop> shopList, int left, int right) {
        int i = left, j = right;
        JavaShop tmp;
        JavaShop pivot = shopList.get((left + right) / 2);

        while (i <= j) {
            while (shopList.get(i).getVotes() > pivot.getVotes())
                i++;
            while (shopList.get(j).getVotes() < pivot.getVotes())
                j--;
            if (i <= j) {
                tmp = shopList.get(i);
                shopList.set(i, shopList.get(j));
                shopList.set(j, tmp);
                i++;
                j--;
            }
        }

        if (left < i - 1)
            SortShopList(shopList, left, i - 1);
        if (i < right)
            SortShopList(shopList, i, right);
    }

    //TODO: show the user's current shop vote & maybe vote history.
    public static void GetTopTen(final Activity activity, final Handler handler, boolean override) {
        final SharedPreferences settings = activity.getSharedPreferences(Const.GenPrefs, 0);
        final List<JavaShop> TopTen = new ArrayList<JavaShop>();
        for (int i = 0; i < 10; i++) {
            TopTen.add(new JavaShop());
        }
        if (settings.getLong(Const.LastTopTenQueryTime, 0) > (Calendar.getInstance().getTimeInMillis() - 180000) & !override) {
            Message msg = new Message();
            msg.arg1 = Const.CODE_GETTOPTEN;
            handler.sendMessage(msg);
        } else
            new Thread() {
                @Override
                public void run() {
                    BufferedReader in = null;
                    try {
                        HttpClient client = new DefaultHttpClient();
                        HttpGet request = new HttpGet();
                        request.setURI(new URI(activity.getResources().getString(R.string.server_url)));
                        HttpResponse response = client.execute(request);
                        in = new BufferedReader
                                (new InputStreamReader(response.getEntity().getContent()));
                        StringBuffer sb = new StringBuffer("");
                        String line = "";
                        while ((line = in.readLine()) != null) {
                            sb.append(line);
                        }
                        in.close();
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(Const.LastTopTenQueryResults, sb.toString());
                        editor.putLong(Const.LastTopTenQueryTime, Calendar.getInstance().getTimeInMillis());
                        editor.commit();

                        Message msg = new Message();
                        msg.arg1 = Const.CODE_GETTOPTEN;
                        handler.sendMessage(msg);

                        // more generic error handling
                        //TODO: implement better error handling
                    } catch (URISyntaxException usex) {
                        usex.printStackTrace();
                        Utils.PostToastMessageToHandler(handler, "Unable to retrieve Brew of the day. Poor signal? Please try again", Toast.LENGTH_LONG);
                    } catch (ClientProtocolException cpex) {
                        cpex.printStackTrace();
                        Utils.PostToastMessageToHandler(handler, "Unable to retrieve Brew of the day. Poor signal? Please try again", Toast.LENGTH_LONG);
                    } catch (IOException iex) {
                        iex.printStackTrace();
                        Utils.PostToastMessageToHandler(handler, "Unable to retrieve Brew of the day. Poor signal? Please try again", Toast.LENGTH_LONG);
                    } finally {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }.start();
    }
}
