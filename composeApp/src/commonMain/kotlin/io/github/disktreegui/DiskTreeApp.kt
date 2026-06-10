package io.github.disktreegui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    Box(
        Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                )
            )
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp)) {
                if (state.activeTab == BottomTab.Files) {
                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        CompactHeroSection(state.loadedFileName, visibleNodes.size, searchResults.size, searching)
                        CompactFilePanel(state.loadedFileName, state.errorMessage, onOpenFile)
                        FilesPane(state, visibleNodes, searchResults, searching, Modifier.weight(1f), showFileSummary = true)
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                    ) {
                        SettingsPane(state.themeMode) { state.themeMode = it }
                    }
                }
            }
            Spacer(Modifier.height(96.dp))
        }

        FloatingBottomBar(
            activeTab = state.activeTab,
            onFilesClick = { state.activeTab = BottomTab.Files },
            onSettingsClick = { state.activeTab = BottomTab.Settings },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
}

@Composable
private fun CompactFilePanel(fileName: String?, errorMessage: String?, onOpenFile: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        FilePane(fileName, errorMessage, onOpenFile)
    }
}

@Composable
private fun WideLayout(state: DiskTreeState, visibleNodes: List<TreeNode>, searchResults: List<SearchResultItem>, searching: Boolean, onOpenFile: () -> Unit) {
    Row(
        Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    )
                )
            )
            .padding(24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DesktopSidebar(state, Modifier.width(104.dp).fillMaxHeight())
        if (state.activeTab == BottomTab.Files) {
            Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                DesktopHeroSection(state.loadedFileName, visibleNodes.size, searchResults.size, searching)
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    FilesPane(state, visibleNodes, searchResults, searching, Modifier.weight(1f), showFileSummary = false)
                    DesktopQuickPanel(state.loadedFileName, state.errorMessage, onOpenFile, Modifier.widthIn(min = 340.dp, max = 380.dp).fillMaxHeight())
                }
            }
        } else {
            Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.TopCenter) {
                Card(
                    modifier = Modifier.fillMaxWidth().widthIn(max = 960.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                ) { SettingsPane(state.themeMode) { state.themeMode = it } }
            }
        }
    }
}

