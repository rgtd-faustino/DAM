package dam.a51394.nevazio.data.model

import java.util.Date
import java.util.concurrent.TimeUnit

object ExpiryUtils {

    fun computeStatus(expiryDate: Date?): ExpiryStatus {
        if (expiryDate == null) return ExpiryStatus.FRESH
        val now = Date()
        val diffMs = expiryDate.time - now.time
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)
        return when {
            diffDays < 0   -> ExpiryStatus.EXPIRED
            diffDays == 0L -> ExpiryStatus.EXPIRES_TODAY
            diffDays <= 3  -> ExpiryStatus.EXPIRING_SOON
            else           -> ExpiryStatus.FRESH
        }
    }

    fun computeLabel(expiryDate: Date?): String {
        if (expiryDate == null) return "Sem validade"
        val now = Date()
        val diffMs = expiryDate.time - now.time
        val diffDays = TimeUnit.MILLISECONDS.toDays(diffMs)
        return when {
            diffDays < 0   -> "Expirado há ${-diffDays}d"
            diffDays == 0L -> "Expira hoje!"
            diffDays == 1L -> "Expira amanhã"
            diffDays <= 3  -> "Expira em ${diffDays}d"
            diffDays <= 7  -> "Expira em ${diffDays}d"
            else -> {
                val cal = java.util.Calendar.getInstance()
                cal.time = expiryDate
                val months = arrayOf("Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez")
                "${cal.get(java.util.Calendar.DAY_OF_MONTH)} ${months[cal.get(java.util.Calendar.MONTH)]}"
            }
        }
    }

    fun Ingredient.withComputedExpiry(): Ingredient = copy(
        status = computeStatus(expiryDate),
        expiryLabel = computeLabel(expiryDate)
    )
}
