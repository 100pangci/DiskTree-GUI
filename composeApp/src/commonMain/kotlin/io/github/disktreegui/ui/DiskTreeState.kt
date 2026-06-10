package io.github.disktreegui.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.disktreegui.model.TreeNode
import io.github.disktreegui.parser.TreeScanParser

enum class BottomTab {
    Files,
    Settings
}

enum class ThemeMode {
    Light,
    Dark
}

class DiskTreeState {
    var loadedFileName by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var activeTab by mutableStateOf(BottomTab.Files)
    var themeMode by mutableStateOf(ThemeMode.Dark)
    val roots = mutableStateListOf<TreeNode>()
    val expandedIds = mutableStateListOf<String>()

    fun loadFromFile(content: String, fileName: String? = null) {
        runCatching {
            TreeScanParser.parse(content)
        }.onSuccess { nodes ->
            roots.clear()
            roots.addAll(nodes)
            expandedIds.clear()
            nodes.forEach { expandedIds.add(it.id) }
            loadedFileName = fileName
            activeTab = BottomTab.Files
            errorMessage = if (nodes.isEmpty()) "没有解析到可用节点，请确认导入的是 Tree.py 导出的文本。" else null
        }.onFailure {
            roots.clear()
            expandedIds.clear()
            loadedFileName = fileName
            errorMessage = it.message ?: "解析失败"
        }
    }

    fun setError(message: String) {
        errorMessage = message
    }

    fun toggle(node: TreeNode) {
        if (expandedIds.contains(node.id)) {
            expandedIds.remove(node.id)
        } else {
            expandedIds.add(node.id)
        }
    }

    fun isExpanded(node: TreeNode): Boolean = expandedIds.contains(node.id)
}