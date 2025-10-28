package org.ikbey.planner

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.vectorResource
import planner.composeapp.generated.resources.*

object Icons {
    val filter: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_filter)

    val settingsOpen: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_settings_open)

    val settingsClosed: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_settings_closed)

    val training: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_training)

    val trash: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_trash)

    val lilStick: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_lil_sticky_notes)

    val bigStick: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_big_sticky_notes)

    val plus: ImageVector
        @Composable
        get() = vectorResource(Res.drawable.ic_plus)
}

enum class IconType {
    FILTER, SETTINGS_OPEN, SETTINGS_CLOSED, TRAINING, TRASH, LIL_STICK, BIG_STICK, PLUS

}

@Composable
fun getIcon(iconType: IconType): ImageVector {
    return when(iconType) {
        IconType.FILTER -> Icons.filter
        IconType.SETTINGS_OPEN -> Icons.settingsOpen
        IconType.SETTINGS_CLOSED -> Icons.settingsClosed
        IconType.TRAINING -> Icons.training
        IconType.TRASH -> Icons.trash
        IconType.LIL_STICK -> Icons.lilStick
        IconType.BIG_STICK -> Icons.bigStick
        IconType.PLUS -> Icons.plus
    }
}