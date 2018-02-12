package org.open.openstore.file

import com.google.gson.JsonObject


/**
 * 描述文件元数据的类，他可以包括自定义属性。文件元信息包含以下两部分：
 * <p>1.只读部分：文件创建者、创建时间等
 * <p>2.可写部分：文件大小、文件权限、自定义元属性
 * 每个部分都使用JsonObject对象表示
 */
interface FileMeta {

    fun getReadable(): JsonObject

    fun getWriteable():JsonObject

    fun getFile():FileObject

    /**
     * 用指定的元数据，注解文件
     */
    fun annoted(meta:JsonObject)
}