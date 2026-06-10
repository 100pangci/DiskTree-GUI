package io.github.disktreegui

fun interface FilePickerLauncher {
    fun open(onLoaded: (String, String?) -> Unit, onError: (String) -> Unit)
}