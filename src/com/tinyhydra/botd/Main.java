package com.tinyhydra.botd;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.*;
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
 * Copyright © 2012 tinyhydra.com
 */
public class Main extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // set the ListView adapter, for use in the vote dialog
        javaShopAdapter = new JavaShopAdapter(this, new ArrayList<JavaShop>());

        // set location services, we'll use this later to find nearby coffee shops
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ll = new BrewLocationListener();
        currentLoc = lm.getLastKnownLocation(lm.getBestProvider(new Criteria(), true));
        origin = new Location(lm.getBestProvider(new Criteria(), false));
        origin.setLatitude(Double.parseDouble(this.getResources().getString(R.string.seattlelat)));
        origin.setLongitude(Double.parseDouble(this.getResources().getString(R.string.seattlelng)));
        locationUpdating = false;

        // Set local resources for use later on
        handler = new Handler();
        voteButton = (ImageButton) findViewById(R.id.main_votebutton);
        dateText = (TextView) findViewById(R.id.main_datetext);
        maxDistance = getResources().getInteger(R.integer.maxdistance);
        settings = getSharedPreferences(Const.GenPrefs, 0);
        editor = settings.edit();
        vShops = false;
        // Set current date on the UI
        cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy");
        dateText.setText(sdf.format(cal.getTimeInMillis()));
        // We use distance to make sure you're in the right area to vote.
        // Set default to double max. gps updates will override.
        vDistance = maxDistance * 2;

        // Get the current brew of the day from the server
        BotdServerOperations.GetTopFive(this, handler);
    }

    @Override
    public void onResume() {
        super.onResume();
        // update current brew of the day from the server 
        BotdServerOperations.GetTopFive(this, handler);
    }

    // Local variables
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    LocationManager lm;
    LocationListener ll;
    Location currentLoc;
    Location origin;
    boolean locationUpdating;
    ProgressDialog validatePD;
    JavaShopAdapter javaShopAdapter;
    Account account;
    Calendar cal;
    Handler handler;

    ImageButton voteButton;
    TextView dateText;

    int maxDistance;
    int vDistance;
    boolean vShops;

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
                // request current brew of the day (on demand) from server
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
        // If validation passes, pop up the vote dialog. This will show all nearby coffee shops
        // and allow the user to select the one they'd like to vote for.
        final Dialog ld = new Dialog(this);
        ld.setContentView(R.layout.javashop_dialog);
        ld.setTitle("Select a location:");
        if (!lm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
            LinearLayout ldParent = (LinearLayout) ld.findViewById(R.id.sd_parent);
            LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            TextView gpsDisabledText = new TextView(ldParent.getContext());
            gpsDisabledText.setText("Need more accurate results?");
            gpsDisabledText.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            gpsDisabledText.setTextColor(getResources().getColor(R.color.creme));
            gpsDisabledText.setLayoutParams(vlp);
            gpsDisabledText.setGravity(Gravity.CENTER_HORIZONTAL);
            ldParent.addView(gpsDisabledText);
            Button enableGpsButton = new Button(ldParent.getContext());
            enableGpsButton.setLayoutParams(vlp);
            enableGpsButton.setText("Enable GPS");
            enableGpsButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.creme_button));
            enableGpsButton.setTextColor(getResources().getColor(R.color.espresso));
            enableGpsButton.setGravity(Gravity.CENTER_HORIZONTAL);
            enableGpsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                    startActivity(i);
                    ld.dismiss();
                }
            });
            ldParent.addView(enableGpsButton);
        }
        ListView ldList = (ListView) ld.findViewById(R.id.sd_listview);
        ldList.setAdapter(javaShopAdapter);
        ldList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Tag contains the JavaShop object associated with the current selection, it gets set in the adapter.
                final JavaShop javaShop = (JavaShop) view.findViewById(R.id.si_name).getTag();

                final Dialog confirmDialog = new Dialog(Main.this);
                confirmDialog.setContentView(R.layout.vote_confirmation_dialog);
                confirmDialog.setTitle("Cast vote for:");
                ((TextView) confirmDialog.findViewById(R.id.cd_vote_nametext)).setText(javaShop.getName());
                Button confirmButton = (Button) confirmDialog.findViewById(R.id.cd_confirmbutton);
                Button cancelButton = (Button) confirmDialog.findViewById(R.id.cd_cancelbutton);
                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        BotdServerOperations.CastVote(Main.this, handler, account.name, javaShop.getId(), javaShop.getReference());
                        confirmDialog.dismiss();
                        ld.dismiss();
                    }
                });
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        confirmDialog.dismiss();
                    }
                });
                confirmDialog.show();
            }
        });
        ld.show();
    }

    public boolean GetShops() {
        try {
            // Use google places to get all the shops within '500' (I believe meters is the default measurement they use)
            // make a list of JavaShops and pass it to the ListView adapter
            List<JavaShop> tmpLocList = new ArrayList<JavaShop>();

            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            int accuracy = Math.round(currentLoc.getAccuracy());
            if (accuracy < 500)
                accuracy = 500;
            request.setURI(URI.create("https://maps.googleapis.com/maps/api/place/search/json?location=" + currentLoc.getLatitude() + "," + currentLoc.getLongitude() + "&radius=" + accuracy + "&types=" + URLEncoder.encode("cafe|restaurant|food", "UTF-8") + "&keyword=coffee&sensor=true&key=" + getResources().getString(R.string.google_api_key)));
            HttpResponse response = client.execute(request);
            BufferedReader in = new BufferedReader
                    (new InputStreamReader(response.getEntity().getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }

            JSONObject predictions = new JSONObject(sb.toString());
            // Google passes back a status string. if we screw up, it won't say "OK". Alert the user.
            String jstatus = predictions.getString("status");
            if (jstatus.equals("ZERO_RESULTS")) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "No shops found in your area.", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            } else if (!jstatus.equals("OK")) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Error retrieving local shops.", Toast.LENGTH_SHORT).show();
                    }
                });
                return false;
            }
            // if all is well, set this section as a pass.
            vShops = true;

            // This section may fail if there's no results, but we'll just display an empty list.
            //TODO: alert the user and cancel the dialog if this fails
            JSONArray ja = new JSONArray(predictions.getString("results"));

            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = (JSONObject) ja.get(i);
                tmpLocList.add(new JavaShop(jo.getString("name"), jo.getString("id"), "", jo.getString("reference"), jo.getString("vicinity")));
            }
            javaShopAdapter.refreshShopList(tmpLocList);
            return true;
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
        return false;
    }

    private void Validate() {
        vDistance = maxDistance * 2;
        // Check the account to make sure we can read it. We use the user's google account to prevent multiple votes
        // check the distance, and grab nearby shops, then launch the listview dialog with Vote();
        validatePD = new ProgressDialog(this);
        validatePD.setMessage("Please wait");
        validatePD.show();
        new Thread() {
            @Override
            public void run() {
                if (account == null) {
                    Account[] accounts = AccountManager.get(getApplicationContext()).getAccountsByType("com.google");
                    if (accounts.length > 0) {
                        account = accounts[0];
                    } else {
                        account = null;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "There was an error retrieving your google account information. Voting is disabled.", Toast.LENGTH_SHORT).show();
                                validatePD.dismiss();
                            }
                        });
                        return;
                    }
                }
                locationUpdating = true;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        validatePD.setMessage("Updating location");
                        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
                        else
                            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
                    }
                });
                int count = 0;
                while (count < 10 && locationUpdating) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException iex) {
                        Log.e("Location Update", "Interrupted waiting for location update");
                    }
                    if (count == 5 && lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "GPS signal unavailable. Using Network locator", Toast.LENGTH_SHORT).show();
                                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, ll);
                            }
                        });
                    count++;
                }
                if (locationUpdating) {
                    locationUpdating = false;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Unable to update location at this time. Please check phone & GPS signal", Toast.LENGTH_LONG).show();
                            validatePD.dismiss();
                        }
                    });
                    return;
                }
                if (vDistance < maxDistance) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            validatePD.setMessage("Getting nearby cafe locations");
                        }
                    });
                    if (GetShops()) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Vote();
                            }
                        });
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "You seem to be outside the Seattle area. Please try again from a location closer to the Emerald City.", Toast.LENGTH_LONG).show();
                        }
                    });
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        validatePD.dismiss();
                    }
                });
            }
        }.start();
    }

    public class BrewLocationListener implements LocationListener {
        //Default GPS location listener.
        @Override
        public void onLocationChanged(Location loc) {
            currentLoc = loc;
            vDistance = Math.round(currentLoc.distanceTo(origin));
            locationUpdating = false;
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }
}
