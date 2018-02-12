package org.open.openstore.file

/**
 * 描述贡献点、贡献者发送变化的信息。通常被注册表事件使用
 */
interface IContributChange {
    companion object {
        val ADDED:Int = 0

        val REMOVED:Int = 1
    }

    fun getPoint():IContributPoint

    fun getContributor():IContributor

    fun getType():Int
}

