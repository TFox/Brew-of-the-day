package com.tinyhydra.botd;

import android.location.Location;
import android.os.Handler;
import android.widget.Toast;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * © copyright 2012 tinyhydra.com
 */
public class GoogleOperations {
    public static List<JavaShop> GetShops(Handler handler, Location currentLocation, String placesApiKey) {
        // Use google places to get all the shops within '500' (I believe meters is the default measurement they use)
        // make a list of JavaShops and pass it to the ListView adapter
        List<JavaShop> shopList = new ArrayList<JavaShop>();
        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet request = new HttpGet();
            int accuracy = Math.round(currentLocation.getAccuracy());
            if (accuracy < 500)
                accuracy = 500;
            request.setURI(URI.create("https://maps.googleapis.com/maps/api/place/search/json?location=" + currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&radius=" + accuracy + "&types=" + URLEncoder.encode("cafe|restaurant|food", "UTF-8") + "&keyword=coffee&sensor=true&key=" + placesApiKey));
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
                Utils.PostToastMessageToHandler(handler, "No shops found in your area.", Toast.LENGTH_SHORT);
                return shopList;
            } else if (!jstatus.equals("OK")) {
                Utils.PostToastMessageToHandler(handler, "Error retrieving local shops.", Toast.LENGTH_SHORT);
                return shopList;
            }

            // This section may fail if there's no results, but we'll just display an empty list.
            //TODO: alert the user and cancel the dialog if this fails
            JSONArray ja = new JSONArray(predictions.getString("results"));

            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = (JSONObject) ja.get(i);
                shopList.add(new JavaShop(jo.getString("name"), jo.getString("id"), "", jo.getString("reference"), jo.getString("vicinity")));
            }
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
        return shopList;
    }
}
