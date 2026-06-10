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
    val parent: SearchIndexEntry?,
    var cachedFullPath: String? = null
)

class DiskTreeState {
    companion object {
        /**
         * 避免把超大文本直接塞进 Preferences / SharedPreferences。
         * Windows 上 Java Preferences 单条 value 长度有限，超过后会报 `value too long`。
         */
        private const val MAX_PERSISTED_CONTENT_LENGTH = 6_000
    }

    var loadedFileName by mutableStateOf<String?>(null)
    var errorMessage by mutableStateOf<String?>(null)
    var activeTab by mutableStateOf(BottomTab.Files)
    var selectedNodeId by mutableStateOf<String?>(null)
    var searchQuery by mutableStateOf("")
    var appliedSearchQuery by mutableStateOf("")
        private set
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
            appliedSearchQuery = ""
            rebuildSearchIndex(nodes)
            searchResults.clear()
            loadedFileName = fileName
            selectedNodeId = null
            activeTab = BottomTab.Files
            persistLastLoadedFile(content, fileName)
            errorMessage = if (nodes.isEmpty()) "没有解析到可用节点，请确认导入的是 Tree.py 导出的文本。" else null
        }.onFailure {
            roots.clear()
            expandedIds.clear()
            searchIndex.clear()
            searchResults.clear()
            searchQuery = ""
            appliedSearchQuery = ""
            loadedFileName = fileName
            selectedNodeId = null
            errorMessage = it.message ?: "解析失败"
        }
    }

    fun restoreLastLoadedFile() {
        if (roots.isNotEmpty()) return

        val content = AppSettings.getString(SettingsKeys.LAST_OPENED_FILE_CONTENT, "")
        if (content.isBlank()) return

        val name = AppSettings.getString(SettingsKeys.LAST_OPENED_FILE_NAME, "").ifBlank { null }
        loadFromFile(content, name)
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
    }

    fun selectNode(node: TreeNode) {
        selectedNodeId = node.id
    }

    fun isSelected(node: TreeNode): Boolean = selectedNodeId == node.id

    fun revealNode(targetId: String) {
        fun expandPath(nodes: List<TreeNode>): Boolean {
            for (node in nodes) {
                if (node.id == targetId) {
                    selectedNodeId = node.id
                    return true
                }
                if (expandPath(node.children)) {
                    if (node.children.isNotEmpty() && !expandedIds.contains(node.id)) {
                        expandedIds.add(node.id)
                    }
                    return true
                }
            }
            return false
        }

        expandPath(roots)
    }

    fun revealNode(node: TreeNode) {
        revealNode(node.id)
    }

    fun performSearch() {
        val normalizedQuery = searchQuery.trim().lowercase()
        appliedSearchQuery = normalizedQuery
        if (normalizedQuery.isEmpty()) {
            searchResults.clear()
            return
        }

        val matches = searchIndex.asSequence()
            .filter { entry ->
                entry.normalizedName.contains(normalizedQuery) || buildFullPath(entry).lowercase().contains(normalizedQuery)
            }
            .map { entry ->
                val fullPath = buildFullPath(entry)
                val parentPath = buildParentPath(entry)
                SearchResultItem(
                    node = entry.node,
                    fullPath = fullPath,
                    parentPath = parentPath,
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

        fun visit(node: TreeNode, parent: SearchIndexEntry?) {
            val entry = SearchIndexEntry(
                node = node,
                normalizedName = node.name.lowercase(),
                parent = parent
            )
            searchIndex += entry

            node.children.forEach { child ->
                visit(child, entry)
            }
        }

        nodes.forEach { root ->
            visit(root, null)
        }
    }

    private fun buildFullPath(entry: SearchIndexEntry): String {
        entry.cachedFullPath?.let { return it }

        val fullPath = entry.parent?.let { parent ->
            buildFullPath(parent) + "/" + entry.node.name
        } ?: entry.node.name

        entry.cachedFullPath = fullPath
        return fullPath
    }

    private fun buildParentPath(entry: SearchIndexEntry): String {
        val parent = entry.parent ?: return ""
        return buildFullPath(parent)
    }

    private fun persistLastLoadedFile(content: String, fileName: String?) {
        val persistedContent = if (content.length <= MAX_PERSISTED_CONTENT_LENGTH) content else ""
        AppSettings.putString(SettingsKeys.LAST_OPENED_FILE_CONTENT, persistedContent)
        AppSettings.putString(SettingsKeys.LAST_OPENED_FILE_NAME, fileName.orEmpty())
    }
}