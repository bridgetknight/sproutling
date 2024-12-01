package com.project.sproutling.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.project.sproutling.R
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NotificationService(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // Channel IDs
    companion object {
        const val REMINDERS_CHANNEL_ID = "watering_reminders"
        const val MESSAGES_CHANNEL_ID = "plant_messages"
        const val WATER_ALERTS_CHANNEL_ID = "water_alerts"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val reminderChannel = NotificationChannel(
                REMINDERS_CHANNEL_ID,
                "Watering Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders to water your plants"
            }

            val messageChannel = NotificationChannel(
                MESSAGES_CHANNEL_ID,
                "Plant Messages",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Messages from your plants"
            }

            val alertChannel = NotificationChannel(
                WATER_ALERTS_CHANNEL_ID,
                "Water Level Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for low water levels"
            }

            notificationManager.createNotificationChannels(listOf(reminderChannel, messageChannel, alertChannel))
        }
    }

    fun checkAndSendWateringReminder(plantName: String, newLastWatered: String) {
        val notification = NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
            .setSmallIcon(R.drawable.planticon)
            .setContentTitle("Time to Water!")
            .setContentText("$plantName needs watering")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }


    fun sendPlantMessage(plantName: String) {
        // List of possible messages from plant
        val messages = listOf(
            "It's me, $plantName! How are you?",
            "I hope you're having a good day!",
            "What a lovely day to be a plant!",
            "You're doing great at plant parenting!",
            "Thanks for taking care of me!",
            "I'm growing strong thanks to you!"
        )

        fun checkAndSendWateringReminder(plantName: String, lastWateredTime: String?) {
            if (lastWateredTime == null) return

            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault())
            val lastWatered = LocalDateTime.parse(lastWateredTime, formatter)
            val now = LocalDateTime.now()

            val daysSinceWatered = ChronoUnit.DAYS.between(lastWatered, now)

            if (daysSinceWatered >= 2) {
                val notification = NotificationCompat.Builder(context, REMINDERS_CHANNEL_ID)
                    .setSmallIcon(R.drawable.planticon2)
                    .setContentTitle("Time to Water!")
                    .setContentText("$plantName hasn't been watered in $daysSinceWatered days")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .build()

                notificationManager.notify(1, notification)
            }
        }

        val randomMessage = messages.random()

        val notification = NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
            .setSmallIcon(R.drawable.planticon2)
            .setContentTitle("Message from $plantName")
            .setContentText(randomMessage)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(2, notification)
    }


    fun checkAndSendMoistureAlert(plantName: String, moisture: String) {
        try {
            val moistureValue = moisture.toDouble()
            if (moistureValue < 50.0) {
                val notification = NotificationCompat.Builder(context, WATER_ALERTS_CHANNEL_ID)
                    .setSmallIcon(R.drawable.planticon2)
                    .setContentTitle("Low Water Alert!")
                    .setContentText("$plantName's soil is getting dry (${moisture}%)")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

                notificationManager.notify(3, notification)
            }
        } catch (e: NumberFormatException) {
            // Handle non-numeric moisture values (like "Loading..." or "Error")
        }
    }
}