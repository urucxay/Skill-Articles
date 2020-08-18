package ru.skillbranch.skillarticles.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.liveData
import androidx.preference.PreferenceManager
import ru.skillbranch.skillarticles.App
import ru.skillbranch.skillarticles.data.delegates.PrefDelegate
import ru.skillbranch.skillarticles.data.models.AppSettings

object PrefManager {

    internal val preferences: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(App.applicationContext())
    }

    var isAuth by PrefDelegate(false)

    private val isDarkMode by PrefDelegate(false)
    private val isBigText by PrefDelegate(false)
    val appSettings = MediatorLiveData<AppSettings>().apply {
        value = AppSettings()
        addSource(getMode()) {
            val copy = value!!.copy(isDarkMode = it)
            if (value != copy) value = copy
        }
        addSource(getTextMode()) {
            val copy = value!!.copy(isBigText = it)
            if (value != copy) value = copy
        }
    }

    fun clearAll() {
        preferences.edit { clear() }
    }

    fun updateSettings(settings: AppSettings) {
        preferences.edit {
            putBoolean("isDarkMode", settings.isDarkMode)
            putBoolean("isBigText", settings.isBigText)
        }
    }


    fun getAuthStatus() = liveData { emit(isAuth ?: false) }

    private fun getMode() = liveData { emit(isDarkMode ?: false) }
    private fun getTextMode() = liveData { emit(isBigText ?: false) }
}
