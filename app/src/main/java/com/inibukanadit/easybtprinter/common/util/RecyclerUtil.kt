package com.inibukanadit.easybtprinter.common.util

import android.content.Context
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

object RecyclerUtil {

    const val TYPE_LIST = 0
    const val TYPE_SLIDE = 1
    const val TYPE_GRID = 2

    const val VERTICAL = 0
    const val HORIZONTAL = 1

    fun getLayoutManager(
        context: Context,
        type: Int = TYPE_LIST,
        spanCount: Int = 2
    ): RecyclerView.LayoutManager {
        return when (type) {
            TYPE_SLIDE -> LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            TYPE_GRID -> GridLayoutManager(context, spanCount)
            else -> LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    fun getDivider(context: Context, orientation: Int = VERTICAL): DividerItemDecoration {
        return DividerItemDecoration(
            context, when (orientation) {
                HORIZONTAL -> DividerItemDecoration.HORIZONTAL
                else -> DividerItemDecoration.VERTICAL
            }
        )
    }

}