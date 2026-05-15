package com.example.moneytracker

import android.app.Application
import com.example.moneytracker.di.AppContainer

class MoneyTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
    }
}
