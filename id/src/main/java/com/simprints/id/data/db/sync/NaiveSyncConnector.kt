package com.simprints.id.data.db.sync

import java.io.InputStream

interface NaiveSyncConnector {

    val inputStreamForDownload: InputStream
    //getInputStreamForUpload()
}
