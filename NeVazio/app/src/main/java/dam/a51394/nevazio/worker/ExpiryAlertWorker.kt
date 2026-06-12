package dam.a51394.nevazio.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dam.a51394.nevazio.MainActivity
import dam.a51394.nevazio.data.model.Ingredient
import dam.a51394.nevazio.data.repository.AuthRepository
import dam.a51394.nevazio.data.repository.FridgeRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.concurrent.TimeUnit

class ExpiryAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val authRepository: AuthRepository,
    private val fridgeRepository: FridgeRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val user = authRepository.currentUser ?: return Result.success()
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        // Utilização centralizada do getCurrentFridgeId() em substituição da lógica descontinuada
        // que lia o 'familyCode' diretamente como ID. Isto garante que o Worker de notificações
        // analisa o inventário do Anfitrião correto e não uma coleção vazia fantasma.
        val fridgeId = authRepository.getCurrentFridgeId()

        val ingredients = fridgeRepository.getIngredients(fridgeId).firstOrNull() ?: emptyList()
        val now = Date()

        val expiringSoon = ingredients.filter { ingredient ->
            ingredient.expiryDate?.let { expiry ->
                val diffInMillis = expiry.time - now.time
                val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
                diffInDays in 0..2
            } == true
        }

        if (expiringSoon.isNotEmpty()) {
            sendNotification(expiringSoon)
        }

        return Result.success()
    }

    private fun sendNotification(ingredients: List<Ingredient>) {
        val context = applicationContext
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && 
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        val channelId = "expiry_alerts"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alertas de Validade",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Avisos sobre ingredientes prestes a expirar"
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val itemNames = ingredients.take(3).joinToString { it.name } + if (ingredients.size > 3) " e mais" else ""
        
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert) 
            .setContentTitle("Ingredientes a expirar!")
            .setContentText("Tens ${ingredients.size} ingrediente(s) a expirar: $itemNames")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context).notify(1001, builder.build())
    }
}
