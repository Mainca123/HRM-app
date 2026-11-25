package com.example.baseproject.core.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

/**
 * Base Fragment with ViewBinding support (inspired by AF_ProjectBase).
 */
abstract class BaseFragment<V : ViewBinding> : Fragment() {
    private var _viewBinding: V? = null
    protected val viewBinding: V
        get() = _viewBinding ?: throw IllegalStateException("ViewBinding cannot be accessed before onCreateView or after onDestroyView")

    abstract fun provideViewBinding(inflater: LayoutInflater, container: ViewGroup?): V

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _viewBinding = provideViewBinding(inflater, container)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initData()
        initObserver()
    }

    open fun initViews() {}
    open fun initData() {}
    open fun initObserver() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
}
