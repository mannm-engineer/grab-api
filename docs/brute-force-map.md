# Các kiểu Brute Force Nested Loop

## Nhiệm vụ của vòng ngoài và vòng trong

**Vòng ngoài = chọn đối tượng.** Nó trả lời "đang xét ai?" — bước qua từng phần tử, nói "lượt của mày". Nhiệm vụ này không bao giờ thay đổi giữa các bài.

**Vòng trong = làm việc cho đối tượng đó.** Nó trả lời "đối tượng này cần gì?" — và tuỳ bài, công việc đó khác nhau:
- containsDuplicate: "Có ai giống tôi không?"
- twoSum: "Ai cộng với tôi bằng target?"
- isAnagram: "Ai bên kia khớp với tôi?"
- groupAnagram: "Ai cùng nhóm với tôi?"
- productExceptSelf: "Tích của mọi người trừ tôi?"
- K Maximum: "Ai lớn nhất mà chưa bị chọn?"

**Toàn bộ sự khác biệt giữa các bài nằm ở vòng trong.**

Và đây cũng là lý do tối ưu luôn nhắm vào **vòng trong**: vòng ngoài đã là O(n) — phải xét mọi phần tử, không bỏ qua được. Cái có thể loại bỏ là công việc lặp lại mà vòng trong đang làm. Thay vòng trong bằng Set/Map/Prefix → vòng ngoài vẫn O(n), nhưng tổng thể từ O(n²) xuống O(n).

---

## Lưu ý: số nguồn dữ liệu

Bài toán có thể duyệt trong **một nguồn** (cùng một mảng) hoặc **hai nguồn** (hai mảng/string khác nhau). Số nguồn không quyết định dạng bài hay cách tối ưu, nhưng là ngữ cảnh cần nhận ra khi viết brute force.

## Lưu ý: điểm bắt đầu của j

Yếu tố quyết định `j = i + 1` hay `j = 0` là **mục đích**:

- **Tìm một cặp** thoả mãn điều kiện → `j = i + 1` (vì cặp (i,j) và (j,i) là một)
  - Ví dụ: containsDuplicate, twoSum
- **Tính kết quả cho từng phần tử** dựa trên mọi phần tử khác → `j = 0` (vì mỗi phần tử có đáp án riêng)
  - Ví dụ: productExceptSelf, "đếm phần tử nhỏ hơn" (LeetCode 1365)
- **Khớp giữa hai nguồn** → `j = 0` (mỗi phần tử bên A cần xét toàn bộ bên B)
  - Ví dụ: isAnagram

---

Nested loop chia thành hai nhánh theo bản chất câu hỏi bên trong vòng lặp.

```
Brute Force Nested Loop
├── Nhánh A: So sánh / Khớp
│   "Quan hệ giữa các phần tử là gì?"
│   ├── A1: So sánh không tiêu hao
│   ├── A2: So sánh có tiêu hao
│   └── A3: So khớp + gom nhóm
│
├── Nhánh B: Tính toán / Tổng hợp
│   "Tính gì đó dựa trên giá trị các phần tử?"
│   └── B1: Tính toán lặp
│
└── Bài đa bước
    "Chuỗi nested loop nối tiếp, mỗi bước thuộc dạng khác nhau"
    └── Ví dụ: Top K Frequent Elements (A3 + A2)
```

---

# Nhánh A: So sánh / Khớp

Vòng trong hỏi về **quan hệ** giữa phần tử này với phần tử kia.

---

## A1: So sánh không tiêu hao

Duyệt từng cặp, so sánh, không cần đánh dấu gì.

```java
for (int i = 0; i < n; i++)
    for (int j = /* i+1 hoặc 0 */; j < n; j++)
        if (/* phép so sánh */) ...
```

- Không cần state tracking
- Dừng sớm khi tìm thấy
- Phép so sánh bên trong thay đổi tuỳ bài, cấu trúc vòng lặp giống nhau

| Bài | Phép so sánh | Optimal |
|-----|-------------|---------|
| containsDuplicate | `nums[i] == nums[j]` | HashSet — "đã gặp chưa?" |
| twoSum | `nums[i] + nums[j] == target` | HashMap — tra phần bù (target - num) |
| Kiểm tra tồn tại giữa hai tập | `a[i] == b[j]` | HashSet chứa một tập — tra cứu O(1) |

