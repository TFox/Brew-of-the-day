package com.tinyhydra.botd;

/**
 * Copyright © 2012 tinyhydra.com
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

    public JavaShop(String name, String id, String url, String reference, String vicinity) {
        this.name = name;
        this.id = id;
        this.url = url;
        this.reference = reference;
        this.vicinity = vicinity;
    }

    public JavaShop() {
        // default constructor, set bogus data for UI use.
        //TODO: could probably use something a little friendlier than '---'
        this.name = "---";
        this.id = "---";
        this.url = "---";
        this.reference = "---";
        this.vicinity = "---";
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
}
