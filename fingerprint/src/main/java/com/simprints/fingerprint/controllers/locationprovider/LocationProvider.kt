package com.simprints.fingerprint.controllers.locationprovider

import android.location.Location
import com.google.android.gms.location.LocationRequest
import io.reactivex.Observable

interface LocationProvider {

    fun getUpdatedLocation(locationRequest: LocationRequest): Observable<Location>
}
