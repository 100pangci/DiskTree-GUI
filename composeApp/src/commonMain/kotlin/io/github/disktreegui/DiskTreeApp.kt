package io.github.disktreegui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.disktreegui.model.TreeNode
import io.github.disktreegui.theme.DiskTreeTheme
import io.github.disktreegui.ui.BottomTab
import io.github.disktreegui.ui.DiskTreeState
import io.github.disktreegui.ui.SearchResultItem
import io.github.disktreegui.ui.ThemeMode

@Composable
fun DiskTreeApp(
    filePickerLauncher: FilePickerLauncher? = null
) {
    val state = remember { DiskTreeState() }
    val visibleNodes by rememberUpdatedState(newValue = flattenVisibleNodes(state.roots, state))
    val searching = state.searchQuery.isNotBlank()
    val searchResults by rememberUpdatedState(newValue = state.searchResults)

    DiskTreeTheme(darkTheme = state.themeMode == ThemeMode.Dark) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                when (state.activeTab) {
                    BottomTab.Files -> {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("树形浏览", style = MaterialTheme.typography.titleLarge)
                                if (state.roots.isNotEmpty()) {
                                    Spacer(Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "${visibleNodes.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = state.loadedFileName?.let { "当前文件：$it" } ?: "尚未打开 Tree.py 导出的文本文件",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.roots.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = state.searchQuery,
                                    onValueChange = state::updateSearchQuery,
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true,
                                    leadingIcon = {
                                        Icon(Icons.Filled.Search, contentDescription = null)
                                    },
                                    label = { Text("搜索文件或路径") },
                                    placeholder = { Text("输入关键字后秒级筛选") }
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = if (searching) {
                                        "搜索到 ${searchResults.size} 条结果"
                                    } else {
                                        "当前共 ${visibleNodes.size} 个可见节点"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(12.dp))

                            if (state.roots.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.UploadFile,
                                                contentDescription = null,
                                                modifier = Modifier.size(56.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            )
                                            Text(
                                                "暂无数据",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                            Text(
                                                "在下方点击「选择文件」\n打开 Tree.py 导出的文本",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            } else {
                                Card(
                                    modifier = Modifier.fillMaxSize(),
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    if (searching && searchResults.isEmpty()) {
                                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "没有找到匹配项",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else if (searching) {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            items(searchResults, key = { it.node.id }) { item ->
                                                SearchResultRow(item)
                                            }
                                        }
                                    } else {
                                        LazyColumn(
                                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            items(visibleNodes, key = { it.id }) { node ->
                                                TreeRow(
                                                    node = node,
                                                    expanded = state.isExpanded(node),
                                                    onToggle = { state.toggle(node) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BottomTab.Settings -> {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            SettingsTabContent(
                                themeMode = state.themeMode,
                                onThemeChange = { state.themeMode = it }
                            )
                        }
                    }

                }
                HorizontalDivider()

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    when (state.activeTab) {
                        BottomTab.Files -> FileTabContent(
                            fileName = state.loadedFileName,
                            errorMessage = state.errorMessage,
                            onOpenFile = {
                                filePickerLauncher?.open(
                                    { content, name -> state.loadFromFile(content, name) },
                                    state::setError
                                ) ?: state.setError("当前平台暂不支持文件选择器")
                            }
                        )

                        BottomTab.Settings -> Spacer(Modifier.height(0.dp))
                    }

                    NavigationBar {
                        NavigationBarItem(
                            selected = state.activeTab == BottomTab.Files,
                            onClick = { state.activeTab = BottomTab.Files },
                            icon = { Icon(Icons.Filled.FolderOpen, contentDescription = "文件") },
                            label = { Text("文件") }
                        )
                        NavigationBarItem(
                            selected = state.activeTab == BottomTab.Settings,
                            onClick = { state.activeTab = BottomTab.Settings },
                            icon = { Icon(Icons.Filled.Settings, contentDescription = "设置") },
                            label = { Text("设置") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FileTabContent(
    fileName: String?,
    errorMessage: String?,
    onOpenFile: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("文件", style = MaterialTheme.typography.titleMedium)
        Text(
            text = fileName?.let { "已打开：$it" } ?: "选择 Tree.py 导出的文本文件并加载到上方树视图",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Button(onClick = onOpenFile) {
            Icon(
                imageVector = Icons.Filled.UploadFile,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(if (fileName != null) "更换文件" else "选择文件")
        }
        if (errorMessage != null) {
            Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun SettingsTabContent(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.titleMedium)
        Text(
            "主题模式",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ThemeSegmentedControl(
            selectedMode = themeMode,
            onThemeChange = onThemeChange
        )

        Spacer(Modifier.height(8.dp))
        HorizontalDivider()
        Spacer(Modifier.height(8.dp))

        // 关于区块
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text("DiskTree GUI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("版本 1.0.0", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "一个用于读取 Tree.py 导出文本的跨平台文件树浏览器。\nKotlin Multiplatform + Compose Multiplatform",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Text(
                "https://github.com/100pangci/DiskTree-GUI",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ThemeSegmentedControl(
    selectedMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(999.dp)
            )
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ThemeSegmentButton(
            label = "深色",
            icon = { Icon(Icons.Filled.DarkMode, contentDescription = null, modifier = Modifier.size(18.dp)) },
            selected = selectedMode == ThemeMode.Dark,
            onClick = { onThemeChange(ThemeMode.Dark) },
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        )
        ThemeSegmentButton(
            label = "浅色",
            icon = { Icon(Icons.Filled.LightMode, contentDescription = null, modifier = Modifier.size(18.dp)) },
            selected = selectedMode == ThemeMode.Light,
            onClick = { onThemeChange(ThemeMode.Light) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ThemeSegmentButton(
    label: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick)
            .background(containerColor)
            .defaultMinSize(minHeight = 44.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides contentColor
        ) {
            icon()
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = label,
            color = contentColor,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

private fun flattenVisibleNodes(nodes: List<TreeNode>, state: DiskTreeState): List<TreeNode> {
    val result = mutableListOf<TreeNode>()

    fun visit(node: TreeNode) {
        result += node
        if (state.isExpanded(node)) {
            node.children.forEach(::visit)
        }
    }

    nodes.forEach(::visit)
    return result
}

@Composable
private fun SearchResultRow(item: SearchResultItem) {
    val node = item.node
    val isDir = node.isDirectory || node.children.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isDir) Icons.Filled.Folder else Icons.Filled.Description,
                contentDescription = null,
                tint = if (isDir) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = node.name,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }

        if (item.parentPath.isNotBlank()) {
            Text(
                text = item.parentPath,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }

        Text(
            text = item.fullPath,
            style = MaterialTheme.typography.labelSmall,
            color = if (item.isDirectMatch) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun TreeRow(node: TreeNode, expanded: Boolean, onToggle: () -> Unit) {
    val isDir = node.isDirectory || node.children.isNotEmpty()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = node.children.isNotEmpty(), onClick = onToggle)
            .background(MaterialTheme.colorScheme.surface)
            .padding(start = (node.depth * 16).dp + 4.dp, top = 8.dp, bottom = 8.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 展开/折叠箭头占位（文件不显示箭头，但需要占位对齐）
        if (node.children.isNotEmpty()) {
            Icon(
                imageVector = if (expanded) Icons.Filled.ExpandMore else Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Spacer(Modifier.size(18.dp))
        }
        Spacer(Modifier.width(4.dp))
        Icon(
            imageVector = if (isDir) {
                if (expanded) Icons.Filled.FolderOpen else Icons.Filled.Folder
            } else {
                Icons.Filled.Description
            },
            contentDescription = null,
            tint = if (isDir) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = node.name,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isDir) FontWeight.Medium else FontWeight.Normal,
            maxLines = 1
        )
    }
}
