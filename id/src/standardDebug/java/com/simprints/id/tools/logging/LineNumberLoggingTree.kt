package com.simprints.id.tools.logging

import timber.log.Timber

class LineNumberLoggingTree : Timber.DebugTree() {

    override fun createStackElementTag(element: StackTraceElement): String? {
        return "(${element.fileName}:${element.lineNumber})"
    }

}
