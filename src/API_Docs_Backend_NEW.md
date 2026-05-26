# 🎬 API Documentation - Cine Backend

---

# 🔐 Autenticación

## 🔹 POST /api/auth/register

**Descripción:** Registrar un nuevo comprador (BUYER)

**Request**

```json
{
  "email": "comprador@example.com",
  "name": "Juan Pérez",
  "password": "SecurePass123!",
  "userType": "BUYER"
}
```

**Response (HTTP 201)**

```json
{
  "userType": "BUYER",
  "username": "Juan Pérez",
  "message": "User registered successfully. Please check your email to verify your account."
}
```

**Errores:**
- Email ya en uso
- Email inválido
- Contraseña menor a 8 caracteres

---

## 🔹 POST /api/auth/login

**Descripción:** Iniciar sesión y obtener token JWT

**Request**

```json
{
  "email": "usuario@example.com",
  "password": "SecurePass123!"
}
```

**Response (HTTP 200)**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userType": "BUYER",
  "name": "Juan Pérez"
}
```

**Errores:**
- Usuario no encontrado
- Contraseña incorrecta
- Email no verificado

---

## 🔹 GET /api/auth/verify?token={token}

**Descripción:** Verificar email con token de verificación

**Response (HTTP 200)**

```
"Cuenta confirmada exitosamente. Ya puedes iniciar sesión."
```

**Errores:**
- Token inválido
- Token expirado
- Token ya usado

---

# 🏢 1. Multiplex

**Acceso:** 
- GET: ADMIN, MANAGER
- POST/PUT/DELETE: ADMIN solamente
- El MANAGER solo puede ver/editar su multiplex asignado

## 🔹 GET /api/admin/multiplexes

**Descripción:** Obtener todos los multiplex (ADMIN ve todos, MANAGER ve solo el suyo)

**Autorización:** Bearer token requerido

**Response (HTTP 200)**

```json
[
  {
    "idMultiplex": "550e8400-e29b-41d4-a716-446655440000",
    "nameMultiplex": "Cine Pacho - Centro",
    "cityMultiplex": "Medellín"
  },
  {
    "idMultiplex": "550e8400-e29b-41d4-a716-446655440001",
    "nameMultiplex": "Cine Pacho - Sur",
    "cityMultiplex": "Bogotá"
  }
]
```

---

## 🔹 GET /api/admin/multiplexes/{id}

**Descripción:** Obtener detalle de un multiplex con todas sus salas

**Parámetros:**
- `id` (UUID): ID del multiplex

**Autorización:** Bearer token requerido (ADMIN o MANAGER del multiplex)

**Response (HTTP 200)**

```json
{
  "idMultiplex": "550e8400-e29b-41d4-a716-446655440000",
  "nameMultiplex": "Cine Pacho - Centro",
  "addressMultiplex": "Carrera 10 #15-30",
  "cityMultiplex": "Medellín",
  "rooms": [
    {
      "idRoom": "650e8400-e29b-41d4-a716-446655440000",
      "isRoomActive": true
    },
    {
      "idRoom": "650e8400-e29b-41d4-a716-446655440001",
      "isRoomActive": true
    }
  ]
}
```

---

## 🔹 POST /api/admin/multiplexes

**Descripción:** Crear un nuevo multiplex

**Autorización:** ADMIN solamente

**Request**

```json
{
  "nameMultiplex": "Cine Pacho - Centro",
  "addressMultiplex": "Carrera 10 #15-30",
  "cityMultiplex": "Medellín",
  "numberOfRooms": 8
}
```

**Response (HTTP 201)**

```json
{
  "idMultiplex": "550e8400-e29b-41d4-a716-446655440000",
  "nameMultiplex": "Cine Pacho - Centro",
  "addressMultiplex": "Carrera 10 #15-30",
  "cityMultiplex": "Medellín",
  "rooms": [
    {
      "idRoom": "650e8400-e29b-41d4-a716-446655440000",
      "isRoomActive": true
    }
  ]
}
```

**Validaciones:**
- `numberOfRooms`: Entre 5 y 15
- No puede haber otro multiplex con el mismo nombre en la misma ciudad

---

## 🔹 PUT /api/admin/multiplexes/{id}

**Descripción:** Actualizar datos de un multiplex

**Autorización:** ADMIN solamente

**Parámetros:**
- `id` (UUID): ID del multiplex

**Request**

```json
{
  "nameMultiplex": "Cine Pacho - Centro Actualizado",
  "addressMultiplex": "Carrera 10 #15-30",
  "cityMultiplex": "Medellín",
  "numberOfRooms": 8
}
```

**Response (HTTP 200)**

```json
{
  "idMultiplex": "550e8400-e29b-41d4-a716-446655440000",
  "nameMultiplex": "Cine Pacho - Centro Actualizado",
  "addressMultiplex": "Carrera 10 #15-30",
  "cityMultiplex": "Medellín",
  "rooms": []
}
```

---

## 🔹 DELETE /api/admin/multiplexes/{id}

**Descripción:** Eliminar un multiplex

**Autorización:** ADMIN solamente

**Parámetros:**
- `id` (UUID): ID del multiplex

**Response (HTTP 204)**

Sin contenido

---

# 🎥 2. Salas (Rooms)

**Acceso:** 
- POST/DELETE: ADMIN y MANAGER (solo su multiplex)

## 🔹 POST /api/admin/{multiplexId}/rooms

**Descripción:** Crear una nueva sala en un multiplex

**Autorización:** ADMIN y MANAGER (del multiplex)

**Parámetros:**
- `multiplexId` (UUID): ID del multiplex donde crear la sala

**Request:** No requiere body

**Response (HTTP 200)**

```json
{
  "message": "Sala de cine creada con éxito",
  "roomId": "650e8400-e29b-41d4-a716-446655440000"
}
```

**Notas:**
- Cada sala se crea con capacidad de 40 sillas generales y 20 preferenciales (total 60)
- Las sillas se crean automáticamente al crear la sala

---

## 🔹 DELETE /api/admin/rooms/{id}

**Descripción:** Eliminar (desactivar lógicamente) una sala

**Autorización:** ADMIN y MANAGER (del multiplex)

**Parámetros:**
- `id` (UUID): ID de la sala

**Response (HTTP 200)**

```json
{
  "message": "Sala de cine eliminada con éxito",
  "roomId": "650e8400-e29b-41d4-a716-446655440000"
}
```

---

# 💺 3. Sillas (Seats)

**Acceso:** 
- PUT: BUYER y EMPLOYEE (cambiar estado de silla)
- GET: BUYER y EMPLOYEE (ver disponibilidad)

## 🔹 GET /api/seats/{roomId}

**Descripción:** Obtener todas las sillas de una sala con su estado

**Autorización:** BUYER y EMPLOYEE

**Parámetros:**
- `roomId` (UUID): ID de la sala

**Response (HTTP 200)**

```json
[
  {
    "idSeat": "750e8400-e29b-41d4-a716-446655440000",
    "roomId": "650e8400-e29b-41d4-a716-446655440000",
    "seatNumber": 1,
    "type": "GENERAL",
    "status": "AVAILABLE"
  },
  {
    "idSeat": "750e8400-e29b-41d4-a716-446655440001",
    "roomId": "650e8400-e29b-41d4-a716-446655440000",
    "seatNumber": 2,
    "type": "GENERAL",
    "status": "BLOCKED"
  },
  {
    "idSeat": "750e8400-e29b-41d4-a716-446655440050",
    "roomId": "650e8400-e29b-41d4-a716-446655440000",
    "seatNumber": 41,
    "type": "PREFERENTIAL",
    "status": "AVAILABLE"
  }
]
```

---

## 🔹 PUT /api/seats/{seatId}/changeStatus

**Descripción:** Cambiar el estado de una silla (bloquear si disponible, desbloquear si bloqueada por el usuario)

**Autorización:** BUYER y EMPLOYEE (Bearer token requerido)

**Parámetros:**
- `seatId` (UUID): ID de la silla

**Headers:**
- `Authorization: Bearer {token}` (requerido)

**Request:** No requiere body

**Response (HTTP 200)**

```json
{
  "idSeat": "750e8400-e29b-41d4-a716-446655440000",
  "roomId": "650e8400-e29b-41d4-a716-446655440000",
  "seatNumber": 1,
  "type": "GENERAL",
  "status": "BLOCKED"
}
```

**Lógica:**
1. Si está `AVAILABLE` → cambia a `BLOCKED` y se bloquea por 10 minutos automáticamente
2. Si está `BLOCKED` por el usuario actual → cambia a `AVAILABLE`
3. Si está `BLOCKED` por otro usuario → retorna error CONFLICT
4. Si está `SOLD` → retorna error (no se puede modificar)

**Errores:**
- Silla no encontrada (404)
- Silla reservada por otro usuario (409)
- Silla ya vendida (409)

---

# 🎬 4. Películas

**Acceso:** 
- GET búsqueda: ADMIN y MANAGER
- POST crear/select/cambiar estado: ADMIN y MANAGER (del multiplex)

## 🔹 GET /api/admin/movie/search?query={text}&page={numero}

**Descripción:** Buscar películas en TMDB (The Movie Database)

**Autorización:** ADMIN y MANAGER

**Parámetros Query:**
- `query` (string): Término de búsqueda (ej: "The Matrix")
- `page` (integer, opcional): Número de página. Default: 1

**Response (HTTP 200)**

```json
[
  {
    "id": 603,
    "backdropPath": "/path/to/backdrop.jpg",
    "genreIds": [28, 12, 878],
    "originalLanguage": "en",
    "originalTitle": "The Matrix",
    "overview": "A computer programmer discovers...",
    "posterPath": "/path/to/poster.jpg",
    "releaseDate": "1999-03-31"
  },
  {
    "id": 234215,
    "backdropPath": "/path/to/backdrop2.jpg",
    "genreIds": [28, 12],
    "originalLanguage": "en",
    "originalTitle": "The Matrix Reloaded",
    "overview": "Neo and his allies race against...",
    "posterPath": "/path/to/poster2.jpg",
    "releaseDate": "2003-05-15"
  }
]
```

---

## 🔹 POST /api/admin/movie/select/{movieId}

**Descripción:** Seleccionar una película (guardarla en BD si no existe)

**Autorización:** ADMIN y MANAGER

**Parámetros:**
- `movieId` (Long): ID de la película en TMDB

**Request:** No requiere body

**Response (HTTP 200)**

```json
{
  "originarTitle": "The Matrix",
  "director": "The Wachowskis",
  "message": "Película añadida con éxito"
}
```

**Notas:**
- Si la película ya existe en BD, solo la retorna
- Si no existe, la obtiene de TMDB y la guarda

---

## 🔹 POST /api/admin/movie/createScreening

**Descripción:** Crear una función (proyección) de una película

**Autorización:** ADMIN y MANAGER (del multiplex de la sala)

**Request**

```json
{
  "movieId": 603,
  "roomId": "650e8400-e29b-41d4-a716-446655440000",
  "dateTime": "2026-06-15 14:30:00"
}
```

**Response (HTTP 200)**

```json
{
  "screeningId": "850e8400-e29b-41d4-a716-446655440000",
  "dateTime": "2026-06-15 14:30:00",
  "originalLanguage": "en",
  "originalTitle": "The Matrix",
  "overview": "A computer programmer discovers...",
  "rating": 8.7,
  "director": "The Wachowskis",
  "status": "ACTIVE",
  "genres": ["Action", "Sci-Fi", "Adventure"]
}
```

**Validaciones:**
- La película debe existir
- La sala debe existir
- La fecha y hora no pueden estar en el pasado

---

## 🔹 PUT /api/admin/movie/changeStatus/{idScreening}

**Descripción:** Cambiar el estado de una función

**Autorización:** ADMIN y MANAGER (del multiplex)

**Parámetros:**
- `idScreening` (UUID): ID de la función

**Query Parameters:**
- `status` (string): Nuevo estado (ACTIVE, CANCELLED, COMPLETED)

**Request:** No requiere body

**Example:**
```
PUT /api/admin/movie/changeStatus/850e8400-e29b-41d4-a716-446655440000?status=CANCELLED
```

**Response (HTTP 200)**

```json
{
  "screeningStatus": "CANCELLED",
  "screeningId": "850e8400-e29b-41d4-a716-446655440000"
}
```

**Estados válidos:**
- `ACTIVE`: Función activa y disponible
- `CANCELLED`: Función cancelada
- `COMPLETED`: Función completada

---

# 🍿 5. Snacks

**Acceso:** 
- GET: ADMIN solamente
- POST/PUT/DELETE: ADMIN solamente

## 🔹 GET /api/admin/snacks

**Descripción:** Obtener todos los snacks disponibles

**Autorización:** ADMIN

**Response (HTTP 200)**

```json
[
  {
    "idSnack": "950e8400-e29b-41d4-a716-446655440000",
    "nameSnack": "Palomitas Medianas",
    "descriptionSnack": "Palomitas de maíz recién hechas",
    "priceSnack": 8500,
    "quantitySnack": 150
  },
  {
    "idSnack": "950e8400-e29b-41d4-a716-446655440001",
    "nameSnack": "Bebida Cola 500ml",
    "descriptionSnack": "Bebida gaseosa fría",
    "priceSnack": 5000,
    "quantitySnack": 200
  }
]
```

---

## 🔹 GET /api/admin/snacks/{id}

**Descripción:** Obtener un snack específico

**Autorización:** ADMIN

**Parámetros:**
- `id` (UUID): ID del snack

**Response (HTTP 200)**

```json
{
  "idSnack": "950e8400-e29b-41d4-a716-446655440000",
  "nameSnack": "Palomitas Medianas",
  "descriptionSnack": "Palomitas de maíz recién hechas",
  "priceSnack": 8500,
  "quantitySnack": 150
}
```

---

## 🔹 POST /api/admin/snacks

**Descripción:** Crear un nuevo snack

**Autorización:** ADMIN

**Request**

```json
{
  "nameSnack": "Palomitas Grandes",
  "descriptionSnack": "Palomitas de maíz con mantequilla",
  "priceSnack": 12000,
  "quantitySnack": 100
}
```

**Response (HTTP 201)**

```json
{
  "idSnack": "950e8400-e29b-41d4-a716-446655440002",
  "nameSnack": "Palomitas Grandes",
  "descriptionSnack": "Palomitas de maíz con mantequilla",
  "priceSnack": 12000,
  "quantitySnack": 100
}
```

**Validaciones:**
- Precio debe ser mayor a 0
- Cantidad no puede ser negativa

---

## 🔹 PUT /api/admin/snacks/{id}

**Descripción:** Actualizar un snack

**Autorización:** ADMIN

**Parámetros:**
- `id` (UUID): ID del snack

**Request**

```json
{
  "nameSnack": "Palomitas Grandes",
  "descriptionSnack": "Palomitas de maíz con mantequilla",
  "priceSnack": 13000,
  "quantitySnack": 80
}
```

**Response (HTTP 200)**

```json
{
  "idSnack": "950e8400-e29b-41d4-a716-446655440002",
  "nameSnack": "Palomitas Grandes",
  "descriptionSnack": "Palomitas de maíz con mantequilla",
  "priceSnack": 13000,
  "quantitySnack": 80
}
```

---

## 🔹 DELETE /api/admin/snacks/{id}

**Descripción:** Eliminar un snack

**Autorización:** ADMIN

**Parámetros:**
- `id` (UUID): ID del snack

**Response (HTTP 204)**

Sin contenido
---

# 🧾 Checkout / Compra

**Acceso:** BUYER / EMPLOYEE

## 🔹 POST /api/checkout/preview

**Descripción:** Recibe desde el front la lista de sillas y snacks para calcular totales (vista previa).

**Autorización:** Bearer token (BUYER, EMPLOYEE)

**Request**

```json
{
  "seats": [
    { "seatId": "750e8400-e29b-41d4-a716-446655440000" },
    { "seatId": "750e8400-e29b-41d4-a716-446655440001" }
  ],
  "snacks": [
    { "snackId": "950e8400-e29b-41d4-a716-446655440000", "quantity": 2 },
    { "snackId": "950e8400-e29b-41d4-a716-446655440001", "quantity": 1 }
  ]
}
```

**Notas de los campos:**
- `seats`: Lista obligatoria de objetos `SeatSelectionRequest` con `seatId` (UUID).
- `snacks`: Lista opcional de objetos `SnackSelectionRequest` con `snackId` (UUID) y `quantity` (int).

**Response (HTTP 200)**

Retorna un `CheckoutSummaryResponse` con totales y desglose para mostrar en la UI.

## 🔹 POST /api/checkout/confirm

**Descripción:** Revalida disponibilidad y confirma la compra (reserva/pago). Recibe el mismo body que `/preview`.

**Autorización:** Bearer token (BUYER)

**Request:** Igual que en `/api/checkout/preview`.

**Response (HTTP 200)**

Retorna `CheckoutSummaryResponse` final y datos de la transacción (ticket/snippets).

---
---

# 👥 6. Empleados y Gerentes

**Acceso:** 
- POST registrar: ADMIN y MANAGER (solo en su multiplex)

## 🔹 POST /api/admin/register_employee

**Descripción:** Registrar un empleado o gerente (personal del cine)

**Autorización:** ADMIN y MANAGER (solo puede registrar en su multiplex)

**Request**

```json
{
  "email": "empleado@cinepacho.com",
  "name": "Carlos López",
  "password": "EmpPass123!",
  "userType": "EMPLOYEE",
  "indentityCard": "1023456789",
  "phoneNumber": "3012345678",
  "salary": 2500000,
  "position": "Vendedor",
  "multiplexId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response (HTTP 200)**

```json
{
  "userType": "EMPLOYEE",
  "username": "Carlos López",
  "message": "Se ha creado correctamente el empleado"
}
```

**Valores válidos para `userType`:**
- `EMPLOYEE`: Empleado de taquilla (acceso a cambio de sillas y visualización)
- `MANAGER`: Gerente de multiplex (gestiona su multiplex asignado)

**Validaciones:**
- Email debe ser único
- Contraseña mínimo 8 caracteres
- Cédula entre 8 y 10 dígitos
- Teléfono exactamente 10 dígitos
- Salario debe ser positivo
- El MANAGER solo puede registrar personal en su multiplex

---

# 🔑 Niveles de Acceso

## ADMIN
- Crear/actualizar/eliminar multiplex
- Crear/eliminar salas en cualquier multiplex
- Crear/cambiar estado de películas en cualquier multiplex
- Crear/actualizar/eliminar snacks
- Registrar empleados y gerentes en cualquier multiplex

## MANAGER
- Consultar solo su multiplex asignado
- Crear/eliminar salas en su multiplex
- Crear/cambiar estado de películas en su multiplex
- Registrar empleados en su multiplex
- No puede registrar otros gerentes

## EMPLOYEE
- Cambiar estado de sillas (bloquear/desbloquear)
- Consultar disponibilidad de sillas

## BUYER
- Cambiar estado de sillas (bloquear/desbloquear)
- Consultar disponibilidad de sillas

---

# ⚠️ Notas Generales

## Formatos
- Todas las fechas usan formato: `yyyy-MM-dd HH:mm:ss`
- UUID se maneja como string
- Las listas siempre vienen en formato `[ ]`
- Los objetos en `{ }`
- Precios en BigDecimal (hasta 8 dígitos enteros y 2 decimales)

## Autenticación
- Todos los endpoints excepto `/api/auth/**` requieren Bearer token
- El token se envía en el header: `Authorization: Bearer {token}`
- El token expira después de configurarse (ver propiedades jwt.expiration)

## Códigos de Estado HTTP
- `200`: Éxito en GET, PUT
- `201`: Éxito en POST (creación)
- `204`: Éxito en DELETE
- `400`: Error de validación
- `401`: No autenticado
- `403`: No autorizado
- `404`: Recurso no encontrado
- `409`: Conflicto (ej: email duplicado, silla bloqueada por otro)
- `500`: Error del servidor

## Restricción de Multiplex
- El MANAGER solo puede gestionar su multiplex asignado
- El backend valida esto automáticamente con `AccessValidator`
- Los intentos de acceder a otro multiplex retornan error 403

## Bloqueo de Sillas
- Se bloquean automáticamente por 10 minutos
- Si pasa el tiempo, se desbloquean automáticamente
- Solo el usuario que bloqueó puede desbloquearla antes del tiempo

---

# 🛠 Endpoints detectados en el código (agregados / corregidos)

Se agregan las rutas que estaban faltando o se actualizan las que en el markdown no coincidían con el código.

- Películas (MovieController)
  - GET  /api/admin/movie/search?query={text}&page={numero}
    - Descripción: Buscar en TMDB y devolver lista de MovieSearchResponseDTO.
    - Autorización: ADMIN o MANAGER (según securityFilterChain). Nota: el path en el código tenía un typo "adminon" y fue corregido a /api/admin/movie/search.
  - POST /api/admin/movie/select/{movieId}
    - Descripción: Seleccionar/guardar película por id TMDB.
    - Autorización: ADMIN y MANAGER
  - POST /api/admin/movie/createScreening
    - Descripción: Crear función (CreateScreeningDTO).
    - Autorización: ADMIN y MANAGER (del multiplex)
  - PUT  /api/admin/movie/changeStatus/{idScreening}?status={ACTIVE|CANCELLED|COMPLETED}
    - Descripción: Cambiar estado de función.
    - Autorización: ADMIN y MANAGER

- Reseñas (ReviewController)
  - GET  /api/review/movie/{movieId}
    - Descripción: Lista de reseñas de una película.
    - Autorización: pública (permitAll) — visible incluso sin token.
  - GET  /api/{buyerId}/review
    - Descripción: Lista de reseñas de un usuario.
    - Autorización: BUYER, MANAGER, ADMIN
  - POST /api/{buyerId}/review/movie
    - Descripción: Crear reseña de película para buyerId (solo BUYER puede crear).
    - Autorización: BUYER
  - POST /api/{buyerId}/review/service
    - Descripción: Crear reseña de servicio.
    - Autorización: BUYER

- Snacks (SnackController)
  - GET  /api/snacks
    - Descripción: Lista pública de snacks disponibles (visible a BUYER/EMPLOYEE/MANAGER/ADMIN según security config).
    - Autorización: requiere token según securityFilterChain (BUYER/EMPLOYEE/MANAGER/ADMIN).
  - GET  /api/admin/snacks and /api/admin/snacks/{id}
  - POST /api/admin/snacks
  - PUT  /api/admin/snacks/{id}
  - DELETE /api/admin/snacks/{id}
    - Descripción: CRUD de snacks (ADMIN)

- Checkout (CheckoutController)
  - POST /api/checkout/stripe
    - Descripción: Procesa pago con Stripe; recibe CheckoutRequest en body y Authorization header.
    - Headers: Authorization: Bearer {token}
    - Autorización: BUYER/EMPLOYEE/MANAGER/ADMIN (según security config)
  - Nota: endpoints /preview y /confirm están en el markdown original pero en el código están comentados; actualmente sólo existe /stripe.

- Salas (RoomController)
  - POST /api/admin/{multiplexId}/rooms
    - Descripción: Crear sala en multiplex (devuelve id de sala).
    - Autorización: ADMIN y MANAGER (según security config y validación por servicio)
  - DELETE /api/admin/rooms/{id}
    - Descripción: Eliminar (desactivar) sala.
    - Autorización: ADMIN y MANAGER

- Sillas (SeatController)
  - GET  /api/seats/{roomId}
    - Descripción: Obtener sillas y estados de una sala.
    - Autorización: BUYER, EMPLOYEE, MANAGER, ADMIN
  - PUT  /api/seats/{seatId}/changeStatus
    - Descripción: Cambiar estado (toggle) de una silla. Header Authorization requerido.
    - Autorización: BUYER, EMPLOYEE, MANAGER, ADMIN

- Multiplex (MultiplexController)
  - GET  /api/admin/multiplexes
  - GET  /api/admin/multiplexes/{id}
  - POST /api/admin/multiplexes
  - PUT  /api/admin/multiplexes/{id}
  - DELETE /api/admin/multiplexes/{id}
    - Autorización: ver sección "Niveles de Acceso"; POST/DELETE requieren ADMIN; GET/PUT permiten MANAGER/ADMIN con validación por servicio.

- Empleados (EmployeeController)
  - POST /api/admin/register_employee
    - Descripción: Registrar empleado/manager.
    - Autorización: ADMIN y MANAGER (solo en su multiplex)

- Autenticación (AuthController) — ya documentado
  - POST /api/auth/register
  - POST /api/auth/login
  - GET  /api/auth/verify?token={token}


## Resumen de permisos relevantes (según securityFilterChain en Config.java)
- /api/auth/** → permitAll
- /api/seats/** → BUYER, EMPLOYEE, MANAGER, ADMIN
- GET /api/snacks/** → BUYER, EMPLOYEE, MANAGER, ADMIN
- /api/checkout/** → BUYER, EMPLOYEE, MANAGER, ADMIN (nota: en negocio final debería ser BUYER)
- /api/admin/multiplexes POST, DELETE → ADMIN
- /api/admin/multiplexes/** → ADMIN, MANAGER (consulta/actualización validada en servicio)
- /api/admin/*/rooms and /api/admin/rooms/** → ADMIN, MANAGER
- /api/admin/register_employee → ADMIN, MANAGER
- /api/admin/movie/** → ADMIN, MANAGER
- GET /api/review/** → permitAll (reseñas por película visibles para todos)
- GET /api/*/review → BUYER, MANAGER, ADMIN
- POST /api/*/review/** → BUYER
