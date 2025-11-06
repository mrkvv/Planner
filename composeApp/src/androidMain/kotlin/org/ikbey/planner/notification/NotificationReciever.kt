package org.ikbey.planner.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val noteId = intent.getIntExtra("note_id", -1)
        val header = intent.getStringExtra("header") ?: "Напоминание"
        val message = intent.getStringExtra("message") ?: "Время для вашей заметки"

        showNotification(context, noteId, header, message)
    }

    private fun showNotification(context: Context, noteId: Int, title: String, message: String) {
        try {
            val notification = NotificationCompat.Builder(context, "notes_channel")
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(noteId, notification)
            println("Уведомление показано: $title")

        } catch (securityException: SecurityException) {
            println("SecurityException: Нет разрешения на показ уведомления")
        } catch (e: Exception) {
            println("Ошибка при показе уведомления: ${e.message}")
        }
    }
}