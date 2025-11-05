package org.ikbey.planner.notification

import kotlinx.coroutines.flow.Flow
import org.ikbey.planner.dataBase.Note

expect class NotificationManager() {
    fun scheduleNotification(note: Note)
    fun cancelNotification(noteId: Int)
    fun requestNotificationPermissions()
    fun hasNotificationPermission(): Boolean
}

class CommonNotificationManager {
    fun shouldScheduleNotification(note: Note): Boolean {
        return note.is_notifications_enabled == true &&
                note.start_time != null &&
                note.date != null
    }

    fun parseNotificationTime(date: String, time: String): Long {
        // Общая логика парсинга времени
        // Реализация будет в платформенных модулях
        return 0L
    }
}