**Câu hỏi bên trong:** "Hai cái này có thoả mãn điều kiện không?"
**Quy luật:** so bằng trực tiếp → Set, so bằng phép tính → Map (cần lưu thêm giá trị)

---

## A2: So sánh có tiêu hao

Thêm ràng buộc: phần tử đã chọn/khớp rồi thì **loại ra**, không dùng lại. Dấu hiệu nhận biết: cần `used[]`.

```java
// Dạng 1: chọn rồi loại ra (K Maximum)
Set<Integer> used = new HashSet<>();
for (int round = 0; round < k; round++) {
    int max = Integer.MIN_VALUE;
    for (int j = 0; j < nums.length; j++) {
        if (!used.contains(nums[j]) && nums[j] >= max) {
            max = nums[j];
        }
    }
    used.add(max);
}

// Dạng 2: khớp rồi loại ra (isAnagram)
boolean[] used = new boolean[t.length()];
for (int i = 0; i < s.length(); i++) {
    for (int j = 0; j < t.length(); j++) {
        if (!used[j] && s.charAt(i) == t.charAt(j)) {
            used[j] = true;
            break;
        }
    }
}
```

| Bài | Hành động | Optimal |
|-----|-----------|---------|
| K Maximum Number | Chọn max, loại ra, lặp lại k lần | Sort / Heap |
| isAnagram | Khớp 1-1 giữa hai tập | int[26] — đếm tần suất |

**Câu hỏi bên trong:** "Cái này đã dùng chưa?"
**Cách phát hiện:** chạy tay → thấy khựng "cái này dùng rồi mà?"

---

## A3: So khớp + gom nhóm

Kết hợp nested loop với state tracking ở cấp nhóm.

```java
boolean[] grouped = new boolean[n];
for (int i = 0; i < n; i++) {
    if (grouped[i]) continue;
    List<String> group = new ArrayList<>();
    group.add(strs[i]);
    for (int j = i + 1; j < n; j++) {
        if (!grouped[j] && isAnagram(strs[i], strs[j])) {
            group.add(strs[j]);
            grouped[j] = true;
        }
    }
}
```

- `grouped[]` thay vì `used[]` — cùng pattern, khác ngữ cảnh
- Phần tử đã thuộc nhóm rồi thì bỏ qua
- Phép so sánh bên trong là một hàm phức tạp (isAnagram)
- Ba tầng loop: i × j × isAnagram bên trong

**Bài mẫu:** groupAnagram
**Câu hỏi bên trong:** "Hai cái này có cùng nhóm không?"
**Optimal:** HashMap — sorted string làm key, gom nhóm O(1)

---

# Nhánh B: Tính toán / Tổng hợp

Vòng trong không so sánh cặp — nó **tính toán dựa trên giá trị** các phần tử.

---

## B1: Tính toán lặp

Vòng ngoài chọn vị trí, vòng trong tính trên toàn bộ mảng.

```java
for (int i = 0; i < n; i++) {
    int product = 1;
    for (int j = 0; j < n; j++) {
        if (j != i) product *= nums[j];
    }
    output[i] = product;
}
```

- j = 0 và duyệt hết — cần tất cả trừ vị trí hiện tại
- Không có state tracking, không có so sánh cặp
- Sự lặp lại nằm ở phép tính — tính đi tính lại những tích/tổng trùng nhau

**Bài mẫu:** productExceptSelf
**Câu hỏi bên trong:** "Tích mọi thứ trừ tôi là bao nhiêu?"
**Optimal:** Prefix × Suffix — tích luỹ dần thay vì tính lại

*(Nhánh này sẽ mở rộng thêm khi gặp các bài như subarray sum, sliding window...)*

---

# Bài đa bước

Một số bài không rơi gọn vào một tầng — brute force là **chuỗi nested loop nối tiếp**, mỗi bước xử lý output của bước trước.

---

## Cách nhận biết

Khi viết brute force mà thấy cần **hai vòng nested loop riêng biệt**, mỗi vòng làm việc khác nhau → đó là bài đa bước.

---

## Ví dụ: Top K Frequent Elements

**Bước 1 — Đếm tần suất (dạng A3):**

```java
boolean[] counted = new boolean[nums.length];
List<int[]> freqs = new ArrayList<>();

for (int i = 0; i < nums.length; i++) {
    if (counted[i]) continue;
    int count = 0;
    for (int j = i; j < nums.length; j++) {
        if (nums[j] == nums[i]) {
            count++;
            counted[j] = true;
        }
    }
    freqs.add(new int[]{nums[i], count});
}
```

