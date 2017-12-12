package com.simprints.id.exceptions.safe


class NullDbContextException(message: String) : RuntimeException(message) {

    companion object {

        fun forAttemptedMethod(methodName: String) =
                NullDbContextException("Cannot $methodName because dbContext is null")

    }

}

