package net.mrgaabriel.currencytracker.utils

fun String.fancy(): String {
    return "${this[0].toUpperCase()}${this.toMutableList().apply { removeAt(0) }.joinToString("").toLowerCase()}"
}