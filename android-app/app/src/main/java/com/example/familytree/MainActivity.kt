package com.example.familytree

import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familytree.repository.MemberRepository
import com.example.familytree.ui.AddMemberScreen
import com.example.familytree.ui.MemberDetailScreen
import com.example.familytree.ui.MemberListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FamilyTreeApp()
            }
        }
    }
}

private enum class AppScreen {
    LIST,
    ADD,
    DETAIL,
}

@Composable
private fun FamilyTreeApp() {
    val repository = remember { MemberRepository() }
    var screen by remember { mutableStateOf(AppScreen.LIST) }
    var selectedMemberId by remember { mutableIntStateOf(-1) }
    var listRefreshToken by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Family Tree") },
                actions = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (screen != AppScreen.LIST) {
                            Button(onClick = { screen = AppScreen.LIST }) {
                                Text("Back")
                            }
                        }
                        if (screen == AppScreen.LIST) {
                            Button(onClick = { screen = AppScreen.ADD }) {
                                Text("Add")
                            }
                        }
                        if (screen == AppScreen.DETAIL && selectedMemberId > 0) {
                            Button(onClick = {
                                screen = AppScreen.LIST
                                listRefreshToken++
                            }) {
                                Text("Refresh")
                            }
                        }
                        if (screen == AppScreen.ADD) {
                            Button(onClick = {
                                screen = AppScreen.LIST
                                listRefreshToken++
                            }) {
                                Text("Done")
                            }
                        }
                        if (screen == AppScreen.LIST) {
                            Button(onClick = { listRefreshToken++ }) {
                                Text("Reload")
                            }
                        }
                    }
                }
            )
        },
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        when (screen) {
            AppScreen.LIST -> MemberListScreen(
                repository = repository,
                reloadToken = listRefreshToken,
                onMemberSelected = {
                    selectedMemberId = it.id
                    screen = AppScreen.DETAIL
                },
                modifier = Modifier.padding(paddingValues),
            )

            AppScreen.ADD -> AddMemberScreen(
                repository = repository,
                onMemberSaved = {
                    listRefreshToken++
                    screen = AppScreen.LIST
                },
                modifier = Modifier.padding(paddingValues),
            )

            AppScreen.DETAIL -> {
                if (selectedMemberId > 0) {
                    MemberDetailScreen(
                        memberId = selectedMemberId,
                        repository = repository,
                        modifier = Modifier.padding(paddingValues),
                    )
                } else {
                    screen = AppScreen.LIST
                }
            }
        }
    }
}
