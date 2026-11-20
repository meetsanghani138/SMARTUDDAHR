package com.marwadiuniversity.radhe.utils

import android.database.Cursor

fun Cursor.getStringByColumn(columnName: String): String {
    val index = getColumnIndex(columnName)
    return if (index != -1 && !isNull(index)) getString(index) else ""
}

fun Cursor.getLongByColumn(columnName: String): Long {
    val index = getColumnIndex(columnName)
    return if (index != -1 && !isNull(index)) getLong(index) else 0L
}

fun Cursor.getIntByColumn(columnName: String): Int {
    val index = getColumnIndex(columnName)
    return if (index != -1 && !isNull(index)) getInt(index) else 0
}

fun Cursor.getBooleanByColumn(columnName: String): Boolean {
    return getIntByColumn(columnName) == 1
}
