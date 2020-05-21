package com.simprints.id.tools.utils

fun <T : Any> generateSequenceN(n: Int, f: () -> T) = generateSequence(f).take(n)
