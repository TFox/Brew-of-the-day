package com.mercapps.botd;

/**
 * Copyright © 2012 mercapps.com
 */
public class Location {
    String name;
    String reference;
    String vicinity;

    public Location(String name, String reference, String vicinity) {
        this.name = name;
        this.reference = reference;
        this.vicinity = vicinity;
    }

    public Location() {
        this.name = "";
        this.reference = "";
        this.vicinity = "";
    }

    public String getName() {
        return name;
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

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
}
