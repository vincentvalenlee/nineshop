package org.open.openstore.file.internal

import org.open.openstore.file.*
import org.open.openstore.file.contributors.getAttribute
import kotlin.reflect.KClass

/**
 * 文件额外操作的抽象类
 */
abstract class AbstractFileOperation(): FileOperation {
    var file:FileObject? = null
}

/**
 * 从文件对象所在的系统中获取operations贡献点的默认实现
 */
class DefaultFileOperations(val file: FileObject): FileOperations {
    override fun getOperations(): Array<KClass<out FileOperation>> {
        val contributor = FilePlatform.getContributRegistry().getContributor(FilePlatform.CONTRIBUT_POINT_FILE_OPERA,
                file.getAttribute(FileObject.META_FILE_CONTENT_TYPE) as String)
        return contributor.getConfig().filter {
            it.getName() == "operations"
        }.map {
            it.createExecutable("class")!!.javaClass.kotlin as KClass<FileOperation>
        }.toTypedArray()
    }

    override fun getOperation(operationClass: KClass<out FileOperation>): FileOperation? {
        val contributor = FilePlatform.getContributRegistry().getContributor(FilePlatform.CONTRIBUT_POINT_FILE_OPERA,
                file.getAttribute(FileObject.META_FILE_CONTENT_TYPE) as String)
        return contributor.getConfig().filter {
            it.getName() == "operations"
        }.firstOrNull {
            it.getAttr("class") == operationClass.qualifiedName
        }?.let {
            it.createExecutable("class") as FileOperation
        }?:null
    }

    override fun hasOperation(operationClass: KClass<out FileOperation>): Boolean {
        val contributor = FilePlatform.getContributRegistry().getContributor(FilePlatform.CONTRIBUT_POINT_FILE_OPERA,
                file.getAttribute(FileObject.META_FILE_CONTENT_TYPE) as String)
        return contributor.getConfig().filter {
            it.getName() == "operations"
        }.filter {
            it.getAttr("class") == operationClass.qualifiedName
        }.isNotEmpty()
    }

}