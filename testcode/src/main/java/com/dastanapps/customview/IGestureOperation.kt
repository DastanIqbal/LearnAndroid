package com.dastanapps.customview

interface IGestureOperation {
    fun onSelect(itemObj: GestureView, tag: String)
    fun onDelete(tag: String)
    //fun onEdit(tag: String)
}