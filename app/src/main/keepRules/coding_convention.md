# Android Developer Coding Convention (Sổ tay Coding Convention)

## 1. Quy tắc Viết Hàm trong Activity / Fragment (`initViews`)
- **KHÔNG** viết logic, gán sự kiện (click listener), hay cấu hình adapter trực tiếp một đống code dài dằng dặc bên trong `initViews()` hoặc `onCreate()`.
- **Hàm `initViews()` chỉ được phép gọi các hàm khởi tạo nhỏ** được tách bạch rõ ràng (ví dụ: `initAdapter()`, `initListeners()`, `initToolbar()`).
- Giúp code module hóa, dễ đọc, dễ bảo trì và dễ dò tìm lỗi.

## 2. Thứ tự Sắp xếp Hàm chuẩn (Function Order)
1. Các biến và thuộc tính (Properties)
2. Phương thức vòng đời (`onCreate`, `onResume`, `onStop`, `onDestroy`)
3. `provideViewBinding()`
4. Các hàm khởi tạo (`initViews()`, `initData()`, `initObserver()`)
5. Các hàm tách biệt khởi tạo chi tiết (`initListeners()`, `initAdapter()`, v.v.)
6. Các hàm xử lý sự kiện logic riêng (`on...Clicked()`, v.v.)
7. `companion object` (hàm `start(...)`)
