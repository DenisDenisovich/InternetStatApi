package com.example.network.statistic.domian.category

import com.example.network.statistic.db.DbHelper
import kotlinx.coroutines.*
import java.lang.Runnable

object CategoryUpdater {

    private val job = Job()
    private val coroutineScope = CoroutineScope(job + Dispatchers.Default)
    private val queue: ArrayList<String> = arrayListOf()
    private val queueRunnable: ArrayList<CategoryCheckRunnable> = arrayListOf()
    private val categoryWorker = CategoryWorker()
    private var workerInProgress = false

    init {
        coroutineScope.launch {
            while (true) {
                delay(1000)
                if (queueRunnable.size > 0) {
                    queueRunnable.firstOrNull()?.run()
                    queueRunnable.removeAt(0)
                }
            }
        }
    }

    @Synchronized
    fun addAppsForCheck(apps: List<String>) {
        queueRunnable.add(CategoryCheckRunnable(apps))
    }

    private suspend fun startWorker() {
        workerInProgress = true
        while (queue.size != 0) {
            queue.firstOrNull()?.let { app ->
                val category = categoryWorker.checkApp(app)
                category?.let {
                    AddCategoryUseCase(app, category).execute()
                }
                queue.removeAt(0)
            }
        }
        workerInProgress = false
    }

    class CategoryCheckRunnable(val apps: List<String>) : Runnable {
        override fun run() {
            runBlocking {
                apps.forEach {
                    if (!queue.contains(it) && !DbHelper.categoryIsExist(it)) {
                        queue.add(it)
                    }
                }
                if (!workerInProgress) {
                    startWorker()
                }
            }
        }
    }
}