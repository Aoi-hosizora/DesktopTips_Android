package com.aoihosizora.desktoptips.ui.widget

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

class RecyclerViewEmptySupport : RecyclerView {

    private var mEmptyView: View? = null

    private val emptyObserver = object : RecyclerView.AdapterDataObserver() {

        override fun onChanged() {
            val adapter = adapter
            if (adapter != null && mEmptyView != null) {

                if (adapter.itemCount == 0) {
                    this@RecyclerViewEmptySupport.visibility = View.GONE
                    mEmptyView!!.visibility = View.VISIBLE
                } else {
                    mEmptyView!!.visibility = View.GONE
                    this@RecyclerViewEmptySupport.visibility = View.VISIBLE
                }
            }
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun setEmptyView(emptyView: View) {
        this.mEmptyView = emptyView
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        if (adapter != null) {
            if (!adapter.hasObservers())
                adapter.registerAdapterDataObserver(emptyObserver)
        }
        emptyObserver.onChanged()
    }
}