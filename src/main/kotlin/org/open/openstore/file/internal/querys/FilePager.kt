package org.open.openstore.file.internal.querys

import com.google.gson.JsonObject


/**
 * 文件查询的分页器
 */
interface FilePager {

    /**
     * 当前页号
     */
    fun current():Long

    /**
     * 页大小
     */
    val size:Long

    /**
     * 数据总数，
     */
    fun total():Long

    /**
     * 下一页
     */
    fun  next():FilePager

    /**
     * 上一页
     */
    fun  pre():FilePager

    /**
     * 第一页
     */
    fun first():FilePager

    /**
     * 最后一页
     */
    fun last():FilePager

    /**
     * 跳转到指定页
     */
    fun jumpTo(pageNum:Long): FilePager

    /**
     * 获取分页内，指定索引的数据[0<i<size-1]
     */
    operator fun get(i:Int): JsonObject

    /**
     * 获取本页内所有数据
     */
    fun allData():Array<JsonObject>
}