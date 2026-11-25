# HRM App - Ứng Dụng Chấm Công & Quản Lý Nhân Sự Android

Ứng dụng di động Chấm công và Quản lý nhân sự (HRM), phát triển trên nền tảng Android với kiến trúc **Clean Architecture + MVVM** (Kotlin, Hilt DI, Retrofit, Coroutines/StateFlow, ViewBinding).

---

## 📲 Tải Về Ứng Dụng (Download APK)

Người dùng và kiểm thử viên có thể tải trực tiếp file APK phiên bản mới nhất tại đây:

[![Download APK](https://img.shields.io/badge/Download-HRM__App__v1.0.apk-2563EB?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Mainca123/HRM-app/raw/main/HRM_App_v1.0.apk)

- 🔗 **Link tải trực tiếp (mới nhất trên `main`):** [Tải xuống HRM_App_v1.0.apk](https://github.com/Mainca123/HRM-app/raw/main/HRM_App_v1.0.apk)
- 📦 **Trang phát hành (Releases):** [GitHub Releases](https://github.com/Mainca123/HRM-app/releases)
- 📌 **Phiên bản:** `v1.0.0` (Debug build)
- 📱 **Hỗ trợ:** Android 7.0 (API 24) trở lên

---

## ✨ Tính năng Nổi bật
- **Chấm công GPS thông minh:** Check-in / Check-out trực tiếp kèm tọa độ GPS, hộp thoại xác nhận chống bấm nhầm và khóa trạng thái sau khi hoàn thành ca.
- **Lịch sử & Bảng công tháng:** Theo dõi chi tiết giờ vào/ra, trạng thái đi muộn/về sớm/thiếu giờ ra, số công chuẩn, giờ tăng ca và quỹ phép năm.
- **Quản lý đơn từ:** Tạo mới, chỉnh sửa, hủy/xóa đơn **Nghỉ phép**, **Tăng ca (OT)** và **Giải trình đi muộn/về sớm**.
- **Quản lý & Phê duyệt (Manager / HR / Admin):** Xem thống kê chuyên cần theo phòng ban và phê duyệt / từ chối đơn từ cấp dưới ngay trên ứng dụng.
- **Tài khoản & Đa ngôn ngữ:** Xem hồ sơ cá nhân, đổi mật khẩu (có xác nhận & ẩn/hiện mật khẩu), chuyển đổi mượt mà giữa **Tiếng Việt** và **English**.

---

## 📁 Kiến trúc & Cấu trúc Thư mục (Clean Architecture + MVVM)

```text
com.example.baseproject/
├── App.kt                              # Application class (Hilt entry point & khởi tạo locale)
│
├── core/                               # Tầng dùng chung toàn ứng dụng
│   ├── base/                           # BaseActivity, BaseFragment, BaseViewModel, BaseAdapter, BaseDialog, SingleLiveEvent
│   ├── common/                         # ApiErrorParser, Constant, Logger, Resource, Utils
│   ├── extensions/                     # ContextExt, ViewExt
│   ├── helper/                         # LocaleHelper, LocationHelper, ModernDialogHelper, RoleHelper
│   └── storage/                        # SpManager (SharedPreferences / JWT Token)
│
├── di/                                 # Hilt Dependency Injection Modules
│   ├── AppModule.kt                    # SharedPreferences, SpManager
│   └── NetworkModule.kt                # OkHttpClient, Custom DNS, Retrofit, ApiService
│
├── data/                               # Tầng Dữ liệu (Remote API + Local + Repository)
│   ├── local/                          # LocalDataSource (dữ liệu ngôn ngữ & onboarding)
│   ├── remote/
│   │   ├── api/                        # ApiService (Retrofit endpoints)
│   │   ├── interceptor/                # AuthInterceptor (tự động gắn Bearer JWT Token)
│   │   └── dto/                        # attendance, auth, common, dashboard, employee, leave, overtime
│   └── repository/                     # AttendanceRepository, AuthRepository, DashboardRepository,
│                                       # LeaveRepository, LocalRepository, OvertimeRepository
│
├── domain/                             # Tầng Nghiệp vụ
│   ├── model/                          # Language, OnBoarding
│   └── usecase/                        # UseCase, GetListLanguageUseCase, GetListOnBoardingUseCase
│
└── presentation/                       # Tầng Giao diện (UI gom theo từng Feature)
    ├── common/                         # LoadingDialog
    ├── splash/                         # SplashActivity, SplashViewModel
    ├── language/                       # LanguageActivity, LanguageAdapter, LanguageViewModel
    ├── onboarding/                     # OnboardingActivity, OnBoardingAdapter, OnboardingViewModel
    ├── auth/
    │   ├── login/                      # LoginActivity, LoginViewModel
    │   └── register/                   # RegisterActivity, RegisterViewModel
    ├── main/                           # MainActivity, MainViewModel (Bottom Navigation)
    ├── home/                           # HomeFragment, HomeViewModel (Chấm công GPS & tổng quan)
    ├── attendance/
    │   ├── history/                    # HistoryFragment, HistoryViewModel, AttendanceHistoryAdapter
    │   └── timesheet/                  # TimesheetActivity (Bảng công & quỹ phép tháng)
    ├── requests/
    │   ├── RequestsFragment.kt         # Danh sách đơn từ cá nhân
    │   ├── RequestsViewModel.kt
    │   ├── adapter/                    # LeaveRequestAdapter, OvertimeAdapter, ExplanationRequestAdapter
    │   └── form/                       # CreateLeaveActivity, CreateOvertimeActivity, CreateExplanationActivity
    ├── management/                     # ApprovalActivity, ApprovalAdapter, DepartmentStatsActivity
    └── profile/                        # ProfileFragment, ProfileViewModel
```

---

## 🛠 Công nghệ Sử dụng
- **Ngôn ngữ:** Kotlin
- **Kiến trúc:** Clean Architecture + MVVM (StateFlow / LiveData + ViewBinding)
- **Dependency Injection:** Dagger Hilt
- **Networking:** Retrofit 2, OkHttp 3, Gson
- **Async:** Kotlin Coroutines
- **Testing:** JUnit 4 (`HrmCoreUnitTest`)

---

## 🚀 Hướng dẫn Build & Kiểm thử

```bash
# Chạy toàn bộ Unit Test
./gradlew testDebugUnitTest

# Build file APK Debug
./gradlew assembleDebug
```

---

## 🔄 Luồng Khởi động Ứng dụng (App Flow)
1. **SplashActivity**: Kiểm tra trạng thái đăng nhập / ngôn ngữ đã lưu.
2. **LanguageActivity**: Chọn ngôn ngữ hiển thị (`LocaleHelper`).
3. **OnboardingActivity**: Giới thiệu nhanh các tính năng chính cho lần mở đầu tiên.
4. **LoginActivity / RegisterActivity**: Đăng nhập bằng tài khoản nhân sự kết nối Backend ASP.NET Core (`https://hrm-tmyj.onrender.com`).
5. **MainActivity**: Điều hướng 4 tab chính — **Trang chủ (Chấm công)**, **Lịch sử**, **Đơn từ**, **Cá nhân**.
