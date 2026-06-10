package io.github.disktreegui

import io.github.disktreegui.settings.AppSettings
import io.github.disktreegui.settings.SettingsKeys
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun main() = application {
    Window(onCloseRequest = ::exitApplication, title = "DiskTree GUI") {
        DiskTreeApp(
            onAppLaunch = { state ->
                val lastOpenedFilePath = AppSettings.getString(SettingsKeys.LAST_OPENED_FILE_PATH, "")
                if (lastOpenedFilePath.isNotBlank()) {
                    val lastOpenedFile = File(lastOpenedFilePath)
                    runCatching {
                        if (!lastOpenedFile.isFile) {
                            error("上次打开的文件不存在：${lastOpenedFile.absolutePath}")
                        }
                        lastOpenedFile.readText(Charsets.UTF_8)
                    }.onSuccess { content ->
                        state.loadFromFile(content, lastOpenedFile.name)
                    }.onFailure {
                        state.setError(it.message ?: "自动加载上次文件失败")
                    }
                }
            },
            filePickerLauncher = FilePickerLauncher { onLoaded, onError ->
                val chooser = JFileChooser().apply {
                    dialogTitle = "选择 Tree.py 导出的文本文件"
                    fileFilter = FileNameExtensionFilter("Text Files", "txt", "log")
                    isAcceptAllFileFilterUsed = true
                }
                val result = chooser.showOpenDialog(null)
                if (result == JFileChooser.APPROVE_OPTION) {
                    val selectedFile: File = chooser.selectedFile
                    runCatching {
                        selectedFile.readText(Charsets.UTF_8)
                    }.onSuccess { content ->
                        AppSettings.putString(SettingsKeys.LAST_OPENED_FILE_PATH, selectedFile.absolutePath)
                        onLoaded(content, selectedFile.name)
                    }.onFailure {
                        onError(it.message ?: "读取文件失败")
                    }
                }
            }
        )
    }
}