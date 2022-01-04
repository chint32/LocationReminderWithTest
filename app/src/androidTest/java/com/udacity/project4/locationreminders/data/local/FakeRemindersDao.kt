package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import java.lang.Exception

class FakeRemindersDao: RemindersDao {

    var shouldReturnError = false

    val remindersServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    override suspend fun getReminders(): List<ReminderDTO> {
        if (shouldReturnError) {
            throw (Exception("Test exception"))
        }

        val remindersList = mutableListOf<ReminderDTO>()
        remindersList.addAll(remindersServiceData.values)
        return remindersList
    }

    override suspend fun getReminderById(reminderId: String): ReminderDTO? {
        if (shouldReturnError) {
            throw (Exception("Test exception"))
        }

        remindersServiceData[reminderId]?.let {
            return it
        }
        return null
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersServiceData[reminder.id] = reminder
    }

    override suspend fun deleteAllReminders() {
        remindersServiceData.clear()
    }

}