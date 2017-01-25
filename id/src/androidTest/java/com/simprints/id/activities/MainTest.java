package com.simprints.id.activities;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.simprints.id.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

@RunWith(AndroidJUnit4.class)
public class MainTest {

    @Rule
    public ActivityTestRule<LaunchActivity> activityRule
            = new ActivityTestRule<>(
            LaunchActivity.class,
            true,     // initialTouchMode
            false);   // launchActivity. False to customize the intent

    @Test
    public void mainTest() {
        Intent intent = new Intent();
        intent.setAction("com.simprints.id.IDENTIFY");
        intent.putExtra("apiKey", "bcbe1563-b065-4508-8a4c-d26677bb5328");
        intent.putExtra("userId", "bcbe1563-b065-4508-8a4c-d26677bb5328");

        activityRule.launchActivity(intent);

        Looper.prepare();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                clickXY(5, 5);

                ViewInteraction appCompatButton = onView(
                        allOf(withId(R.id.scan_button), withText("SCAN"), isDisplayed()));
                appCompatButton.perform(click());

                ViewInteraction appCompatButton2 = onView(
                        allOf(withId(R.id.scan_button), withText("CANCEL"), isDisplayed()));
                appCompatButton2.perform(click());

                ViewInteraction appCompatImageButton = onView(
                        allOf(withContentDescription("Open navigation drawer"),
                                withParent(withId(R.id.toolbar)),
                                isDisplayed()));
                appCompatImageButton.perform(click());

                ViewInteraction appCompatCheckedTextView = onView(
                        allOf(withId(R.id.design_menu_item_text), withText("Add Finger"), isDisplayed()));
                appCompatCheckedTextView.perform(click());

                ViewInteraction appCompatImageButton2 = onView(
                        allOf(withContentDescription("Open navigation drawer"),
                                withParent(withId(R.id.toolbar)),
                                isDisplayed()));
                appCompatImageButton2.perform(click());

                ViewInteraction appCompatCheckedTextView2 = onView(
                        allOf(withId(R.id.design_menu_item_text), withText("Select Finger"), isDisplayed()));
                appCompatCheckedTextView2.perform(click());

                ViewInteraction appCompatCheckedTextView3 = onView(
                        allOf(withId(android.R.id.text1), withText("RIGHT INDEX FINGER"),
                                childAtPosition(
                                        allOf(withId(R.id.select_dialog_listview),
                                                withParent(withId(R.id.contentPanel))),
                                        3),
                                isDisplayed()));
                appCompatCheckedTextView3.perform(click());

                ViewInteraction appCompatButton3 = onView(
                        allOf(withId(android.R.id.button1), withText("OK")));
                appCompatButton3.perform(scrollTo(), click());

                ViewInteraction appCompatImageButton3 = onView(
                        allOf(withContentDescription("Open navigation drawer"),
                                withParent(withId(R.id.toolbar)),
                                isDisplayed()));
                appCompatImageButton3.perform(click());

                ViewInteraction appCompatCheckedTextView4 = onView(
                        allOf(withId(R.id.design_menu_item_text), withText("Sync"), isDisplayed()));
                appCompatCheckedTextView4.perform(click());

                ViewInteraction appCompatCheckedTextView5 = onView(
                        allOf(withId(R.id.design_menu_item_text), withText("Sync Complete"), isDisplayed()));
                appCompatCheckedTextView5.perform(click());

                ViewInteraction appCompatCheckedTextView6 = onView(
                        allOf(withId(R.id.design_menu_item_text), withText("Help"), isDisplayed()));
                appCompatCheckedTextView6.perform(click());

                ViewInteraction appCompatImageButton4 = onView(
                        allOf(withContentDescription("Open navigation drawer"),
                                withParent(withId(R.id.toolbar)),
                                isDisplayed()));
                appCompatImageButton4.perform(click());

                ViewInteraction appCompatCheckedTextView7 = onView(
                        allOf(withId(R.id.design_menu_item_text), withText("Privacy & Consent"), isDisplayed()));
                appCompatCheckedTextView7.perform(click());

                ViewInteraction appCompatImageButton5 = onView(
                        allOf(withContentDescription("Navigate up"),
                                withParent(withId(R.id.toolbar_settings)),
                                isDisplayed()));
                appCompatImageButton5.perform(click());

                ViewInteraction appCompatImageButton6 = onView(
                        allOf(withContentDescription("Open navigation drawer"),
                                withParent(withId(R.id.toolbar)),
                                isDisplayed()));
                appCompatImageButton6.perform(click());

                ViewInteraction appCompatImageButton7 = onView(
                        allOf(withContentDescription("Open navigation drawer"),
                                withParent(withId(R.id.toolbar)),
                                isDisplayed()));
                appCompatImageButton7.perform(click());

                ViewInteraction appCompatCheckedTextView8 = onView(
                        allOf(withId(R.id.design_menu_item_text), withText("Settings"), isDisplayed()));
                appCompatCheckedTextView8.perform(click());

                ViewInteraction appCompatImageButton8 = onView(
                        allOf(withContentDescription("Navigate up"),
                                withParent(allOf(withId(R.id.toolbar_settings),
                                        withParent(withId(R.id.appBarLayout)))),
                                isDisplayed()));
                appCompatImageButton8.perform(click());

                ViewInteraction appCompatImageButton9 = onView(
                        allOf(withContentDescription("Open navigation drawer"),
                                withParent(withId(R.id.toolbar)),
                                isDisplayed()));
                appCompatImageButton9.perform(click());

                ViewInteraction appCompatCheckedTextView9 = onView(
                        allOf(withId(R.id.design_menu_item_text), withText("About"), isDisplayed()));
                appCompatCheckedTextView9.perform(click());

                ViewInteraction appCompatImageButton10 = onView(
                        allOf(withContentDescription("Navigate up"),
                                withParent(withId(R.id.toolbar_about)),
                                isDisplayed()));
                appCompatImageButton10.perform(click());

                ViewInteraction appCompatButton4 = onView(
                        allOf(withId(R.id.scan_button), withText("SCAN"), isDisplayed()));
                appCompatButton4.perform(click());

                ViewInteraction appCompatButton5 = onView(
                        allOf(withId(R.id.scan_button), withText("SCAN"), isDisplayed()));
                appCompatButton5.perform(click());

                ViewInteraction appCompatButton6 = onView(
                        allOf(withId(R.id.scan_button), withText("CANCEL"), isDisplayed()));
                appCompatButton6.perform(click());

                ViewInteraction appCompatButton7 = onView(
                        allOf(withId(R.id.scan_button), withText("SCAN"), isDisplayed()));
                appCompatButton7.perform(click());

                ViewInteraction appCompatButton8 = onView(
                        allOf(withId(R.id.scan_button), withText("SCAN"), isDisplayed()));
                appCompatButton8.perform(click());

                ViewInteraction appCompatButton9 = onView(
                        allOf(withId(R.id.scan_button), withText("SCAN"), isDisplayed()));
                appCompatButton9.perform(click());

                ViewInteraction appCompatButton10 = onView(
                        allOf(withId(R.id.scan_button), withText("SCAN"), isDisplayed()));
                appCompatButton10.perform(click());

            }
        }, 10000);

    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    public static ViewAction clickXY(final int x, final int y) {
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);

                        final float screenX = screenPos[0] + x;
                        final float screenY = screenPos[1] + y;

                        return new float[]{screenX, screenY};
                    }
                },
                Press.FINGER);
    }
}
