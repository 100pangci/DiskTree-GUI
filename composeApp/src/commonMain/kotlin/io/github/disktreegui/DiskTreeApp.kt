package io.github.disktreegui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import io.github.disktreegui.model.TreeNode
import io.github.disktreegui.theme.DiskTreeTheme
import io.github.disktreegui.ui.BottomTab
import io.github.disktreegui.ui.DiskTreeState
import io.github.disktreegui.ui.SearchResultItem
import io.github.disktreegui.ui.ThemeMode
import kotlinx.coroutines.delay

@Composable
fun DiskTreeApp(filePickerLauncher: FilePickerLauncher? = null, onAppLaunch: ((DiskTreeState) -> Unit)? = null) {
    val state = remember { DiskTreeState() }
    val visibleNodes by rememberUpdatedState(flattenVisibleNodes(state.roots, state))
    val searching = state.appliedSearchQuery.isNotBlank()
    val searchResults by rememberUpdatedState(state.searchResults)
    val openFile = {
        filePickerLauncher?.open({ content, name -> state.loadFromFile(content, name) }, state::setError)
            ?: state.setError("当前平台暂不支持文件选择器")
    }

    LaunchedEffect(state) { state.restoreLastLoadedFile(); onAppLaunch?.invoke(state) }
    LaunchedEffect(state.searchQuery, state.roots.size) { delay(250); state.performSearch() }

    DiskTreeTheme(darkTheme = state.themeMode == ThemeMode.Dark) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                if (maxWidth >= 1400.dp) {
                    WideLayout(state, visibleNodes, searchResults, searching, openFile)
                } else {
                    CompactLayout(state, visibleNodes, searchResults, searching, openFile)
                }
            }
        }
    }
}

@Composable
private fun CompactLayout(state: DiskTreeState, visibleNodes: List<TreeNode>, searchResults: List<SearchResultItem>, searching: Boolean, onOpenFile: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth().padding(16.dp)) {
            if (state.activeTab == BottomTab.Files) FilesPane(state, visibleNodes, searchResults, searching)
            else SettingsPane(state.themeMode) { state.themeMode = it }
        }
        HorizontalDivider()
        Column(Modifier.fillMaxWidth().navigationBarsPadding()) {
            if (state.activeTab == BottomTab.Files) FilePane(state.loadedFileName, state.errorMessage, onOpenFile)
            NavigationBar {
                NavigationBarItem(state.activeTab == BottomTab.Files, { state.activeTab = BottomTab.Files }, { Icon(Icons.Filled.FolderOpen, null) }, label = { Text("文件") })
                NavigationBarItem(state.activeTab == BottomTab.Settings, { state.activeTab = BottomTab.Settings }, { Icon(Icons.Filled.Settings, null) }, label = { Text("设置") })
            }
        }
    }
}

@Composable
private fun WideLayout(state: DiskTreeState, visibleNodes: List<TreeNode>, searchResults: List<SearchResultItem>, searching: Boolean, onOpenFile: () -> Unit) {
    Row(Modifier.fillMaxSize()) {
        NavigationRail {
            NavigationRailItem(state.activeTab == BottomTab.Files, { state.activeTab = BottomTab.Files }, { Icon(Icons.Filled.FolderOpen, null) }, label = { Text("文件") })
            NavigationRailItem(state.activeTab == BottomTab.Settings, { state.activeTab = BottomTab.Settings }, { Icon(Icons.Filled.Settings, null) }, label = { Text("设置") })
        }
        VerticalDivider()
        if (state.activeTab == BottomTab.Files) {
            Row(Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                FilesPane(state, visibleNodes, searchResults, searching, Modifier.weight(1f))
                Card(
                    modifier = Modifier.widthIn(min = 320.dp, max = 360.dp).fillMaxHeight(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) { FilePane(state.loadedFileName, state.errorMessage, onOpenFile) }
            }
        } else {
            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.TopCenter) {
                Card(
                    modifier = Modifier.widthIn(max = 900.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) { SettingsPane(state.themeMode) { state.themeMode = it } }
            }
        }
    }
}

@Composable
private fun FilesPane(state: DiskTreeState, visibleNodes: List<TreeNode>, searchResults: List<SearchResultItem>, searching: Boolean, modifier: Modifier = Modifier) {
    val count = if (searching) searchResults.size else visibleNodes.size
    Column(modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("树形浏览", style = MaterialTheme.typography.titleLarge)
            if (state.roots.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Box(Modifier.clip(RoundedCornerShape(999.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 8.dp, vertical = 2.dp)) {
                    Text("$count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(state.loadedFileName?.let { "当前文件：$it" } ?: "尚未打开 Tree.py 导出的文本文件", color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (state.roots.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = state::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                label = { Text("搜索文件或路径") },
                placeholder = { Text("输入停止 250ms 后再搜索") }
            )
        }
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxSize(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            when {
                state.roots.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("暂无数据") }
                searching && searchResults.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("没有找到匹配项") }
                searching -> LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(searchResults, key = { it.node.id }) { SearchRow(it) }
                }
                else -> LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(visibleNodes, key = { it.id }) { TreeRow(it, state.isExpanded(it)) { state.toggle(it) } }
                }
            }
        }
    }
}

