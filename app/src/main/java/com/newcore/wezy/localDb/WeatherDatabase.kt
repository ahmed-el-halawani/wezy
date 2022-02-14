package com.newcore.wezy.localDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.newcore.wezy.models.MyAlert
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.newcore.wezy.utils.Constants.DATABASE_NAME


@TypeConverters(Converters::class)
@Database(
    entities = [
        WeatherLang::class,
        MyAlert::class
    ],
    version = 4
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDeo(): WeatherDao

    companion object {
        private var instance: WeatherDatabase? = null
        private val Lock = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(Lock) {
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                WeatherDatabase::class.java,
                DATABASE_NAME
            )
                .build()

    }
}