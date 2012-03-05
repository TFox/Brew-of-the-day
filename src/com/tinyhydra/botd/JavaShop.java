package com.tinyhydra.botd;

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
// The JavaShop object. Contains relevant data about coffee shops we've either
// gotten back from google places. 'url' may not always be set, but should be
// when we need it
public class JavaShop {
    // user-friendly name string
    String name;
    // google places 'id' string, cannot be used to fetch place data, but will remain consistent,
    // we can use it to vote with
    String id;
    // the place url. Useful for showing location on map, but requires a second http request
    String url;
    // places 'reference' string. String may change, but will always resolve to the same location.
    // Pair this with 'id' to keep consistent votes, and enable access to other data we need such as
    // url.
    String reference;
    // the 'nearest address' as defined by google places. Doesn't contain state, zip, or country data
    // which we don't care about anyway. So far this has been accurate enough to rely on.
    //TODO: keep an eye on this, if users start reporting the address isn't correct, switch to the
    //TODO: 2nd http request to get the correct address and parse the state/zip/country data out
    String vicinity;
    int votes;

    public JavaShop(String name, String id, String url, String reference, String vicinity) {
        this.name = name;
        this.id = id;
        this.url = url;
        this.reference = reference;
        this.vicinity = vicinity;
        this.votes = 0;
    }

    public JavaShop(String name, String id, String url, String reference, String vicinity, int votes) {
        this.name = name;
        this.id = id;
        this.url = url;
        this.reference = reference;
        this.vicinity = vicinity;
        this.votes = votes;
    }

    public JavaShop() {
        // default constructor, set bogus data for UI use.
        //TODO: could probably use something a little friendlier than '---'
        this.name = "---";
        this.id = "---";
        this.url = "---";
        this.reference = "---";
        this.vicinity = "---";
        this.votes = 0;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getReference() {
        return reference;
    }

    public String getVicinity() {
        return vicinity;
    }

    public int getVotes() {
        return votes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }
}
