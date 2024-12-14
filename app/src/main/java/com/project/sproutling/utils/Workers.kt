package com.project.sproutling.utils

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.project.sproutling.data.PlantStorage
import java.util.concurrent.TimeUnit

class MoistureCheckWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val plantStorage = PlantStorage(applicationContext)
        val notificationService = NotificationService(applicationContext)

        return try {
            val plants = plantStorage.getPlants()
            if (plants.isNotEmpty()) {
                // Get moisture from Arduino here
                // For now, we'll read the last stored moisture value
                val lastMoisture = plantStorage.getLastMoisture()
                if (lastMoisture != null) {
                    notificationService.checkAndSendMoistureAlert(
                        plants.first().name,
                        lastMoisture
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "moisture_check_work"

        fun schedule(context: Context, intervalMinutes: Int) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val repeatingRequest = PeriodicWorkRequestBuilder<MoistureCheckWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES,
                15, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    repeatingRequest
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

// Create WateringReminderWorker.kt
class WateringReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val plantStorage = PlantStorage(applicationContext)
        val notificationService = NotificationService(applicationContext)

        return try {
            val plants = plantStorage.getPlants()
            if (plants.isNotEmpty()) {
                val lastWatered = plantStorage.getLastWatered(plants.first().name)
                if (lastWatered != null) {
                    notificationService.checkAndSendWateringReminder(
                        plants.first().name,
                        lastWatered
                    )
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "watering_reminder_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            // Check every hour for watering reminders
            val repeatingRequest = PeriodicWorkRequestBuilder<WateringReminderWorker>(
                1, TimeUnit.HOURS,
                15, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    repeatingRequest
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}

class PlantMessageWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val plantStorage = PlantStorage(applicationContext)
        val notificationService = NotificationService(applicationContext)

        return try {
            val plants = plantStorage.getPlants()
            if (plants.isNotEmpty()) {
                notificationService.sendPlantMessage(plants.first().name)
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "plant_message_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val repeatingRequest = PeriodicWorkRequestBuilder<PlantMessageWorker>(
                6, TimeUnit.HOURS,  // Repeat every 6 hours
                15, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP, // Keep existing if already scheduled
                    repeatingRequest
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}