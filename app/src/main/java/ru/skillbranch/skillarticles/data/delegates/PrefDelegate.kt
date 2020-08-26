package ru.skillbranch.skillarticles.data.delegates

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import ru.skillbranch.skillarticles.data.local.PrefManager
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
class PrefDelegate<T>(private val defaultValue: T) : ReadWriteProperty<PrefManager, T?> {

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
        val prefs = thisRef.preferences

        return when (defaultValue) {
            is Int -> prefs.getInt(property.name, defaultValue as Int) as T
            is Long -> prefs.getLong(property.name, defaultValue as Long) as T
            is Float -> prefs.getFloat(property.name, defaultValue as Float) as T
            is String -> prefs.getString(property.name, defaultValue as String) as T
            is Boolean -> prefs.getBoolean(property.name, defaultValue as Boolean) as T
            else -> error("Only primitive types are allowed")
        }
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        with(thisRef.preferences.edit()) {
            when (value) {
                is Int -> putInt(property.name, value)
                is Long -> putLong(property.name, value)
                is Float -> putFloat(property.name, value)
                is String -> putString(property.name, value)
                is Boolean -> putBoolean(property.name, value)
                else -> error("Only primitive types are allowed")
            }
            apply()
        }
    }
}

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