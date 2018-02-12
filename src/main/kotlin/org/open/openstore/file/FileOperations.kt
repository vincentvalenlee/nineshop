package org.open.openstore.file

import kotlin.reflect.KClass

/**
 * FileOperation文件操作对象的注册表集合接口
 */
interface FileOperations {


    fun getOperations(): Array<KClass<out FileOperation>>


    fun getOperation(operationClass: KClass<out FileOperation>): FileOperation?


    fun hasOperation(operationClass: KClass<out FileOperation>): Boolean

}

/**
 * 文件操作对象，用于文件对象的额外处理，例如版本控制、权限限制等
 */
interface FileOperation {

    fun process()
}

/**
 * 提供文件操作对象的工厂，由文件仓库提供。他是贡献点：org.xctrl.xfilesystem.fileoperations.factorys上的一个贡献者
 */
interface FileOperationFactory {

    fun getOperation(file: FileObject):Collection<KClass<out FileOperation>>

    fun getOperation(operationClass: KClass<out FileOperation>, file: FileObject): FileOperation
}