package com.simprints.libdata;


public interface DataCallback {

    void onSuccess();

    void onFailure(DATA_ERROR error);

}
