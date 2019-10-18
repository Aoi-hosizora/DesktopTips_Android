package com.aoihosizora.desktoptips.ui.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aoihosizora.desktoptips.R
import com.aoihosizora.desktoptips.model.TipItem
import kotlinx.android.synthetic.main.view_tips_adapter.view.*

class TipItemAdapter(

    private val context: Context,
    private val tipItems: List<TipItem>,

    private val onItemClick: (View, TipItem) -> Unit,
    private val onItemLongClick: (View, TipItem) -> Unit,
    private val onCheckedChanged: (isCheckMode: Boolean, items: List<TipItem>) -> Unit

) : RecyclerView.Adapter<TipItemAdapter.ViewHolder>(), View.OnClickListener, View.OnLongClickListener {

    /**
     * 多选模式
     */
    var checkMode = false
        set(value) {
            field = value
            if (!value) checkItems.clear()
            notifyDataSetChanged()

            // 通知选中状态更改
            onCheckedChanged(value, checkItems)
        }

    /**
     * 当前选中项
     * checkMode 为 false 时清空
     */
    private val checkItems: MutableList<TipItem> = mutableListOf()

    init {
        onCheckedChanged(checkMode, checkItems)
    }

    /**
     * 设置选中
     */
    fun setItemChecked(tipItem: TipItem, isCheck: Boolean) {
        if (isCheck && checkItems.indexOf(tipItem) == -1)
            checkItems.add(tipItem)
        else if (!isCheck)
            checkItems.remove(tipItem)
        onCheckedChanged(checkMode, checkItems)
    }

    /**
     * 获得所有选中
     */
    fun getAllChecked(): List<TipItem> = checkItems

    /**
     * 初始化 VIEW 和 点击事件
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_tips_adapter, parent, false)

        view.setOnClickListener(this)
        view.setOnLongClickListener(this)

        return ViewHolder(view)
    }

    /**
     * 绑定 ViewHolder 和界面显示
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // holder.setIsRecyclable(false)

        val tipItem = tipItems[position]
        holder.view.tag = tipItem

        // Content
        holder.view.txt_content.text = tipItem.content
        if (tipItem.highLight)
            holder.view.txt_content.setTextColor(ContextCompat.getColor(context, R.color.highlight_color))
        else
            holder.view.txt_content.setTextColor(ContextCompat.getColor(context, R.color.text_color))

        // Check
        holder.view.check_box.setOnCheckedChangeListener(null)
        holder.view.check_box.isChecked = checkItems.indexOf(tipItem) != -1
        holder.view.check_box.visibility = if (checkMode) View.VISIBLE else View.GONE
        holder.view.check_box.setOnCheckedChangeListener { _ , checked -> run {
            if (checked)
                checkItems.add(tipItem)
            else
                checkItems.remove(tipItem)

            // 通知选中内容变更
            onCheckedChanged(checkMode, checkItems)
        }}
    }

    override fun getItemCount(): Int = tipItems.size

    /**
     * 单击 忽略多选
     */
    override fun onClick(view: View?) {
        if (!checkMode)
            view?.let { onItemClick(it, it.tag as TipItem) }
        else
            view?.check_box?.let {
                it.isChecked = !it.isChecked
            }
    }

    /**
     * 长按，忽略多选
     */
    override fun onLongClick(view: View?): Boolean {
        if (!checkMode)
            view?.let { onItemLongClick(it, it.tag as TipItem) }
        return true
    }

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
}