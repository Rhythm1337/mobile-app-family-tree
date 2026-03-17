package com.example.familytree.ui

import android.content.Context
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.familytree.network.CreateMemberRequest
import com.example.familytree.repository.MemberRepository
import kotlinx.coroutines.launch

@Composable
fun AddMemberScreen(
    repository: MemberRepository,
    onMemberSaved: () -> Unit,
) {
    val firstName = remember { mutableStateOf("") }
    val lastName = remember { mutableStateOf("") }
    val relationType = remember { mutableStateOf("") }
    val relatedMemberId = remember { mutableStateOf("") }
    val imageBase64 = remember { mutableStateOf<String?>(null) }
    var submitError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageBase64.value = readImageBase64(context, uri.toString())
        }
    }

    Column(
        modifier = Modifier.padding(PaddingValues(16.dp)),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = firstName.value,
            onValueChange = { firstName.value = it },
            label = { Text("First Name") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = lastName.value,
            onValueChange = { lastName.value = it },
            label = { Text("Last Name") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = relationType.value,
            onValueChange = { relationType.value = it },
            label = { Text("Relation (parent or child)") }
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = relatedMemberId.value,
            onValueChange = { relatedMemberId.value = it },
            label = { Text("Related Member ID") }
        )
        Button(onClick = { imagePicker.launch("image/*") }) {
            Text(text = "Pick Image")
        }
        Button(
            onClick = {
                val relation = relationType.value.trim().lowercase().ifEmpty { null }
                val relatedId = relatedMemberId.value.trim().toIntOrNull()
                val request = CreateMemberRequest(
                    firstName = firstName.value.trim(),
                    lastName = lastName.value.trim(),
                    relationType = relation,
                    relatedMemberId = relatedId,
                    imageBase64 = imageBase64.value,
                )
                scope.launch {
                    val result = runCatching { repository.addMember(request) }
                    if (result.isSuccess) {
                        submitError = null
                        onMemberSaved()
                    } else {
                        submitError = "Could not save member. Check relation and API status."
                    }
                }
            }
        ) {
            Text(text = "Save")
        }
        if (submitError != null) {
            Text(text = submitError!!)
        }
    }
}

private fun readImageBase64(context: Context, uriString: String): String? {
    val uri = android.net.Uri.parse(uriString)
    return try {
        val bytes = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes()
        }
        if (bytes != null) Base64.encodeToString(bytes, Base64.NO_WRAP) else null
    } catch (_: Exception) {
        null
    }
}
