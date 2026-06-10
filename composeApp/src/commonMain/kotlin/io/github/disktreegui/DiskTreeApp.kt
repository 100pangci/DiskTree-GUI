package io.github.disktreegui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.disktreegui.model.TreeNode
import io.github.disktreegui.theme.DiskTreeTheme
import io.github.disktreegui.ui.BottomTab
import io.github.disktreegui.ui.DiskTreeState
import io.github.disktreegui.ui.ThemeMode

@Composable
fun DiskTreeApp(
    filePickerLauncher: FilePickerLauncher? = null
) {
    val state = remember { DiskTreeState() }
    val visibleNodes by rememberUpdatedState(newValue = flattenVisibleNodes(state.roots, state))

    DiskTreeTheme(darkTheme = state.themeMode == ThemeMode.Dark) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text("树形浏览", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.loadedFileName?.let { "当前文件：$it" } ?: "尚未打开 Tree.py 导出的文本文件",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    if (state.roots.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("请在下方“文件”页签中点击打开文件")
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxSize(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
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

                        BottomTab.Settings -> SettingsTabContent(
                            themeMode = state.themeMode,
                            onThemeChange = { state.themeMode = it }
                        )
                    }

                    NavigationBar {
                        NavigationBarItem(
                            selected = state.activeTab == BottomTab.Files,
                            onClick = { state.activeTab = BottomTab.Files },
                            icon = { Text("文件") },
                            label = { Text("文件") }
                        )
                        NavigationBarItem(
                            selected = state.activeTab == BottomTab.Settings,
                            onClick = { state.activeTab = BottomTab.Settings },
                            icon = { Text("设置") },
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
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("文件", style = MaterialTheme.typography.titleMedium)
        Text(
            text = fileName?.let { "已打开：$it" } ?: "选择 Tree.py 导出的文本文件并加载到上方树视图",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(onClick = onOpenFile) {
            Text("点击打开文件")
        }
        if (errorMessage != null) {
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SettingsTabContent(
    themeMode: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("设置", style = MaterialTheme.typography.titleMedium)
        Text(
            "主题模式",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        ThemeOptionRow(
            label = "深色",
            selected = themeMode == ThemeMode.Dark,
            onClick = { onThemeChange(ThemeMode.Dark) }
        )
        ThemeOptionRow(
            label = "浅色",
            selected = themeMode == ThemeMode.Light,
            onClick = { onThemeChange(ThemeMode.Light) }
        )
    }
}

@Composable
private fun ThemeOptionRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Spacer(Modifier.width(8.dp))
        Text(label)
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
private fun TreeRow(node: TreeNode, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = node.children.isNotEmpty(), onClick = onToggle)
            .padding(start = (node.depth * 20).dp, top = 6.dp, bottom = 6.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = when {
                node.children.isEmpty() -> "•"
                expanded -> "▼"
                else -> "▶"
            },
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(node.name, color = MaterialTheme.colorScheme.onSurface)
    }
}