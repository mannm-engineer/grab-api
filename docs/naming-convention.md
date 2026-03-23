# Coding Convention

## Layer-Specific Naming

Class and exception names must reflect the architectural layer they belong to.

| Layer      | Vocabulary                          | Example                   |
|------------|-------------------------------------|---------------------------|
| Service    | Domain terminology                  | `DomainNotFoundException` |
| Controller | HTTP/REST terminology               | `RestController`          |
| Repository | Data/persistence terminology        | `DatabaseStore`           |

Avoid leaking concepts across layers. For example, "Resource" is an HTTP/REST concept and should not appear in service layer naming.

## Identification Fields

Identification fields (IDs) must be typed as `String` in the controller, service, and store layers. Only the repository layer should use the concrete type (e.g., `Long`).

## Method Ordering

Methods that only read data must be declared before methods that write data. This applies to interfaces, implementations, services, and controllers.
