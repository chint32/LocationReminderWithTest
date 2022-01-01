package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setup(){
        stopKoin()
    }

    @Test
    fun givenDataIsNull_validateData_returnsFalse(){
        // GIVEN
        val reminder: ReminderDataItem? = null
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource())

        // WHEN
        val isValidated = saveReminderViewModel.validateEnteredData(reminder)

        // THEN
        assert(!isValidated)

    }

    @Test
    fun givenDataLocationIsNull_validateData_returnsFalse(){
        // GIVEN
        val reminders = mutableListOf<ReminderDTO>()
        val reminder = ReminderDataItem(
            "TestTitle", "TestDescription", null, 0.0, 0.0, "testId"
        )
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(reminders))

        // WHEN
        val isValidated = saveReminderViewModel.validateEnteredData(reminder)

        // THEN
        assert(!isValidated)

    }

    @Test
    fun givenDataTitleIsNull_validateData_returnsFalse(){
        // GIVEN
        val reminders = mutableListOf<ReminderDTO>()
        val reminder = ReminderDataItem(
            null, "TestDescription", "location", 0.0, 0.0, "testId"
        )
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(reminders))

        // WHEN
        val isValidated = saveReminderViewModel.validateEnteredData(reminder)

        // THEN
        assert(!isValidated)

    }

    @Test
    fun givenValidData_validateData_returnsTrue(){
        // GIVEN
        val reminders = mutableListOf<ReminderDTO>()
        val reminder = ReminderDataItem(
            "TestTitle", "TestDescription", "location", 0.0, 0.0, "testId"
        )
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(reminders))

        // WHEN
        val isValidated = saveReminderViewModel.validateEnteredData(reminder)

        // THEN
        assert(isValidated)

    }

    @Test
    fun givenReminder_saveReminder_addsReminderToList() {

        // GIVEN
        val reminders = mutableListOf<ReminderDTO>()
        val reminder = ReminderDataItem(
            "TestTitle", "TestDescription", "TestLocation", 0.0, 0.0, "testId"
        )
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), FakeDataSource(reminders))

        // WHEN
        saveReminderViewModel.saveReminder(reminder)

        // THEN
        val hasBeenSaved = reminders[0].id == "testId"
        assert(hasBeenSaved)

    }

}