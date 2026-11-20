# Android ProjectBase Template (AF_ProjectBase Standard)

Bộ template dự án gốc Android chuyên nghiệp, được đồng bộ chuẩn xác theo kiến trúc và thư mục mẫu của **AF_ProjectBase**, hỗ trợ song song **Kotlin XML (MVVM)** và dễ dàng mở rộng sang **Kotlin Jetpack Compose**.

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
