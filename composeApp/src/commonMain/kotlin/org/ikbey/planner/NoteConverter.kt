
package org.ikbey.planner

import org.ikbey.planner.dataBase.Note
import kotlin.random.Random

fun generateNoteId(): Int {
    return Random.nextInt(1000, 9999) // Упростим диапазон
}

fun NoteData.toNote(): Note {
    // Разделяем текст на заголовок и описание
    val lines = this.note.split('\n')
    val (header, body) = when {
        lines.size == 1 -> {
            // Если одна строка - весь текст идет в заголовок
            this.note to ""
        }
        lines.size >= 2 -> {
            // Первая строка - заголовок, остальное - текст
            val headerText = lines[0]
            val bodyText = lines.subList(1, lines.size).joinToString("\n")
            headerText to bodyText
        }
        else -> "" to ""
    }

    return Note(
        id = generateNoteId(),
        lesson_id = null,
        date = this.date,
        header = if (header.isNotEmpty()) header else null,
        note = if (body.isNotEmpty()) body else null,
        is_notifications_enabled = this.isNotification,
        start_time = this.startTime,
        end_time = if (this.isInterval) this.endTime else null
    )
}