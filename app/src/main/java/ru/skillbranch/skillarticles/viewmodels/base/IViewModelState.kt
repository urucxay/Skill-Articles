package ru.skillbranch.skillarticles.viewmodels.base

import androidx.lifecycle.SavedStateHandle

interface IViewModelState {
    fun save(outState: SavedStateHandle) {
        //empty default implementation
    }

    fun restore(savedState: SavedStateHandle): IViewModelState {
        //empty default implementation
        return this
    }
}