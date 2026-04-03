# API Operation Types

## Phân loại theo Outcomes

| Loại                 | Outcomes                            | Đặc điểm                  |
| -------------------- | ----------------------------------- | ------------------------- |
| **Single Operation** | Success / Failure                   | 1 item → không có Partial |
| **Multi Operation**  | Success / Partial Success / Failure | Nhiều items → có Partial  |

## So sánh Bulk / Batch / Composite

|                                       | **Bulk**                   | **Batch**           | **Composite**                                 |
| ------------------------------------- | -------------------------- | ------------------- | --------------------------------------------- |
| **Giống nhau**                        |                            |                     |                                               |
| Multi Operation                       | x                          | x                   | x                                             |
| Outcomes: Success / Partial / Failure | x                          | x                   | x                                             |
| Gộp nhiều thao tác trong 1 request    | x                          | x                   | x                                             |
| **Khác nhau**                         |                            |                     |                                               |
| Loại thao tác                         | Cùng loại                  | Cùng hoặc khác loại | Khác loại                                     |
| Xử lý                                 | Đồng bộ                    | Bất đồng bộ         | Đồng bộ                                       |
| Kết quả trả về                        | Ngay lập tức               | Poll sau (job ID)   | Ngay lập tức                                  |
| **Ví dụ**                             | Lấy 100 documents cùng lúc | Gửi 10,000 emails   | 1 request: tạo user + assign role + gửi email |

## So sánh Normal Endpoint vs Composite

|                | **Normal Endpoint**                                          | **Composite**                                                        |
| -------------- | ------------------------------------------------------------ | -------------------------------------------------------------------- |
| Thao tác con   | Server quyết định (internal logic)                           | **Client chỉ định** trong request                                    |
| Client biết gì | Chỉ biết 1 kết quả cuối cùng                                 | Biết kết quả **từng thao tác con**                                   |
| Ví dụ          | `POST /users` → server tự tạo user, hash password, gửi email | `POST /composite` → client gửi: "tạo user + assign role + gửi email" |
