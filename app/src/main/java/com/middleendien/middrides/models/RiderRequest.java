package com.middleendien.middrides.models;

import com.middleendien.middrides.utils.Request;

/**
 * Created by Peter on 10/1/15.
 */
public class RiderRequest extends Request{
    private int position;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
