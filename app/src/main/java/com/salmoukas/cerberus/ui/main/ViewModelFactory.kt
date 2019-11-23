package com.salmoukas.cerberus.ui.main

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

class ViewModelFactory<T>(val creator: () -> T) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return creator() as T
    }
}

inline fun <reified T : ViewModel> ViewModelStoreOwner.createViewModel(noinline creator: (() -> T)? = null): T {
    return ViewModelProvider(
        this,
        if (creator == null) (
                if (this is Context)
                    ViewModelProvider.AndroidViewModelFactory((this as Context).applicationContext as Application)
                else
                    ViewModelProvider.NewInstanceFactory()
                )
        else
            ViewModelFactory(creator)
    ).get(T::class.java)
}
