package com.tinyhydra.botd;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Copyright © 2012 mercapps.com
 */
public class BotdServerOperations {
    public static void CastVote(final Activity activity, final Handler handler, final String email, final String shopId, final String shopRef) {
        new Thread() {
            @Override
            public void run() {
                try {
                    URI uri = new URI(activity.getResources().getString(R.string.server_url));
                    HttpClient client = new DefaultHttpClient();
                    HttpPut put = new HttpPut(uri);

                    JSONObject voteObj = new JSONObject();

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
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity.getApplicationContext(), "Vote cast!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        SharedPreferences settings = activity.getSharedPreferences(Const.GenPrefs, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putLong(Const.LastVoteDate, Utils.GetDate());
                        editor.commit();
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity.getApplicationContext(), "Vote refused. You've probably already voted today.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (URISyntaxException usex) {
                    usex.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), "There was a problem submitting your vote. Poor signal? Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (UnsupportedEncodingException ueex) {
                    ueex.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), "There was a problem submitting your vote. Poor signal? Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (ClientProtocolException cpex) {
                    cpex.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), "There was a problem submitting your vote. Poor signal? Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException ioex) {
                    ioex.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), "There was a problem submitting your vote. Poor signal? Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException jex) {
                    jex.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), "There was a problem submitting your vote. Poor signal? Please try again.", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        }.start();
    }

    public static void GetTopFive(final Activity activity, final Handler handler) {
        new Thread() {
            @Override
            public void run() {
                final HashMap<Integer, JavaShop> TopFive = new HashMap<Integer, JavaShop>();
                for (int i = 1; i <= 5; i++) {
                    TopFive.put(i, new JavaShop());
                }
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
                    JSONObject results = new JSONObject(sb.toString());
                    for (int i = 1; i <= results.length(); i++) {
                        TopFive.put(i, GetLocation(activity, results.getString("" + i)));
                    }
                } catch (URISyntaxException usex) {
                    usex.printStackTrace();
                    Toast.makeText(activity.getApplicationContext(), "Unable to retrieve Brew of the day. Poor signal? Please try again", Toast.LENGTH_LONG).show();
                } catch (ClientProtocolException cpex) {
                    cpex.printStackTrace();
                    Toast.makeText(activity.getApplicationContext(), "Unable to retrieve Brew of the day. Poor signal? Please try again", Toast.LENGTH_LONG).show();
                } catch (IOException iex) {
                    iex.printStackTrace();
                    Toast.makeText(activity.getApplicationContext(), "Unable to retrieve Brew of the day. Poor signal? Please try again", Toast.LENGTH_LONG).show();
                } catch (JSONException jex) {
                    jex.printStackTrace();
                    Toast.makeText(activity.getApplicationContext(), "Unable to retrieve Brew of the day. Poor signal? Please try again", Toast.LENGTH_LONG).show();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) activity.findViewById(R.id.main_currentbotdnametext)).setText(TopFive.get(1).getName());
                        ((TextView) activity.findViewById(R.id.main_currentbotdaddresstext)).setText(TopFive.get(1).getVicinity());
                        activity.findViewById(R.id.main_currentbotdparent).setTag(TopFive.get(1));
                        if (TopFive.get(1).getUrl().contains("http")) {
                            activity.findViewById(R.id.main_currentbotdparent).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TopFive.get(1).getUrl()));
                                    activity.startActivity(browserIntent);
                                }
                            });
                        }
                    }
                });
            }
        }.start();
    }

    private static JavaShop GetLocation(Activity activity, String reference) {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI("https://maps.googleapis.com/maps/api/place/details/json?reference=" + reference + "&sensor=true&key=" + activity.getResources().getString(R.string.google_api_key)));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader
                    (new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }

            JSONObject results = new JSONObject(sb.toString());
            String jstatus = results.getString("status");
            if (jstatus.equals("OK")) {
                JSONObject resultObj = results.getJSONObject("result");
                return new JavaShop(resultObj.getString("name"), resultObj.getString("id"), resultObj.getString("url"), reference, resultObj.getString("vicinity"));
            }
//            vLocations = true;
//
//            JSONArray ja = new JSONArray(predictions.getString("results"));
//
//            for (int i = 0; i < ja.length(); i++) {
//                JSONObject jo = (JSONObject) ja.get(i);
//                tmpLocList.add(new com.mercapps.botd.Location(jo.getString("name"), jo.getString("reference"), jo.getString("vicinity")));
//            }
//            locationAdapter.refreshLocationList(tmpLocList);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException usex) {
            usex.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return new JavaShop();
    }
}
