package com.simprints.id.services.progress.service

import com.simprints.libcommon.Progress
import io.reactivex.Observable

interface ProgressTask {

    fun run(isInterrupted: () -> Boolean): Observable<Progress>
}
