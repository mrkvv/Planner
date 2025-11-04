package org.ikbey.planner

fun formatDate(year: Int, month: Int, day: Int): String {
    val monthStr = if (month < 10) "0$month" else "$month"
    val dayStr = if (day < 10) "0$day" else "$day"
    val result = "$year-$monthStr-$dayStr"
    println("DEBUG: Форматированная дата: $result")
    return result
}