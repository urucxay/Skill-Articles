package ru.skillbranch.skillarticles

import android.app.Application
import android.content.Context

class App : Application() {

    companion object {
        private var instance: App? = null

        fun applicationContext(): Context = instance!!.applicationContext
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        //TODO set default night mode
    }

}