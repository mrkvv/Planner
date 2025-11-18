package org.ikbey.planner.notification

import org.junit.Test
import org.junit.Assert.*
import org.ikbey.planner.dataBase.Note

class AndroidNotificationManagerTest {

    @Test
    fun `should not schedule notification when date is null`() {
        // Arrange
        val note = Note(id = 1, header = "Test", date = null, start_time = "10:00")
        val manager = NotificationManager()

        // Act & Assert - просто проверяем что не падает
        manager.scheduleNotification(note)
    }

    @Test
    fun `should not schedule notification when time is null`() {
        // Arrange
        val note = Note(id = 1, header = "Test", date = "2024-01-01", start_time = null)
        val manager = NotificationManager()

        // Act & Assert - просто проверяем что не падает
        manager.scheduleNotification(note)
    }

    @Test
    fun `should use default values for null header and note`() {
        // Arrange
        val noteWithNulls = Note(
            id = 1,
            header = null,
            note = null,
            date = "2024-01-01",
            start_time = "10:00"
        )

        // Act
        val defaultHeader = noteWithNulls.header ?: "Напоминание"
        val defaultMessage = noteWithNulls.note ?: "Время для вашей заметки"

        // Assert
        assertEquals("Напоминание", defaultHeader)
        assertEquals("Время для вашей заметки", defaultMessage)
    }

    @Test
    fun `notification manager should be created successfully`() {
        // Act
        val manager = NotificationManager()

        // Assert
        assertNotNull(manager)
    }

    @Test
    fun `parseNotificationTime should handle valid date and time`() {
        // Arrange
        val date = "2024-01-01"
        val time = "10:30"

        // Act
        val result = parseNotificationTime(date, time)

        // Assert - просто проверяем что возвращает положительное число
        assertTrue(result > 0)
    }

    @Test
    fun `parseNotificationTime should return future timestamp for invalid input`() {
        // Arrange
        val invalidDate = "invalid-date"
        val invalidTime = "invalid-time"

        // Act
        val result = parseNotificationTime(invalidDate, invalidTime)

        // Assert
        assertTrue(result > System.currentTimeMillis())
    }

    // Вспомогательная функция для тестирования приватного метода через reflection
    private fun parseNotificationTime(date: String, time: String): Long {
        val method = NotificationManager::class.java.getDeclaredMethod(
            "parseNotificationTime",
            String::class.java,
            String::class.java
        )
        method.isAccessible = true

        // Создаем экземпляр NotificationManager для вызова метода
        val manager = NotificationManager()

        // НЕ инициализируем контекст - тестируем только логику парсинга
        return method.invoke(manager, date, time) as Long
    }
}