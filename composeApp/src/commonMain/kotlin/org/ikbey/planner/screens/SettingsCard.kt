package org.ikbey.planner.screens

import org.ikbey.planner.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import org.ikbey.planner.dataBase.Faculty
import org.ikbey.planner.dataBase.Group
import org.ikbey.planner.dataBase.LocalDatabaseManager
import org.ikbey.planner.dataBase.SyncManager
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.ikbey.planner.dataBase.ServiceLocator


@Composable
fun SettingsCard(
    onDismiss: () -> Unit,
) {
    val localDb = ServiceLocator.localDatabaseManager
    val syncManager = ServiceLocator.syncManager

    val coroutineScope = rememberCoroutineScope()

    val faculties = remember { mutableStateOf(emptyList<Faculty>()) }
    val groups = remember { mutableStateOf(emptyList<Group>()) }

    var tempInstitute by remember {
        mutableStateOf("")
    }
    var tempGroup by remember {
        mutableStateOf("")
    }

    var currentGroupId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        faculties.value = localDb.getFaculties()

        currentGroupId = ServiceLocator.syncManager.getGroupId()

        if (currentGroupId != null) {
            tempGroup = currentGroupId!!

            val allGroups = localDb.getGroups()
            val currentGroup = allGroups.find { it.id.toString() == currentGroupId }

            if (currentGroup != null) {
                tempInstitute = currentGroup.faculty_id.toString()
                groups.value = localDb.getGroupsByFaculty(currentGroup.faculty_id)
            }
        }
    }

    LaunchedEffect(tempInstitute) {
        if (tempInstitute.isNotBlank()) {
            groups.value = localDb.getGroupsByFaculty(tempInstitute.toInt())
        }
        else {
            groups.value = emptyList()
        }
    }

    var showInstitutes by remember { mutableStateOf(false) }
    var showGroups by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onDismiss() }
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                    .clickable {},
                colors = CardDefaults.cardColors(
                    containerColor = LightGreen
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                ) {
                    Text(
                        text = "Выберите институт:",
                        fontSize = 20.sp,
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    InstituteDropdown(
                        faculties = faculties.value,
                        tempInstitute = tempInstitute,
                        showInstitutes = showInstitutes,
                        onInstituteSelected = { facultyId ->
                            tempInstitute = facultyId
                            tempGroup = ""
                            showInstitutes = false
                            showGroups = false
                        },
                        onToggleDropdown = { showInstitutes = !showInstitutes }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Выберите группу:",
                        fontSize = 20.sp,
                        fontFamily = getInterFont(InterFontType.REGULAR),
                        color = Color.Black,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    GroupDropdown(
                        groups = groups.value,
                        tempInstitute = tempInstitute,
                        tempGroup = tempGroup,
                        showGroups = showGroups,
                        onGroupSelected = { groupId ->
                            tempGroup = groupId
                            showGroups = false
                        },
                        onToggleDropdown = {
                            if (tempInstitute.isNotBlank()) {
                                showGroups = !showGroups
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (tempGroup.isNotBlank()) {
                                coroutineScope.launch {
                                    syncManager.setGroupId(tempGroup.toInt())
                                }
                            }
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange
                        ),
                        enabled = tempInstitute.isNotBlank() && tempGroup.isNotBlank()
                    ) {
                        Text(
                            text = "Сохранить",
                            fontFamily = getInterFont(InterFontType.REGULAR),
                            fontSize = 20.sp,
                            color = if (tempInstitute.isNotBlank() && tempGroup.isNotBlank()) DarkOrange else DarkGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InstituteDropdown(
    faculties: List<Faculty>,
    tempInstitute: String,
    showInstitutes: Boolean,
    onInstituteSelected: (String) -> Unit,
    onToggleDropdown: () -> Unit
){
    val selectedFaculty = faculties.find { it.id.toString() == tempInstitute }
    val displayText = selectedFaculty?.abbr ?: selectedFaculty?.name ?: "Выберите институт"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleDropdown() }
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = displayText,
            fontSize = 16.sp,
            color = if (tempInstitute.isBlank()) Color.Gray else Color.Black
        )
    }

    if (showInstitutes) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn {
                items(faculties) { faculty ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onInstituteSelected(faculty.id.toString()) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = faculty.abbr ?: faculty.name,
                            fontSize = 16.sp,
                            color = if (faculty.id.toString() == tempInstitute) Color(0xFFEE9528) else Color.Black
                        )
                    }
                    Divider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun GroupDropdown(
    groups: List<Group>,
    tempInstitute: String,
    tempGroup: String,
    showGroups: Boolean,
    onGroupSelected: (String) -> Unit,
    onToggleDropdown: () -> Unit
) {
    val selectedGroup = groups.find { it.id.toString() == tempGroup }
    val displayText = selectedGroup?.name ?: "Выберите группу"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (tempInstitute.isNotBlank()) {
                    onToggleDropdown()
                }
            }
            .background(
                if (tempInstitute.isBlank()) Color(0xFFF5F5F5).copy(alpha = 0.5f)
                else Color(0xFFF5F5F5),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = displayText,
                fontSize = 16.sp,
                color = when {
                    tempInstitute.isBlank() -> Color.Gray.copy(alpha = 0.5f)
                    tempGroup.isBlank() -> Color.Gray
                    else -> Color.Black
                }
            )
        }
    }

    if (showGroups && tempInstitute.isNotBlank()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            LazyColumn {
                items(groups) { group ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGroupSelected(group.id.toString()) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = group.name,
                            fontSize = 16.sp,
                            color = if (group.id.toString() == tempGroup) Color(0xFFEE9528) else Color.Black
                        )
                    }
                    Divider(thickness = 0.5.dp, color = Color.LightGray)
                }
            }
        }
    }
}

@Composable
fun SettingsButton(
    isSettingsOpen: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(60.dp)
            .clip(CircleShape),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = LightOrange
        )
    ){
        Icon(
            imageVector =
                if (isSettingsOpen){
                    getIcon(IconType.SETTINGS_OPEN)
                }
                else {
                    getIcon(IconType.SETTINGS_CLOSED)
                },
            contentDescription = null,
            modifier = Modifier.size(44.dp),
            tint = DarkGreen
        )
    }
}