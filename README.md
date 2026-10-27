# AgroAPI

Backend REST del caso AgroSierra para el Examen Parcial II. Usa Kotlin, Spring Boot 4.1.2, Java 21,
PostgreSQL, Flyway, Spring Security y JWT.

## Ejecución

Requisitos: Docker Desktop con Docker Compose.

```bash
docker compose up --build -d
docker compose ps
```

La API queda disponible en `http://localhost:8080`. Para detenerla:

```bash
docker compose down
```

Las credenciales y el secreto JWT se configuran por variables de entorno. `.env.example` contiene los
nombres requeridos y valores de ejemplo; no debe reutilizarse en producción.

Para desarrollo local con PostgreSQL ya disponible:

```bash
./gradlew bootRun
```

## Usuarios de demostración

| Usuario | Contraseña | Rol |
|---|---|---|
| `tecnico1` | `Agro2026*` | `TECNICO` |
| `jefe1` | `Agro2026*` | `JEFE_CAMPO` |
| `admin` | `Agro2026*` | `ADMINISTRADOR` |
| `supervisor1` | `Agro2026*` | `SUPERVISOR_CALIDAD` |

La contraseña se almacena con BCrypt; el valor en claro existe únicamente como dato del examen.

## Arquitectura

- `config`: JWT, filtro por petición, Spring Security, errores comunes y datos semilla.
- `seguridad`: usuarios, roles, privilegios y autenticación.
- `dominio`: lotes, labores y tipos de labor.
- `repositorio`: persistencia JPA sobre PostgreSQL.
- `servicio`: transacciones y reglas RN-01 a RN-04.
- `web`: controladores versionados bajo `/api/v1`.
- `web/dto`: contrato JSON independiente de las entidades JPA.

Los controladores verifican autoridades con `@PreAuthorize`; las reglas que dependen de datos viven en los
servicios. La capacidad de editar todas las labores es una propiedad central del rol, lo que evita condicionales
por nombre de rol y permite incorporar roles nuevos.

## Privilegios

| Rol | Privilegios |
|---|---|
| TECNICO | LOTE_LEER, LABOR_LEER, LABOR_CREAR, LABOR_EDITAR sobre labores propias durante 24 horas |
| JEFE_CAMPO | LOTE_LEER, LABOR_LEER, LABOR_CREAR, LABOR_EDITAR todas, LABOR_ELIMINAR, CAMPANIA_CERRAR |
| ADMINISTRADOR | LOTE_LEER, LABOR_LEER, USUARIO_GESTIONAR |
| SUPERVISOR_CALIDAD | LOTE_LEER, LABOR_LEER y LABOR_EDITAR sobre todas las labores; no puede cerrar campañas |

Un token ausente, vencido o alterado produce `401`. Un usuario autenticado sin autoridad produce `403` sin
cerrar su sesión.

## Contrato para AgroCampo

### Inicio de sesión

`POST /api/v1/auth/login`

```json
{
  "usuario": "tecnico1",
  "contrasena": "Agro2026*"
}
```

```json
{
  "token": "jwt-firmado",
  "nombre": "Técnico de campo",
  "usuario": "tecnico1",
  "rol": "TECNICO",
  "privilegios": ["LABOR_CREAR", "LABOR_EDITAR", "LABOR_LEER", "LOTE_LEER"]
}
```

Las demás peticiones usan `Authorization: Bearer <token>`.

### Lotes

- `GET /api/v1/lotes?pagina=0&tamanio=20&valle=Mantaro&campaniaCerrada=false`
- `GET /api/v1/lotes/{id}`
- `POST /api/v1/lotes/{id}/cerrar-campania`

```json
{
  "id": 3,
  "codigo": "LOT-003",
  "nombre": "Quebrada Alta",
  "hectareas": 3.2,
  "cultivo": "Quinua",
  "valle": "Urubamba",
  "campaniaCerrada": false,
  "dosisMaximaPorHectarea": 1.5
}
```

### Labores

- `GET /api/v1/lotes/{id}/labores?pagina=0&tamanio=20`
- `POST /api/v1/labores`
- `PUT /api/v1/labores/{id}`
- `DELETE /api/v1/labores/{id}`

```json
{
  "loteId": 3,
  "tipo": "RIEGO",
  "fecha": "2026-10-27",
  "cantidad": 12.5,
  "cantidadJornales": 3,
  "observacion": "Riego de mantenimiento"
}
```

```json
{
  "id": 21,
  "loteId": 3,
  "tipo": "RIEGO",
  "fecha": "2026-10-27",
  "responsable": "tecnico1",
  "cantidad": 12.5,
  "cantidadJornales": 3,
  "observacion": "Riego de mantenimiento",
  "creadoEn": "2026-10-27T15:00:00Z"
}
```

El cierre de campaña devuelve el lote actualizado. La eliminación correcta responde `204` sin cuerpo.

### Usuario actual

`GET /api/v1/usuarios/me`

```json
{
  "id": 1,
  "usuario": "tecnico1",
  "nombre": "Técnico de campo",
  "rol": "TECNICO",
  "privilegios": ["LABOR_CREAR", "LABOR_EDITAR", "LABOR_LEER", "LOTE_LEER"]
}
```

### Paginación

```json
{
  "contenido": [],
  "pagina": 0,
  "tamanio": 20,
  "totalElementos": 0,
  "totalPaginas": 0,
  "ultima": true
}
```

### Error común

```json
{
  "timestamp": "2026-10-27T15:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Datos inválidos",
  "path": "/api/v1/labores",
  "validationErrors": {
    "cantidad": "La cantidad debe ser mayor que cero"
  }
}
```

## Reglas de negocio

- RN-01: crear o modificar una labor en una campaña cerrada devuelve `409`.
- RN-02: un técnico solo modifica labores propias durante 24 horas; si no, devuelve `403`.
- RN-03: cerrar una campaña sin una cosecha devuelve `409`.
- RN-04: exceder la dosis máxima total de fumigación devuelve `400` y `validationErrors.cantidad`.

## Pruebas

```bash
./gradlew test
```

Las pruebas unitarias cubren emisión, alteración y expiración del JWT, además de RN-01, RN-02, RN-03 y
RN-04. Las pruebas de contexto usan PostgreSQL mediante Testcontainers.

La colección `postman/AgroAPI.postman_collection.json` incluye autenticación y ejemplos ejecutables de
`400`, `401` y `403`.
