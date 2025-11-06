package org.ikbey.planner.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager as AndroidNotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import org.ikbey.planner.dataBase.Note
import java.text.SimpleDateFormat
import java.util.*

actual class NotificationManager actual constructor() {
    private lateinit var context: Context
    private val channelId = "notes_channel"
    private val channelName = "Уведомления о заметках"
    private val postNotificationsPermission = android.Manifest.permission.POST_NOTIFICATIONS

    fun initialize(context: Context) {
        this.context = context
        createNotificationChannel()
    }

    actual fun scheduleNotification(note: Note) {
        println("🔔 [DEBUG] scheduleNotification вызван для заметки ${note.id}")

        if (note.start_time == null || note.date == null) {
            println("❌ [DEBUG] Нельзя запланировать уведомление: время=${note.start_time}, дата=${note.date}")
            return
        }

        // Проверяем разрешение перед планированием уведомления
        if (!hasNotificationPermission()) {
            println("❌ [DEBUG] Нет разрешения на уведомления")
            return
        }

        val notificationTime = parseNotificationTime(note.date, note.start_time)
        val currentTime = System.currentTimeMillis()

        println("📅 [DEBUG] Заметка: ${note.header}")
        println("⏰ [DEBUG] Дата: ${note.date}, Время: ${note.start_time}")
        println("🕒 [DEBUG] Текущее время: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(currentTime))}")
        println("🔔 [DEBUG] Время уведомления: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(notificationTime))}")
        println("⏱️ [DEBUG] Разница: ${(notificationTime - currentTime) / 1000} секунд")

        if (notificationTime <= currentTime) {
            println("❌ [DEBUG] Время уведомления уже прошло!")
            return
        }

        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java).apply {
                putExtra("note_id", note.id)
                putExtra("header", note.header ?: "Напоминание")
                putExtra("message", note.note ?: "Время для вашей заметки")
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                note.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            println("📱 [DEBUG] AlarmManager: $alarmManager")
            println("📤 [DEBUG] PendingIntent создан: $pendingIntent")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
                )
                println("✅ [DEBUG] Уведомление запланировано через setExactAndAllowWhileIdle")
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime,
                    pendingIntent
                )
                println("✅ [DEBUG] Уведомление запланировано через setExact")
            }

            // Проверим, действительно ли запланировано
            val checkIntent = PendingIntent.getBroadcast(
                context,
                note.id,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (checkIntent != null) {
                println("✅ [DEBUG] PendingIntent подтвержден - уведомление запланировано")
            } else {
                println("❌ [DEBUG] PendingIntent не найден - ошибка планирования")
            }

            println("✅ [DEBUG] Уведомление запланировано для заметки ${note.id} на $notificationTime")
        } catch (securityException: SecurityException) {
            println("❌ [DEBUG] SecurityException: Нет разрешения для установки будильника: ${securityException.message}")
        } catch (e: Exception) {
            println("❌ [DEBUG] Ошибка при планировании уведомления: ${e.message}")
            e.printStackTrace()
        }
    }

    actual fun cancelNotification(noteId: Int) {
        println("🔔 [DEBUG] cancelNotification вызван для заметки $noteId")
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                noteId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(pendingIntent)
            println("✅ [DEBUG] Уведомление отменено для заметки $noteId")
        } catch (securityException: SecurityException) {
            println("❌ [DEBUG] SecurityException: Нет разрешения для отмены будильника")
        }
    }

    actual fun requestNotificationPermissions() {
        // Эта функция будет вызываться из Activity
        println("🔔 [DEBUG] requestNotificationPermissions вызван")
    }

    // Дополнительный метод для удобства
    fun requestNotificationPermissions(onRationale: (Boolean) -> Unit = {}) {
        println("🔔 [DEBUG] requestNotificationPermissions (расширенный) вызван")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val shouldShowRationale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                (context as? androidx.activity.ComponentActivity)?.shouldShowRequestPermissionRationale(
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ?: false
            } else {
                false
            }
            println("🔔 [DEBUG] shouldShowRationale: $shouldShowRationale")
            onRationale(shouldShowRationale)
        }
    }

    actual fun hasNotificationPermission(): Boolean {
        val hasPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                postNotificationsPermission
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        println("🔔 [DEBUG] hasNotificationPermission: $hasPermission")
        return hasPermission
    }

    private fun createNotificationChannel() {
        println("🔔 [DEBUG] createNotificationChannel вызван")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                AndroidNotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления для напоминаний о заметках"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as AndroidNotificationManager
            notificationManager.createNotificationChannel(channel)
            println("✅ [DEBUG] Канал уведомлений создан: $channelId")

            // Проверим создание канала
            val createdChannel = notificationManager.getNotificationChannel(channelId)
            if (createdChannel != null) {
                println("✅ [DEBUG] Канал подтвержден: ${createdChannel.name}, важность: ${createdChannel.importance}")
            } else {
                println("❌ [DEBUG] Канал не создан!")
            }
        } else {
            println("ℹ️ [DEBUG] Канал уведомлений не требуется (API < 26)")
        }
    }

    private fun parseNotificationTime(date: String, time: String): Long {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateTimeString = "$date $time"
            println("🕐 [DEBUG] Парсим время: '$dateTimeString'")

            val notificationDate = dateFormat.parse(dateTimeString)
            val result = notificationDate?.time ?: System.currentTimeMillis() + 60000

            println("🕐 [DEBUG] Результат парсинга: $result")
            result

        } catch (e: Exception) {
            val fallback = System.currentTimeMillis() + 60000
            println("❌ [DEBUG] Ошибка парсинга времени: ${e.message}, используем fallback: $fallback")
            fallback
        }
    }
}