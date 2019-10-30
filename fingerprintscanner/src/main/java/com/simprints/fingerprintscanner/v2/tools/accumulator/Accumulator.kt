package com.simprints.fingerprintscanner.v2.tools.accumulator

import io.reactivex.Flowable

/**
 * An [Accumulator] is used to transform a stream of [Fragment]s into [Element]s.
 *
 * Elements can be broken up over multiple Fragments, or multiple Elements can be within a Fragment.
 * The Element is expected to have a header containing the length of the Element.
 * New Fragments are added with [updateWithNewFragment] which accumulates them into a [FragmentCollection].
 * Elements are retrieved when available with [takeElements].
 *
 * Designed to be used with [accumulateAndTakeElements] within an Rx stream.
 * The same instance of the Accumulator is used for the lifetime of the stream.
 *
 * @param Fragment The type of the input of the Accumulator
 * @param FragmentCollection The type of the collection in which Fragments are stored as they are accumulated
 * @param Element The type of the output of the Accumulator
 *
 * @param initialFragmentCollection The initial empty collection with which to accumulate elements into
 * @param addFragmentToCollection The method in which fragments are added to the collection
 * @param canComputeElementLengthFromCollection Whether there is sufficient information within the collection to determine the next Element's length
 * @param computeElementLengthFromCollection Compute the length of the current Element from the information within the Collection
 * @param getCollectionLength Method with which to calculate the length of the whole collection
 * @param sliceCollection Method with which to extract a chunk from the collection
 * @param buildElementFromCompleteCollection Method with which to build the next available Element from the collection
 */
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
