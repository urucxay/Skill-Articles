package ru.skillbranch.skillarticles.data.delegates

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

class PrefLiveDelegate<T>(
    private val fieldKey: String,
    private val defaultValue: T,
    private val preferences: SharedPreferences
) : ReadOnlyProperty<Any?, LiveData<T>> {

    private var storedValue: LiveData<T>? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): LiveData<T> {
        if (storedValue == null) {
            storedValue = SharedPreferencesLiveData(preferences, fieldKey, defaultValue)
        }
        return storedValue!!
    }

}

internal class SharedPreferencesLiveData<T>(
    private val preferences: SharedPreferences,
    private val fieldKey: String,
    private val defaultValue: T
) : LiveData<T>() {

    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, shKey ->
            if (shKey == fieldKey) {
                value = readValue(defaultValue)
            }
        }

    override fun onActive() {
        super.onActive()
        value = readValue(defaultValue)
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    override fun onInactive() {
        preferences.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener)
        super.onInactive()
    }

    @Suppress("UNCHECKED_CAST")
    private fun readValue(defaultValue: T): T {
        return when (defaultValue) {
            is Int -> preferences.getInt(fieldKey, defaultValue as Int) as T
            is Long -> preferences.getLong(fieldKey, defaultValue as Long) as T
            is Float -> preferences.getFloat(fieldKey, defaultValue as Float) as T
            is String -> preferences.getString(fieldKey, defaultValue as String) as T
            is Boolean -> preferences.getBoolean(fieldKey, defaultValue as Boolean) as T
            else -> error("This type can not be stored into Preferences")
        }
    }

}