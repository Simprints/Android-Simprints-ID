package com.simprints.id.data.db;


public interface DataCallback {

    void onSuccess(boolean isDataFromRemote);

    void onFailure(DATA_ERROR error);

}
