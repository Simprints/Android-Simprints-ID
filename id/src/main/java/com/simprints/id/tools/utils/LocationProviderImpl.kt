package com.simprints.id.tools.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import io.reactivex.Observable
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider

class LocationProviderImpl(private val ctx: Context): LocationProvider {

    @SuppressLint("MissingPermission")
    override fun getUpdatedLocation(locationRequest: LocationRequest): Observable<Location> =
        ReactiveLocationProvider(ctx).getUpdatedLocation(locationRequest)

}
