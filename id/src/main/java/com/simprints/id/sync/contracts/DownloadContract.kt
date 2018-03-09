package com.simprints.id.sync.contracts

interface DownloadContract {
    fun downloadStart()
    fun downloadUpdate(current: Int)
    fun downloadFinished()
    fun downloadFailed()
}
