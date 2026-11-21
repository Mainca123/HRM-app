# HRM App - Ứng Dụng Chấm Công & Quản Lý Nhân Sự Android

Ứng dụng di động Chấm công và Quản lý nhân sự (HRM), phát triển trên nền tảng Android (Kotlin MVVM, Jetpack, Hilt, Retrofit, Coroutines).

---

## 📲 Tải Về Ứng Dụng (Download APK)

Người dùng và kiểm thử viên có thể tải trực tiếp file APK cài đặt phiên bản mới nhất tại đây:

[![Download APK](https://img.shields.io/badge/Download-HRM__App__v1.0.apk-2563EB?style=for-the-badge&logo=android&logoColor=white)](https://github.com/Mainca123/HRM-app/releases/download/v1.0.0/HRM_App_v1.0.apk)

- 🔗 **Link tải trực tiếp:** [Tải xuống HRM_App_v1.0.apk](https://github.com/Mainca123/HRM-app/releases/download/v1.0.0/HRM_App_v1.0.apk)
- 📦 **Trang phiên bản Release:** [GitHub Releases v1.0.0](https://github.com/Mainca123/HRM-app/releases/tag/v1.0.0)
- 📌 **Phiên bản:** `v1.0.0` (Build debug)
- 📱 **Hỗ trợ:** Android 7.0 (API 24) trở lên

---

## ✨ Tính năng Nổi bật
- **Chấm công thông minh:** Điểm danh ca làm việc, hỗ trợ nhận diện khuôn mặt / vị trí.
- **Lịch sử công:** Xem chi tiết thời gian check-in / check-out, ca làm việc, số giờ làm, số phút đi muộn/về sớm theo từng tháng.
- **Quản lý đơn từ:** Đăng ký nghỉ phép, làm thêm giờ (OT), và gửi đơn giải trình đi muộn/về sớm.
- **Duyệt đơn quản lý:** Phê duyệt / Từ chối đơn từ cấp dưới trực tiếp trên app.
- **Đa ngôn ngữ:** Hỗ trợ song ngữ Tiếng Việt & English mượt mà.

---

## 📁 Cấu trúc Thư mục Dự án

```text
com.example.baseproject/
│
├── App.kt                      # Application class (khởi tạo toàn cục)
│
├── base/                       # Các lớp cơ sở dùng chung
│   ├── activity/               # BaseActivity (ViewBinding, Toast, BackPress)
│   ├── adapter/                # BaseAdapter (generic RecyclerView adapter)
│   ├── dialog/                 # BaseDialog
│   ├── fragment/               # BaseFragment
│   └── viewmodel/              # BaseViewModel & SingleLiveEvent
│
├── ui/                         # Các màn hình tính năng (Flow: Splash -> Language -> OBD -> Login -> Register -> Main)
│   ├── splash/
│   ├── language/
│   ├── onboarding/
│   ├── login/
│   ├── register/
│   ├── main/
│   └── theme/
│
├── data/                       # Tầng dữ liệu (API, Repository, Database, Models)
├── navigation/                 # Điều hướng (Navigation graphs)
├── di/                         # Hilt Dependency Injection modules
└── utils/                      # Tiện ích (Constant, Ext, LocaleHelper, Logger, SpManager, Utils)
```

---

## 🚀 Hướng dẫn Sử dụng (Guide)

### 1. Dành cho phát triển bằng Kotlin XML (MVVM)
- **Base Activity/Fragment:** Kế thừa từ `BaseActivity<VB>` hoặc `BaseFragment<VB>`. Cung cấp sẵn hàm `viewBinding` và `showToast(...)`.
- **ViewModel:** Kế thừa từ `BaseViewModel` để quản lý sự kiện loading tự động.
- **Adapter:** Sử dụng `BaseAdapter` cho RecyclerView để tối ưu hóa code danh sách.

### 2. Dành cho mở rộng sang Kotlin Jetpack Compose
- Dự án đã được cấu hình sẵn **Jetpack Compose BOM & Compose Plugin** trong [`build.gradle.kts`](file:///D:/App_cham_cong/BaseProject/app/build.gradle.kts) (`compose = true`).
- Bạn có thể viết các Composable functions trực tiếp trong các package UI hoặc tạo Activity mới sử dụng `setContent { ... }`.

---

## 🔄 Luồng Khởi động Ứng dụng (App Flow)
1. **SplashActivity**: Kiểm tra trạng thái khởi động, chuyển đến `LanguageActivity`.
2. **LanguageActivity**: Chọn ngôn ngữ ứng dụng (`LocaleHelper`).
3. **OnboardingActivity**: Giới thiệu tính năng qua các bước.
4. **LoginActivity / RegisterActivity**: Đăng nhập / Đăng ký (Sẵn sàng tích hợp API Java Spring / ASP.NET).
5. **MainActivity**: Màn hình chính sau khi đăng nhập thành công.
