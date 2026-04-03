# Reading List

## Generic Entity Identity in DDD

Related to issue #3 in ISSUES.md — `Driver.id` dùng `String` ở domain layer để giữ generic. Có nhiều cách thiết kế id linh hoạt hơn, dưới đây là tài liệu cho 5 hướng tiếp cận.

### Cách 1: Value Object (Strongly Typed Id)

Bọc id trong một class riêng (e.g. `DriverId`) để có type safety và validation tập trung.

- [3 Reasons to Model Identity as a Value Object](https://buildplease.com/pages/vo-ids/) — 3 lý do: type safety, validation tập trung, Ubiquitous Language
- [Clean DDD lessons: Modeling Identity](https://medium.com/unil-ci-software-engineering/clean-ddd-lessons-modeling-identity-ff8bc17e0ae6) — Chi tiết về thiết kế identity trong Clean DDD
- [Domain-Driven Design: The Identifier Type Pattern](https://medium.com/@gara.mohamed/domain-driven-design-the-identifier-type-pattern-d86fd3c128b3) — Pattern "Identifier Type" trong DDD
- [Strongly Typed Entity IDs (aka Value Objects)](https://medium.com/@frode_nilsen/strongly-typed-entity-ids-aka-value-objects-a2150e6b0dc5) — Primitive Obsession anti-pattern và cách giải quyết
- [Using Value Objects as Aggregate Identifiers with Hibernate](https://dev.to/peholmst/using-value-objects-as-aggregate-identifiers-with-hibernate-a2e) — Hướng dẫn thực hành Java + Hibernate
- [An Introduction to Strongly-Typed Entity IDs](https://andrewlock.net/using-strongly-typed-entity-ids-to-avoid-primitive-obsession-part-1/) — Series chi tiết (.NET nhưng concept áp dụng được cho Java)

### Cách 2: Generic Type Parameter

Dùng type parameter trên base Entity class (e.g. `Entity<ID>`) để mỗi entity tự chọn kiểu id.

- [Implementing DDD Building Blocks in Java](http://odrotbohm.de/2020/03/Implementing-DDD-Building-Blocks-in-Java/) — Oliver Drotbohm (Spring team) trình bày cách dùng generic Entity base class trong Java
- [Entity Base Class](https://enterprisecraftsmanship.com/posts/entity-base-class/) — Thiết kế Entity base class với generic id, bao gồm equality và hashing
- [DDD Part 2: Tactical Domain-Driven Design](https://vaadin.com/blog/ddd-part-2-tactical-domain-driven-design) — Hướng dẫn tổng quan về Entity, Value Object, Aggregate với generic id trong Java
- [ddd-generic-java (GitHub)](https://github.com/Sofka-XT/ddd-generic-java) — Thư viện mẫu implement DDD building blocks với generic Entity<ID> trong Java
- [Don't use Ids in your domain entities!](https://enterprisecraftsmanship.com/posts/dont-use-ids-domain-entities/) — Góc nhìn ngược lại: khi nào KHÔNG nên có id trong domain model

### Cách 3: Sealed Interface

Dùng `sealed interface` (Java 17+) để giới hạn tập hợp kiểu id hợp lệ, compiler đảm bảo exhaustiveness trong `switch`. Hữu ích khi cần hỗ trợ nhiều kiểu id cùng lúc.

- [Clean DDD lessons: Modeling Identity](https://medium.com/unil-ci-software-engineering/clean-ddd-lessons-modeling-identity-ff8bc17e0ae6) — Thảo luận về modeling identity, áp dụng được cho sealed interface approach
- [Implementing DDD Building Blocks in Java](http://odrotbohm.de/2020/03/Implementing-DDD-Building-Blocks-in-Java/) — Oliver Drotbohm bàn về cách express DDD concepts qua Java type system

### Cách 4: No ID in Domain Model

Bỏ id ra khỏi domain model hoàn toàn — id chỉ là chi tiết persistence, domain model không cần biết.

- [Don't use Ids in your domain entities!](https://enterprisecraftsmanship.com/posts/dont-use-ids-domain-entities/) — Quan điểm "purist": tại sao id không thuộc về domain model
- [DDD Part 2: Tactical Domain-Driven Design](https://vaadin.com/blog/ddd-part-2-tactical-domain-driven-design) — Phân biệt Entity vs Value Object, khi nào id thực sự cần thiết

### Cách 5: Identity Converter / Strategy Pattern

Giữ `String` ở domain (như hiện tại), nhưng tách logic convert `String ↔ persistence type` ra một strategy/converter riêng biệt. Ít thay đổi code nhất, nhưng không có compile-time type safety.

- [Domain-Driven Design and Hexagonal Architecture](https://vaadin.com/blog/ddd-part-3-domain-driven-design-and-the-hexagonal-architecture) — Hexagonal architecture với converter ở boundary giữa domain và persistence

### Search keywords

- `"Primitive Obsession" DDD`
- `"Strongly Typed Id" Java Spring Data`
- `"Generic Entity base class" Java DDD`
- `"sealed interface" Java DDD identity`
- `"no id domain entity" DDD`
