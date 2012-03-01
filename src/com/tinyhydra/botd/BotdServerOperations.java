package com.tinyhydra.botd;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Copyright © 2012 tinyhydra.com
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
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity.getApplicationContext(), "Vote cast!", Toast.LENGTH_SHORT).show();
                            }
                        });
                        // Set a local flag to prevent duplicate voting
                        SharedPreferences settings = activity.getSharedPreferences(Const.GenPrefs, 0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putLong(Const.LastVoteDate, Utils.GetDate());
                        editor.commit();
                    } else {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                // The user shouldn't see this. The above SharedPreferences code will be evaluated
                                // when the user hits the Vote button. If the user gets sneaky and deletes local data though,
                                // the server will catch the duplicate vote based on the user's email address and send back a '1'.
                                Toast.makeText(activity.getApplicationContext(), "Vote refused. You've probably already voted today.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    // Catch blocks. Return a generic error if anything goes wrong.
                    //TODO: implement some better/more appropriate error handling.
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

    // We're only using the #1 voted shop right now, but at some point it would be nice to have
    // a display for the top 5. This code allows for that, I just have to -
    //TODO: implement 'top 5' listview activity to implement this code. Would also be helpful to
    //TODO: show the user's current shop vote & maybe vote history.
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
                        // get JavaShop object and add it to the array with a rank indicator.
                        //TODO: make a cleaner ranking process. This seems sloppy
                        TopFive.put(i, GetLocation(activity, results.getString("" + i)));
                    }
                    // more generic error handling
                    //TODO: implement better error handling
                } catch (URISyntaxException usex) {
                    usex.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), "Unable to retrieve Brew of the day. Poor signal? Please try again", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (ClientProtocolException cpex) {
                    cpex.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), "Unable to retrieve Brew of the day. Poor signal? Please try again", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (IOException iex) {
                    iex.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), "Unable to retrieve Brew of the day. Poor signal? Please try again", Toast.LENGTH_LONG).show();
                        }
                    });
                } catch (JSONException jex) {
                    jex.printStackTrace();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity.getApplicationContext(), "Unable to retrieve Brew of the day. Poor signal? Please try again", Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                // Set the top vote data on the main activity UI. If we add pages, this is going to cause a problem.
                //TODO: make a proper handler in the main activity, set the javashop data to a variable and have the
                //TODO: handler pick it up and display if the main activity is active.
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ((TextView) activity.findViewById(R.id.main_currentbotdnametext)).setText(TopFive.get(1).getName());
                        ((TextView) activity.findViewById(R.id.main_currentbotdaddresstext)).setText(TopFive.get(1).getVicinity());
                        activity.findViewById(R.id.main_currentbotdparent).setTag(TopFive.get(1));
                        // default url is '---', and may not get set if the http request fails, so just doublecheck before
                        // setting the onClick.
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

    // secondary http request to the google places api. this resolves a reference code into all the other data
    // we want to use, like name, vicinity, and url. The server only stores and returns id and ref.
    public static JavaShop GetLocation(Activity activity, String reference) {
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
