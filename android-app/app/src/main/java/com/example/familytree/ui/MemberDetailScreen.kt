package com.example.familytree.ui

import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.example.familytree.network.MemberDto
import com.example.familytree.repository.MemberRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MemberDetailScreen(
    memberId: Int,
    repository: MemberRepository,
    modifier: Modifier = Modifier,
) {
    val memberState = remember { mutableStateOf<MemberDto?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(memberId) {
        val result = runCatching { repository.fetchMember(memberId) }
        result.onSuccess {
            memberState.value = it
            errorMessage = null
        }.onFailure {
            memberState.value = null
            errorMessage = "Failed to load member details."
        }
    }

    Column(
        modifier = modifier.padding(PaddingValues(16.dp)),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val member = memberState.value
        if (errorMessage != null) {
            Text(text = errorMessage!!)
            return@Column
        }
        if (member == null) {
            Text(text = "Loading...")
            return@Column
        }

        Text(text = "${member.firstName} ${member.lastName}")
        Text(text = "Parent ID: ${member.parentId ?: "None"}")

        val bitmapState = produceState<android.graphics.Bitmap?>(initialValue = null, key1 = member.imageBase64) {
            value = withContext(Dispatchers.Default) {
                val imageBytes = member.imageBase64?.let { Base64.decode(it, Base64.DEFAULT) }
                imageBytes?.let { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size) }
            }
        }
        val bitmap = bitmapState.value
        if (bitmap != null) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Profile image",
            )
        }
    }
}
