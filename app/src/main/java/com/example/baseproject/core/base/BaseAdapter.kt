package com.example.baseproject.core.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Base RecyclerView Adapter matching AF_ProjectBase coding conventions.
 */
abstract class BaseAdapter<T, VB : ViewBinding> : RecyclerView.Adapter<BaseAdapter<T, VB>.BaseViewHolder>() {
    var dataSet: MutableList<T> = mutableListOf()
    var onClick: ((T) -> Unit)? = null

    abstract fun provideViewBinding(parent: ViewGroup): VB
    abstract fun binData(viewBinding: VB, item: T, position: Int)

    inner class BaseViewHolder(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val binding = provideViewBinding(parent)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        binData(holder.binding, dataSet[position], position)
    }

    override fun getItemCount(): Int = dataSet.size

    fun setData(newDataSet: ArrayList<T>) {
        dataSet = newDataSet
        notifyDataSetChanged()
    }
}
