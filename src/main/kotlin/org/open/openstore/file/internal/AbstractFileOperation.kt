package org.open.openstore.file.internal

import org.open.openstore.file.FileObject
import org.open.openstore.file.FileOperation
import org.open.openstore.file.FileOperations
import kotlin.reflect.KClass

/**
 * 文件额外操作的抽象类
 */
abstract class AbstractFileOperation(val file: FileObject): FileOperation {}

/**
 * 从文件对象所在的系统中获取operations贡献点的默认实现
 */
class DefaultFileOperations(val file: FileObject): FileOperations {
    override fun getOperations(): Array<KClass<out FileOperation>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getOperation(operationClass: KClass<out FileOperation>): FileOperation? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hasOperation(operationClass: KClass<out FileOperation>): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}