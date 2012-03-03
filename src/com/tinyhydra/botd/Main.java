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
import android.os.Message;
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
        voteButton = (ImageButton) findViewById(R.id.main_votebutton);
        dateText = (TextView) findViewById(R.id.main_datetext);
        maxDistance = getResources().getInteger(R.integer.maxdistance);
        settings = getSharedPreferences(Const.GenPrefs, 0);
        editor = settings.edit();
        // Set current date on the UI
        cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d, yyyy");
        brewOfTheDayName = (TextView) findViewById(R.id.main_currentbotdnametext);
        brewOfTheDayVicinity = (TextView) findViewById(R.id.main_currentbotdaddresstext);
        dateText.setText(sdf.format(cal.getTimeInMillis()));
        // We use distance to make sure you're in the right area to vote.
        // Set default to double max. gps updates will override.
        vDistance = maxDistance * 2;

        // Get the current brew of the day from the server
        BotdServerOperations.GetTopTen(this, handler);
    }

    @Override
    public void onResume() {
        super.onResume();
        // update current brew of the day from the server 
        BotdServerOperations.GetTopTen(this, handler);
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

    ImageButton voteButton;
    TextView brewOfTheDayName;
    TextView brewOfTheDayVicinity;
    TextView dateText;

    int maxDistance;
    int vDistance;

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
                BotdServerOperations.GetTopTen(this, handler);
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
                    List<JavaShop> shopList = GoogleOperations.GetShops(handler, currentLoc, getResources().getString(R.string.google_api_key));
                    if (shopList.size() > 0) {
                        javaShopAdapter.refreshShopList(shopList);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Vote();
                            }
                        });
                    }
                } else {
                    Utils.PostToastMessageToHandler(handler, "You seem to be outside the Seattle area. Please try again from a location closer to the Emerald City.", Toast.LENGTH_LONG);
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
