package org.ikbey.planner

actual class PlatformDate {
    actual val year: Int
        get() = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    actual val month: Int
        get() = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    actual val day: Int
        get() = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)
}