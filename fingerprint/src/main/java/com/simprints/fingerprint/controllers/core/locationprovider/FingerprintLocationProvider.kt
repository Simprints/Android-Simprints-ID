package com.simprints.fingerprint.controllers.core.locationprovider

import android.location.Location
import com.google.android.gms.location.LocationRequest
import io.reactivex.Observable

interface FingerprintLocationProvider {

    fun getUpdatedLocation(locationRequest: LocationRequest): Observable<Location>
}
