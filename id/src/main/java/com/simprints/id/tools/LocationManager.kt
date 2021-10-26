package com.simprints.id.tools

import android.location.Location
import com.google.android.gms.location.LocationRequest
import kotlinx.coroutines.flow.Flow

interface LocationManager {
    fun requestLocation(request: LocationRequest): Flow<Location?>

}
