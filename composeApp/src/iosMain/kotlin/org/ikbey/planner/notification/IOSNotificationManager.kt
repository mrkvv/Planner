package org.ikbey.planner.notification

import platform.UserNotifications.*
import platform.Foundation.*
import org.ikbey.planner.dataBase.Note

actual class NotificationManager actual constructor() {

    actual fun scheduleNotification(note: Note) {
        if (note.start_time == null || note.date == null) return

        val content = createNotificationContent(note)
        val trigger = createNotificationTrigger(note)
        val request = createNotificationRequest(note, content, trigger)

        notificationCenter.addNotificationRequest(request) { error ->
            error?.let {
                println("Ошибка при планировании уведомления: $it")
            }
        }
    }

    actual fun cancelNotification(noteId: Int) {
        notificationCenter.removePendingNotificationRequestsWithIdentifiers(
            listOf(noteId.toString())
        )
    }

    actual fun requestNotificationPermissions() {
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound
        notificationCenter.requestAuthorizationWithOptions(options) { granted, error ->
            error?.let {
                println("Ошибка запроса разрешений: $it")
            } ?: run {
                println("Разрешение на уведомления получено: $granted")
            }
        }
    }

    actual fun hasNotificationPermission(): Boolean {
        // На iOS реальную проверку нужно делать через getNotificationSettings
        return true
    }

    private val notificationCenter: UNUserNotificationCenter
        get() = UNUserNotificationCenter.currentNotificationCenter()

    private fun createNotificationContent(note: Note): UNMutableNotificationContent {
        val content = UNMutableNotificationContent()
        content.setTitle(note.header ?: "Напоминание")
        content.setBody(note.note ?: "Время для вашей заметки")
        content.setSound(UNNotificationSound.defaultSound())
        return content
    }

    private fun createNotificationTrigger(note: Note): UNTimeIntervalNotificationTrigger {
        val notificationTime = parseNotificationTime(note.date!!, note.start_time!!)
        val currentTime = NSDate.date().timeIntervalSince1970()
        val timeInterval = maxOf(notificationTime - currentTime, 1.0)

        return UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(
            timeInterval = timeInterval,
            repeats = false
        )
    }

    private fun createNotificationRequest(
        note: Note,
        content: UNMutableNotificationContent,
        trigger: UNTimeIntervalNotificationTrigger
    ): UNNotificationRequest {
        return UNNotificationRequest.requestWithIdentifier(
            identifier = note.id.toString(),
            content = content,
            trigger = trigger
        )
    }

    private fun parseNotificationTime(date: String, time: String): Double {
        return try {
            val dateFormatter = NSDateFormatter()
            dateFormatter.dateFormat = "yyyy-MM-dd HH:mm"
            val dateTimeString = "$date $time"
            val notificationDate = dateFormatter.dateFromString(dateTimeString)
            notificationDate?.timeIntervalSince1970 ?: getDefaultTime()
        } catch (e: Exception) {
            getDefaultTime()
        }
    }

    private fun getDefaultTime(): Double {
        return NSDate.date().timeIntervalSince1970() + 60.0
    }
}