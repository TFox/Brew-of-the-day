package com.mercapps.botd;

/**
 * Property of Groupsy Mobile, inc.
 * <p/>
 * THIS FILE AND ITS CONTENTS ARE THE SOLE PROPERTY
 * OF GROUPSY MOBILE, INC. EXCEPT AS REQUIRED BY
 * ALTERNATE LICENSES OR LAW. IT MAY NOT BE DUPLICATED OR
 * USED IN ANY FASHION WITHOUT EXPRESS PERMISSION FROM AN
 * AUTHORIZED REPRESENTATIVE OF GROUPSY MOBILE, INC.
 * <p/>
 * User: TFox
 * Date: 1/29/12
 * Time: 7:24 PM
 */
public class Location {
    String name;
    String id;
    String vicinity;

    public Location(String name, String id, String vicinity) {
        this.name = name;
        this.id = id;
        this.vicinity = vicinity;
    }

    public Location() {
        this.name = "";
        this.id = "";
        this.vicinity = "";
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
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

    public void setVicinity(String vicinity) {
        this.vicinity = vicinity;
    }
}
