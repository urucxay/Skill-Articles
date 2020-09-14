package ru.skillbranch.skillarticles.data.repositories

import ru.skillbranch.skillarticles.data.local.PrefManager
import ru.skillbranch.skillarticles.data.remote.NetworkManager
import ru.skillbranch.skillarticles.data.remote.req.LoginReq

object RootRepository {
    private val prefs = PrefManager
    private val network = NetworkManager.api

    fun isAuth() = prefs.isAuthLiveData

    fun setAuth(isAuth: Boolean) {
        prefs.isAuth = isAuth
    }

    suspend fun login(login: String, pass: String) {
        val auth = network.login(LoginReq(login, pass))
        prefs.profile = auth.user
        prefs.accessToken = "Bearer ${auth.accessToken}"
        prefs.refreshToken = auth.refreshToken
    }
}