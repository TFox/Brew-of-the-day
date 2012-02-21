package com.mercapps.botd;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
        locationAdapter = new LocationAdapter(this, new ArrayList<com.mercapps.botd.Location>());
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ll = new BrewLocationListener();
        cal = Calendar.getInstance();
        handler = new Handler();

        voteButton = (Button) findViewById(R.id.main_votebutton);
        dateText = (TextView) findViewById(R.id.main_datetext);

        maxDistance = getResources().getInteger(R.integer.maxdistance);
        vDistance = maxDistance * 2;

        //SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy");
        dateText.setText(sdf.format(cal.getTimeInMillis()));

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
    }

    LocationManager lm;
    LocationListener ll;
    String lat;
    String lng;
    LocationAdapter locationAdapter;
    Account account;
    Calendar cal;
    Handler handler;

    Button voteButton;
    TextView dateText;

    int maxDistance;
    int vDistance;
    boolean vAccount = false;
    boolean vLocations = false;

    public void Vote(View v) {
        Validate();
    }

    public void Vote() {
        final Dialog ld = new Dialog(this);
        ld.setContentView(R.layout.location_dialog);
        ld.setTitle("Select a location:");
        ListView ldList = (ListView) ld.findViewById(R.id.ld_listview);
        ldList.setAdapter(locationAdapter);
        ldList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String reference = view.findViewById(R.id.li_location).getTag().toString();
                ld.hide();
            }
        });
        ld.show();
    }

    public void GetLocations() {
        try {
            List<com.mercapps.botd.Location> tmpLocList = new ArrayList<com.mercapps.botd.Location>();

            URL googlePlaces = new URL(
                    // URLEncoder.encode(url,"UTF-8");
                    "https://maps.googleapis.com/maps/api/place/search/json?location=" + lat + "," + lng + "&radius=500&types=cafe&sensor=true&key=" + getResources().getString(R.string.google_places_key));
            URLConnection tc = googlePlaces.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    tc.getInputStream()));
            String line;
            StringBuffer sb = new StringBuffer();
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
            vLocations = true;

            JSONArray ja = new JSONArray(predictions.getString("results"));

            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = (JSONObject) ja.get(i);
                tmpLocList.add(new com.mercapps.botd.Location(jo.getString("name"), jo.getString("reference"), jo.getString("vicinity")));
            }
            locationAdapter.refreshLocationList(tmpLocList);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            URL googleDistance = new URL(
                    // URLEncoder.encode(url,"UTF-8");
                    "http://maps.googleapis.com/maps/api/distancematrix/json?origins=Seattle&destinations=" + lat + "," + lng + "&mode=bicycling&sensor=true");
            URLConnection tc = googleDistance.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    tc.getInputStream()));
            String line;
            StringBuffer sb = new StringBuffer();
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

    void Validate() {
        vDistance = maxDistance * 2;
        vAccount = false;

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
                                vAccount = true;
                            } else {
                                account = null;
                                Toast.makeText(getApplicationContext(), "There was an error retrieving your google account information. Voting is disabled.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }

                        getDistanceFromOrigin();
                        if (vDistance > maxDistance)
                            return;

                        GetLocations();
                        if (vAccount && vLocations)
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
