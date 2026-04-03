# Problem Solving Framework

---

## Bước 1 — Hiểu đề

Đọc đề xong **chưa nghĩ cách giải vội**. Làm rõ 3 thứ trước:

**Input**
- Kiểu dữ liệu gì? (array, string, tree, graph…)
- Constraints: kích thước tối đa (`n`), phạm vi giá trị, có sorted không, có duplicate không, có null/empty không?

**Output**
- Trả về gì? (index, value, boolean, new array…)
- Constraints: thứ tự có quan trọng không, có cần in-place không, có nhiều đáp án hợp lệ không?

**Clarify**
- Hỏi lại interviewer nếu đề mơ hồ. Ví dụ: "Nếu không tìm thấy thì trả về gì?", "Input có luôn hợp lệ không?"
- Việc hỏi thể hiện bạn không vội vàng và biết cách làm việc với requirements không rõ ràng.

> **Ví dụ — Two Sum:** cho mảng số nguyên và một target, tìm hai chỉ số có tổng bằng target.
>
> Câu hỏi clarify:
> - Mảng có thể có số âm không? → Có (`-10⁹ <= nums[i] <= 10⁹`)
> - Mỗi input chỉ có đúng một lời giải? → Có
> - Không được dùng cùng một phần tử hai lần? → Đúng
> - Trả về kết quả theo thứ tự bất kỳ? → Được
>
> Constraints: `2 <= nums.length <= 10⁴`, `-10⁹ <= nums[i] <= 10⁹`, `-10⁹ <= target <= 10⁹`

---

## Bước 2 — Tìm lời giải

### 2a. Bắt đầu từ cách đơn giản nhất (Brute Force)

- Đừng cố tìm cách tối ưu ngay. Brute force giúp bạn **hiểu bài toán rõ hơn** và cho interviewer thấy bạn có baseline.
- Nói rõ approach trước khi code: *"Cách đơn giản nhất là duyệt 2 vòng lặp lồng nhau, O(n²) time, O(1) space."*

> **Ví dụ — Two Sum brute force O(n²):** dùng 2 vòng for lồng nhau, duyệt tất cả các cặp.
>
> Lỗi thường gặp:
>
> 1. **Vòng lặp trong phải bắt đầu từ `i + 1`**, không phải `i` hay `0`.
>    - Nếu `j = i` → so sánh phần tử với chính nó.
>    - Nếu `j = 0` → duyệt thừa cặp trùng (cặp `(0,1)` và `(1,0)` là một) và gặp lại bug khi `j == i`.
>    - Với Two Sum: sai khi `nums[i] + nums[i] == target`. Với Contains Duplicate: sai 100% vì `nums[i] == nums[i]` luôn đúng.
>    - **Quy tắc chung:** khi duyệt cặp phần tử khác nhau trong mảng, vòng trong luôn bắt đầu từ `i + 1`.
>
> 2. **Dùng `return` thay vì `break`** khi tìm thấy đáp án.
>    - Trong vòng for lồng nhau, `break` chỉ thoát vòng trong, vòng ngoài vẫn chạy tiếp.
>    - Dùng `return` để thoát method luôn. Lưu ý thêm `return` hoặc `throw` ở cuối method vì compiler không biết chắc luôn có đáp án.
>
> 3. **Dùng kiểu nguyên thủy (`int`) thay vì wrapper type (`Integer`).**
>    - Wrapper type gây autoboxing — Java tự tạo object trên heap mỗi lần gán giá trị — tốn bộ nhớ và chậm hơn.
>    - Chỉ dùng wrapper khi cần cho vào Collection hoặc cần giá trị `null`.

### 2b. Dry-run thủ công

- Chọn một tập dữ liệu nhỏ (3–5 phần tử), chạy tay qua từng bước của giải thuật.
- Mục đích: kiểm chứng logic đúng **trước khi viết code**, đồng thời giúp interviewer theo dõi được cách bạn nghĩ.

### 2c. Xử lý edge cases

Liệt kê các trường hợp bất thường trước khi code:
- Input rỗng hoặc chỉ có 1 phần tử
- Tất cả phần tử giống nhau
- Giá trị âm, giá trị 0
- Input đã sorted (tăng hoặc giảm)
- Kết quả nằm ở đầu/cuối
- Không có kết quả hợp lệ

---

## Bước 3 — Tối ưu

Sau khi có brute force hoạt động, hỏi bản thân:

**"Đang lãng phí gì?"**
- Tính toán lặp lại? → Caching / HashMap / DP
- Duyệt thừa? → Two Pointers / Binary Search / Sliding Window
- So sánh thừa? → Sorting trước, rồi áp dụng kỹ thuật phù hợp

**Trade-off: Time vs Space**
- Nói rõ: *"Dùng HashMap giảm time từ O(n²) xuống O(n), đổi lại tốn O(n) space."*
- Interviewer muốn thấy bạn **nhận thức được trade-off**, không chỉ biết tối ưu.

**Code Readability**
- Đặt tên biến có nghĩa
- Tách logic phức tạp thành helper function nếu cần
- Code gọn không có nghĩa là code tốt — code **dễ đọc** mới là code tốt

---

## Xuyên suốt — Communicate

Đây không phải một bước riêng, mà là thứ bạn làm **liên tục từ đầu đến cuối**:

- **Đừng im lặng.** Nói ra những gì bạn đang nghĩ, kể cả khi chưa chắc chắn.
- Khi chọn approach: *"Tôi thấy bài này có thể dùng HashMap vì cần lookup O(1)…"*
- Khi gặp bế tắc: *"Tôi đang nghĩ xem có cách nào tránh duyệt lại phần tử đã xét…"* — tốt hơn nhiều so với ngồi im.
- Khi code xong: tóm tắt lại complexity và walk-through code với một test case.

---

## Tóm tắt quy trình

```
Hiểu đề → Brute Force → Dry-run → Edge Cases → Tối ưu
↑                                                    ↑
└───── Communicate liên tục trong suốt quá trình ────┘
```