- `counted[]` → state tracking, tránh đếm lại phần tử trùng
- Giống A3: so khớp + gom nhóm

**Bước 2 — Tìm top k (dạng A2):**

```java
int[] result = new int[k];
boolean[] used = new boolean[freqs.size()];

for (int i = 0; i < k; i++) {
    int maxFreq = -1, maxIdx = 0;
    for (int j = 0; j < freqs.size(); j++) {
        if (!used[j] && freqs.get(j)[1] > maxFreq) {
            maxFreq = freqs.get(j)[1];
            maxIdx = j;
        }
    }
    result[i] = freqs.get(maxIdx)[0];
    used[maxIdx] = true;
}
```

- `used[]` → state tracking, tránh chọn lại phần tử đã lấy
- Giống A2: có tiêu hao

**Optimal:** Bước 1 thay bằng HashMap (đếm O(n)), bước 2 thay bằng Bucket Sort (O(n)) hoặc Heap (O(n log k)). Mỗi bước tối ưu độc lập theo dạng tương ứng.

---

## Cách phân tích bài đa bước

1. Tách brute force thành từng bước
2. Xác định mỗi bước thuộc dạng nào (A1-A3, B1...)
3. Tối ưu từng bước độc lập theo dạng tương ứng

---

# Tổng kết

```
Nhánh A — So sánh / Khớp:
  A1: So sánh không tiêu hao          → Set / Map
  A2: So sánh có tiêu hao             → Đếm (int[]) / Sort / Heap
  A3: So khớp + gom nhóm              → Key + Map

Nhánh B — Tính toán / Tổng hợp:
  B1: Tính toán lặp                    → Prefix / Suffix

Bài đa bước:
  Tách từng bước → phân loại riêng → tối ưu từng bước độc lập
```

**Nhánh A — câu hỏi phát triển dần:**
1. "Hai cái này thoả mãn điều kiện không?"
2. "Đã dùng chưa?" ← state tracking xuất hiện
3. "Thuộc nhóm nào?" ← state tracking mở rộng

**Nhánh B — câu hỏi khác bản chất:**
1. "Tính gì đó từ giá trị các phần tử?"

---

# Cách dùng tài liệu này

Khi gặp bài mới:
1. Viết brute force
2. Brute force có **một nested loop** hay **nhiều nested loop nối tiếp**?
   - Một → phân loại vào nhánh A hoặc B
   - Nhiều → bài đa bước, tách từng bước và phân loại riêng
3. Vòng trong **so sánh** hay **tính toán**? → Nhánh A hay B
4. Nếu nhánh A → có cần `used[]` không? → A1 hay A2
5. Nếu có `used[]` + gom nhóm → A3
6. Tầng đó gợi ý cấu trúc dữ liệu optimal tương ứng
7. Nếu không chắc → chạy tay input nhỏ, tìm điểm khựng lại

---

# Câu hỏi cần đặt khi đọc đề (mảng)

## Input — hiểu dữ liệu đầu vào

| Câu hỏi | Tại sao | Ví dụ |
|----------|---------|-------|
| Sorted chưa? | Nếu sorted → binary search, two pointers. Nếu không → HashMap hoặc tự sort | twoSum: không sorted → HashMap |
| Có phần tử trùng không? | Ảnh hưởng state tracking (đánh dấu index hay value) và loại trùng | 3Sum: cần skip duplicate |
| Rỗng/1 phần tử? | Edge case — vòng trong có thể không chạy | Third Maximum với < 3 phần tử |
| Âm/dương? | Ảnh hưởng phép tính — tích đổi dấu, tổng bằng 0 cần cả âm lẫn dương | productExceptSelf: số âm → tích đổi dấu |
| Kích thước? | Quyết định O(n²) có pass không | n ≤ 10⁴ → O(n²) được. n ≤ 10⁵ → cần O(n) |

## Output — hiểu yêu cầu đầu ra

| Câu hỏi | Tại sao | Ví dụ |
|----------|---------|-------|
| Trả index hay value? | Nhầm cái này → sai output dù logic đúng | twoSum: trả index, 3Sum: trả value |
| Luôn có đáp án không? | Nếu không → cần xử lý fallback | Third Maximum: không đủ 3 distinct → trả max |
| Kết quả được trùng không? | Nếu không → cần cơ chế loại trùng | 3Sum: không được trùng bộ ba |

