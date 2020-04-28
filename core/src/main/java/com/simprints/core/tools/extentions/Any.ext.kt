package com.simprints.core.tools.extentions

// Sealed whens throws compiling issues only if the result is assigned to variables.
// Call safe to force the Sealed whens to be exhaustive : https://youtrack.jetbrains.com/issue/KT-12380
val Any?.safeSealedWhens get() = Unit
