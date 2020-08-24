package ru.skillbranch.skillarticles.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate
import ru.skillbranch.skillarticles.data.delegates.PrefLiveDelegate
import ru.skillbranch.skillarticles.data.models.AppSettings

object PrefManager {

    internal val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

    var isAuth by PrefDelegate(false)

    var isDarkMode by PrefDelegate(false)
    var isBigText by PrefDelegate(false)

    val isAuthLiveData by PrefLiveDelegate("isAuth", false, preferences)

    val appSettings = MediatorLiveData<AppSettings>().apply {

        val isDarkModeLiveData by PrefLiveDelegate("isDarkMode", false, preferences)
        val isBigTextLiveData by PrefLiveDelegate("isBigText", false, preferences)

        value = AppSettings()
        addSource(isDarkModeLiveData) {
            value = value!!.copy(isDarkMode = it)
        }
        addSource(isBigTextLiveData) {
            value = value!!.copy(isBigText = it)
        }
    }.distinctUntilChanged()

    fun clearAll() {
        preferences.edit{ clear() }
    }

    fun updateSettings(settings: AppSettings) {
        isDarkMode = settings.isDarkMode
        isBigText = settings.isBigText
    }

}
