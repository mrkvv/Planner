package org.ikbey.planner.notification

import org.ikbey.planner.dataBase.Note
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertEquals

class NotificationManagerTest {

    private val commonNotificationManager = CommonNotificationManager()

    @Test
    fun `shouldScheduleNotification returns true when all conditions are met`() {
        // Arrange
        val note = Note(
            id = 1,
            is_notifications_enabled = true,
            start_time = "10:00",
            date = "2024-01-01"
            // остальные поля можно не указывать для этого теста
        )

        // Act
        val result = commonNotificationManager.shouldScheduleNotification(note)

        // Assert
        assertTrue(result)
    }

    @Test
    fun `shouldScheduleNotification returns false when notifications are disabled`() {
        // Arrange
        val note = Note(
            id = 1,
            is_notifications_enabled = false,
            start_time = "10:00",
            date = "2024-01-01"
        )

        // Act
        val result = commonNotificationManager.shouldScheduleNotification(note)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `shouldScheduleNotification returns false when start_time is null`() {
        // Arrange
        val note = Note(
            id = 1,
            is_notifications_enabled = true,
            start_time = null,
            date = "2024-01-01"
        )

        // Act
        val result = commonNotificationManager.shouldScheduleNotification(note)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `shouldScheduleNotification returns false when date is null`() {
        // Arrange
        val note = Note(
            id = 1,
            is_notifications_enabled = true,
            start_time = "10:00",
            date = null
        )

        // Act
        val result = commonNotificationManager.shouldScheduleNotification(note)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `shouldScheduleNotification returns false when both time and date are null`() {
        // Arrange
        val note = Note(
            id = 1,
            is_notifications_enabled = true,
            start_time = null,
            date = null
        )

        // Act
        val result = commonNotificationManager.shouldScheduleNotification(note)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `shouldScheduleNotification returns false when is_notifications_enabled is null`() {
        // Arrange
        val note = Note(
            id = 1,
            is_notifications_enabled = null,
            start_time = "10:00",
            date = "2024-01-01"
        )

        // Act
        val result = commonNotificationManager.shouldScheduleNotification(note)

        // Assert
        assertFalse(result)
    }

    @Test
    fun `parseNotificationTime returns zero as default implementation`() {
        // Arrange
        val date = "2024-01-01"
        val time = "10:30"

        // Act
        val result = commonNotificationManager.parseNotificationTime(date, time)

        // Assert
        assertEquals(0L, result)
    }

    @Test
    fun `parseNotificationTime returns zero for empty strings`() {
        // Arrange
        val date = ""
        val time = ""

        // Act
        val result = commonNotificationManager.parseNotificationTime(date, time)

        // Assert
        assertEquals(0L, result)
    }
}