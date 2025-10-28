package org.ikbey.planner.dataBase

import kotlinx.serialization.Serializable

//Тут все классы для сериализации JSON в котлин код.
//Нужны для чтения и из облачной БД и из локальной SQLDelight

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
    val date: String,
    val weekday: Int,
    val subject: String,
    val type: String,
    val start_time: String,
    val end_time: String,
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
    val start_time: String,
    val end_time: String,
    val location: String? = null,
    val creator: String,
    val calendar_name: String
)
