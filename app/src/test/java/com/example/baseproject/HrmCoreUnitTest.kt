package com.example.baseproject

import com.example.baseproject.data.remote.dto.attendance.ShiftDto
import com.example.baseproject.data.remote.dto.leave.LeaveBalanceDto
import com.example.baseproject.data.remote.dto.leave.LeaveRequestDto
import com.example.baseproject.core.common.ApiErrorParser
import com.example.baseproject.core.helper.RoleHelper
import com.example.baseproject.core.helper.RoleType
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class HrmCoreUnitTest {

    private val gson = Gson()

    @Test
    fun roleHelper_parsesAllBackendRolesCorrectly() {
        assertEquals(RoleType.ADMIN, RoleHelper.parseRole("1"))
        assertEquals(RoleType.ADMIN, RoleHelper.parseRole("ADMIN"))
        assertEquals(RoleType.HR, RoleHelper.parseRole("4"))
        assertEquals(RoleType.HR, RoleHelper.parseRole("HR"))
        assertEquals(RoleType.MANAGER, RoleHelper.parseRole("2"))
        assertEquals(RoleType.MANAGER, RoleHelper.parseRole("6"))
        assertEquals(RoleType.MANAGER, RoleHelper.parseRole("8"))
        assertEquals(RoleType.EMPLOYEE, RoleHelper.parseRole("EMPLOYEE"))
        assertEquals(RoleType.EMPLOYEE, RoleHelper.parseRole(null))

        assertTrue(RoleHelper.isManagementRole(RoleType.ADMIN))
        assertTrue(RoleHelper.isManagementRole(RoleType.HR))
        assertTrue(RoleHelper.isManagementRole(RoleType.MANAGER))
        assertFalse(RoleHelper.isManagementRole(RoleType.EMPLOYEE))
    }

    @Test
    fun apiErrorParser_extractsBackendMessageOrFallback() {
        val jsonError = """{"success":false,"message":"Bạn không có quyền phê duyệt.","data":null}"""
        val errorResponse = Response.error<String>(
            403,
            jsonError.toResponseBody("application/json".toMediaType())
        )
        assertEquals(
            "Bạn không có quyền phê duyệt.",
            ApiErrorParser.extractErrorMessage(errorResponse, "Fallback")
        )

        val emptyErrorResponse = Response.error<String>(
            500,
            "".toResponseBody("text/plain".toMediaType())
        )
        assertEquals(
            "Lỗi máy chủ (500). Vui lòng thử lại sau.",
            ApiErrorParser.extractErrorMessage(emptyErrorResponse, "Fallback")
        )
    }

    @Test
    fun leaveBalanceDto_deserializesBackendFields() {
        val json = """
            {
              "employeeId": 264,
              "employeeCode": "NV00264",
              "employeeFullName": "Giàng Seo Chính",
              "year": 2026,
              "totalAllocated": 14.0,
              "usedDays": 2.5,
              "pendingDays": 1.0,
              "remainingDays": 11.5
            }
        """.trimIndent()

        val dto = gson.fromJson(json, LeaveBalanceDto::class.java)
        assertEquals(264, dto.employeeId)
        assertEquals(14.0, dto.standardLeaveDays, 0.001)
        assertEquals(2.5, dto.usedLeaveDays, 0.001)
        assertEquals(11.5, dto.remainingLeaveDays, 0.001)
    }

    @Test
    fun shiftDto_deserializesBackendFields() {
        val json = """
            {
              "id": "HC",
              "name": "Hành chính",
              "isDefault": true,
              "normalTimeIn": "08:00:00",
              "normalTimeOut": "17:00:00",
              "lateComing": 5,
              "earlyReturning": 5
            }
        """.trimIndent()

        val dto = gson.fromJson(json, ShiftDto::class.java)
        assertEquals("HC", dto.id)
        assertEquals("Hành chính", dto.name)
        assertEquals(true, dto.isDefault)
        assertEquals("08:00:00", dto.normalTimeIn)
        assertEquals("17:00:00", dto.normalTimeOut)
    }

    @Test
    fun leaveRequestDto_isPendingReflectsStatus() {
        val pendingNull = LeaveRequestDto(status = null)
        val pendingZero = LeaveRequestDto(status = 0)
        val approved = LeaveRequestDto(status = 1)
        val rejected = LeaveRequestDto(status = 2)
        val cancelled = LeaveRequestDto(status = 3)

        assertTrue(pendingNull.isPending)
        assertTrue(pendingZero.isPending)
        assertFalse(approved.isPending)
        assertFalse(rejected.isPending)
        assertFalse(cancelled.isPending)
    }
}
