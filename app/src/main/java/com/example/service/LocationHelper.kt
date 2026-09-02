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
    onResult: (LocationSearchResult) -> Unit,
  ) {
    // 1. Check Permissions
    if (!hasLocationPermission(context)) {
      onResult(
        LocationSearchResult.Failure(
          reason = LocationErrorReason.PERMISSION_DENIED,
          explanationArabic = "يرجى منح إذن تحديد الموقع لتسجيل الحضور وجلب إحداثياتك الحقيقية.",
          explanationEnglish = "Please grant Location permission to get real GPS coordinates.",
          fallbackCoordinates = null,
        )
      )
      return
    }

    // 2. Check GPS Hardware / Service
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    if (locationManager == null) {
      onResult(
        LocationSearchResult.Failure(
          reason = LocationErrorReason.PROVIDER_UNAVAILABLE,
          explanationArabic = "خدمة الموقع غير متوفرة على هذا الجهاز.",
          explanationEnglish = "Location service is unavailable on this device.",
          fallbackCoordinates = null,
        )
      )
      return
    }

    var resultDispatched = false
    val mainHandler = Handler(Looper.getMainLooper())

    fun dispatchSuccess(location: Location, providerName: String) {
      if (resultDispatched) return
      if (isMockLocation(location)) {
        resultDispatched = true
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

    // 3. Try Google Play Services FusedLocationProviderClient first (highest accuracy)
    try {
      val fusedClient = LocationServices.getFusedLocationProviderClient(context)
      val cts = CancellationTokenSource()

      fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
        .addOnSuccessListener { loc ->
          if (loc != null) {
            dispatchSuccess(loc, "FUSED_LIVE")
          }
        }
        .addOnFailureListener {
          // Fallback to locationManager
        }

      fusedClient.lastLocation.addOnSuccessListener { loc ->
        if (loc != null && !resultDispatched) {
          dispatchSuccess(loc, "FUSED_LAST_KNOWN")
        }
      }
    } catch (_: Exception) {}

    // 4. Also query Android LocationManager across all enabled providers
    try {
      val allProviders = try {
        locationManager.getProviders(true)
      } catch (_: Exception) {
        listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
      }

      val lastKnownLocations = allProviders.mapNotNull { provider ->
        try {
          locationManager.getLastKnownLocation(provider)
        } catch (_: Exception) {
          null
        }
      }

      val bestLastLocation = lastKnownLocations.maxByOrNull { it.time }
      if (bestLastLocation != null && !resultDispatched) {
        dispatchSuccess(bestLastLocation, bestLastLocation.provider ?: "LM_LAST_KNOWN")
      }

      // Request fresh updates on GPS and Network
      val listener = object : LocationListener {
        override fun onLocationChanged(loc: Location) {
          dispatchSuccess(loc, loc.provider ?: "LM_LIVE")
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
      }

      if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        try {
          locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            500L,
            0f,
            listener,
            Looper.getMainLooper()
          )
        } catch (_: Exception) {}
      }

      if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
        try {
          locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER,
            500L,
            0f,
            listener,
            Looper.getMainLooper()
          )
        } catch (_: Exception) {}
      }

      // 4-second timeout to clean up listener
      mainHandler.postDelayed({
        try {
          locationManager.removeUpdates(listener)
        } catch (_: Exception) {}

        if (!resultDispatched) {
          // If no provider responded, notify failure cleanly
          onResult(
            LocationSearchResult.Failure(
              reason = LocationErrorReason.TIMEOUT_NO_SATELLITE_FIX,
              explanationArabic = "تعذر الحصول على إشارة GPS حالياً. يرجى التأكد من تفعيل خدمة الموقع والخروج لمنطقة مفتوحة.",
              explanationEnglish = "Unable to acquire satellite GPS fix. Please ensure location is enabled.",
              fallbackCoordinates = null,
            )
          )
        }
      }, 4000)

    } catch (e: Exception) {
      if (!resultDispatched) {
        onResult(
          LocationSearchResult.Failure(
            reason = LocationErrorReason.UNKNOWN_ERROR,
            explanationArabic = "حدث خطأ أثناء جلب إحداثيات الموقع: ${e.localizedMessage}",
            explanationEnglish = "Error fetching location: ${e.localizedMessage}",
            fallbackCoordinates = null,
          )
        )
      }
    }
  }

  fun getCurrentLocation(
    context: Context,
    onResult: (GpsCoordinates) -> Unit,
  ) {
    searchLocationWithDiagnostics(context = context, isOnline = true) { result ->
      when (result) {
        is LocationSearchResult.Success -> onResult(result.coordinates)
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
