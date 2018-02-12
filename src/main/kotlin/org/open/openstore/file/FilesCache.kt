package org.open.openstore.file

/**
 * 文件缓存对象，用于在文件仓库中缓存文件对象

 */
interface FilesCache {

    fun add(file: FileObject, cover:Boolean = false)

    fun getFile(filesystem: FileSystem, name: IFileName): FileObject

    fun clear(fileSystem: FileSystem)

    fun close()

    fun removeFile(filesystem: FileSystem, name: IFileName)

}

interface CacheProvider {

    fun getCache():FilesCache

    fun getContributPoint(pointid:String):IContributPoint
}



