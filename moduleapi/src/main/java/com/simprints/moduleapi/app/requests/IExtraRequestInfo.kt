package com.simprints.moduleapi.app.requests

import android.os.Parcelable

interface IExtraRequestInfo : Parcelable {
    val integration: IIntegrationInfo
}

interface IOdkIntegrationInfo : IIntegrationInfo
interface IStandardIntegrationInfo : IIntegrationInfo

interface IIntegrationInfo : Parcelable
