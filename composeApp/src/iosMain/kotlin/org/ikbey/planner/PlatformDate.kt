package org.ikbey.planner

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter

actual class PlatformDate {
    actual val year: Int
        get() = getDateComponent("yyyy").toInt()

    actual val month: Int
        get() = getDateComponent("MM").toInt()

    actual val day: Int
        get() = getDateComponent("dd").toInt()

    private fun getDateComponent(format: String): String {
        val formatter = NSDateFormatter()
        formatter.dateFormat = format
        return formatter.stringFromDate(NSDate())
    }
}