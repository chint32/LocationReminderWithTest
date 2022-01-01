package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var dataSource: FakeDataSource

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()
        dataSource = FakeDataSource()
        dataSource.reminders = createRemindersForTesting()
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource)
    }

    private fun createRemindersForTesting(): MutableList<ReminderDTO> {

        val r1 = ReminderDTO("TestTitle","TestDescirption",
            "TestLocation",
            0.0,0.0)

        val r2 = ReminderDTO("TestTitle2","TestDescirption2",
            "TestLocation2",
            0.0,0.0)

        return mutableListOf(r1, r2)
    }

    @Test
    fun givenRemindersToLoad_loadReminders_addsRemindersToReminderList()= runBlockingTest {

        // GIVEN
        // 2 reminders in setup function

        // WHEN
        remindersListViewModel.loadReminders()

        // THEN
        assert(remindersListViewModel.remindersList.value?.size!! > 0)

    }

    @Test
    fun givenEmptyList_loadReminders_addsZeroRemindersToList() = runBlockingTest {

        // GIVEN
        dataSource.deleteAllReminders()

        // WHEN
        remindersListViewModel.loadReminders()

        // THEN
        assert(remindersListViewModel.remindersList.value?.size == 0)
    }

    @Test
    fun givenDataSourceError_loadReminders_showsError(){
        // Giving - repo with 2 reminders
        dataSource.setReturnError(true)
        // When -
        remindersListViewModel.loadReminders()
        // Then -
        val error = remindersListViewModel.showSnackBar.getOrAwaitValue()
        assert(error.contains("Exception"))
    }

    @Test
    fun givenRemindersInList_reminderListEmptied_showNoDataIsTrue() = runBlockingTest {

        // GIVEN
        // 2 reminders in setup function

        // When
        dataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()

        // Then - showNoData should be true
        assert(remindersListViewModel.showNoData.value!!)

    }

    @Test
    fun showLoading_() {
        // Given - repo with 2 reminders
        mainCoroutineRule.pauseDispatcher()

        // When - loading reminders
        remindersListViewModel.loadReminders()

        // Then - show loading
        assert(remindersListViewModel.showLoading.getOrAwaitValue() == true)

        // Then - hide loading
        mainCoroutineRule.resumeDispatcher()
        assert(remindersListViewModel.showLoading.getOrAwaitValue() == false)
    }

}