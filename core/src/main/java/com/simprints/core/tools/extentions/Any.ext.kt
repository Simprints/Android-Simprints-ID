package com.simprints.core.tools.extentions

// Sealed whens throw compiling issues only if the whens result is assigned to variables.
// safeSealedWhens to force the Sealed whens to be exhaustive:
// https://youtrack.jetbrains.com/issue/KT-12380
// https://stackoverflow.com/questions/38169933/force-compilation-error-with-sealed-classes
val Any?.safeSealedWhens get() = Unit
