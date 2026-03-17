package com.example.familytree.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.familytree.network.MemberDto
import com.example.familytree.repository.MemberRepository

@Composable
fun MemberListScreen(
    repository: MemberRepository,
    onMemberSelected: (MemberDto) -> Unit,
) {
    val membersState = remember { mutableStateOf<List<MemberDto>>(emptyList()) }
    val isLoading = remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        isLoading.value = true
        val result = runCatching { repository.fetchMembers() }
        result.onSuccess {
            membersState.value = it
            errorMessage = null
        }.onFailure {
            membersState.value = emptyList()
            errorMessage = "Failed to load members. Check API connectivity."
        }
        isLoading.value = false
    }

    if (isLoading.value) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    if (errorMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = errorMessage!!)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(membersState.value) { member ->
            Column(
                modifier = Modifier
                    .clickable { onMemberSelected(member) }
                    .padding(8.dp)
            ) {
                Text(text = "${member.firstName} ${member.lastName}")
                Text(text = "Parent ID: ${member.parentId ?: "None"}")
            }
        }
    }
}
