# Architecture Checklist

Những lỗi thường gặp khi thiết kế hệ thống, chia theo thời điểm nên kiểm tra.

---

## Phase 1: Requirements — Hỏi trước khi bắt tay vào làm

### App sẽ chạy mấy instance?

- Nếu nhiều hơn 1 → kiểm tra 2 vấn đề:
  - App có lưu dữ liệu trên filesystem hoặc memory (cache) không?
    - Nếu có → dữ liệu đó by default là isolated hay shared?
      - Cách kiểm tra: thứ đó có gọi qua network không? Có → shared. Không → isolated
      - Nếu isolated:
        - Ổn nếu dữ liệu đó không cần chia sẻ (vd: local temp file chỉ dùng trong 1 request)
        - Không ổn nếu instance khác cần thấy dữ liệu đó (vd: user upload file ở instance A, instance B phải đọc được)
  - App có scheduled job không?
    - Nếu có → job đó chạy trùng nhiều lần có gây lỗi không?
      - Ổn nếu job là idempotent (chạy bao nhiêu lần cũng cho kết quả giống nhau)
      - Không ổn nếu job gây side effect (gửi email, publish event, xử lý đơn hàng)
- Nếu chỉ 1 → không có vấn đề, nhưng ghi nhận lại để biết khi nào scale lên cần revisit

---

## Phase 2: Design — Kiểm tra khi chọn giải pháp

Khi chạy multi-instance, có 2 loại vấn đề thường gặp:

### Sai lầm: Dữ liệu bị isolated — mỗi instance có filesystem và memory riêng

**Chuyện gì xảy ra:** Ghi file bằng `Files.write()`, cache bằng `HashMap` — dữ liệu chỉ tồn tại trên instance đó. Instance khác không thấy. Chạy 1 instance thì đúng, tăng lên 2 thì lỗi âm thầm: mất file, cache lệch.

**Cách nhận biết:** Thứ mình đang dùng có gọi qua network không?
- Có (Database, Redis, S3, Kafka) → tất cả instance đều thấy, an toàn
- Không (filesystem, memory) → mỗi instance một bản riêng, nguy hiểm

**Cách sửa:**

| Thứ đang dùng | Lỗi khi multi-instance | Thay bằng |
|---|---|---|
| Local filesystem | Instance A ghi, instance B không thấy | Remote storage (S3, MinIO) hoặc shared volume (RWX) |
| In-memory cache | Mỗi instance cache riêng, dữ liệu lệch nhau | Distributed cache (Redis) |

- [ ] Có dùng resource nào chỉ tồn tại trên 1 instance không?
- [ ] Nếu có, instance khác có cần thấy dữ liệu này không?
- [ ] Nếu câu trả lời là "tùy cách setup infrastructure" → code đang phụ thuộc ngầm vào infrastructure, cần làm tường minh

### Sai lầm: Job bị chạy trùng — mỗi instance đều trigger cùng một scheduled job

**Chuyện gì xảy ra:** Dùng `@Scheduled` để chạy job định kỳ — mỗi instance đều chạy job riêng. 3 instance = job chạy 3 lần. Gửi email trùng, xử lý đơn hàng trùng, publish event trùng.

**Cách nhận biết:** App có `@Scheduled`, `@EnableScheduling`, cron job không? Nếu có và chạy multi-instance → chắc chắn bị trùng.

**Cách sửa:**

| Cách | Khi nào dùng |
|---|---|
| Distributed lock (`SELECT ... FOR UPDATE SKIP LOCKED`) | Đã có database, job cần chạy đúng 1 lần |
| ShedLock | Cần giải pháp có sẵn, không muốn tự viết lock |
| Tách scheduler thành service riêng chạy 1 instance | Job phức tạp, muốn isolate khỏi main app |

- [ ] Có dùng `@Scheduled` hoặc cron job không?
- [ ] Nếu có, job đó có chạy đúng nếu bị trigger nhiều lần cùng lúc không?

### Sai lầm: Để chi tiết của infrastructure lọt vào domain

**Chuyện gì xảy ra:** Domain model chứa `topic` (Kafka), `bucketName` (S3) — domain biết mình đang dùng Kafka/S3. Khi đổi sang hệ thống khác, phải sửa cả domain.

**Cách nhận biết:**
- Bỏ Kafka/S3/RabbitMQ đi, field này có còn ý nghĩa không? `domainType` = có, `topic` = không
- Nếu đổi Kafka sang RabbitMQ, domain model có phải sửa không?
- Service có đang inject config của infrastructure (`@Value("kafka.topics...")`) không?

**Cách sửa:**
- Dùng domain concept thay vì infrastructure concept (`DomainType.DRIVER` thay vì `"driver-events"`)
- Đẩy logic mapping xuống infrastructure layer (adapter, scheduler)

- [ ] Domain model có chứa field chỉ có nghĩa với 1 infrastructure cụ thể không?
- [ ] Service có đang inject config của infrastructure không?

---

## Phase 3: Code Review — Kiểm tra lần cuối trước khi merge

Rà lại 3 sai lầm ở Phase 2:

- [ ] Có dùng `Files.write()`, `new File()`, `HashMap` làm cache không? Nếu có, đã xử lý cho multi-instance chưa?
- [ ] Có dùng `@Scheduled` không? Nếu có, đã có cơ chế chống chạy trùng chưa?
- [ ] Domain model hoặc service có import trực tiếp infrastructure class (KafkaTemplate, S3Client, ...) không?
