package com.simprints.testtools.android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import java.util.*
import android.graphics.drawable.ColorDrawable
import android.os.Parcelable
import org.hamcrest.BaseMatcher


private fun hasBackgroundColor(expectedObject: Matcher<Int>): Matcher<Any> {

    return object : BoundedMatcher<Any, View>(View::class.java) {

        var color: Int = -1

        public override fun matchesSafely(actualObject: View): Boolean {
            color = (actualObject.background as ColorDrawable).color
            return expectedObject.matches(color)
        }

        override fun describeTo(description: Description) {
            description.appendText("Color did not match $color")
        }
    }
}


fun hasImage(drawableId: Int): Matcher<View> {
    return object : BoundedMatcher<View, ImageView>(ImageView::class.java) {

        override fun describeTo(description: Description) {
            description.appendText("has image with drawable ID: $drawableId")
        }

        override fun matchesSafely(view: ImageView): Boolean {
            return assertDrawable(view.drawable, drawableId, view)
        }
    }
}

private fun compareBitmaps(img1: Bitmap, img2: Bitmap): Boolean {
    if (img1.width == img2.width && img1.height == img2.height) {
        val img1Pixels = IntArray(img1.width * img1.height)
        val img2Pixels = IntArray(img2.width * img2.height)

        img1.getPixels(img1Pixels, 0, img1.width, 0, 0, img1.width, img1.height)
        img2.getPixels(img2Pixels, 0, img2.width, 0, 0, img2.width, img2.height)

        return Arrays.equals(img1Pixels, img2Pixels)
    }
    return false
}

private fun assertDrawable(actual: Drawable, expectedId: Int, v: View): Boolean {
    if (actual !is BitmapDrawable) {
        return false
    }

    var expectedBitmap: Bitmap? = null
    return try {
        expectedBitmap = BitmapFactory.decodeResource(v.context.resources, expectedId)
        actual.bitmap.sameAs(expectedBitmap)
    } catch (error: OutOfMemoryError) {
        return false

    } finally {
        expectedBitmap?.recycle()
    }
}


fun <T : Parcelable> bundleDataMatcherForParcelable(parcelable: T) =
    object : BaseMatcher<T>() {
        override fun describeTo(description: Description?) {}
        override fun matches(item: Any?): Boolean {
            return item.toString() == parcelable.toString()
        }
    }
