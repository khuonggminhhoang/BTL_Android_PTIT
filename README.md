#  JobFinder - Ứng dụng Tìm kiếm Việc làm trên Android

JobFinder là một ứng dụng di động Android được thiết kế để giúp người dùng dễ dàng tìm kiếm và ứng tuyển các công việc phù hợp với kỹ năng và mong muốn của họ. Ứng dụng cung cấp một giao diện trực quan để duyệt qua các danh sách việc làm, xem chi tiết công việc, quản lý hồ sơ cá nhân và theo dõi quá trình ứng tuyển.

---

## 📁 Cấu trúc thư mục chính (Simplified)
```
BTL_Android_PTIT/

├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/foodorderapp/  # Mã nguồn chính của ứng dụng
│   │   │   │   ├── activity/                   # Các Activity (Màn hình) cũ (có thể đã được refactor)
│   │   │   │   ├── config/                     # Cấu hình (ví dụ: URL backend)
│   │   │   │   ├── core/model/                 # Các lớp Model (POJO) cho dữ liệu
│   │   │   │   ├── features/                   # Các module tính năng chính của ứng dụng
│   │   │   │   │   ├── auth/                   #   Xác thực người dùng (Đăng nhập, Đăng ký,...)
│   │   │   │   │   ├── jobs/                   #   Quản lý và hiển thị việc làm
│   │   │   │   │   ├── main/                   #   Màn hình chính và các fragment con
│   │   │   │   │   ├── profile/                #   Quản lý hồ sơ người dùng
│   │   │   │   │   ├── settings/               #   Cài đặt ứng dụng
│   │   │   │   │   └── welcome/                #   Màn hình chào mừng, giới thiệu
│   │   │   │   └── network/                    #   Xử lý mạng (API Service, Request, Response)
│   │   │   ├── res/                            # Tài nguyên của ứng dụng
│   │   │   │   ├── drawable/                   # Hình ảnh, icon, shape,...
│   │   │   │   ├── layout/                     # Các tệp XML định nghĩa giao diện
│   │   │   │   ├── menu/                       # Các tệp XML định nghĩa menu
│   │   │   │   ├── values/                     # Các giá trị như strings, colors, styles,...
│   │   │   │   └── xml/                        # Các tệp XML cấu hình (ví dụ: network security)
│   │   │   └── AndroidManifest.xml             # Tệp kê khai chính của ứng dụng
│   │   └── test/                             # Mã nguồn cho Unit Test
│   │   └── androidTest/                      # Mã nguồn cho Instrumented Test
│   ├── build.gradle.kts                      # Cấu hình build cho module app
│   └── proguard-rules.pro                  # Cấu hình Proguard cho việc rút gọn mã
├── gradle/libs.versions.toml                 # Định nghĩa phiên bản thư viện tập trung
├── build.gradle.kts                          # Cấu hình build cho toàn bộ dự án
└── settings.gradle.kts                       # Cấu hình các module trong dự án
```
---

## ✨ Tính năng nổi bật

* **Xác thực người dùng:** Đăng ký, đăng nhập, quên mật khẩu, xác thực OTP.
* **Tìm kiếm việc làm:**
    * Tìm kiếm theo từ khóa, địa điểm.
    * Lọc kết quả theo loại công việc, mức lương.
    * Xem danh sách công việc theo danh mục.
* **Chi tiết công việc:** Hiển thị thông tin chi tiết về công việc, công ty.
* **Ứng tuyển trực tuyến:** Cho phép người dùng nộp CV và thư xin việc.
* **Quản lý hồ sơ:**
    * Cập nhật thông tin cá nhân (tên, email, số điện thoại, địa chỉ, ngày sinh).
    * Tải lên và quản lý CV (PDF).
    * Thêm, sửa, xóa kinh nghiệm làm việc.
    * Thêm, sửa, xóa kỹ năng.
    * Chỉnh sửa giới thiệu bản thân (About Me).
* **Công việc yêu thích:** Lưu lại các công việc quan tâm.
* **Thông báo:** Nhận thông báo về trạng thái ứng tuyển, công việc mới (dự kiến).
* **Giao diện người dùng:**
    * Thân thiện, dễ sử dụng với Material Design.
    * Điều hướng bằng Bottom Navigation.
    * Hỗ trợ chế độ tối (dự kiến).
* **Cài đặt:**
    * Quản lý thông báo ứng dụng.
    * Đăng xuất.
    * Liên hệ, chia sẻ, đánh giá ứng dụng.

---

## 🛠️ Công nghệ và Thư viện sử dụng

Dự án được xây dựng bằng Java/Kotlin và sử dụng các thư viện phổ biến trong hệ sinh thái Android:

