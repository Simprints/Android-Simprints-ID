package com.simprints.id.data.db;


public interface DataCallback {

    void onSuccess();

    void onFailure(DATA_ERROR error);

}
