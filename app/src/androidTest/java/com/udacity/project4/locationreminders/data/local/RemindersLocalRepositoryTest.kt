package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.*
import org.junit.runner.RunWith


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule=InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersDao: FakeRemindersDao
    private lateinit var remindersRepository: RemindersLocalRepository

    @Before
    fun setupRepository() {
        remindersDao = FakeRemindersDao()
        remindersRepository = RemindersLocalRepository(remindersDao, Dispatchers.Unconfined)
    }

    @Test
    fun givenRemindersInDb_getReminderById_returnsCorrectReminder() = mainCoroutineRule.runBlockingTest {
        // Given
        val r1 = ReminderDTO("t1", "d1", "l1",
            0.0, 0.0)

        val r2 = ReminderDTO("t2", "d2", "l2",
            0.0, 0.0)

        remindersDao.saveReminder(r1)
        remindersDao.saveReminder(r2)

        // When
        val lr1 = remindersRepository.getReminder(r1.id)
        val lr2 = remindersRepository.getReminder(r2.id)

        lr1 as Result.Success
        lr2 as Result.Success

        // Then
        assertThat(lr1, `is`(notNullValue()))
        assertThat(lr1.data.title, `is`(r1.title))
        assertThat(lr1.data.location, `is`(r1.location))
        assertThat(lr1.data.latitude, `is`(r1.latitude))
        assertThat(lr1.data.longitude, `is`(r1.longitude))

        assertThat(lr2, `is`(notNullValue()))
        assertThat(lr2.data.title, `is`(r2.title))
        assertThat(lr2.data.location, `is`(r2.location))
        assertThat(lr2.data.latitude, `is`(r2.latitude))
        assertThat(lr2.data.longitude, `is`(r2.longitude))
    }



    @Test
    fun givenEmptyDb_getReminderById_returnsReminderNotFoundError() = mainCoroutineRule.runBlockingTest {

        // GIVEN
        val r1 = ReminderDTO("t1", "d1", "l1",
            0.0, 0.0)
        remindersDao.deleteAllReminders()

        // WHEN
        val lr = remindersRepository.getReminder(r1.id)

        // THEN
        lr as Result.Error
        assertThat(lr.message, `is`("Reminder not found!"))
    }

    @Test
    fun givenRemindersInDb_deleteAllReminders_deletesRemindersInDb() = mainCoroutineRule.runBlockingTest {
        // GIVEN
        val r1 = ReminderDTO("t1", "d1", "l1",
            0.0, 0.0)

        val r2 = ReminderDTO("t2", "d2", "l2",
            0.0, 0.0)

        remindersDao.saveReminder(r1)
        remindersDao.saveReminder(r2)

        // WHEN
        remindersDao.deleteAllReminders()

        val lr1 = remindersRepository.getReminder(r1.id)
        val lr2 = remindersRepository.getReminder(r2.id)

        // THEN
        lr1 as Result.Error
        lr2 as Result.Error
        assertThat(lr1.message,`is`("Reminder not found!"))
        assertThat(lr2.message,`is`("Reminder not found!"))
    }


}