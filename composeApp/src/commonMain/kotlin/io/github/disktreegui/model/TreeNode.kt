package io.github.disktreegui.model

data class TreeNode(
    val id: String,
    val name: String,
    val depth: Int,
    val isDirectory: Boolean = true,
    val message: String? = null,
    val children: MutableList<TreeNode> = mutableListOf()
)