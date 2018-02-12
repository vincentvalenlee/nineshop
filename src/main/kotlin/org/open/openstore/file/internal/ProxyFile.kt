package org.open.openstore.file.internal

import org.open.openstore.file.FileObject

/**
 * 代理到实际文件的文件对象。他是一个抽象类，必须有子类实现此代理文件对象；同时，
 * 子类也可以重写文件对象的方法，例如：对于链接文件，可以有自己独立的文件名、文件
 * 元属性、文件标签等
 */
abstract class ProxyFile(val proxied: FileObject): FileObject by proxied {}