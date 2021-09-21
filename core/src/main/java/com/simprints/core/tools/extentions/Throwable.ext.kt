package com.simprints.core.tools.extentions

import com.simprints.core.network.NetworkConstants
import retrofit2.HttpException

fun Throwable.isCloudRecoverableIssue() =
    this is HttpException && NetworkConstants.httpCodesForRecoverableCloudIssues.contains(this.code())
