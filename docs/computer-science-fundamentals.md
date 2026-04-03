# CS Fundamentals — Binary & Number Systems

- Let's go back to the beginning of computer science to learn about it
- Programming is just a tool and methodology to solve problems
- How can we represent inputs and outputs (problem solving) in a standardized way? Is it with English or Spanish? → **Binary digits**

## Number Representation

- **Finger position–based** representation of numbers
- **Length-based numeral system** (unary) / **Position-based numeral system**
- Giá trị mà một số đang biểu diễn phụ thuộc **số lượng (độ dài)** của các digit trong số đó (length-based numeral system)
- Giá trị mà một số đang biểu diễn phụ thuộc vào **vị trí** của từng digit trong số đó (position-based numeral system)

## Bits & Binary

A **bit** is a single binary digit, which can be either `0` or `1`.

### Approach 1: Sequential Counting

List out all combinations of 0s and 1s in order, just like counting up:

```
0 → 000
1 → 001
2 → 010
3 → 011
4 → 100
5 → 101
6 → 110
7 → 111
```

### Approach 2: Positional Decomposition

Break a decimal number down by which powers of 2 it contains:

| Decimal | 2² (4) | 2¹ (2) | 2⁰ (1) | Binary |
|---------|--------|--------|--------|--------|
| 0       | 0  | 0  | 0   | 000    |
| 1       | 0  | 0  | 1   | 001    |
| 2       | 0  | 1  | 0   | 010    |
| 3       | 0  | 1  | 1   | 011    |
| 4       | 1  | 0  | 0   | 100    |
| 5       | 1  | 0  | 1   | 101    |
| 6       | 1  | 1  | 0   | 110    |
| 7       | 1  | 1  | 1   | 111    |
