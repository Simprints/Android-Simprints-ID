package com.simprints.infra.orchestration.data.responses

import android.os.Parcelable

/*
 * Sealed class ensures that any switch-case on AppResponses is exhaustive.
 */
sealed class AppResponse : Parcelable
