package com.apx.sc2brackets.utils

fun <T> MutableList<T>.replace(oldValue: T, newValue: T): Int{
    val index = indexOf(oldValue)
    if(index >= 0){
        removeAt(index)
        add(index, newValue)
    }
    return index
}