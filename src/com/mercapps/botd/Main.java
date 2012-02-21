package com.mercapps.botd;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
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
import java.util.ArrayList;
import java.util.List;

public class Main extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        locationAdapter = new LocationAdapter(this, new ArrayList<com.mercapps.botd.Location>());
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ll = new MyLocationListener();

        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, ll);
    }

    LocationManager lm;
    LocationListener ll;
    String lati;
    String longi;
    LocationAdapter locationAdapter;

    public void GetLocations(View v) {
        getData();
        final Dialog ld = new Dialog(this);
        ld.setContentView(R.layout.location_dialog);
        ld.setTitle("Select a location:");
        ListView ldList = (ListView) ld.findViewById(R.id.ld_listview);
        ldList.setAdapter(locationAdapter);
        ldList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(Main.this, locationAdapter.getItem(i).getName(), Toast.LENGTH_SHORT).show();
                ld.hide();
            }
        });
        ld.show();
    }

    public void getData() {
        try {
            List<com.mercapps.botd.Location> tmpLocList = new ArrayList<com.mercapps.botd.Location>();

            URL googlePlaces = new URL(
                    // URLEncoder.encode(url,"UTF-8");
                    "https://maps.googleapis.com/maps/api/place/search/json?location=" + lati + "," + longi + "&radius=500&types=cafe&sensor=true&key=" + getResources().getString(R.string.placeskey));
            URLConnection tc = googlePlaces.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    tc.getInputStream()));
            String line;
            StringBuffer sb = new StringBuffer();
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            JSONObject predictions = new JSONObject(sb.toString());
            JSONArray ja = new JSONArray(predictions.getString("results"));

            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = (JSONObject) ja.get(i);
                tmpLocList.add(new com.mercapps.botd.Location(jo.getString("name"), jo.getString("id"), jo.getString("vicinity")));
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

    public class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
            lati = Double.toString(loc.getLatitude());
            longi = Double.toString(loc.getLongitude());

            String Text = "My current location is:" +
                    "Latitude =" + lati +
                    "Longitude =" + longi;

            Toast.makeText(getApplicationContext(), Text, Toast.LENGTH_SHORT).show();
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

    }/* End of Class MyLocationListener */
}
