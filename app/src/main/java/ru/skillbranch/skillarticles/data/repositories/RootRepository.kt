package ru.skillbranch.skillarticles.data.repositories

import ru.skillbranch.skillarticles.data.local.PrefManager

object RootRepository {
    private val prefs = PrefManager

    fun isAuth() = prefs.isAuthLiveData

    fun setAuth(isAuth: Boolean) {
        prefs.isAuth = isAuth
    }
}