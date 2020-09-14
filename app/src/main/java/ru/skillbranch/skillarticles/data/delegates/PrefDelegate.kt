package ru.skillbranch.skillarticles.data.delegates

import com.squareup.moshi.JsonAdapter
import ru.skillbranch.skillarticles.data.local.PrefManager
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

class PrefObjDelegate<T>(
    private val adapter: JsonAdapter<T>
) {
    private var storedValue: T? = null

    operator fun provideDelegate(
        thisRef: PrefManager,
        property: KProperty<*>
    ): ReadWriteProperty<PrefManager, T?> {
        val key = property.name
        return object : ReadWriteProperty<PrefManager, T?> {
            override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
                if (storedValue == null) {
                    storedValue = thisRef.preferences.getString(key, null)
                        ?.let { adapter.fromJson(it) }
                }
                return storedValue
            }

            override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
                storedValue = value
                thisRef.preferences.edit()
                    .putString(key, value?.let { adapter.toJson(it) })
                    .apply()
            }
        }
    }
}