package com.example.baseproject.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class CapturedLocation(
    val latitude: Double?,
    val longitude: Double?,
    val address: String? = null
) {
    fun toNote(): String {
        if (latitude == null || longitude == null) {
            return "GPS: Chưa xác định được tọa độ"
        }
        val coords = String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
        return if (!address.isNullOrBlank()) {
            "GPS: $coords ($address)"
        } else {
            "GPS: $coords"
        }
    }
}

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Lấy tọa độ GPS hiện tại (kèm tên địa chỉ nếu có) để đính kèm vào ghi chú chấm công cho HR đối chiếu.
     * Không chặn Check-in nếu không bắt được sóng GPS.
     */
    @SuppressLint("MissingPermission")
    suspend fun captureCurrentLocation(context: Context): CapturedLocation {
        if (!hasLocationPermission(context)) {
            return CapturedLocation(null, null, null)
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return CapturedLocation(null, null, null)

        var location = getBestLastKnownLocation(locationManager)

        if (location == null) {
            location = withTimeoutOrNull(3000L) {
                requestSingleLocationUpdate(locationManager)
            }
        }

        if (location == null) {
            return CapturedLocation(null, null, null)
        }

        val address = withTimeoutOrNull(1500L) {
            resolveAddressLine(context, location.latitude, location.longitude)
        }

        return CapturedLocation(location.latitude, location.longitude, address)
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveAddressLine(context: Context, lat: Double, lng: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext null
                val geocoder = Geocoder(context, Locale("vi", "VN"))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { cont ->
                        geocoder.getFromLocation(lat, lng, 1) { addresses ->
                            val line = addresses.firstOrNull()?.getAddressLine(0)
                            if (cont.isActive) cont.resume(line)
                        }
                    }
                } else {
                    geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()?.getAddressLine(0)
                }
            } catch (_: Exception) {
                null
            }
        }

    @SuppressLint("MissingPermission")
    private fun getBestLastKnownLocation(lm: LocationManager): Location? {
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        var best: Location? = null
        for (p in providers) {
            try {
                if (!lm.isProviderEnabled(p)) continue
                val loc = lm.getLastKnownLocation(p) ?: continue
                if (best == null || loc.time > best.time) {
                    best = loc
                }
            } catch (_: Exception) {
            }
        }
        return best
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocationUpdate(lm: LocationManager): Location? =
        suspendCancellableCoroutine { cont ->
            val provider = when {
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> null
            }
            if (provider == null) {
                cont.resume(null)
                return@suspendCancellableCoroutine
            }

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lm.removeUpdates(this)
                    if (cont.isActive) cont.resume(location)
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            try {
                lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                cont.invokeOnCancellation { lm.removeUpdates(listener) }
            } catch (_: Exception) {
                if (cont.isActive) cont.resume(null)
            }
        }
}
