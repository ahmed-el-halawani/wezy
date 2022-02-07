package com.newcore.wezy.localDb

import androidx.lifecycle.LiveData
import androidx.room.*
import com.newcore.wezy.models.Article

@Dao
interface ArticleDao {

    //insert article come from api
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(article: Article):Long

    // get all articles stored in db
    @Query("Select * from articles")
    fun getAll():LiveData<List<Article>>

    // delete article from db
    @Delete
    suspend fun deleteArticle(article: Article)

}