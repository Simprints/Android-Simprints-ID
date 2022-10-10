package com.simprints.face

fun <T : Any> generateSequenceN(n: Int, f: () -> T) = generateSequence(f).take(n)
