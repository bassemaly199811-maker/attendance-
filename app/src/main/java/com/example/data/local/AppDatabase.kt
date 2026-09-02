package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.BuildConfig
import com.example.data.model.ActivityLog
import com.example.data.model.AttendanceRecord
import com.example.data.model.AttendanceStatus
import com.example.data.model.DeviceSecurityAlert
import com.example.data.model.LeaveBalance
import com.example.data.model.LeaveRequest
import com.example.data.model.LeaveStatus
import com.example.data.model.LeaveType
import com.example.data.model.UserAccount
import com.example.data.model.UserRole
import com.example.data.model.WorkShiftConfig
import com.example.data.model.WorkSite
import com.example.data.model.WorkerEntity
import at.favre.lib.crypto.bcrypt.BCrypt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
  @TypeConverter
  fun fromAttendanceStatus(value: AttendanceStatus): String = value.name

  @TypeConverter
  fun toAttendanceStatus(value: String): AttendanceStatus =
    try {
      AttendanceStatus.valueOf(value)
    } catch (_: Exception) {
      AttendanceStatus.NOT_CHECKED_IN
    }

  @TypeConverter
  fun fromUserRole(value: UserRole): String = value.name

  @TypeConverter
  fun toUserRole(value: String): UserRole =
    try {
      UserRole.valueOf(value)
    } catch (_: Exception) {
      UserRole.WORKER
    }

  @TypeConverter
  fun fromLeaveType(value: LeaveType): String = value.name

  @TypeConverter
  fun toLeaveType(value: String): LeaveType =
    try {
      LeaveType.valueOf(value)
    } catch (_: Exception) {
      LeaveType.ANNUAL
    }

  @TypeConverter
  fun fromLeaveStatus(value: LeaveStatus): String = value.name

  @TypeConverter
  fun toLeaveStatus(value: String): LeaveStatus =
    try {
      LeaveStatus.valueOf(value)
    } catch (_: Exception) {
      LeaveStatus.PENDING
    }
}

