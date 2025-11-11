package org.ikbey.planner

import androidx.compose.ui.window.ComposeUIViewController
import org.ikbey.planner.notification.NotificationManager

fun MainViewController() = ComposeUIViewController { App(notificationManager = NotificationManager()) }