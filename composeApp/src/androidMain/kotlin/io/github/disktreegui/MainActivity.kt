package io.github.disktreegui

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.github.disktreegui.settings.initAppSettings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initAppSettings(this)
        setContent {
            AndroidAppContent()
        }
    }
}

@Composable
private fun MainActivity.AndroidAppContent() {
    val pendingCallbacks = remember {
        mutableStateOf<Pair<((String, String?) -> Unit), ((String) -> Unit)>?>(null)
    }

    val documentLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        val callbacks = pendingCallbacks.value ?: return@rememberLauncherForActivityResult
        pendingCallbacks.value = null

        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
            contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: error("无法读取所选文件")
        }.onSuccess { content ->
            val name = queryDisplayName(uri) ?: uri.lastPathSegment
            callbacks.first(content, name)
        }.onFailure {
            callbacks.second(it.message ?: "读取文件失败")
        }
    }

    DiskTreeApp(
        filePickerLauncher = FilePickerLauncher { onLoaded, onError ->
            pendingCallbacks.value = onLoaded to onError
            documentLauncher.launch(arrayOf("text/*", "application/octet-stream"))
        }
    )
}

private fun MainActivity.queryDisplayName(uri: Uri): String? {
    return contentResolver.query(uri, arrayOf("_display_name"), null, null, null)?.use { cursor ->
        val index = cursor.getColumnIndex("_display_name")
        if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
    }
}