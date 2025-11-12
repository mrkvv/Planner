package org.ikbey.planner.notification

import org.ikbey.planner.dataBase.Note

//ПУСТАЯ ЗУГЛУШКА
actual class NotificationManager actual constructor() {
    actual fun scheduleNotification(note: Note) { }
    actual fun cancelNotification(noteId: Int) { }
    actual fun requestNotificationPermissions() { }
    actual fun hasNotificationPermission(): Boolean { return false }
}