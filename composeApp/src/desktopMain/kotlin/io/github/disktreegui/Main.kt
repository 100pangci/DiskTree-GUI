package io.github.disktreegui

import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.io.File
import javax.imageio.ImageIO
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

fun main() = application {
    val icon = runCatching {
        ImageIO.read(Thread.currentThread().contextClassLoader.getResourceAsStream("app_icon.png"))
            ?.toComposeImageBitmap()
    }.getOrNull()

    Window(onCloseRequest = ::exitApplication, title = "DiskTree GUI", icon = icon) {
        DiskTreeApp(
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
                        onLoaded(content, selectedFile.name)
                    }.onFailure {
                        onError(it.message ?: "读取文件失败")
                    }
                }
            }
        )
    }
}