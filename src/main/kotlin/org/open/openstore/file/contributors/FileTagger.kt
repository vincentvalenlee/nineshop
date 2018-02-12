package org.open.openstore.file.contributors

import org.open.openstore.file.FileObject

/**
 * 为文件对象打Tag标签的标签器，他负责文件对象标签的管理。标签只能追加、去除，不能修改！
 */
interface FileTagger {

    /**
     * 使用标签全名为文件打标签
     */
    fun tag(file: FileObject, tagFullName:String)

    fun tag(file: FileObject, tag:Tag)

    /**
     * 擦除标签，如果标签为空，则将文件所有tag擦除
     */
    fun erase(file: FileObject, tagFullName:String = "")

    fun erase(file: FileObject, tag:Tag)

    /**
     * 在标签库中加入标签
     */
    fun addTag(tag: Tag)

    /**
     * 获取文件对象所有标签
     */
    fun allTag(file:FileObject): Array<Tag>

    /**
     * 匹配所有指定标签，指定范围（begin~end）的文件对象
     */
    fun matchs(range:LongRange,vararg tagNames: String):Array<FileObject>
}

/**
 * 标签不仅具有名称，还具有分类信息，例如：娱乐，社交，物联网等。同时，标签还具有版本信息，例如web1.0，web2.0，工业4.0等。
 * 标签可以按照以下字符描述，类别：标签内容：版本号（其中版本号可为空）
 */
data class Tag(val category: String, val content: String, val version: String = "") {
    override fun toString(): String = if (version.isEmpty()) "$category:$content" else "$category:$content:$version"
}