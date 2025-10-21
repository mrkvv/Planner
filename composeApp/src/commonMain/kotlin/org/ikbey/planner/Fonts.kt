package org.ikbey.planner

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import planner.composeapp.generated.resources.*

enum class InterFontType {
    BOLD, EXTRA_BOLD, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN
}

@Composable
fun getInterFont(fontName: InterFontType) : FontFamily {
    return when(fontName) {
        InterFontType.BOLD -> FontFamily(Font(Res.font.Inter_Bold))
        InterFontType.EXTRA_BOLD -> FontFamily(Font(Res.font.Inter_ExtraBold))
        InterFontType.EXTRA_LIGHT -> FontFamily(Font(Res.font.Inter_ExtraLight))
        InterFontType.LIGHT -> FontFamily(Font(Res.font.Inter_Light))
        InterFontType.MEDIUM -> FontFamily(Font(Res.font.Inter_Medium))
        InterFontType.REGULAR -> FontFamily(Font(Res.font.Inter_Regular))
        InterFontType.SEMI_BOLD -> FontFamily(Font(Res.font.Inter_SemiBold))
        InterFontType.THIN -> FontFamily(Font(Res.font.Inter_Thin))
    }
}
