package org.ikbey.planner.notification

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import io.mockk.*
import org.junit.Test
import org.junit.Before
import org.junit.After

class NotificationReceiverTest {

    private lateinit var notificationReceiver: NotificationReceiver
    private lateinit var mockContext: Context
    private lateinit var mockNotificationManager: NotificationManagerCompat

    @Before
    fun setUp() {
        notificationReceiver = NotificationReceiver()
        mockContext = mockk(relaxed = true)
        mockNotificationManager = mockk(relaxed = true)

        // Мокаем только NotificationManagerCompat.from()
        mockkStatic(NotificationManagerCompat::class)
        every { NotificationManagerCompat.from(any()) } returns mockNotificationManager

        // Мокаем конструктор NotificationCompat.Builder
        mockkConstructor(NotificationCompat.Builder::class)

        // Создаем мок для методов билдера
        val mockBuilder = mockk<NotificationCompat.Builder>(relaxed = true)
        val mockNotification = mockk<android.app.Notification>(relaxed = true)

        // Настраиваем методы с явными типами
        every { anyConstructed<NotificationCompat.Builder>().setContentTitle(any<String>()) } returns mockBuilder
        every { anyConstructed<NotificationCompat.Builder>().setContentText(any<String>()) } returns mockBuilder
        every { anyConstructed<NotificationCompat.Builder>().setSmallIcon(any<Int>()) } returns mockBuilder
        every { anyConstructed<NotificationCompat.Builder>().setPriority(any<Int>()) } returns mockBuilder
        every { anyConstructed<NotificationCompat.Builder>().setAutoCancel(any<Boolean>()) } returns mockBuilder
        every { anyConstructed<NotificationCompat.Builder>().build() } returns mockNotification

        every { mockNotificationManager.notify(any<Int>(), any()) } just Runs
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `basic notification test`() {
        // Arrange - создаем мок Intent чтобы избежать проблем с Android методами
        val noteId = 999
        val mockIntent = mockk<Intent>(relaxed = true)

        every { mockIntent.getIntExtra("note_id", any()) } returns noteId
        every { mockIntent.getStringExtra("header") } returns "Test Header"
        every { mockIntent.getStringExtra("message") } returns "Test Message"

        // Act
        notificationReceiver.onReceive(mockContext, mockIntent)

        // Assert
        verify { mockNotificationManager.notify(noteId, any()) }
    }

    @Test
    fun `should use default values when extras are null`() {
        // Arrange
        val noteId = 456
        val mockIntent = mockk<Intent>(relaxed = true)

        every { mockIntent.getIntExtra("note_id", any()) } returns noteId
        every { mockIntent.getStringExtra("header") } returns null
        every { mockIntent.getStringExtra("message") } returns null

        // Act
        notificationReceiver.onReceive(mockContext, mockIntent)

        // Assert - проверяем только setContentTitle и notify
        verify {
            anyConstructed<NotificationCompat.Builder>().setContentTitle("Напоминание")
            mockNotificationManager.notify(noteId, any())
        }
    }

    @Test
    fun `should handle notification manager exceptions`() {
        // Arrange
        val noteId = 111
        val mockIntent = mockk<Intent>(relaxed = true)

        every { mockIntent.getIntExtra("note_id", any()) } returns noteId
        every { mockIntent.getStringExtra("header") } returns "Test"
        every { mockIntent.getStringExtra("message") } returns "Test Message"

        // Эмулируем исключение
        every { mockNotificationManager.notify(any(), any()) } throws SecurityException("No permission")

        // Act - не должно падать
        notificationReceiver.onReceive(mockContext, mockIntent)

        // Assert - проверяем что метод вызывался
        verify { mockNotificationManager.notify(noteId, any()) }
    }

    @Test
    fun `showNotification private method test`() {
        // Arrange
        val noteId = 444
        val title = "Custom Title"
        val message = "Custom Message"

        // Используем reflection для тестирования приватного метода
        val method = NotificationReceiver::class.java.getDeclaredMethod(
            "showNotification",
            Context::class.java,
            Int::class.java,
            String::class.java,
            String::class.java
        )
        method.isAccessible = true

        // Act
        method.invoke(notificationReceiver, mockContext, noteId, title, message)

        // Assert - проверяем только setContentTitle и notify
        verify {
            anyConstructed<NotificationCompat.Builder>().setContentTitle(title)
            mockNotificationManager.notify(noteId, any())
        }
    }
}