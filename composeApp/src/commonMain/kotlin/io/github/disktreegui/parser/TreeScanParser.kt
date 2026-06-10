package io.github.disktreegui.parser

import io.github.disktreegui.model.TreeNode

object TreeScanParser {
    private val branchMarkers = listOf("├── ", "└── ")
    private val ignoredPrefixes = listOf("=", " 文件树扫描报告", " 扫描时间:", " 扫描位置:", " 深度限制:")

    fun parse(content: String): List<TreeNode> {
        val roots = mutableListOf<TreeNode>()
        val stack = mutableListOf<TreeNode>()

        content.lines()
            .map { it.trimEnd() }
            .filter { it.isNotBlank() }
            .filterNot { line -> ignoredPrefixes.any { prefix -> line.startsWith(prefix) } }
            .forEach { line ->
                if (line.startsWith("磁盘/挂载点: ")) {
                    val mount = normalizeNodeName(line.removePrefix("磁盘/挂载点: "))
                    val node = TreeNode(id = mount, name = mount, depth = 0)
                    roots += node
                    stack.clear()
                    stack += node
                    return@forEach
                }

                if (stack.isNotEmpty() && normalizeNodeName(line) == stack.first().name) {
                    return@forEach
                }

                val markerIndex = branchMarkers.map { marker -> line.indexOf(marker) }
                    .firstOrNull { it >= 0 }

                if (markerIndex == null) {
                    return@forEach
                }

                val depth = computeDepth(line, markerIndex) + 1
                val rawName = line.substring(markerIndex + 4).trim()
                val name = normalizeNodeName(rawName)
                val isDir = rawName.endsWith("/") || rawName.endsWith("\\") || !rawName.contains(".")
                val node = TreeNode(
                    id = buildId(stack, name, depth),
                    name = name,
                    depth = depth,
                    isDirectory = isDir
                )

                while (stack.size > depth) {
                    stack.removeAt(stack.lastIndex)
                }

                val parent = stack.lastOrNull()
                if (parent == null) {
                    roots += node
                    stack.clear()
                    stack += node
                } else {
                    parent.children += node
                    stack += node
                }
            }

        return roots.sortedTree()
    }

    private fun computeDepth(line: String, markerIndex: Int): Int {
        val prefix = line.substring(0, markerIndex)
        return prefix.chunked(4).count()
    }

    private fun normalizeNodeName(raw: String): String {
        val normalized = raw.trim().replace('\\', '/')
        return if (normalized.length > 1) normalized.trimEnd('/') else normalized
    }

    private fun buildId(stack: List<TreeNode>, name: String, depth: Int): String {
        val parentPath = stack.take(depth + 1).joinToString("/") { it.name }
        return "$parentPath/$name"
    }

    private fun List<TreeNode>.sortedTree(): List<TreeNode> =
        sortedWith(treeNodeComparator()).map { node ->
            node.copy(children = node.children.sortedTree().toMutableList())
        }

    private fun treeNodeComparator(): Comparator<TreeNode> = compareBy<TreeNode>(
        { !it.isDirectory },
        { it.name.lowercase() }
    )
}