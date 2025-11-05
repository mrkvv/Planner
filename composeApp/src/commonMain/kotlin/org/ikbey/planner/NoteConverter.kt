package org.ikbey.planner

import org.ikbey.planner.dataBase.Note
import kotlin.random.Random

fun formatDate(year: Int, month: Int, day: Int): String {
    val monthStr = if (month < 10) "0$month" else "$month"
    val dayStr = if (day < 10) "0$day" else "$day"
    val result = "$year-$monthStr-$dayStr"
    return result
}

fun formatTime(timeString: String): String {
    return if (timeString.length > 5) {
        timeString.substring(0, 5)
    } else {
        timeString
    }
}

fun generateNoteId(): Int {
    return Random.nextInt(1000, 9999)
}

fun NoteData.toUserNote(): Note {
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
        place = this.location,
        header = header.ifEmpty { null },
        note = body.ifEmpty { null },
        is_notifications_enabled = this.isNotification,
        start_time = this.startTime,
        end_time = if (this.isInterval) this.endTime else null
    )
}

fun org.ikbey.planner.dataBase.Schedule.toNoteData(): NoteData {
    val noteText = buildString {
        append(subject)
        if (type.isNotEmpty()) {
            append(" ($type)")
        }
        if (!teacher.isNullOrEmpty()) {
            append("\nПреподаватель: $teacher")
        }
    }

    val location = buildString {
        if (!place.isNullOrEmpty()) {
            append(place)
        }
        if (!audithory.isNullOrEmpty()) {
            if (!place.isNullOrEmpty()) {
                append(", ")
            }
            append(audithory)
        }
    }

    return NoteData(
        startTime = formatTime(start_time),
        endTime = formatTime(end_time),
        location = location,
        note = noteText,
        isInterval = true,
        isNotification = false,
        date = date,
        type = NoteType.SCHEDULE
    )
}

fun org.ikbey.planner.dataBase.Schedule.toNote(): Note {
    val noteText = buildString {
        if (!teacher.isNullOrEmpty()) {
            append("Преподаватель: $teacher")
        }
    }

    val location = buildString {
        if (!place.isNullOrEmpty()) {
            append(place)
        }
        if (!audithory.isNullOrEmpty()) {
            if (!place.isNullOrEmpty()) {
                append(", ")
            }
            append(audithory)
        }
    }

    return Note(
        id = id,
        date = date,
        place = location.ifEmpty { null },
        header = "$subject${if (type.isNotEmpty()) " ($type)" else ""}",
        note = noteText.ifEmpty { null },
        is_notifications_enabled = false,
        start_time = formatTime(start_time),
        end_time = formatTime(end_time),
        is_done = is_done
    )
}

fun org.ikbey.planner.dataBase.CalendarEvent.toNoteData(): NoteData {
    val noteText = buildString {
        append(title)
        if (!description.isNullOrEmpty()) {
            append("\n$description")
        }
        if (!location.isNullOrEmpty()) {
            append("\nМесто: $location")
        }
        if (creator.isNotEmpty()) {
            append("\nОрганизатор: $creator")
        }
    }

    return NoteData(
        startTime = formatTime(start_time),
        endTime = formatTime(end_time),
        location = location ?: "",
        note = noteText,
        isInterval = true,
        isNotification = false,
        date = date,
        type = NoteType.CALENDAR_EVENT
    )
}

fun org.ikbey.planner.dataBase.CalendarEvent.toNote(): Note {
    val noteText = buildString {
        if (!description.isNullOrEmpty()) {
            append(description)
        }
        if (!location.isNullOrEmpty()) {
            if (!description.isNullOrEmpty()) append("\n")
            append("Место: $location")
        }
        if (creator.isNotEmpty()) {
            if (!description.isNullOrEmpty() || !location.isNullOrEmpty()) append("\n")
            append("Организатор: $creator")
        }
    }

    return Note(
        id = id,
        date = date,
        place = location,
        header = title,
        note = noteText.ifEmpty { null },
        is_notifications_enabled = false,
        start_time = formatTime(start_time),
        end_time = formatTime(end_time),
        is_done = is_done
    )
}

fun Note.toNoteData(): NoteData {
    val fullText = buildString {
        if (!header.isNullOrEmpty()) append(header)
        if (!note.isNullOrEmpty()) {
            if (!header.isNullOrEmpty()) append("\n")
            append(note)
        }
    }

    return NoteData(
        startTime = start_time ?: "",
        endTime = end_time ?: "",
        location = place ?: "",
        note = fullText,
        isInterval = !end_time.isNullOrEmpty(),
        isNotification = is_notifications_enabled == true,
        date = date,
        type = NoteType.USER_NOTE
    )
}