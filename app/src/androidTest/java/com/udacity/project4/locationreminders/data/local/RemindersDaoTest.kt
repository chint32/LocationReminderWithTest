package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var remindersDao: RemindersDao
    private lateinit var db: RemindersDatabase

    @Before
    fun setupDb(){
        val context = InstrumentationRegistry.getInstrumentation().context
        db = Room.inMemoryDatabaseBuilder(
            context, RemindersDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        remindersDao = db.reminderDao()
    }

    @After
    @Throws(IOException::class)
    fun tearDownDB(){
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun givenReminderWithId_getReminderById_returnsCorrectReminder() = runBlockingTest{
        // GIVEN - insert a reminder

        val reminderId = UUID.randomUUID().toString()
        val reminder = ReminderDTO("Title","Description",
            "Location",
            0.0,0.0,
            reminderId)

        remindersDao.saveReminder(reminder)

        // WHEN - Get the reminder by id from the database
        val loaded = remindersDao.getReminderById(reminderId)
        val loadedFromRandomId = remindersDao.getReminderById(UUID.randomUUID().toString())

        // THEN - The loaded data contains the expected values
        assertThat(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.latitude, `is`(reminder.latitude))
        assertThat(loaded.longitude, `is`(reminder.longitude))

        assertThat(loadedFromRandomId, `is`(nullValue()))
    }

    @Test
    fun givenRemindersInDb_getReminders_returnsRemindersInDb() = runBlockingTest {
        // GIVEN - three reminders inserted in db
        val r1 = ReminderDTO("Title1","Description1",
            "Location1",
            0.0,0.0)

        val r2 = ReminderDTO("Title2","Description2",
            "Location2",
            0.0,0.0)

        val r3 = ReminderDTO("Title3","Description3",
            "Location3",
            0.0,0.0)

        remindersDao.saveReminder(r1)
        remindersDao.saveReminder(r2)
        remindersDao.saveReminder(r3)

        // WHEN - getReminders()
        val loadedReminders = remindersDao.getReminders()

        // THEN - returns the reminders in the db
        assertThat(loadedReminders.count(), `is`(3))

        assertThat(loadedReminders.first().title, `is`(r1.title))
        assertThat(loadedReminders.first().description, `is`(r1.description))
        assertThat(loadedReminders.first().location, `is`(r1.location))

        assertThat(loadedReminders.last().title, `is`(r3.title))
        assertThat(loadedReminders.last().description, `is`(r3.description))
        assertThat(loadedReminders.last().location, `is`(r3.location))

    }

    @Test
    fun givenRemindersInDb_deleteReminders_remindersCountReturnsZero() = runBlockingTest {
        // GIVEN - three reminders inserted into db
        val r1 = ReminderDTO("Title1","Description1",
            "Location1",
            0.0,0.0)

        val r2 = ReminderDTO("Title2","Description2",
            "Location2",
            0.0,0.0)

        val r3 = ReminderDTO("Title3","Description3",
            "Location3",
            0.0,0.0)


        remindersDao.saveReminder(r1)
        remindersDao.saveReminder(r2)
        remindersDao.saveReminder(r3)


        // WHEN - deleteReminders()
        remindersDao.deleteAllReminders()

        // THEN - check the database is empty
        val loadedReminders = remindersDao.getReminders()
        assertThat(loadedReminders.count(), `is`(0))

    }

}