**Kiểu trả về → ảnh hưởng cách viết code:**

| Kiểu trả về | Từ khóa trong đề | Return sớm? | Lưu ý |
|---|---|---|---|
| `boolean` | "any", "exists", "contains", "check if" | Có — tìm được là return ngay | Chỉ cần một trường hợp thoả mãn |
| `int` (single value) | "find", "maximum", "minimum", "count" | Tuỳ — đếm/tìm max thì phải duyệt hết, tìm index thì có thể sớm | twoSum trả index → return sớm. Third Maximum → duyệt hết |
| `int[]` / `List` | "all", "every", "find all", "return all" | Không — phải duyệt hết, gom vào list | Cần xét thêm: kết quả được trùng không? |
| `void` | "modify in-place", "rearrange" | Không — phải xử lý hết mảng | Cẩn thận thứ tự xử lý, tránh ghi đè |

Nếu đề không rõ, đọc kỹ phần mô tả output và examples để suy ra. Trong phỏng vấn thì hỏi lại interviewer.

## Tóm tắt

```
Input:  sorted? trùng? rỗng? âm/dương? kích thước?
Output: trả gì? luôn có đáp án? kết quả được trùng không? return sớm được không?
```

---

# Lỗi logic thường gặp

Sắp xếp theo thứ tự giải bài: đọc đề → viết vòng lặp → viết logic bên trong → xử lý kết quả.

## 1. Đọc đề

**Tự thêm ràng buộc mà đề không có**
- 3Sum: viết `nums[i + 1]` → giả định liền kề, đề chỉ yêu cầu index khác nhau
- Cách bắt: gạch chân từng ràng buộc trong đề, so với code

**Quên kiểm tra điều kiện đầu vào**
- isAnagram: không check `s.length() != t.length()`
- Third Maximum: không đủ 3 giá trị distinct
- Cách bắt: nghĩ edge case trước khi code — rỗng, 1 phần tử, tất cả giống nhau

## 2. Viết vòng lặp

**Chọn sai điểm bắt đầu của j**
- `j = i + 1` khi cần `j = 0` → bỏ sót
- `j = 0` khi cần `j = i + 1` → xét thừa
- Cách bắt: hỏi "tìm cặp hay tính cho từng phần tử?"

**So phần tử với chính nó**
- containsDuplicate: `j = 0` mà không check `j != i`
- Cách bắt: hỏi "vòng trong có bao gồm chính phần tử đang xét không?"

## 3. Viết logic bên trong

**Quên `used[]` khi có tiêu hao**
- isAnagram `s = "aa"`, `t = "ab"` → dùng lại `t[0]`
- Cách bắt: chạy tay input có phần tử trùng

**Dừng sớm khi chưa nên**
- isAnagram: không khớp ở vị trí j → return false, nhưng có thể khớp ở vị trí khác
- Cách bắt: hỏi "không tìm thấy ở đây = không tồn tại?"

**State tracking sai phạm vi**
- K Maximum: đánh dấu **giá trị** thay vì **index**
- Cách bắt: hỏi "đang đánh dấu cái gì? Giá trị hay vị trí?"

## 4. Xử lý kết quả

**Nhầm index với value**
- twoSum: trả `nums[i]` thay vì `i`
- Cách bắt: đọc lại đề yêu cầu trả về gì

**Loại trùng sai cách**
- 3Sum: so với tất cả candidates thay vì so với result
- Cách bắt: hỏi "đang so với cái gì?"

## Checklist nhanh

```
Đọc đề    → Có tự thêm ràng buộc không? Edge case?
Vòng lặp  → j bắt đầu đúng chưa? Có xét chính mình không?
Logic trong → Cần used[] không? Dừng đúng lúc chưa? Đánh dấu đúng thứ chưa?
Kết quả   → Trả đúng thứ đề yêu cầu chưa? Loại trùng đúng cách chưa?
```

Sau khi viết xong, hỏi hai câu:
1. *"Mình có bỏ sót trường hợp nào mà đề cho phép không?"*
2. *"Mình có chấp nhận trường hợp nào mà đề không cho phép không?"*

Rồi chạy tay với input nhỏ — đặc biệt input có phần tử trùng, input rỗng, input 1-2 phần tử.
