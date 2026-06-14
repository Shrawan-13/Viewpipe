package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.LocalRepository
import com.example.data.remote.YouTubeRepository

class ViewPipeApplication : Application() {
    val youTubeRepository by lazy { YouTubeRepository() }
    
    val database by lazy {
        Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "viewpipe_db"
        ).build()
    }
    
    val localRepository by lazy {
        LocalRepository(
            database.historyDao(),
            database.watchLaterDao(),
            database.subscriptionDao()
        )
    }
}
