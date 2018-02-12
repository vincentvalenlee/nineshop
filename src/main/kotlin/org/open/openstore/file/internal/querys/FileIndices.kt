package org.open.openstore.file.internal.querys

import com.google.gson.JsonObject
import org.open.openstore.file.FileObject

/**
 * 索引器对象，封装对文件系统文件的索引化过程
 * <p>注意：id搜索直接通过索引器进行，多条件搜索则根据Searcher搜索器进行
 */
interface FileIndices {

    /**
     * 将指定对象索引化，索引名，索引类型名，要索引的数据
     */
    fun index(file: FileIndex)

    /**
     * 使用指定id字段索引化，如果id已经存在，则将更新索引
     */
    fun index(idField: String, file: FileIndex)

    /**
     * 判断指定索引中是否存在指定id的数据
     */
    fun isExist(file: FileIndex):Boolean

    /**
     * 删除指定id的索引
     */
    fun rmIndex(file: FileIndex)

    /**
     * 将指定的信息加入文件已经存在的索引中
     */
    fun addToIndex(file: FileIndex, extendDoc: JsonObject)

    /**
     * 批量将指定对象数据索引化
     */
    fun bulkIndex(file: FileIndex, doc:Array<JsonObject>)

    /**
     * 批量删除指定id的索引
     */
    fun bulkRmIndex(file: FileIndex, ids: Array<String>)

    /**
     * 从索引中检索指定id的文件索引数据， ，返回的数据使用json表示，并非绑定FileObject
     */
    fun getData(file: FileIndex, vararg fids: Array<String>):Array<JsonObject>

    /**
     * 从索引中获取指定id的文件索引数据
     */
    fun getData(file: FileIndex, fid: String): JsonObject
}