@Composable
private fun FilePane(fileName: String?, errorMessage: String?, onOpenFile: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("文件", style = MaterialTheme.typography.titleMedium)
        Text(fileName?.let { "已打开：$it" } ?: "选择 Tree.py 导出的文本文件并加载到上方树视图", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Button(onClick = onOpenFile) { Icon(Icons.Filled.UploadFile, null); Spacer(Modifier.width(8.dp)); Text(if (fileName != null) "更换文件" else "选择文件") }
        if (errorMessage != null) Text(errorMessage, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SettingsPane(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    val uriHandler = LocalUriHandler.current
    val projectUrl = "https://github.com/100pangci/DiskTree-GUI"
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("设置", style = MaterialTheme.typography.titleMedium)
        Text("主题模式", color = MaterialTheme.colorScheme.onSurfaceVariant)
        ThemeSwitch(themeMode, onThemeChange)
        HorizontalDivider()
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Filled.FolderOpen, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Text("DiskTree GUI", fontWeight = FontWeight.Bold)
            Text("版本 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("一个用于读取 Tree.py 导出文本的跨平台文件树浏览器。\nKotlin Multiplatform + Compose Multiplatform", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Text(projectUrl, color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline, modifier = Modifier.clickable { uriHandler.openUri(projectUrl) })
        }
    }
}

@Composable
private fun ThemeSwitch(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(999.dp)).background(MaterialTheme.colorScheme.surfaceVariant).border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(999.dp)).padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ThemeButton("深色", { Icon(Icons.Filled.DarkMode, null, modifier = Modifier.size(18.dp)) }, themeMode == ThemeMode.Dark, { onThemeChange(ThemeMode.Dark) }, Modifier.weight(1f))
        Box(Modifier.width(1.dp).height(24.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)))
        ThemeButton("浅色", { Icon(Icons.Filled.LightMode, null, modifier = Modifier.size(18.dp)) }, themeMode == ThemeMode.Light, { onThemeChange(ThemeMode.Light) }, Modifier.weight(1f))
    }
}

@Composable
private fun ThemeButton(label: String, icon: @Composable () -> Unit, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier.clip(RoundedCornerShape(999.dp)).clickable(onClick = onClick).background(bg).defaultMinSize(minHeight = 44.dp).padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
        CompositionLocalProvider(LocalContentColor provides fg) { icon() }
        Spacer(Modifier.width(8.dp))
        Text(label, color = fg, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}

private fun flattenVisibleNodes(nodes: List<TreeNode>, state: DiskTreeState): List<TreeNode> {
    val result = mutableListOf<TreeNode>()
    fun visit(node: TreeNode) {
        result += node
        if (state.isExpanded(node)) node.children.forEach(::visit)
    }
    nodes.forEach(::visit)
    return result
}

@Composable
private fun SearchRow(item: SearchResultItem) {
    val isDir = item.node.isDirectory || item.node.children.isNotEmpty()
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isDir) Icons.Filled.Folder else Icons.Filled.Description, null, tint = if (isDir) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(item.node.name, fontWeight = FontWeight.Medium, maxLines = 1)
        }
        if (item.parentPath.isNotBlank()) Text(item.parentPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        Text(item.fullPath, style = MaterialTheme.typography.labelSmall, color = if (item.isDirectMatch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
    }
}

@Composable
private fun TreeRow(node: TreeNode, expanded: Boolean, onToggle: () -> Unit) {
    val isDir = node.isDirectory || node.children.isNotEmpty()
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(enabled = node.children.isNotEmpty(), onClick = onToggle).background(MaterialTheme.colorScheme.surface).padding(start = (node.depth * 16).dp + 4.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (node.children.isNotEmpty()) Icon(if (expanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) else Spacer(Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Icon(if (isDir) if (expanded) Icons.Filled.FolderOpen else Icons.Filled.Folder else Icons.Filled.Description, null, tint = if (isDir) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(node.name, fontWeight = if (isDir) FontWeight.Medium else FontWeight.Normal, maxLines = 1)
    }
}