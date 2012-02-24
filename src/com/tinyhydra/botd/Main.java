package com.tinyhydra.botd;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Copyright © 2012 mercapps.com
 */
public class Main extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        javaShopAdapter = new JavaShopAdapter(this, new ArrayList<JavaShop>());
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ll = new BrewLocationListener();
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
        cal = Calendar.getInstance();
        handler = new Handler();
        BotdServerOperations.GetTopFive(this, handler);

        voteButton = (ImageButton) findViewById(R.id.main_votebutton);
        dateText = (TextView) findViewById(R.id.main_datetext);

        maxDistance = getResources().getInteger(R.integer.maxdistance);
        vDistance = maxDistance * 2;

        //SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy");
        dateText.setText(sdf.format(cal.getTimeInMillis()));

        settings = getSharedPreferences(Const.GenPrefs, 0);
        editor = settings.edit();
    }

    @Override
    public void onResume() {
        super.onResume();
        BotdServerOperations.GetTopFive(this, handler);
    }

    SharedPreferences settings;
    SharedPreferences.Editor editor;

    LocationManager lm;
    LocationListener ll;
    String lat;
    String lng;
    JavaShopAdapter javaShopAdapter;
    Account account;
    Calendar cal;
    Handler handler;

    ImageButton voteButton;
    TextView dateText;

    int maxDistance;
    int vDistance;
    boolean vShops = false;

    // Initiating Menu XML file (menu.xml)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    /**
     * Event Handling for Individual menu item selected
     * Identify single menu item by it's id
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mainmenu_refresh:
                BotdServerOperations.GetTopFive(this, handler);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void Vote(View v) {
        if (settings.getLong(Const.LastVoteDate, 0) == Utils.GetDate())
            Toast.makeText(this, "You've already voted today!", Toast.LENGTH_SHORT).show();
        else
            Validate();
    }

    public void Vote() {
        final Dialog ld = new Dialog(this);
        ld.setContentView(R.layout.javashop_dialog);
        ld.setTitle("Select a location:");
        ListView ldList = (ListView) ld.findViewById(R.id.sd_listview);
        ldList.setAdapter(javaShopAdapter);
        ldList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                JavaShop javaShop = (JavaShop) view.findViewById(R.id.si_name).getTag();
                BotdServerOperations.CastVote(Main.this, handler, account.name, javaShop.getId(), javaShop.getReference());
                ld.hide();
            }
        });
        ld.show();
    }

    public void GetShops() {
        try {
            List<JavaShop> tmpLocList = new ArrayList<JavaShop>();

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI("https://maps.googleapis.com/maps/api/place/search/json?location=" + lat + "," + lng + "&radius=500&types=cafe&sensor=true&key=" + getResources().getString(R.string.google_api_key)));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader
                    (new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }

            JSONObject predictions = new JSONObject(sb.toString());
            String jstatus = predictions.getString("status");
            if (!jstatus.equals("OK")) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error retrieving local shops.", Toast.LENGTH_SHORT).show();
                    }
                });
                return;
            }
            vShops = true;

            JSONArray ja = new JSONArray(predictions.getString("results"));

            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = (JSONObject) ja.get(i);
                tmpLocList.add(new JavaShop(jo.getString("name"), jo.getString("id"), "", jo.getString("reference"), jo.getString("vicinity")));
            }
            javaShopAdapter.refreshShopList(tmpLocList);
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
    }

    public void getDistanceFromOrigin() {
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            request.setURI(new URI("http://maps.googleapis.com/maps/api/distancematrix/json?origins=Seattle&destinations=" + lat + "," + lng + "&mode=bicycling&sensor=true"));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader
                    (new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }

            JSONObject distanceInfo = new JSONObject(sb.toString());
            String status = distanceInfo.getString("status");
            if (status.equals("OK")) {
                JSONArray ja = distanceInfo.getJSONArray("rows").getJSONObject(0).getJSONArray("elements");
                for (int i = 0; i < ja.length(); i++) {
                    if (ja.getJSONObject(i).has("distance"))
                        vDistance = ja.getJSONObject(i).getJSONObject("distance").getInt("value");
                }
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "There was a problem determining your location. Please check your phone & GPS signal and try again. Voting is disabled.", Toast.LENGTH_LONG).show();
                    }
                });
            }
            if (vDistance > maxDistance)
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "You seem to be outside the Seattle area, please try again from a location closer to the Emerald City. Voting is disabled.", Toast.LENGTH_LONG).show();
                    }
                });

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
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Error fetching location. Voting is disabled.", Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
    }

    private void Validate() {
        vDistance = maxDistance * 2;

        new Thread() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (account == null) {
                            Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType("com.google");
                            if (accounts.length > 0) {
                                account = accounts[0];
                            } else {
                                account = null;
                                Toast.makeText(getApplicationContext(), "There was an error retrieving your google account information. Voting is disabled.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        getDistanceFromOrigin();
                        if (vDistance > maxDistance)
                            return;

                        GetShops();
                        if (account != null && vShops)
                            Vote();
                    }
                });
            }
        }.start();
    }

    public class BrewLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            lat = Double.toString(loc.getLatitude());
            lng = Double.toString(loc.getLongitude());
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }
}
