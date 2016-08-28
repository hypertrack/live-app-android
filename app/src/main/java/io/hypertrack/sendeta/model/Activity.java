package io.hypertrack.sendeta.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by piyush on 28/08/16.
 */
public class Activity extends RealmObject {

    @PrimaryKey
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