* **Giao diện người dùng (UI):**
    * `androidx.appcompat:appcompat`: Hỗ trợ tương thích ngược cho các thành phần UI.
    * `com.google.android.material:material`: Cung cấp các Material Design Components (Button, CardView, TabLayout, BottomNavigationView, TextInputLayout,...).
    * `androidx.constraintlayout:constraintlayout`: Xây dựng layout linh hoạt.
    * `androidx.recyclerview:recyclerview`: Hiển thị danh sách hiệu quả.
* **Tải và hiển thị hình ảnh:**
    * `com.github.bumptech.glide:glide`: Thư viện mạnh mẽ để tải và cache hình ảnh.
* **Xử lý mạng (Networking):**
    * `com.android.volley:volley`: Sử dụng cho các tác vụ mạng cơ bản (chủ yếu trong module `auth`).
    * `com.squareup.retrofit2:retrofit`: HTTP client type-safe, giúp tương tác với API RESTful dễ dàng.
    * `com.squareup.retrofit2:converter-gson`: Chuyển đổi JSON sang đối tượng Java (POJO) và ngược lại, sử dụng thư viện Gson.
    * `com.squareup.okhttp3:okhttp`: Nền tảng HTTP client cho Retrofit.
    * `com.squareup.okhttp3:logging-interceptor`: Ghi log chi tiết các yêu cầu và phản hồi mạng.
* **Kiểm thử (Testing):**
    * `junit:junit`: Framework kiểm thử đơn vị.
    * `androidx.test.ext:junit`: Hỗ trợ JUnit cho Android Instrumented Tests.
    * `androidx.test.espresso:espresso-core`: Framework kiểm thử giao diện người dùng.
* **Cấu hình Build:**
    * Gradle Kotlin DSL (`build.gradle.kts`).
    * TOML Version Catalog (`gradle/libs.versions.toml`) để quản lý phiên bản thư viện.

---

## 🚀 Bắt đầu (Getting Started)

Để build và chạy dự án này, bạn cần:

1.  **Cài đặt Android Studio:** Phiên bản mới nhất được khuyến nghị.
2.  **Clone repository này:**
    ```bash
    git clone <URL_REPOSITORY_CUA_BAN>
    cd BTL_Android_PTIT
    ```
3.  **Mở dự án trong Android Studio.**
4.  **Đồng bộ Gradle:** Android Studio sẽ tự động đồng bộ các thư viện cần thiết.
5.  **Cấu hình URL Backend:**
    * Mở tệp `app/src/main/java/com/example/foodorderapp/config/Config.java`.
    * Thay đổi giá trị của `BE_URL` thành địa chỉ IP và cổng của máy chủ backend của bạn. Ví dụ:
        ```java
        public class Config {
            public static String BE_URL = "http://YOUR_BACKEND_IP_ADDRESS:PORT/api/v1";
        }
        ```
    * **Lưu ý quan trọng:** Để ứng dụng trên máy ảo Android (emulator) hoặc thiết bị thật có thể kết nối đến server backend đang chạy trên máy tính của bạn (localhost), bạn cần:
        * **Đối với Emulator:** Sử dụng địa chỉ IP đặc biệt `10.0.2.2` để trỏ đến `localhost` của máy host. Ví dụ: `http://10.0.2.2:3001/api/v1`.
        * **Đối với Thiết bị thật:** Đảm bảo thiết bị và máy tính chạy server backend đang kết nối cùng một mạng Wi-Fi. Sử dụng địa chỉ IP của máy tính trong mạng Wi-Fi đó. Ví dụ: `http://192.168.1.100:3001/api/v1` (thay `192.168.1.100` bằng IP thực tế của máy bạn).
        * Đảm bảo backend server của bạn cho phép kết nối từ các địa chỉ IP này và không bị firewall chặn.
        * Trong tệp `app/src/main/res/xml/network_security_config.xml`, dự án đã cho phép `cleartextTrafficPermitted="true"`. Điều này cần thiết nếu backend của bạn chạy trên HTTP thay vì HTTPS trong môi trường phát triển. **Không nên sử dụng cấu hình này cho môi trường production.**
6.  **Chạy ứng dụng:** Chọn một thiết bị (thật hoặc ảo) và nhấn nút Run.

---

## 📸 Ảnh chụp màn hình (Screenshots)


| Màn hình Đăng nhập                                 | Màn hình Trang chủ                                  | Màn hình Chi tiết Công việc                           |
| :--------------------------------------------------: | :----------------------------------------------------: | :------------------------------------------------------: |
| ![Login Screen](https://drive.google.com/file/d/1mmOXbg1nJh9C53hJ6PN1GjVDDM4YGEz9/view?usp=sharing) | ![Home Screen](https://drive.google.com/file/d/1mmOXbg1nJh9C53hJ6PN1GjVDDM4YGEz9/view) | ![Job Detail Screen](https://drive.google.com/drive/u/0/folders/1mlmAOFK-rUd3gfVLJdAfykACBmJJNpjA) |
