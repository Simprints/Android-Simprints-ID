package com.simprints.id.services.progress.service

import com.simprints.libcommon.Progress
import io.reactivex.Emitter


interface ProgressTask {

    fun run(isInterrupted: () -> Boolean,
            emitter: Emitter<Progress>)

}
