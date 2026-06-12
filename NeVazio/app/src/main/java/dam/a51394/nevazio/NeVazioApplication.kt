package dam.a51394.nevazio

import android.app.Application
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dam.a51394.nevazio.di.appModule
import dam.a51394.nevazio.worker.ExpiryAlertWorker
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import java.util.concurrent.TimeUnit

class NeVazioApplication : Application(), Configuration.Provider {
    
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(org.koin.androidx.workmanager.factory.KoinWorkerFactory())
            .build()
            
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@NeVazioApplication)
            workManagerFactory()
            modules(appModule)
        }
        
        setupExpiryWorker()
    }
    
    private fun setupExpiryWorker() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
            
        val workRequest = PeriodicWorkRequestBuilder<ExpiryAlertWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "ExpiryAlertWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
