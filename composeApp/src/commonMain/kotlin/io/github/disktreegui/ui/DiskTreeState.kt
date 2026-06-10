package io.github.disktreegui.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.disktreegui.model.TreeNode
import io.github.disktreegui.parser.TreeScanParser

class DiskTreeState {
    var inputText by mutableStateOf("")
    var errorMessage by mutableStateOf<String?>(null)
    val roots = mutableStateListOf<TreeNode>()
    val expandedIds = mutableStateListOf<String>()

    fun parseInput() {
        runCatching {
            TreeScanParser.parse(inputText)
        }.onSuccess { nodes ->
            roots.clear()
            roots.addAll(nodes)
            expandedIds.clear()
            nodes.forEach { expandedIds.add(it.id) }
            errorMessage = if (nodes.isEmpty()) "没有解析到可用节点，请确认导入的是 Tree.py 导出的文本。" else null
        }.onFailure {
            roots.clear()
            expandedIds.clear()
            errorMessage = it.message ?: "解析失败"
        }
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