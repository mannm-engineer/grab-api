# Task
Implement endpoint **Create Order** theo pattern của **create Driver**
trong reference repo `grab-api`.
Scope: chỉ implement, KHÔNG viết test.

# Reference

Reference repo: `grab-api` (cố định cho mọi API).
Đường dẫn local lấy từ env var `GRAB_API_ROOT`.

Entry point của luồng reference (create Driver):
`$GRAB_API_ROOT/src/main/java/com/grab/api/controller/api/DriverApi.java`

> Lưu ý: target repo (nơi viết code mới) có thể trùng hoặc khác grab-api.
> Mọi command đọc reference (`git log`, đọc file...) phải chạy trên
> `$GRAB_API_ROOT`, KHÔNG phải trên target repo.

# Preconditions

Trước khi bắt đầu workflow, kiểm tra:

1. **Env `GRAB_API_ROOT` đã set và tồn tại.**
   Nếu thiếu → abort, yêu cầu user set env.

2. **Stack match.** Target repo phải cùng stack với grab-api
   (Java + Spring Boot + Maven/Gradle, JPA, etc.).
   Nếu khác stack → **abort**, không tự dịch pattern sang stack khác.

# Constraints

1. **Chỉ dùng pattern đã có trong reference create Driver
   (`$GRAB_API_ROOT`).** Mọi annotation, layer, naming, error handling
   phải có precedent trong luồng đó.

2. **Stop-and-ask khi không có precedent.** Test: nếu đang nghĩ
   "có lẽ nên dùng X" thay vì "reference dùng X" → dừng và hỏi.
   Không đoán, không "best practice".

3. **Khi hỏi:** nêu (a) thứ không có precedent, (b) 2-3 phương án
   + recommendation. Chờ confirm trước khi tiếp tục.

4. **Không refactor code có sẵn** (cả ở target repo).

# Workflow

1. **Xem xét reference, list capabilities.**
   Trên `$GRAB_API_ROOT`, chạy `git log --oneline` và filter các commit
   có message bắt đầu bằng `feat(create-driver)` — mỗi commit là một
   capability. List theo thứ tự commit. Hỏi user chọn subset áp dụng
   cho task mới. Chờ confirm rồi mới đọc spec và sang bước 2.

2. **Checkpoint:** trước khi viết code, list các file sẽ tạo/sửa
   (đường dẫn trên target repo) và confirm với tôi.

# Spec

...
