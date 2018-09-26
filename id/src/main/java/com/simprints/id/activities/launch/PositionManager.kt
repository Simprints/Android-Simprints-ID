package com.simprints.id.activities.launch

import android.content.Context
import com.simprints.id.data.analytics.eventData.models.session.Location
import io.reactivex.Single
import com.google.android.gms.location.LocationRequest
import pl.charmas.android.reactivelocation2.ReactiveLocationProvider

class PositionManager {

    fun getLocation(context: Context): Single<android.location.Location>? {

        val locationProvider = ReactiveLocationProvider(context)

        val req = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        return locationProvider.getUpdatedLocation(req).firstOrError()
    }
}
