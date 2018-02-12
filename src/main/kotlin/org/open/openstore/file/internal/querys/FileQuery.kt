package org.open.openstore.file.internal.querys

import org.open.openstore.file.FileObject

/**
 * 文件查询接口，用于在文件集合（大规模）中检索符合条件的文件对象。他不同于FileSelector接口，
 * Selector接口注重对单个文件对象的访问逻辑，而query接口注重根据条件，在一个大型集合中检索需要的数据
 */
interface FileQuery {

    /**
     * 在指定索引中按照条件搜索。
     * <p>注意：搜索器不根据id查询，如果仅仅根据id查询，请使用[Indices.getData]方法
     */
    fun search(index: FileIndex, filter: FileQueryFilter, pager: FilePager):Array<FileObject>

    /**
     * 在多个索引中按照条件搜索。
     * <p>注意：搜索器不根据id查询，如果仅仅根据id查询，请使用[Indices.getData]方法
     */
    fun mulitSearch(index: Array<FileIndex>, filter: FileQueryFilter, pager: FilePager): Array<FileObject>

    /**
     * 计算某个index索引中数据的数量
     */
    fun count(index: FileIndex, filter: FileQueryFilter):Long

}

/**
 * 文件索引对象，可以额外的设置index名称和类型。否则默认使用文件对象的信息
 */
data class FileIndex(val file: FileObject, val index:String = "", val type:String = "")