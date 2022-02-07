package com.newcore.wezy.localDb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.newcore.wezy.models.Article



@TypeConverters(Converters::class)
@Database(
    entities = [
        Article::class
    ],
    version = 2
)
abstract class ArticleDatabase : RoomDatabase() {

    abstract fun articleDao(): ArticleDao

    companion object{
        private var instance: ArticleDatabase? = null
        private val Lock = Any()

        operator fun invoke(context: Context) = instance ?:synchronized(Lock){
            instance ?: createDatabase(context).also {
                instance = it
            }
        }

        private fun createDatabase(context: Context) =
            Room.databaseBuilder(
                    context.applicationContext,
                    ArticleDatabase::class.java,
                    "newsArticles.db"
                )
                .build()

    }
}