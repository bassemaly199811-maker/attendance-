package com.example.service

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object LocationHelper {

  data class GpsCoordinates(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Double,
    val provider: String,
  )

  enum class LocationErrorReason {
    PERMISSION_DENIED,
    GPS_DISABLED,
    NO_INTERNET,
    TIMEOUT_NO_SATELLITE_FIX,
    OUT_OF_BOUNDS,
    PROVIDER_UNAVAILABLE,
    MOCK_LOCATION_DETECTED,
    UNKNOWN_ERROR,
  }

  fun isMockLocation(location: Location?): Boolean {
    if (location == null) return false
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
      location.isMock
    } else {
      @Suppress("DEPRECATION")
      location.isFromMockProvider
    }
  }

  fun getQatarTimeZone(): java.util.TimeZone = java.util.TimeZone.getTimeZone("Asia/Qatar")

  fun getQatarCurrentTime(): String {
    val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH)
    sdf.timeZone = getQatarTimeZone()
    return sdf.format(java.util.Date())
  }

  fun getQatarCurrentDate(): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.ENGLISH)
    sdf.timeZone = getQatarTimeZone()
    return sdf.format(java.util.Date())
  }

  fun getQatarDateTimeLabel(): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd hh:mm a (AST GMT+3)", java.util.Locale.ENGLISH)
    sdf.timeZone = getQatarTimeZone()
    return sdf.format(java.util.Date())
  }

  sealed class LocationSearchResult {
    data class Success(
      val coordinates: GpsCoordinates,
      val message: String,
      val isRealGps: Boolean,
    ) : LocationSearchResult()

    data class Failure(
      val reason: LocationErrorReason,
      val explanationArabic: String,
      val explanationEnglish: String,
      val fallbackCoordinates: GpsCoordinates? = null,
    ) : LocationSearchResult()
  }

  fun hasLocationPermission(context: Context): Boolean {
    val fine =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val coarse =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    return fine || coarse
  }

  fun isLocationServiceEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
      locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
  }

  fun validateCoordinates(lat: Double, lng: Double): Boolean {
    return lat in -90.0..90.0 && lng in -180.0..180.0 && !(lat == 0.0 && lng == 0.0)
  }

  @SuppressLint("MissingPermission")
  fun searchLocationWithDiagnostics(
    context: Context,
    isOnline: Boolean = true,
    timeoutMillis: Long = 12000L,
    onResult: (LocationSearchResult) -> Unit,
  ) {
    // 1. Check Permissions
    if (!hasLocationPermission(context)) {
      onResult(
        LocationSearchResult.Failure(
          reason = LocationErrorReason.PERMISSION_DENIED,
          explanationArabic = "تم رفض إذن تحديد الموقع. يرجى تفعيل إذن الموقع للتطبيق من إعدادات الهاتف.",
          explanationEnglish = "Location permission denied. Please grant location access in device settings.",
          fallbackCoordinates = null,
        )
      )
      return
    }

    // 2. Check Hardware Location Providers
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (locationManager == null) {
      onResult(
        LocationSearchResult.Failure(
          reason = LocationErrorReason.PROVIDER_UNAVAILABLE,
          explanationArabic = "خدمة تحديد الموقع غير متوفرة على هذا الجهاز.",
          explanationEnglish = "Location service is unavailable on this device.",
          fallbackCoordinates = null,
        )
      )
      return
    }

    val isGpsEnabled = try { locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (_: Exception) { false }
    val isNetworkLocEnabled = try { locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { false }

    if (!isGpsEnabled && !isNetworkLocEnabled) {
      onResult(
        LocationSearchResult.Failure(
          reason = LocationErrorReason.GPS_DISABLED,
          explanationArabic = "خدمة GPS معطلة. يرجى تفعيل الموقع (GPS) من شريط الإشعارات أو إعدادات الجهاز.",
          explanationEnglish = "GPS Location service is disabled. Please enable GPS in device settings.",
          fallbackCoordinates = null,
        )
      )
      return
    }

    var resultDispatched = false
    val mainHandler = Handler(Looper.getMainLooper())
    var fusedClient: com.google.android.gms.location.FusedLocationProviderClient? = null
    var fusedCallback: LocationCallback? = null
    var lmListener: LocationListener? = null
    val cts = CancellationTokenSource()

    fun cleanup() {
      try {
        cts.cancel()
      } catch (_: Exception) {}

      fusedCallback?.let { cb ->
        try {
          fusedClient?.removeLocationUpdates(cb)
        } catch (_: Exception) {}
      }

      lmListener?.let { l ->
        try {
          locationManager.removeUpdates(l)
        } catch (_: Exception) {}
      }
    }

    fun dispatchSuccess(location: Location, providerName: String) {
      if (resultDispatched) return

      if (isMockLocation(location)) {
        resultDispatched = true
        cleanup()
        onResult(
          LocationSearchResult.Failure(
            reason = LocationErrorReason.MOCK_LOCATION_DETECTED,
            explanationArabic = "تم اكتشاف تطبيق تزييف موقع (Mock Location). يرجى تعطيل تطبيقات Fake GPS.",
            explanationEnglish = "Mock / Fake GPS location detected. Please disable location spoofing.",
            fallbackCoordinates = null,
          )
        )
        return
      }

      if (validateCoordinates(location.latitude, location.longitude)) {
        resultDispatched = true
        cleanup()
        val acc = if (location.accuracy > 0f) location.accuracy.toDouble() else 8.0
        val coords = GpsCoordinates(
          latitude = location.latitude,
          longitude = location.longitude,
          accuracy = acc,
          provider = providerName,
        )
        onResult(
          LocationSearchResult.Success(
            coordinates = coords,
            message = "Real GPS acquired: ${String.format(java.util.Locale.ENGLISH, "%.4f, %.4f", location.latitude, location.longitude)} (±${acc.toInt()}m) ✓",
            isRealGps = true,
          )
        )
      }
    }

    // 3. Request live high accuracy GPS fix via Google Play Services FusedLocationProviderClient
    try {
      fusedClient = LocationServices.getFusedLocationProviderClient(context)

      // A) Immediate single fresh high-accuracy live fix attempt
      fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
        .addOnSuccessListener { loc ->
          if (loc != null && !resultDispatched && validateCoordinates(loc.latitude, loc.longitude)) {
            dispatchSuccess(loc, "FUSED_LIVE_GPS")
          }
        }

      // B) Continuous location stream until first valid fix
      val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
        .setMinUpdateIntervalMillis(500L)
        .setMaxUpdates(10)
        .build()

      fusedCallback = object : LocationCallback() {
        override fun onLocationResult(lr: LocationResult) {
          val loc = lr.lastLocation
          if (loc != null && !resultDispatched && validateCoordinates(loc.latitude, loc.longitude)) {
            dispatchSuccess(loc, "FUSED_STREAM_GPS")
          }
        }
      }

      fusedClient.requestLocationUpdates(
        locationRequest,
        fusedCallback!!,
        Looper.getMainLooper()
      )
    } catch (_: Exception) {}

    // 4. Query native LocationManager in parallel as hardware GPS backup
    try {
      lmListener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
          if (!resultDispatched && validateCoordinates(loc.latitude, loc.longitude)) {
            dispatchSuccess(loc, loc.provider ?: "LM_HARDWARE_GPS")
          }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
      }

      if (isGpsEnabled) {
        try {
          locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            1000L,
            0f,
            lmListener!!,
            Looper.getMainLooper()
          )
        } catch (_: Exception) {}
      }

      if (isNetworkLocEnabled) {
        try {
          locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            1000L,
            0f,
            lmListener!!,
            Looper.getMainLooper()
          )
        } catch (_: Exception) {}
      }
    } catch (_: Exception) {}

    // 6. Timeout Runnable (Configurable timeout, defaults to 12s)
    // On timeout, stop fabricating fake coordinates! Return proper Failure with TIMEOUT_NO_SATELLITE_FIX
    mainHandler.postDelayed({
      if (!resultDispatched) {
        cleanup()
        onResult(
          LocationSearchResult.Failure(
            reason = LocationErrorReason.TIMEOUT_NO_SATELLITE_FIX,
            explanationArabic = "تعذر الحصول على إشارة GPS حالياً. يرجى الانتقال إلى مكان مفتوح أو بجوار نافذة وإعادة المحاولة.",
            explanationEnglish = "Could not get a GPS fix. Move to an open area or near a window and try again.",
            fallbackCoordinates = null,
          )
        )
      }
    }, timeoutMillis)
  }

  fun getCurrentLocation(
    context: Context,
    timeoutMillis: Long = 12000L,
    onResult: (GpsCoordinates) -> Unit,
  ) {
    searchLocationWithDiagnostics(
      context = context,
      isOnline = true,
      timeoutMillis = timeoutMillis,
    ) { result ->
      when (result) {
        is LocationSearchResult.Success -> {
          if (result.isRealGps) {
            onResult(result.coordinates)
          }
        }
        is LocationSearchResult.Failure -> {
          if (result.fallbackCoordinates != null) {
            onResult(result.fallbackCoordinates)
          }
        }
      }
    }
  }

  fun calculateDistanceMeters(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lng2: Double,
  ): Double {
    val results = FloatArray(1)
    Location.distanceBetween(lat1, lng1, lat2, lng2, results)
    return results[0].toDouble()
  }

  fun isWithinGeofence(
    userLat: Double,
    userLng: Double,
    siteLat: Double,
    siteLng: Double,
    radiusMeters: Int,
  ): Boolean {
    val dist = calculateDistanceMeters(userLat, userLng, siteLat, siteLng)
    return dist <= radiusMeters
  }
}
