package com.simprints.id.tools

import android.location.Location
import com.google.android.gms.location.LocationRequest
import kotlinx.coroutines.flow.Flow

interface LocationManager {
    suspend fun requestLocation(request: LocationRequest): Flow<List<Location>>

}