@Composable
private fun FilesPane(
    state: DiskTreeState,
    visibleNodes: List<TreeNode>,
    searchResults: List<SearchResultItem>,
    searching: Boolean,
    modifier: Modifier = Modifier,
    showFileSummary: Boolean = true
) {
    val count = if (searching) searchResults.size else visibleNodes.size
    Column(modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("树形浏览", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            if (state.roots.isNotEmpty()) {
                Spacer(Modifier.width(8.dp))
                Box(Modifier.clip(RoundedCornerShape(999.dp)).background(MaterialTheme.colorScheme.primaryContainer).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text("$count", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        if (showFileSummary) {
            Spacer(Modifier.height(8.dp))
            Text(state.loadedFileName?.let { "当前文件：$it" } ?: "尚未打开 Tree.py 导出的文本文件", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (state.roots.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = state::updateSearchQuery,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                label = { Text("搜索文件或路径") },
                placeholder = { Text("输入停止 250ms 后再搜索") },
                shape = RoundedCornerShape(18.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Card(
            Modifier.fillMaxSize(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
        ) {
            when {
                state.roots.isEmpty() -> EmptyState("暂无数据", "先从右侧导入 Tree.py 导出的文本，然后我会在这里构建可展开的文件树。")
                searching && searchResults.isEmpty() -> EmptyState("没有找到匹配项", "试试更短的关键词，或者按目录名、盘符、文件后缀来搜。")
                searching -> LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(searchResults, key = { it.node.id }) {
                        SearchRow(it, selected = state.isSelected(it.node)) { state.revealNode(it.node) }
                    }
                }
                else -> LazyColumn(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(visibleNodes, key = { it.id }) {
                        TreeRow(
                            node = it,
                            expanded = state.isExpanded(it),
                            selected = state.isSelected(it),
                            onToggle = { state.toggle(it) },
                            onSelect = { state.selectNode(it) }
                        )
                    }
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
        Button(onClick = onOpenFile, shape = RoundedCornerShape(16.dp), contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp)) {
            Icon(Icons.Filled.UploadFile, null)
            Spacer(Modifier.width(8.dp))
            Text(if (fileName != null) "更换文件" else "选择文件")
        }
        if (errorMessage != null) Text(errorMessage, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SettingsPane(themeMode: ThemeMode, onThemeChange: (ThemeMode) -> Unit) {
    val uriHandler = LocalUriHandler.current
    val projectUrl = "https://github.com/100pangci/DiskTree-GUI"
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
        Text("设置", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text("主题模式", color = MaterialTheme.colorScheme.onSurfaceVariant)
        ThemeSwitch(themeMode, onThemeChange)
        HorizontalDivider()
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { uriHandler.openUri(projectUrl) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.FolderOpen, null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Text("DiskTree GUI", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text("版本 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("一个用于读取 Tree.py 导出文本的跨平台文件树浏览器。\nKotlin Multiplatform + Compose Multiplatform", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            Text("点击上方图标访问项目主页", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
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
private fun SearchRow(item: SearchResultItem, selected: Boolean, onClick: () -> Unit) {
    val isDir = item.node.isDirectory || item.node.children.isNotEmpty()
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val backgroundColor = when {
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        hovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    }
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
        hovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
        else -> Color.Transparent
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .hoverable(interactionSource)
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(if (isDir) Icons.Filled.Folder else Icons.Filled.Description, null, tint = if (isDir) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(item.node.name, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (item.parentPath.isNotBlank()) Text(item.parentPath, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(item.fullPath, style = MaterialTheme.typography.labelSmall, color = if (item.isDirectMatch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun TreeRow(node: TreeNode, expanded: Boolean, selected: Boolean, onToggle: () -> Unit, onSelect: () -> Unit) {
    val isDir = node.isDirectory || node.children.isNotEmpty()
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val backgroundColor = when {
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        hovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
        hovered -> MaterialTheme.colorScheme.primary.copy(alpha = 0.20f)
        else -> Color.Transparent
    }
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .hoverable(interactionSource)
            .clickable {
                onSelect()
                if (node.children.isNotEmpty()) onToggle()
            }
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
            .padding(start = (node.depth * 16).dp + 8.dp, top = 10.dp, bottom = 10.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(3.dp)
                .height(24.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
        )
        Spacer(Modifier.width(8.dp))
        if (node.children.isNotEmpty()) Icon(if (expanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp)) else Spacer(Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Icon(if (isDir) if (expanded) Icons.Filled.FolderOpen else Icons.Filled.Folder else Icons.Filled.Description, null, tint = if (isDir) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(node.name, fontWeight = if (isDir) FontWeight.Medium else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DesktopSidebar(state: DiskTreeState, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Column(
            Modifier.fillMaxSize().padding(vertical = 22.dp, horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.AccountTree, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            }
            DesktopNavButton("文件", Icons.Filled.FolderOpen, state.activeTab == BottomTab.Files) { state.activeTab = BottomTab.Files }
            DesktopNavButton("设置", Icons.Filled.Settings, state.activeTab == BottomTab.Settings) { state.activeTab = BottomTab.Settings }
            Spacer(Modifier.weight(1f))
            Text("桌面版", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DesktopNavButton(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(bg)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = fg)
        Text(label, color = fg, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
    }
}

@Composable
private fun DesktopHeroSection(fileName: String?, visibleCount: Int, resultCount: Int, searching: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
        )
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("DiskTree Desktop", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(
                    fileName?.let { "正在浏览：$it" } ?: "导入 Tree.py 导出的文本后，这里会提供更适合大屏的浏览体验。",
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DesktopMetricCard("可见节点", visibleCount.toString())
                DesktopMetricCard(if (searching) "搜索结果" else "当前状态", if (searching) resultCount.toString() else "就绪")
            }
        }
    }
}

@Composable
private fun DesktopMetricCard(label: String, value: String) {
    Column(
        Modifier
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.58f))
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DesktopQuickPanel(fileName: String?, errorMessage: String?, onOpenFile: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("快速操作", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            Text("导入、替换文件，以及查看当前状态。这个区域在大屏下会保持稳定的控制面板体验。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilePane(fileName, errorMessage, onOpenFile)
        }
    }
}

@Composable
private fun EmptyState(title: String, description: String) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.FolderOpen, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(8.dp))
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun CompactHeroSection(fileName: String?, visibleCount: Int, resultCount: Int, searching: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f))
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("DiskTree Mobile", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(
                fileName?.let { "当前文件：$it" } ?: "导入 Tree.py 文本后，就能在手机上更轻盈地浏览目录树。",
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.84f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                CompactMetricChip("节点", visibleCount.toString())
                CompactMetricChip(if (searching) "结果" else "状态", if (searching) resultCount.toString() else "就绪")
            }
        }
    }
}

@Composable
private fun CompactMetricChip(label: String, value: String) {
    Row(
        Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.55f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun FloatingBottomBar(activeTab: BottomTab, onFilesClick: () -> Unit, onSettingsClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.shadow(20.dp, RoundedCornerShape(28.dp), clip = false),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
                        )
                    )
                )
                .border(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.24f),
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
                        )
                    ),
                    RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingBottomBarItem("文件", Icons.Filled.FolderOpen, activeTab == BottomTab.Files, onFilesClick, Modifier.weight(1f))
            FloatingBottomBarItem("设置", Icons.Filled.Settings, activeTab == BottomTab.Settings, onSettingsClick, Modifier.weight(1f))
        }
    }
}

@Composable
private fun FloatingBottomBarItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(label, color = fg, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium)
    }
}