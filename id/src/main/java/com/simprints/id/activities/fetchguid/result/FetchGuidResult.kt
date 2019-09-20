package com.simprints.id.activities.fetchguid.result

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class FetchGuidResult(val isGuidFound: Boolean): Parcelable {

    companion object {
        const val BUNDLE_KEY = "fetch_guid_bundle_key"
        const val RESULT_CODE_FETCH_GUID = 501
    }
}
