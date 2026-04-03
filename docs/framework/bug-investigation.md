# Bug Investigation Framework

## 4 Phases: What → How → Where → Why

```
1. INTAKE      → What?   Bug là gì?
2. REPRODUCE   → How?    Xảy ra thế nào?
3. SCOPE       → Where?  Xảy ra ở đâu?
4. RCA         → Why?    Tại sao?
```

---

## Phase 1: Intake

**Goal:** Thu thập đủ thông tin để bắt đầu reproduce.

**Question:** Tôi cần biết gì để thử reproduce?

**Exit:** Có repro hint (steps/env/user) + ghi lại trong investigation note.

---

## Phase 2: Reproduce

**Goal:** Tự tái hiện bug ít nhất 1 lần, trên 1 path.

**Question:** Tôi có thể tự gây ra bug này không?

**Exit:** Bug reproduce được + capture data (log/network/screenshot).

---

## Phase 3: Scope

**Goal:** Xác định path nào bug, path nào không.

**Question:** Tôi đã thử hết các path liên quan chưa?

- Còn cách nào khác trigger hành động này?
- Mỗi path đi qua component nào?
- Path share gì, khác gì?

**Exit:** Biết tập path bug vs path OK + scope table.

---

## Phase 4: RCA

**Goal:** Xác định nguyên nhân gốc giải thích đúng scope đã tìm.

**Question:** Tôi giải thích được tại sao bug chỉ xảy ra ở scope đó không?

**Exit:** Có statement "bug xảy ra vì X" + evidence (code ref / log / diff).

---

## Iteration Loops

Không tuần tự cứng. Có thể loop ngược:

- Phase N không reproduce được → quay lại Phase N-1

---

## Anti-patterns

❌ **Skip Reproduce** → phân tích trên không khí

❌ **One-shot Scope** → scope sai vì thiếu data

❌ **Skip RCA, nhảy thẳng sang fix** → fix triệu chứng, bug tái xuất hiện

❌ **Cố scope hoàn chỉnh ngay từ đầu** → scope dựa trên giả định

---

## Boundary

Framework dừng ở RCA. **Không** bao gồm fix design, implementation, testing, deployment — đó là engineering process khác.

> Output cuối: *"Bug X xảy ra vì root cause Y, ảnh hưởng scope Z"* → handover cho engineering.