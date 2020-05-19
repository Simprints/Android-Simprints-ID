package com.simprints.id.tools.extensions

import com.simprints.id.network.NetworkConstants
import retrofit2.HttpException

fun Throwable.isClientAndCloudIntegrationIssue() =
    this is HttpException && NetworkConstants.httpCodesForIntegrationIssues.contains(this.code())
