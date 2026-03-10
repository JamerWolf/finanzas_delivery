package com.control_delivery.finanzas_delivery.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.control_delivery.finanzas_delivery.MainActivity
import com.control_delivery.finanzas_delivery.R
import com.control_delivery.finanzas_delivery.domain.location.LocationTracker
import com.control_delivery.finanzas_delivery.domain.trip.ActiveTripManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber
import javax.inject.Inject

/**
 * Foreground Service that keeps GPS tracking alive while the driver has an active trip.
 *
 * Lifecycle:
 * 1. Started via [ACTION_START] when a trip begins.
 * 2. Shows a persistent notification so the OS doesn't kill us.
 * 3. Collects [LocationTracker.getLocationUpdates] and delegates each fix to [ActiveTripManager.onNewLocation].
 * 4. Stopped via [ACTION_STOP] when the trip completes or is cancelled.
 *
 * The service does NOT own the trip state — [ActiveTripManager] does.
 * The service only bridges GPS hardware → ActiveTripManager.
 */
@AndroidEntryPoint
class TrackingService : Service() {

    @Inject lateinit var locationTracker: LocationTracker
    @Inject lateinit var activeTripManager: ActiveTripManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        const val ACTION_START = "ACTION_START_TRACKING"
        const val ACTION_STOP = "ACTION_STOP_TRACKING"

        private const val NOTIFICATION_CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1

        /** Convenience: start tracking from any Context. */
        fun start(context: Context) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_START
            }
            context.startForegroundService(intent)
        }

        /** Convenience: stop tracking from any Context. */
        fun stop(context: Context) {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Timber.d("TrackingService created")
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startTracking()
            ACTION_STOP -> stopTracking()
            else -> Timber.w("TrackingService received unknown action: ${intent?.action}")
        }
        return START_STICKY
    }

    private fun startTracking() {
        Timber.d("Starting GPS tracking service")
        startForeground(NOTIFICATION_ID, buildNotification())

        locationTracker.getLocationUpdates()
            .onEach { location ->
                activeTripManager.onNewLocation(location)
                // Update notification with current distance
                updateNotification()
            }
            .catch { e ->
                Timber.e(e, "Error in location tracking flow")
            }
            .launchIn(serviceScope)
    }

    private fun stopTracking() {
        Timber.d("Stopping GPS tracking service")
        locationTracker.stopTracking()
        serviceScope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Timber.d("TrackingService destroyed")
    }

    // ---- Notification ----

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "Trip Tracking",
            NotificationManager.IMPORTANCE_LOW // Low = no sound, still shows in shade
        ).apply {
            description = "Shows when GPS tracking is active during a trip"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val distanceKm = activeTripManager.getCurrentDistanceKm()
        val orderCount = activeTripManager.getActiveOrderCount()

        // Tapping the notification opens the app
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Trip in progress")
            .setContentText("${String.format("%.1f", distanceKm)} km | $orderCount order(s)")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification())
    }
}
