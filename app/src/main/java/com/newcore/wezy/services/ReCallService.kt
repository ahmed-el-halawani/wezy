package com.newcore.wezy.services

import android.content.Context
import android.util.Log
import androidx.work.*

class ReCallService(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    companion object{
        var  callBacks:HashMap<String,suspend ()->Unit> = HashMap();

        fun recall(key:String,callBack:suspend ()->Unit,context: Context){
            println("i am in worker and why it is not working ")
            println(callBacks)
            WorkManager.getInstance(context).cancelAllWork()

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(
                    NetworkType.UNMETERED
                )
                .build()

            callBacks[key] = callBack

            val reCallRequest: WorkRequest =
                OneTimeWorkRequestBuilder<ReCallService>()
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context)
                .enqueue(reCallRequest)
        }
    }

    override suspend fun doWork(): Result {
        println("i am in worker and why it is not working ")
        println(callBacks)
        try {
            callBacks.forEach {
                it.value();
            }
        }catch (t:Throwable){
            Log.e("workerError", t.message?:"" )
        }
        return Result.success()
    }

}
