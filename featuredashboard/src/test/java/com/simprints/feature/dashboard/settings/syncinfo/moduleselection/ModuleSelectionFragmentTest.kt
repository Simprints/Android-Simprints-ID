package com.simprints.feature.dashboard.settings.syncinfo.moduleselection

import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.simprints.feature.dashboard.R
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.NoModuleSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.exceptions.TooManyModulesSelectedException
import com.simprints.feature.dashboard.settings.syncinfo.moduleselection.repository.Module
import com.simprints.feature.dashboard.tools.clickCloseChipIcon
import com.simprints.feature.dashboard.tools.launchFragmentInHiltContainer
import com.simprints.feature.dashboard.tools.typeSearchViewText
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.hamcrest.core.AllOf.allOf
import org.hamcrest.core.IsNot.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast
import com.simprints.infra.resources.R as IDR

@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
@Config(application = HiltTestApplication::class)
class ModuleSelectionFragmentTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @BindValue
    @JvmField
    internal val viewModel = mockk<ModuleSelectionViewModel>(relaxed = true) {
        every { modulesList } returns mockk {
            every { observe(any(), any()) } answers {
                secondArg<Observer<List<Module>>>().onChanged(
                    listOf(
                        Module("module12", true),
                        Module("module2", false),
                        Module("module3", false)
                    )
                )
            }
        }
    }

    private val context = InstrumentationRegistry.getInstrumentation().context

    @Test
    fun `should init the modules correctly`() {
        launchFragmentInHiltContainer<ModuleSelectionFragment>()

        onView(withId(R.id.rvModules))
            .check(matches(not(hasDescendant(withText("module12")))))
            .check(matches(hasDescendant(withText("module2"))))
            .check(matches(hasDescendant(withText("module3"))))

        onView(withId(R.id.chipGroup))
            .check(matches(hasDescendant(withText("module12"))))
            .check(matches(not(hasDescendant(withText("module2")))))
            .check(matches(not(hasDescendant(withText("module3")))))
    }

    @Test
    fun `should add a new module correctly when clicking on it`() {
        launchFragmentInHiltContainer<ModuleSelectionFragment>()

        onView(allOf(withParent(withId(R.id.rvModules)), withParentIndex(0))).perform(click())

        verify(exactly = 1) {
            viewModel.updateModuleSelection(Module("module2", false))
        }
    }

    @Test
    fun `should display a toast message if the updateModules throw a TooManyModulesSelectedException`() {
        every { viewModel.updateModuleSelection(any()) } throws TooManyModulesSelectedException(
            maxNumberOfModules = 2
        )
        launchFragmentInHiltContainer<ModuleSelectionFragment>()

        onView(allOf(withParent(withId(R.id.rvModules)), withParentIndex(0))).perform(click())

        ShadowToast.showedToast(context?.getString(IDR.string.settings_too_many_modules_toast, 2))
    }

    @Test
    fun `should remove a module correctly when clicking on the close icon`() {
        launchFragmentInHiltContainer<ModuleSelectionFragment>()

        onView(allOf(withParent(withId(R.id.chipGroup)), withParentIndex(0))).perform(
            clickCloseChipIcon()
        )

        verify(exactly = 1) {
            viewModel.updateModuleSelection(Module("module12", true))
        }
    }

    @Test
    fun `should display a toast message if the updateModules throw a NoModuleSelectedException`() {
        every { viewModel.updateModuleSelection(any()) } throws NoModuleSelectedException()
        launchFragmentInHiltContainer<ModuleSelectionFragment>()

        onView(allOf(withParent(withId(R.id.chipGroup)), withParentIndex(0))).perform(
            clickCloseChipIcon()
        )

        ShadowToast.showedToast(context?.getString(IDR.string.settings_no_modules_toast))
    }

    @Test
    fun `should navigate back when clicking on the back navigation and nothing has changed`() {
        every { viewModel.hasSelectionChanged() } returns false

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<ModuleSelectionFragment>(navController = navController)

        onView(withContentDescription("back")).perform(click())

        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should display the save dialog when clicking on the back navigation and the selection has changed and save the selection if validating`() {
        every { viewModel.hasSelectionChanged() } returns true

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<ModuleSelectionFragment>(navController = navController)

        onView(withContentDescription("back")).perform(click())
        onView(withId(android.R.id.button1))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        verify(exactly = 1) { viewModel.saveModules() }
        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should display the save dialog when clicking on the back navigation and the selection has changed and not save the selection if canceling`() {
        every { viewModel.hasSelectionChanged() } returns true

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<ModuleSelectionFragment>(navController = navController)

        onView(withContentDescription("back")).perform(click())
        onView(withId(android.R.id.button2))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        verify(exactly = 0) { viewModel.saveModules() }
        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should navigate back when clicking on the back button and nothing has changed`() {
        every { viewModel.hasSelectionChanged() } returns false

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<ModuleSelectionFragment>(navController = navController)

        onView(withId(R.id.rvModules)).perform(pressBack())

        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should display the save dialog when clicking on the back button and the selection has changed and save the selection if validating`() {
        every { viewModel.hasSelectionChanged() } returns true

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<ModuleSelectionFragment>(navController = navController)

        onView(withId(R.id.rvModules)).perform(pressBack())
        onView(withId(android.R.id.button1))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        verify(exactly = 1) { viewModel.saveModules() }
        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should display the save dialog when clicking on the back button and the selection has changed and not save the selection if canceling`() {
        every { viewModel.hasSelectionChanged() } returns true

        val navController = mockk<NavController>(relaxed = true)

        launchFragmentInHiltContainer<ModuleSelectionFragment>(navController = navController)

        onView(withId(R.id.rvModules)).perform(pressBack())
        onView(withId(android.R.id.button2))
            .inRoot(RootMatchers.isDialog())
            .check(matches(isDisplayed()))
            .perform(click())

        verify(exactly = 0) { viewModel.saveModules() }
        verify(exactly = 1) { navController.popBackStack() }
    }

    @Test
    fun `should filter the modules according to the search`() {
        launchFragmentInHiltContainer<ModuleSelectionFragment>()


        onView(withId(R.id.searchView)).perform(typeSearchViewText("2"))
        onView(withId(R.id.rvModules))
            .check(matches(not(hasDescendant(withText("module12")))))
            .check(matches(hasDescendant(withText("module2"))))
            .check(matches(not(hasDescendant(withText("module3")))))

        onView(withId(R.id.txtNoResults)).check(matches(not(isDisplayed())))
    }

    @Test
    fun `should display the no result test when the search doesn't have results`() {
        launchFragmentInHiltContainer<ModuleSelectionFragment>()


        onView(withId(R.id.searchView)).perform(typeSearchViewText("no-results"))
        onView(withId(R.id.rvModules))
            .check(matches(not(hasDescendant(withText("module12")))))
            .check(matches(not(hasDescendant(withText("module2")))))
            .check(matches(not(hasDescendant(withText("module3")))))

        onView(withId(R.id.txtNoResults)).check(matches(isDisplayed()))
    }
}
