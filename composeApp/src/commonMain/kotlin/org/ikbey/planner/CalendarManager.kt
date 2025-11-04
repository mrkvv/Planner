package org.ikbey.planner

expect class PlatformDate() {
    val year: Int
    val month: Int
    val day: Int
}

class CalendarManager {
    fun getCurrentMonth(): Int {
        val date = PlatformDate()
        return date.month
    }

    fun getCurrentYear() : Int {
        val date = PlatformDate()
        return date.year
    }

    fun getCurrentDay(): Int {
        val date = PlatformDate()
        return date.day
    }

    fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Январь"
            2 -> "Февраль"
            3 -> "Март"
            4 -> "Апрель"
            5 -> "Май"
            6 -> "Июнь"
            7 -> "Июль"
            8 -> "Август"
            9 -> "Сентябрь"
            10 -> "Октябрь"
            11 -> "Ноябрь"
            12 -> "Декабрь"
            else -> ""
        }
    }

    fun getDayOfWeekName(dayOfWeek: Int): String {
        return when (dayOfWeek) {
            1 -> "Понедельник"
            2 -> "Вторник"
            3 -> "Среда"
            4 -> "Четверг"
            5 -> "Пятница"
            6 -> "Суббота"
            7 -> "Воскресенье"
            else -> ""
        }
    }

    /** Упрощенный алгоритм Zeller's congruence. Возвращает число от 1 до 7, где 1 - пн, 2 - вт и т.д.*/
    fun calculateDayOfWeek(year: Int, month: Int, day: Int): Int {
        var m = month
        var y = year
        if (m < 3) {
            m += 12
            y -= 1
        }
        val k = y % 100
        val j = y / 100
        var h = (day + 13*(m+1)/5 + k + k/4 + j/4 + 5*j) % 7
        h = (h + 5) % 7 + 1
        return h
    }

    /** Возвращает число от 1 до 7, показывающее каким днем недели является первый день указанного месяца.*/
    fun getFirstDayOfMonth(year: Int, month: Int): Int {
        return calculateDayOfWeek(year, month, 1)
    }

    fun getDaysAmountInMonth(year: Int, month: Int): Int {
        return when(month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 30
        }
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    fun getCalendarMatrix(year: Int, month: Int): List<List<Int?>> {
        val firstDay = getFirstDayOfMonth(year, month)
        val daysAmount = getDaysAmountInMonth(year, month)

        val matrix = mutableListOf<MutableList<Int?>>()
        var currentWeek = mutableListOf<Int?>()

        for(i in 1 .. firstDay - 1) currentWeek.add(null)

        for(day in 1..daysAmount) {
            currentWeek.add(day)

            if(currentWeek.size == 7 || day == daysAmount) {
                while (currentWeek.size < 7) {
                    currentWeek.add(null)
                }

                matrix.add(currentWeek)
                currentWeek = mutableListOf()
            }
        }
        return matrix
    }

    fun isToday(year: Int, month: Int, day: Int?): Boolean {
        val date = PlatformDate()
        return (year == date.year && month == date.month && day == date.day)
    }
}