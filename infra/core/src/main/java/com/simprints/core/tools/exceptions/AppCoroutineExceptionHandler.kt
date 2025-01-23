package com.simprints.core.tools.exceptions

import com.simprints.infra.logging.LoggingConstants.CrashReportTag.APP_SCOPE_ERROR
import com.simprints.infra.logging.Simber
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

class AppCoroutineExceptionHandler : CoroutineExceptionHandler {
    override val key = CoroutineExceptionHandler.Key

    override fun handleException(
        context: CoroutineContext,
        exception: Throwable,
    ) {
        Simber.e("Coroutine exception", exception, tag = APP_SCOPE_ERROR)
    }
}