@Database(
  entities = [
    AttendanceRecord::class,
    WorkSite::class,
    WorkerEntity::class,
    ActivityLog::class,
    WorkShiftConfig::class,
    UserAccount::class,
    LeaveRequest::class,
    LeaveBalance::class,
    DeviceSecurityAlert::class,
  ],
  version = 9,
  exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
  abstract fun attendanceDao(): AttendanceDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance =
          Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "work_attendance_database",
          )
            .fallbackToDestructiveMigration()
            .addCallback(DatabaseCallback(scope))
            .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(private val scope: CoroutineScope) :
      RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            populateInitialData(database.attendanceDao())
          }
        }
      }

      suspend fun populateInitialData(dao: AttendanceDao) {
        // 1. Shift Configuration
        dao.insertShiftConfig(
          WorkShiftConfig(
            id = "DEFAULT_SHIFT",
            shiftName = "Default Official Shift",
            startTime = "08:00 AM",
            endTime = "04:30 PM",
            startHour = 8,
            startMinute = 0,
            endHour = 16,
            endMinute = 30,
            gracePeriodMinutes = 15,
          )
        )

        // 2. Default Work Sites
        val defaultSites =
          listOf(
            WorkSite(
              id = "SITE-JED-01",
              name = "Jeddah Central Hub",
              latitude = 21.543333,
              longitude = 39.172778,
              radiusMeters = 100,
              address = "King Fahd Road, Industrial Area, Jeddah",
            ),
            WorkSite(
              id = "SITE-RUH-02",
              name = "Al-Nakheel Tower Project",
              latitude = 24.713552,
              longitude = 46.675296,
              radiusMeters = 150,
              address = "Al-Nakheel District, King Fahd Road, Riyadh",
            ),
            WorkSite(
              id = "SITE-DMM-03",
              name = "Dammam Distribution Station",
              latitude = 26.4207,
              longitude = 50.0888,
              radiusMeters = 120,
              address = "King Abdulaziz Port, Dammam",
            ),
          )
        dao.insertWorkSites(defaultSites)

        // 3. Default Workers
        val defaultWorkers =
          listOf(
            WorkerEntity(
              id = "EMP-9821",
              fullName = "Ahmed Mohamed",
              initials = "AM",
              role = "Field Supervisor - Technician",
              siteId = "SITE-JED-01",
              siteName = "Jeddah Central Hub",
              nationalId = "1098234812",
              phoneNumber = "+966 50 123 4567",
              deviceModel = "Samsung Galaxy A54 5G",
              isDeviceApproved = true,
              assignedSiteIds = "SITE-JED-01,SITE-RUH-02",
              assignedSiteNames = "Jeddah Central Hub, Al-Nakheel Tower Project",
              iqamaNumber = "2498234812",
              iqamaStartDate = "2025-09-01",
              iqamaEndDate = "2026-08-29",
              insuranceNumber = "POL-882190-BUPA",
              insuranceProvider = "Bupa Arabia Insurance",
              insuranceStartDate = "2025-09-10",
              insuranceEndDate = "2026-09-05",
              passportNumber = "A19823412",
              nationality = "Resident",
              contractEndDate = "2027-08-30",
              salary = 5500.0,
              hireDate = "2023-01-15",
            ),
            WorkerEntity(
              id = "EMP-9822",
              fullName = "Khaled Al-Otaibi",
              initials = "KO",
              role = "Electrical & Equipment Tech",
              siteId = "SITE-RUH-02",
              siteName = "Al-Nakheel Tower Project",
              nationalId = "1087654321",
              phoneNumber = "+966 55 987 6543",
              deviceModel = "iPhone 14 Pro",
              isDeviceApproved = true,
              assignedSiteIds = "SITE-RUH-02",
              assignedSiteNames = "Al-Nakheel Tower Project",
              iqamaNumber = "1087654321",
              iqamaStartDate = "2025-09-01",
              iqamaEndDate = "2026-09-01",
              insuranceNumber = "POL-992140-TAWUNIYA",
              insuranceProvider = "Tawuniya Insurance Company",
              insuranceStartDate = "2025-10-15",
              insuranceEndDate = "2026-10-15",
              passportNumber = "K87654321",
              nationality = "Saudi",
              contractEndDate = "2027-10-01",
              salary = 6200.0,
              hireDate = "2022-06-01",
            ),
            WorkerEntity(
              id = "EMP-9823",
              fullName = "Sarah Al-Shammari",
              initials = "SS",
              role = "Safety & Quality Engineer",
              siteId = "SITE-JED-01",
              siteName = "Jeddah Central Hub",
              nationalId = "1076543219",
              phoneNumber = "+966 54 321 0987",
              deviceModel = "Google Pixel 8",
              isDeviceApproved = true,
              assignedSiteIds = "SITE-JED-01,SITE-DMM-03",
              assignedSiteNames = "Jeddah Central Hub, Dammam Distribution Station",
              iqamaNumber = "1076543219",
              iqamaStartDate = "2026-01-01",
              iqamaEndDate = "2027-01-01",
              insuranceNumber = "POL-771234-MEDGULF",
              insuranceProvider = "MedGulf Insurance",
              insuranceStartDate = "2026-02-01",
              insuranceEndDate = "2027-02-01",
              passportNumber = "S76543219",
              nationality = "Saudi",
              contractEndDate = "2028-01-01",
              salary = 7800.0,
              hireDate = "2021-11-10",
            ),
            WorkerEntity(
              id = "EMP-9824",
              fullName = "Faisal Al-Dossary",
              initials = "FD",
              role = "Logistics Transport Driver",
              siteId = "SITE-DMM-03",
              siteName = "Dammam Distribution Station",
              nationalId = "1065432198",
              phoneNumber = "+966 56 654 3210",
              deviceModel = "Xiaomi 13T",
              isDeviceApproved = true,
              assignedSiteIds = "SITE-DMM-03,SITE-RUH-02,SITE-JED-01",
              assignedSiteNames = "Dammam Distribution Station, Al-Nakheel Tower Project, Jeddah Central Hub",
              iqamaNumber = "2065432198",
              iqamaStartDate = "2025-08-01",
              iqamaEndDate = "2026-08-22",
              insuranceNumber = "POL-664422-ALRAJHI",
              insuranceProvider = "Al Rajhi Takaful",
              insuranceStartDate = "2025-09-01",
              insuranceEndDate = "2026-09-03",
              passportNumber = "F65432198",
              nationality = "Resident",
              contractEndDate = "2026-09-01",
              salary = 4300.0,
              hireDate = "2024-03-20",
            ),
          )
        dao.insertWorkers(defaultWorkers)

        // 4. Default User Accounts (admin / 123, ahmed / 123) - ONLY in DEBUG builds
        if (BuildConfig.DEBUG) {
          val seededHash = BCrypt.withDefaults().hashToString(12, "123".toCharArray())
          val defaultUsers = listOf(
            UserAccount(
              username = "admin",
              passwordHash = seededHash,
              role = UserRole.ADMIN,
              workerId = "ADMIN-01",
              workerName = "System Administrator",
            ),
            UserAccount(
              username = "ahmed",
              passwordHash = seededHash,
              role = UserRole.WORKER,
              workerId = "EMP-9821",
              workerName = "Ahmed Mohamed",
            ),
          )
          dao.insertUsers(defaultUsers)
        }

        // 5. Default Leave Balances
        val defaultBalances = listOf(
          LeaveBalance(
            workerId = "EMP-9821",
            annualTotal = 21.0,
            annualUsed = 0.0,
            casualTotal = 7.0,
            casualUsed = 0.0,
            sickTotal = 14.0,
            sickUsed = 0.0,
          ),
          LeaveBalance(
            workerId = "EMP-9822",
            annualTotal = 21.0,
            annualUsed = 3.0,
            casualTotal = 7.0,
            casualUsed = 1.0,
            sickTotal = 14.0,
            sickUsed = 0.0,
          ),
        )
        dao.insertLeaveBalances(defaultBalances)

        // 6. Realistic previous attendance records
        val pastRecords =
          listOf(
            AttendanceRecord(
              workDate = "2026-08-20",
              workerName = "Ahmed Mohamed",
              siteName = "Jeddah Central Hub",
              checkInTime = "07:54 AM",
              checkInLat = 21.543320,
              checkInLng = 39.172790,
              checkInAccuracy = 8.5,
              checkInDistanceMeters = 12.4,
              checkOutTime = "04:32 PM",
              checkOutLat = 21.543330,
              checkOutLng = 39.172780,
              checkOutAccuracy = 6.2,
              checkOutDistanceMeters = 8.1,
              status = AttendanceStatus.CHECKED_OUT,
              isLate = false,
              isVerified = true,
              notes = "Regular attendance",
            ),
            AttendanceRecord(
              workDate = "2026-08-19",
              workerName = "Ahmed Mohamed",
              siteName = "Jeddah Central Hub",
              checkInTime = "08:12 AM",
              checkInLat = 21.543340,
              checkInLng = 39.172800,
              checkInAccuracy = 11.2,
              checkInDistanceMeters = 18.0,
              checkOutTime = "04:30 PM",
              checkOutLat = 21.543310,
              checkOutLng = 39.172760,
              checkOutAccuracy = 7.0,
              checkOutDistanceMeters = 14.5,
              status = AttendanceStatus.CHECKED_OUT,
              isLate = true,
              isVerified = true,
              notes = "Traffic delay (12 mins)",
            ),
            AttendanceRecord(
              workDate = "2026-08-18",
              workerName = "Ahmed Mohamed",
              siteName = "Jeddah Central Hub",
              checkInTime = "07:48 AM",
              checkInLat = 21.543315,
              checkInLng = 39.172770,
              checkInAccuracy = 9.1,
              checkInDistanceMeters = 9.8,
              checkOutTime = "04:35 PM",
              checkOutLat = 21.543335,
              checkOutLng = 39.172775,
              checkOutAccuracy = 5.4,
              checkOutDistanceMeters = 6.3,
              status = AttendanceStatus.CHECKED_OUT,
              isLate = false,
              isVerified = true,
              notes = "Regular attendance",
            ),
          )
        pastRecords.forEach { dao.insertAttendance(it) }

        // Initial logs
        dao.insertActivityLog(
          ActivityLog(
            timestamp = "2026-08-21 07:00:00",
            actionType = "DEVICE_VERIFIED",
            isSuccessful = true,
            details = "Device fingerprint verified successfully for Samsung Galaxy A54",
            distanceMeters = null,
            workerName = "Ahmed Mohamed",
          )
        )
      }
    }
  }
}

