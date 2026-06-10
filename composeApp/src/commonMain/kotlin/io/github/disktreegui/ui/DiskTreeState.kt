package io.github.disktreegui.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.disktreegui.model.TreeNode
import io.github.disktreegui.parser.TreeScanParser
import io.github.disktreegui.settings.AppSettings
import io.github.disktreegui.settings.SettingsKeys

enum class BottomTab {
    Files,
    Settings
}

enum class ThemeMode {
    Light,
    Dark
}

data class SearchResultItem(
    val node: TreeNode,
    val fullPath: String,
    val parentPath: String,
    val isDirectMatch: Boolean
)

private data class SearchIndexEntry(
    val node: TreeNode,
    val normalizedName: String,
    val normalizedPath: String,
    val fullPath: String,
    val parentPath: String
)

class DiskTreeState {
    var loadedFileName by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var activeTab by mutableStateOf(BottomTab.Files)
    var searchQuery by mutableStateOf("")
    private val _themeMode = mutableStateOf(
        if (AppSettings.getString(SettingsKeys.THEME_MODE, "Dark") == "Light") ThemeMode.Light else ThemeMode.Dark
    )
    var themeMode: ThemeMode
        get() = _themeMode.value
        set(value) {
            _themeMode.value = value
            AppSettings.putString(SettingsKeys.THEME_MODE, value.name)
        }
    val roots = mutableStateListOf<TreeNode>()
    val expandedIds = mutableStateListOf<String>()
    private val searchIndex = mutableListOf<SearchIndexEntry>()
    val searchResults = mutableStateListOf<SearchResultItem>()

    fun loadFromFile(content: String, fileName: String? = null) {
        runCatching {
            TreeScanParser.parse(content)
        }.onSuccess { nodes ->
            roots.clear()
            roots.addAll(nodes)
            expandedIds.clear()
            searchQuery = ""
            rebuildSearchIndex(nodes)
            searchResults.clear()
            loadedFileName = fileName
            activeTab = BottomTab.Files
            errorMessage = if (nodes.isEmpty()) "没有解析到可用节点，请确认导入的是 Tree.py 导出的文本。" else null
        }.onFailure {
            roots.clear()
            expandedIds.clear()
            searchIndex.clear()
            searchResults.clear()
            searchQuery = ""
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

    fun updateSearchQuery(query: String) {
        searchQuery = query
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isEmpty()) {
            searchResults.clear()
            return
        }

        val matches = searchIndex.asSequence()
            .filter { entry ->
                entry.normalizedName.contains(normalizedQuery) || entry.normalizedPath.contains(normalizedQuery)
            }
            .map { entry ->
                SearchResultItem(
                    node = entry.node,
                    fullPath = entry.fullPath,
                    parentPath = entry.parentPath,
                    isDirectMatch = entry.normalizedName.contains(normalizedQuery)
                )
            }
            .sortedWith(
                compareByDescending<SearchResultItem> { it.isDirectMatch }
                    .thenBy { it.fullPath.length }
                    .thenBy { it.fullPath.lowercase() }
            )
            .toList()

        searchResults.clear()
        searchResults.addAll(matches)
    }

    fun isExpanded(node: TreeNode): Boolean = expandedIds.contains(node.id)

    private fun rebuildSearchIndex(nodes: List<TreeNode>) {
        searchIndex.clear()

        fun visit(node: TreeNode, parentSegments: List<String>) {
            val cleanName = node.name.removeSuffix("/")
            val currentSegments = parentSegments + cleanName
            val fullPath = currentSegments.joinToString("/")
            val parentPath = parentSegments.joinToString("/")

            searchIndex += SearchIndexEntry(
                node = node,
                normalizedName = cleanName.lowercase(),
                normalizedPath = fullPath.lowercase(),
                fullPath = fullPath,
                parentPath = parentPath
            )

            node.children.forEach { child ->
                visit(child, currentSegments)
            }
        }

        nodes.forEach { root ->
            visit(root, emptyList())
        }
    }
}