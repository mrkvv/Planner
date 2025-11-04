package org.ikbey.planner

import org.ikbey.planner.dataBase.Note
import kotlin.random.Random

fun formatDate(year: Int, month: Int, day: Int): String {
    val monthStr = if (month < 10) "0$month" else "$month"
    val dayStr = if (day < 10) "0$day" else "$day"
    val result = "$year-$monthStr-$dayStr"
    println("DEBUG: Форматированная дата: $result")
    return result
}
fun generateNoteId(): Int {
    return Random.nextInt(1000, 9999)
}

fun NoteData.toNote(): Note {
    // Разделяем текст на заголовок и описание
    val lines = this.note.split('\n')
    val (header, body) = when {
        lines.size == 1 -> {
            this.note to ""
        }
        lines.size >= 2 -> {
            val headerText = lines[0]
            val bodyText = lines.subList(1, lines.size).joinToString("\n")
            headerText to bodyText
        }
        else -> "" to ""
    }

    return Note(
        id = generateNoteId(),
        date = this.date,
        place = this.location, // ← ИСПРАВЛЕНО: location идет в place
        header = if (header.isNotEmpty()) header else null,
        note = if (body.isNotEmpty()) body else null,
        is_notifications_enabled = this.isNotification,
        start_time = this.startTime,
        end_time = if (this.isInterval) this.endTime else null
    )
}