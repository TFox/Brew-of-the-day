package com.tinyhydra.botd;

/**
 * Copyright © 2012 mercapps.com
 */
public class JavaShop {
    String name;
    String id;
    String url;
    String reference;
    String vicinity;

    public JavaShop(String name, String id, String url, String reference, String vicinity) {
        this.name = name;
        this.id = id;
        this.url = url;
        this.reference = reference;
        this.vicinity = vicinity;
    }

    public JavaShop() {
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
