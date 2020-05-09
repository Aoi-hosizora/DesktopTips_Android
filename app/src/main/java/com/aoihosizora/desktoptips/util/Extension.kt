package com.aoihosizora.desktoptips.util

fun <T> MutableList<T>.swap(i: Int, j: Int) {
    val fromI = get(i)
    val fromJ = get(j)
    set(i, fromJ)
    set(j, fromI)
}
