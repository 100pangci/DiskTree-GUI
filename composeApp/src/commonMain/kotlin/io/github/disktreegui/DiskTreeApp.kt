package io.github.disktreegui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.disktreegui.model.TreeNode
import io.github.disktreegui.ui.DiskTreeState

@Composable
fun DiskTreeApp() {
    val state = remember { DiskTreeState() }
    val visibleNodes by rememberUpdatedState(newValue = flattenVisibleNodes(state.roots, state))

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Tree.py 导出文本", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .background(Color(0xFFF6F6F6), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        val scrollState = rememberScrollState()
                        BasicTextField(
                            value = state.inputText,
                            onValueChange = { state.inputText = it },
                            modifier = Modifier.fillMaxSize().verticalScroll(scrollState),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = state::parseInput) {
                        Text("解析并展开")
                    }
                    state.errorMessage?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("树形浏览", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    if (state.roots.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0xFFF6F6F6), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("把 Tree.py 导出的文本粘贴到左侧，然后点击“解析并展开”")
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().background(Color(0xFFF6F6F6), RoundedCornerShape(12.dp)).padding(8.dp)
                        ) {
                            items(visibleNodes, key = { it.id }) { node ->
                                TreeRow(node = node, expanded = state.isExpanded(node), onToggle = { state.toggle(node) })
                            }
                        }
                    }
                }
            }
        }
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
            }
        )
        Spacer(Modifier.width(8.dp))
        Text(node.name)
    }
}