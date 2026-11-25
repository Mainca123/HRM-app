package com.example.baseproject.core.helper

/**
 * Standard Role Enumeration for HRM System matching ASP.NET Core UserGroup IDs and names.
 */
enum class RoleType {
    ADMIN,      // ID = "1" / Administration / Admin
    HR,         // ID = "4" / Nhân sự / HR
    MANAGER,    // ID = "2" (Chấm công), "6" (XN1), "8" (XN3), Manager
    EMPLOYEE    // Default / Nhân viên thông thường
}

object RoleHelper {

    fun parseRole(role: String?): RoleType {
        if (role.isNullOrBlank()) return RoleType.EMPLOYEE
        val r = role.trim().uppercase()
        return when {
            r == "1" || r == "ADMIN" || r == "ADMINISTRATION" || r.contains("ADMIN") -> RoleType.ADMIN
            r == "4" || r == "HR" || r.contains("NHÂN SỰ") || r.contains("NHAN SU") -> RoleType.HR
            r in listOf("2", "6", "8", "MANAGER") ||
                    r.contains("QUẢN LÝ") ||
                    r.contains("QUAN LY") ||
                    r.contains("CHẤM CÔNG") ||
                    r.contains("CHAM CONG") -> RoleType.MANAGER
            else -> RoleType.EMPLOYEE
        }
    }

    fun getRoleBadge(roleType: RoleType): String {
        return when (roleType) {
            RoleType.ADMIN -> "Admin"
            RoleType.HR -> "HR"
            RoleType.MANAGER -> "Quản lý"
            RoleType.EMPLOYEE -> "Nhân viên"
        }
    }

    fun getRoleDisplayName(roleType: RoleType): String {
        return when (roleType) {
            RoleType.ADMIN -> "Quản trị viên (Admin)"
            RoleType.HR -> "Nhân sự (HR)"
            RoleType.MANAGER -> "Quản lý phòng ban"
            RoleType.EMPLOYEE -> "Nhân viên"
        }
    }

    fun isManagementRole(roleType: RoleType): Boolean {
        return roleType != RoleType.EMPLOYEE
    }
}
