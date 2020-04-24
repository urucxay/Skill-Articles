package ru.skillbranch.skillarticles.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager

@SuppressLint("RestrictedApi")
class PrefManager(context: Context) {

    val preferences: SharedPreferences by lazy { PreferenceManager(context).sharedPreferences }

    fun clearAll() {
        preferences.all.clear()
    }

}