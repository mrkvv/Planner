package org.ikbey.planner.dataBase

import kotlinx.serialization.Serializable

@Serializable
data class Faculty(
    val id: Int,
    val name: String,
    val abbr: String
)

@Serializable
data class Group(
    val id: Int,
    val faculty_id: Int,
    val name: String
)

@Serializable
data class Schedule(
    val id: Int,
    val group_id: Int,
    val date: String? = null,
    val weekday: Int? = null,
    val subject: String,
    val type: String? = null,
    val start_time: String? = null,
    val end_time: String? = null,
    val teacher: String? = null,
    val audithory: String? = null
)

@Serializable
data class Note(
    val id: Int,
    val user_id: Int,
    val lesson_id: Int? = null,
    val date: String? = null,
    val header: String? = null,
    val note: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class CalendarEvent(
    val id: Int,
    val title: String,
    val description: String? = null,
    val date: String,
    val start_time: String? = null,
    val end_time: String? = null,
    val location: String? = null,
    val creator: String? = null,
    val calendar_name: String? = null
)
