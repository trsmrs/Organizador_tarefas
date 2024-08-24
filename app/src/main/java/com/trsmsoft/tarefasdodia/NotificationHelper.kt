package com.trsmsoft.tarefasdodia

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class NotificationHelper(private val context: Context) {

    private val CHANNEL_ID = "TASK_NOTIFICATION_CHANNEL"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Task Notifications"
            val channelDescription = "Notifications for task reminders"

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendHalfTimeNotification(taskId: Long, taskName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            // Flags para criar uma nova instância da MainActivity
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Cria um PendingIntent com a Intent definida
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(), // Usar o ID da tarefa para garantir que o PendingIntent seja único
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Use um ícone válido
            .setContentTitle("Tarefas")
            .setContentText("Seu tempo para: $taskName está acabando.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(taskId.toInt(), builder.build())
        }
    }

    fun sendOverTimeNotification(taskId: Long, taskName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            // Flags para criar uma nova instância da MainActivity
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Cria um PendingIntent com a Intent definida
        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(), // Usar o ID da tarefa para garantir que o PendingIntent seja único
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification) // Use um ícone válido
            .setContentTitle("Tarefas")
            .setContentText("Seu tempo para: $taskName acabou.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(taskId.toInt(), builder.build())
        }
    }
}

