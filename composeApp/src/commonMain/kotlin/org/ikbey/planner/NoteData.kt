package org.ikbey.planner

data class NoteData(
    val startTime: String,
    val endTime: String,
    val location: String,
    val note: String,
    val isInterval: Boolean,
    val isNotification: Boolean = false,
    val date: String? = null,
    val type: NoteType = NoteType.USER_NOTE
)

enum class NoteType {
    USER_NOTE, SCHEDULE, CALENDAR_EVENT
}