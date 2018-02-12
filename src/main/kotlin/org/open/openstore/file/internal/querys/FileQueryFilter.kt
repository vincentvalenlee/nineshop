package org.open.openstore.file.internal.querys


/**
 * Created by Administrator on 2018/1/23 0023.
 */
interface FileQueryFilter {

    fun and(filter: FileQueryFilter): FileQueryFilter

    fun or(filter: FileQueryFilter): FileQueryFilter

    fun not(field:String, value: Any): FileQueryFilter

    fun withEq(field:String, value: Any?): FileQueryFilter

    fun withGT(field:String, gtVal:Long): FileQueryFilter

    fun withGTE(field:String, gteVal:Long): FileQueryFilter

    fun withLT(field:String, ltVal: Long): FileQueryFilter

    fun withLTE(field:String, lteVal: Long): FileQueryFilter

    fun exists(field:String): FileQueryFilter

    fun notExists(field:String): FileQueryFilter

    fun order(field:String, desc:Boolean = true): FileQueryFilter

    fun acceptSearcher(searcher: FileQuery)
}