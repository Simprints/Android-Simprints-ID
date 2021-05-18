package com.simprints.core.tools.extentions

import com.simprints.core.network.NetworkConstants
import retrofit2.HttpException

fun Throwable.isClientAndCloudIntegrationIssue() =
    this is HttpException && NetworkConstants.httpCodesForIntegrationIssues.contains(this.code())
