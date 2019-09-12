package com.simprints.fingerprintscanner.v2.tools.accumulator

import io.reactivex.Flowable

abstract class Accumulator<in Fragment, in FragmentCollection, Element>(
    initialFragmentCollection: FragmentCollection,
    private inline val addFragmentToCollection: FragmentCollection.(Fragment) -> FragmentCollection,
    private inline val canComputeElementLengthFromCollection: (FragmentCollection) -> Boolean,
    private inline val computeElementLengthFromCollection: (FragmentCollection) -> Int,
    private inline val getCollectionLength: FragmentCollection.() -> Int,
    private inline val sliceCollection: FragmentCollection.(IntRange) -> FragmentCollection,
    private inline val buildElementFromCompleteCollection: (FragmentCollection) -> Element
) {

    private var fragmentCollection: FragmentCollection = initialFragmentCollection

    private var currentElementLength: Int? = null

    fun updateWithNewFragment(fragment: Fragment) {
        fragmentCollection = fragmentCollection.addFragmentToCollection(fragment)
        updateCurrentElementLength()
    }

    fun takeElements(): Flowable<Element> =
        Flowable.generate<Element> { emitter ->
            if (containsCompleteElement()) {
                emitter.onNext(takeElement())
            } else {
                emitter.onComplete()
            }
        }

    private fun containsCompleteElement() =
        currentElementLength?.let { fragmentCollection.getCollectionLength() >= it } ?: false

    private fun takeElement(): Element {
        currentElementLength?.let { packetLength ->
            val completeFragmentCollection = fragmentCollection.sliceCollection(0 until packetLength)
            val overflowCollection = fragmentCollection.sliceCollection(packetLength until fragmentCollection.getCollectionLength())
            fragmentCollection = overflowCollection
            currentElementLength = null
            updateCurrentElementLength()
            return buildElementFromCompleteCollection(completeFragmentCollection)
        } ?: throw IllegalStateException("Trying to take element before the length can be computed")
    }

    private fun updateCurrentElementLength() {
        if (currentElementLength == null && canComputeElementLengthFromCollection(fragmentCollection)) {
            currentElementLength = computeElementLengthFromCollection(fragmentCollection)
        }
    }
}
