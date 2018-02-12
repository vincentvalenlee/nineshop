package org.open.openstore.file

/**
 * 使用文件资源的调用者、应用，可能不是以路径、层次的方式使用资源，例如app首页，桌面等。此时
 * 应该创建一个FlatView视图，文件视图，只会将符合条件的“文件”对象（非fold）放入视图中。
 */
interface FlatView {

    /**
     * 添加视图中的过滤选择（只筛选出文件）
     */
    fun addSelector(selector:FileSelector):FlatView

    fun getName():String

    fun refresh():Unit

    /**
     * 触发视图改变事件，可以使用rxjava包装
     */
    fun fireChanged(type:Int, event:Any):Unit

}