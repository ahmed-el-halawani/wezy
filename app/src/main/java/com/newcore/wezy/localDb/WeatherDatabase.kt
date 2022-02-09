package com.newcore.wezy.localDb

import android.content.Context
import androidx.room.*
import androidx.room.migration.AutoMigrationSpec
import com.newcore.wezy.models.weatherentities.WeatherLang
import com.newcore.wezy.utils.Constants
import com.newcore.wezy.utils.Constants.DATABASE_NAME


@TypeConverters(Converters::class)
@Database(
    entities = [
        WeatherLang::class
    ],
    version = 2
)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDeo(): WeatherDao

    companion object{
        private var instance: WeatherDatabase? = null
        private val Lock = Any()

        operator fun invoke(context: Context) = instance ?:synchronized(Lock){
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