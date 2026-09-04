package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActivityLog
import com.example.data.model.AttendanceRecord
import com.example.data.model.DeviceSecurityAlert
import com.example.data.model.LeaveBalance
import com.example.data.model.LeaveRequest
import com.example.data.model.UserAccount
import com.example.data.model.WorkShiftConfig
import com.example.data.model.WorkSite
import com.example.data.model.WorkerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

  // Attendance Records
  @Query("SELECT * FROM attendance_records ORDER BY id DESC")
  fun getAllAttendanceRecords(): Flow<List<AttendanceRecord>>

  @Query("SELECT * FROM attendance_records ORDER BY id DESC")
  suspend fun getAllAttendanceRecordsDirect(): List<AttendanceRecord>

  @Query("SELECT * FROM attendance_records WHERE workDate = :date LIMIT 1")
  suspend fun getAttendanceForDate(date: String): AttendanceRecord?

  @Query("SELECT * FROM attendance_records WHERE workDate = :date LIMIT 1")
  fun getAttendanceForDateFlow(date: String): Flow<AttendanceRecord?>

  @Query("SELECT * FROM attendance_records WHERE workDate = :date AND (workerName = :workerName OR :workerName = '') LIMIT 1")
  suspend fun getAttendanceForDateAndWorker(date: String, workerName: String): AttendanceRecord?

  @Query("SELECT * FROM attendance_records WHERE workDate = :date AND (workerName = :workerName OR :workerName = '') LIMIT 1")
  fun getAttendanceForDateAndWorkerFlow(date: String, workerName: String): Flow<AttendanceRecord?>

  @Query("SELECT * FROM attendance_records WHERE workerName = :workerName ORDER BY id DESC")
  fun getAttendanceForWorkerFlow(workerName: String): Flow<List<AttendanceRecord>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAttendance(record: AttendanceRecord): Long

  @Update
  suspend fun updateAttendance(record: AttendanceRecord)

  @Delete
  suspend fun deleteAttendanceRecord(record: AttendanceRecord)

  @Query("DELETE FROM attendance_records WHERE id = :recordId")
  suspend fun deleteAttendanceById(recordId: Long)

  @Query("DELETE FROM attendance_records WHERE workDate = :date")
  suspend fun deleteAttendanceByDate(date: String)

  // Work Sites CRUD
  @Query("SELECT * FROM work_sites ORDER BY name ASC")
  fun getAllWorkSites(): Flow<List<WorkSite>>

  @Query("SELECT * FROM work_sites ORDER BY name ASC")
  suspend fun getAllWorkSitesDirect(): List<WorkSite>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWorkSites(sites: List<WorkSite>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWorkSite(site: WorkSite)

  @Update
  suspend fun updateWorkSite(site: WorkSite)

  @Delete
  suspend fun deleteWorkSite(site: WorkSite)

  @Query("DELETE FROM work_sites WHERE id = :siteId")
  suspend fun deleteWorkSiteById(siteId: String)

  // Workers CRUD
  @Query("SELECT * FROM workers ORDER BY fullName ASC")
  fun getAllWorkers(): Flow<List<WorkerEntity>>

  @Query("SELECT * FROM workers ORDER BY fullName ASC")
  suspend fun getAllWorkersDirect(): List<WorkerEntity>

  @Query("SELECT * FROM workers WHERE id = :workerId LIMIT 1")
  suspend fun getWorkerById(workerId: String): WorkerEntity?

  @Query("SELECT * FROM workers WHERE fullName = :fullName LIMIT 1")
  suspend fun getWorkerByName(fullName: String): WorkerEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWorkers(workers: List<WorkerEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertWorker(worker: WorkerEntity)

  @Update
  suspend fun updateWorker(worker: WorkerEntity)

  @Delete
  suspend fun deleteWorker(worker: WorkerEntity)

  @Query("DELETE FROM workers WHERE id = :workerId")
  suspend fun deleteWorkerById(workerId: String)

  // Shift Configuration
  @Query("SELECT * FROM shift_config WHERE id = 'DEFAULT_SHIFT' LIMIT 1")
  fun getShiftConfigFlow(): Flow<WorkShiftConfig?>

  @Query("SELECT * FROM shift_config WHERE id = 'DEFAULT_SHIFT' LIMIT 1")
  suspend fun getShiftConfig(): WorkShiftConfig?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertShiftConfig(config: WorkShiftConfig)

  // Activity Logs
  @Query("SELECT * FROM activity_logs ORDER BY id DESC LIMIT 100")
  fun getActivityLogs(): Flow<List<ActivityLog>>

  @Insert
  suspend fun insertActivityLog(log: ActivityLog)

  // User Accounts CRUD
  @Query("SELECT * FROM user_accounts ORDER BY username ASC")
  fun getAllUsersFlow(): Flow<List<UserAccount>>

  @Query("SELECT * FROM user_accounts ORDER BY username ASC")
  suspend fun getAllUsersDirect(): List<UserAccount>

  @Query("SELECT * FROM user_accounts WHERE username = :username LIMIT 1")
  suspend fun getUserByUsername(username: String): UserAccount?

  @Query("SELECT * FROM user_accounts WHERE workerId = :workerId LIMIT 1")
  suspend fun getUserByWorkerId(workerId: String): UserAccount?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUser(user: UserAccount)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertUsers(users: List<UserAccount>)

  @Update
  suspend fun updateUser(user: UserAccount)

  @Delete
  suspend fun deleteUser(user: UserAccount)

  @Query("DELETE FROM user_accounts WHERE username = :username")
  suspend fun deleteUserByUsername(username: String)

  @Query("DELETE FROM user_accounts WHERE workerId = :workerId")
  suspend fun deleteUserByWorkerId(workerId: String)

  // Leave Requests CRUD
  @Query("SELECT * FROM leave_requests ORDER BY id DESC")
  fun getAllLeaveRequestsFlow(): Flow<List<LeaveRequest>>

  @Query("SELECT * FROM leave_requests ORDER BY id DESC")
  suspend fun getAllLeaveRequestsDirect(): List<LeaveRequest>

  @Query("SELECT * FROM leave_requests WHERE workerId = :workerId ORDER BY id DESC")
  fun getLeaveRequestsForWorkerFlow(workerId: String): Flow<List<LeaveRequest>>

  @Query("SELECT * FROM leave_requests WHERE id = :id LIMIT 1")
  suspend fun getLeaveRequestById(id: Long): LeaveRequest?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLeaveRequest(request: LeaveRequest): Long

  @Update
  suspend fun updateLeaveRequest(request: LeaveRequest)

  @Delete
  suspend fun deleteLeaveRequest(request: LeaveRequest)

  @Query("DELETE FROM leave_requests WHERE id = :id")
  suspend fun deleteLeaveRequestById(id: Long)

  // Leave Balances CRUD
  @Query("SELECT * FROM leave_balances")
  fun getAllLeaveBalancesFlow(): Flow<List<LeaveBalance>>

  @Query("SELECT * FROM leave_balances WHERE workerId = :workerId LIMIT 1")
  fun getLeaveBalanceFlow(workerId: String): Flow<LeaveBalance?>

  @Query("SELECT * FROM leave_balances WHERE workerId = :workerId LIMIT 1")
  suspend fun getLeaveBalance(workerId: String): LeaveBalance?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLeaveBalance(balance: LeaveBalance)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLeaveBalances(balances: List<LeaveBalance>)

  @Update
  suspend fun updateLeaveBalance(balance: LeaveBalance)

  // Device Security Alerts
  @Query("SELECT * FROM device_alerts ORDER BY id DESC")
  fun getAllDeviceAlertsFlow(): Flow<List<DeviceSecurityAlert>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDeviceAlert(alert: DeviceSecurityAlert): Long

  @Query("UPDATE device_alerts SET isResolved = 1 WHERE id = :id")
  suspend fun resolveDeviceAlert(id: Long)
}
