# API Documentation - CinePacho Backend

Documento actualizado contra los controladores reales del proyecto.

## Modelo de acceso

Roles existentes:

- `BUYER`: comprador del portal web.
- `EMPLOYEE`: empleado de taquilla.
- `MANAGER`: gerente de multiplex. Solo puede usar endpoints administrativos y el servicio valida que el multiplex sea el asignado.
- `ADMIN`: administrador global. Puede usar endpoints administrativos de cualquier multiplex.

Reglas generales:

- Autenticacion publica: `/api/auth/register`, `/api/auth/login`, `/api/auth/verify`.
- Portal web y taquilla: `BUYER` y `EMPLOYEE` pueden consultar cartelera, consultar/bloquear sillas, consultar snacks disponibles y ejecutar checkout.
- Administracion de multiplex: `MANAGER` y `ADMIN`; el alcance del `MANAGER` se valida con `AccessValidator`.
- Administracion global: endpoints sin alcance por multiplex, como CRUD de snacks, quedan para `ADMIN`.
- Reviews por pelicula son publicas en lectura.
- Rutas nuevas no clasificadas quedan bloqueadas por `SecurityFilterChain`.

---

# 1. Autenticacion

## POST /api/auth/register

Descripcion: registra un comprador `BUYER`.

Acceso: publico.

Request:

```json
{
  "email": "comprador@example.com",
  "name": "Juan Perez",
  "password": "SecurePass123!",
  "userType": "BUYER"
}
```

Response 201:

```json
{
  "userType": "BUYER",
  "username": "comprador@example.com",
  "message": "User registered successfully. Please check your email to verify your account."
}
```

Errores esperados:

- Email ya registrado.
- Email invalido.
- Password menor a 8 caracteres.
- Tipo de usuario no permitido para auto-registro.

## POST /api/auth/login

Descripcion: autentica un usuario y retorna JWT.

Acceso: publico.

Request:

```json
{
  "email": "usuario@example.com",
  "password": "SecurePass123!"
}
```

Response 200:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "userType": "BUYER",
  "name": "usuario@example.com"
}
```

## GET /api/auth/verify?token={token}

Descripcion: verifica email con el token enviado por correo.

Acceso: publico.

Response 200:

```text
Cuenta confirmada exitosamente. Ya puedes iniciar sesion.
```

---

# 2. Cartelera y funciones para portal web/taquilla

## GET /api/movie/multiplex/{multiplexId}/selectors

Descripcion: devuelve la cartelera de un multiplex. Cada elemento es un `MovieSelectorDTO`, es decir, una pelicula con todos sus screenings agrupados para ese multiplex.

Acceso: `BUYER`, `EMPLOYEE`.

Uso esperado en frontend:

- Cargar la lista desplegable de peliculas disponibles para un multiplex.
- Alimentar la barra de busqueda del portal web o la taquilla.
- Mostrar informacion basica de la pelicula y sus funciones disponibles.

Parametros:

- `multiplexId` (UUID): multiplex que se quiere consultar.
- `query` (string, opcional): filtra por titulo de pelicula. Ejemplo: `/api/movie/multiplex/{multiplexId}/selectors?query=matrix`.

Response 200:

```json
[
  {
    "movieInfo": {
      "id": 603,
      "backdropPath": "/backdrop.jpg",
      "genreIds": [
        { "id": 28, "name": "Action" },
        { "id": 878, "name": "Science Fiction" }
      ],
      "originalLanguage": "en",
      "originalTitle": "The Matrix",
      "overview": "A computer programmer discovers...",
      "posterPath": "/poster.jpg",
      "releaseDate": "1999-03-31",
      "director": "The Wachowskis"
    },
    "rating": 4.5,
    "screenings": [
      {
        "screeningId": "850e8400-e29b-41d4-a716-446655440000",
        "roomId": "650e8400-e29b-41d4-a716-446655440000",
        "roomNumber": "room: 1",
        "screeningDate": "2026-06-15T14:30:00",
        "status": "ACTIVE"
      }
    ]
  }
]
```

Notas:

- Si el multiplex no tiene salas o funciones, retorna lista vacia.
- El backend no consulta directamente repositorios de `rooms` desde movie; usa `RoomManager` de `shared.auxiliaryClass`.

## GET /api/movie/multiplex/{multiplexId}/selectors/{movieId}

Descripcion: devuelve un solo `MovieSelectorDTO` para una pelicula especifica dentro de un multiplex.

Acceso: `BUYER`, `EMPLOYEE`.

Parametros:

- `multiplexId` (UUID): multiplex consultado.
- `movieId` (Long): id de la pelicula.

Errores esperados:

- Pelicula no existe en base de datos.
- No hay funciones de esa pelicula en ese multiplex.

---

# 3. Sillas

## GET /api/seats/{roomId}

Descripcion: lista las sillas de una sala con su estado actual.

Acceso: `BUYER`, `EMPLOYEE`.

Parametros:

- `roomId` (UUID): sala consultada.

Response 200:

```json
[
  {
    "idSeat": "750e8400-e29b-41d4-a716-446655440000",
    "roomId": "650e8400-e29b-41d4-a716-446655440000",
    "seatNumber": 1,
    "type": "GENERAL",
    "status": "AVAILABLE"
  }
]
```

## PUT /api/seats/{seatId}/changeStatus

Descripcion: alterna el estado de una silla.

Acceso: `BUYER`, `EMPLOYEE`.

Headers:

- `Authorization: Bearer {token}`

Logica:

- Si esta `AVAILABLE`, pasa a `BLOCKED` por el usuario actual durante 10 minutos.
- Si esta `BLOCKED` por el mismo usuario, vuelve a `AVAILABLE`.
- Si esta `BLOCKED` por otro usuario, retorna error de negocio.
- Si esta `SOLD`, retorna error de negocio.

Response 200:

```json
{
  "idSeat": "750e8400-e29b-41d4-a716-446655440000",
  "roomId": "650e8400-e29b-41d4-a716-446655440000",
  "seatNumber": 1,
  "type": "GENERAL",
  "status": "BLOCKED"
}
```

---

# 4. Snacks

## GET /api/snacks

Descripcion: lista snacks disponibles para compra o venta. Solo retorna snacks con cantidad mayor a cero.

Acceso: `BUYER`, `EMPLOYEE`, `MANAGER`.

Response 200:

```json
[
  {
    "idSnack": "950e8400-e29b-41d4-a716-446655440000",
    "nameSnack": "Palomitas Medianas",
    "descriptionSnack": "Palomitas de maiz",
    "priceSnack": 8500,
    "quantitySnack": 150
  }
]
```

## GET /api/admin/snacks

Descripcion: lista todos los snacks, incluyendo agotados.

Acceso: `ADMIN`, `MANAGER`.

## GET /api/admin/snacks/{id}

Descripcion: obtiene un snack por id.

Acceso: `ADMIN`, `MANAGER`.

## POST /api/admin/snacks

Descripcion: crea un snack.

Acceso: `ADMIN`, `MANAGER`.

Request:

```json
{
  "nameSnack": "Palomitas Grandes",
  "descriptionSnack": "Palomitas con mantequilla",
  "priceSnack": 12000,
  "quantitySnack": 100
}
```

Response 201: sin body.

## PUT /api/admin/snacks/{id}

Descripcion: actualiza un snack.

Acceso: `ADMIN`, `MANAGER`.

## DELETE /api/admin/snacks/{id}

Descripcion: elimina un snack.

Acceso: `ADMIN`, `MANAGER`.

Response 204: sin body.

Nota: actualmente los snacks no estan modelados por multiplex en el modelo de datos. Se habilito acceso a `MANAGER` para permitir que un gerente gestione snacks relacionados a su multiplex (la validacion de alcance y permisos puede centralizarse con `AccessValidator` si es necesario).

---

# 5. Checkout / compra / venta

## POST /api/checkout/stripe

Descripcion: confirma disponibilidad, calcula totales, crea sesion de pago Stripe y registra un pago `PENDING`.

Acceso: `BUYER`, `EMPLOYEE`.

Headers:

- `Authorization: Bearer {token}`

Request:

```json
{
  "screeningId": "850e8400-e29b-41d4-a716-446655440000",
  "seats": [
    { "seatId": "750e8400-e29b-41d4-a716-446655440000" }
  ],
  "snacks": [
    { "snackId": "950e8400-e29b-41d4-a716-446655440000", "quantity": 2 }
  ]
}
```

Response 200:

```json
{
  "multiplexId": "550e8400-e29b-41d4-a716-446655440000",
  "totalSeats": 11000,
  "totalSnacks": 17000,
  "totalPurchase": 28000,
  "status": "SUCCESS",
  "message": "Checkout creado correctamente",
  "sessionId": "cs_test_...",
  "sessionUrl": "https://checkout.stripe.com/...",
  "seats": [
    {
      "seatId": "750e8400-e29b-41d4-a716-446655440000",
      "seatType": "GENERAL",
      "seatStatus": "BLOCKED",
      "seatPrice": 11000
    }
  ],
  "snacks": [
    {
      "snackId": "950e8400-e29b-41d4-a716-446655440000",
      "nameSnack": "Palomitas Medianas",
      "quantity": 2,
      "unitPrice": 8500,
      "subtotal": 17000
    }
  ]
}
```

Notas:

- `/api/checkout/preview` y `/api/checkout/confirm` no existen como endpoints activos; estan comentados en el controlador.
- Si el checkout lo hace un `BUYER`, se registra la pelicula como vista en su historial.
- Si el checkout lo hace un `EMPLOYEE`, se procesa como venta de taquilla y no se intenta registrar historial de comprador.

---

# 6. Administracion de multiplex

## GET /api/admin/multiplexes

Descripcion: lista multiplex.

Acceso: `ADMIN`, `MANAGER`.

Comportamiento:

- `ADMIN` ve todos los multiplex.
- `MANAGER` recibe solo su multiplex asignado.

## GET /api/admin/multiplexes/{id}

Descripcion: obtiene detalle de un multiplex y sus salas.

Acceso: `ADMIN`, `MANAGER`.

Validacion:

- `MANAGER` solo puede consultar su multiplex asignado.

## POST /api/admin/multiplexes

Descripcion: crea multiplex y sus salas iniciales.

Acceso: `ADMIN`.

Request:

```json
{
  "nameMultiplex": "Cine Pacho Centro",
  "addressMultiplex": "Carrera 10 #15-30",
  "cityMultiplex": "Bogota",
  "numberOfRooms": 8,
  "generalSeatPrice": 11000,
  "preferentialSeatPrice": 15000
}
```

Validaciones:

- `numberOfRooms` minimo 5 y maximo 15.
- No puede existir otro multiplex con el mismo nombre y ciudad.

## PUT /api/admin/multiplexes/{id}

Descripcion: actualiza datos y precios de un multiplex.

Acceso: `ADMIN`, `MANAGER`.

Validacion:

- `MANAGER` solo puede actualizar su multiplex asignado.

## DELETE /api/admin/multiplexes/{id}

Descripcion: elimina fisicamente un multiplex.

Acceso: `ADMIN`.

---

# 7. Administracion de salas

## POST /api/admin/{multiplexId}/rooms

Descripcion: crea una sala en el multiplex indicado y genera sus sillas.

Acceso: `ADMIN`, `MANAGER`.

Validacion:

- `MANAGER` solo puede crear salas en su multiplex asignado.

Response 200:

```json
{
  "message": "Sala de cine creada con exito",
  "roomId": "650e8400-e29b-41d4-a716-446655440000"
}
```

## DELETE /api/admin/rooms/{id}

Descripcion: desactiva logicamente una sala.

Acceso: `ADMIN`, `MANAGER`.

Validacion:

- `MANAGER` solo puede eliminar salas de su multiplex asignado.

---

# 8. Administracion de peliculas y funciones

## GET /api/admin/movie/search?query={text}&page={numero}

Descripcion: busca peliculas en TMDB para seleccionarlas desde administracion.

Acceso: `ADMIN`, `MANAGER`.

Query params:

- `query` (string): texto de busqueda.
- `page` (int, opcional): pagina. Default: 1.

## POST /api/admin/movie/select/{movieId}

Descripcion: guarda una pelicula desde TMDB si no existe en base de datos.

Acceso: `ADMIN`, `MANAGER`.

## POST /api/admin/movie/createScreening

Descripcion: crea una funcion de una pelicula en una sala.

Acceso: `ADMIN`, `MANAGER`.

Validacion:

- `MANAGER` solo puede crear funciones en salas de su multiplex asignado.

Request:

```json
{
  "movieId": 603,
  "roomId": "650e8400-e29b-41d4-a716-446655440000",
  "dateTime": "2026-06-15 14:30:00"
}
```

Response 200:

```json
{
  "screeningId": "850e8400-e29b-41d4-a716-446655440000",
  "dateTime": "2026-06-15T14:30:00",
  "originalLanguage": "en",
  "originalTitle": "The Matrix",
  "overview": "A computer programmer discovers...",
  "rating": 0.0,
  "director": "The Wachowskis",
  "status": "ACTIVE",
  "genres": ["Action", "Science Fiction"]
}
```

## PUT /api/admin/movie/changeStatus/{idScreening}?status={status}

Descripcion: cambia estado de una funcion.

Acceso: `ADMIN`, `MANAGER`.

Validacion:

- `MANAGER` solo puede cambiar funciones de su multiplex asignado.

Valores validos:

- `ACTIVE`
- `CANCELLED`
- `COMPLETED`

---

# 9. Empleados y gerentes

## POST /api/admin/register_employee

Descripcion: registra personal del cine (`EMPLOYEE` o `MANAGER`).

Acceso: `ADMIN`, `MANAGER`.

Validacion:

- `MANAGER` solo puede registrar personal en su multiplex asignado.
- El request solo acepta `EMPLOYEE` o `MANAGER`.
- El campo de cedula se llama `indentityCard` en el DTO actual.

Request:

```json
{
  "email": "empleado@cinepacho.com",
  "name": "Carlos Lopez",
  "password": "EmpPass123!",
  "userType": "EMPLOYEE",
  "indentityCard": "1023456789",
  "phoneNumber": "3012345678",
  "salary": 2500000,
  "position": "Vendedor",
  "multiplexId": "550e8400-e29b-41d4-a716-446655440000"
}
```

Response 200:

```json
{
  "userType": "EMPLOYEE",
  "username": "empleado@cinepacho.com",
  "message": "Se ha creado correctamente el empleado"
}
```

---

# 10. Reviews

## GET /api/review/movie/{movieId}

Descripcion: lista reviews publicas de una pelicula.

Acceso: publico.

## GET /api/{buyerId}/review

Descripcion: lista reviews hechas por un comprador.

Acceso: `BUYER`, `ADMIN`.

Validacion:

- Si el usuario es `BUYER`, solo puede consultar sus propias reviews.
- `ADMIN` puede consultar cualquier comprador.

## POST /api/{buyerId}/review/movie

Descripcion: crea review de pelicula.

Acceso: `BUYER`.

Validacion:

- El comprador autenticado debe coincidir con `buyerId`.
- La pelicula debe existir.
- No puede existir review previa del mismo comprador para la misma pelicula.

## POST /api/{buyerId}/review/service

Descripcion: crea review de servicio.

Acceso: `BUYER`.

Validacion:

- El comprador autenticado debe coincidir con `buyerId`.

---

# 11. Resumen exacto de permisos del SecurityFilterChain

- `OPTIONS /**`: publico para CORS preflight.
- `/api/auth/register`: publico.
- `/api/auth/login`: publico.
- `/api/auth/verify`: publico.
- `GET /api/review/movie/**`: publico.
- `GET /api/topRatedMovies`: publico.
- `GET /api/movie/multiplex/**`: `BUYER`, `EMPLOYEE`.
- `/api/seats/**`: `BUYER`, `EMPLOYEE`.
- `GET /api/snacks`: `BUYER`, `EMPLOYEE`, `MANAGER`.
- `/api/checkout/**`: `BUYER`, `EMPLOYEE`.
- `GET /api/*/review`: `BUYER`, `ADMIN`.
- `POST /api/*/review/**`: `BUYER`.
- `POST /api/admin/multiplexes`: `ADMIN`.
- `DELETE /api/admin/multiplexes/**`: `ADMIN`.
- `GET /api/admin/multiplexes` y `GET /api/admin/multiplexes/**`: `ADMIN`, `MANAGER`.
- `PUT /api/admin/multiplexes/**`: `ADMIN`, `MANAGER`.
- `POST /api/admin/*/rooms`: `ADMIN`, `MANAGER`.
- `DELETE /api/admin/rooms/**`: `ADMIN`, `MANAGER`.
- `/api/admin/register_employee`: `ADMIN`, `MANAGER`.
- `/api/admin/movie/**`: `ADMIN`, `MANAGER`.
- `/api/admin/snacks` y `/api/admin/snacks/**`: `ADMIN`, `MANAGER`.
- `/api/admin/**`: `ADMIN`.
- Cualquier otra ruta: bloqueada.

---

# 12. Top 10 (público), Trailers y flujo de multiplex

## GET /topRatedMovies

Descripcion: devuelve las 10 peliculas con mejor rating en el sistema. Cada elemento es un `MovieListingResponseDTO` (info basica: id, titulo, generos, año de estreno, poster y backdrop).

Acceso: publico (sin autenticacion). Front: en la pantalla de inicio el front debe llamar `/api/topRatedMovies` y renderizar los 10 elementos. Cada tarjeta debe abrir la pagina de detalle de la pelicula al hacer click y debe mostrar un boton "Play" encima del poster.

Uso del trailer (front en React/JSX): al hacer click en el boton "Play" enviar GET a `/api/movie/trailer/{movieId}`. El backend devuelve la "key" del video de TMDB (string). En el front se puede reproducir con la URL:

https://www.youtube.com/watch?v={key}

(Otros usos: para embed usar `https://www.youtube.com/embed/{key}` si se quiere iframe).

Notas: el endpoint es publico para permitir mostrar el Top 10 en la home sin login.

---

## Flujo ideal para multiplex (acceso BUYYER)

1. Acceder a un multiplex (front autenticado como `BUYER`) -> llamar GET `/api/movie/multiplex/{multiplexId}`. Este endpoint devuelve las 8 peliculas con mejor rating disponibles en ese multiplex como `MovieListingResponseDTO` (info basica). El backend usa `getTop8ByMultiplexId`.

2. En la vista del multiplex mostrar esas 8 peliculas (cada tarjeta con poster y titulo). Si el usuario hace click en una de esas 8, el front debe llamar GET `/api/movie/multiplex/{multiplexId}/selectors/{movieId}` — este endpoint devuelve un `MovieSelectorDTO` con todas las funciones disponibles y, muy importante, contiene la `key` del trailer para poder reproducirlo.

3. Barra de búsqueda dentro del multiplex: usar GET `/api/movie/multiplex/{multiplexId}/selectors?query={texto}` (si `query` vacio retorna la cartelera). Importante: por diseño la respuesta de la barra de búsqueda devuelve como maximo 4 `MovieSelectorDTO` (limite en backend para evitar sobrecarga de peticiones). Cada `MovieSelectorDTO` ya incluye la `key` del trailer.

4. Si se desea obtener solo la pelicula seleccionada (sin el listado), usar GET `/api/movie/multiplex/{multiplexId}/selectors/{movieId}`.

Resumen: home -> `/api/topRatedMovies` (publico, top10). Multiplex -> `/api/movie/multiplex/{multiplexId}` (buyer) que llama internamente a `getTop8ByMultiplexId`. La busqueda en multiplex usa `/api/movie/multiplex/{multiplexId}/selectors?query=` y devuelve max 4 resultados.

---

# 13. Notas generales (tecnicas)

- Todas las rutas protegidas requieren header `Authorization: Bearer {token}`.
- UUID se envia como string.
- Fechas de request de screenings usan `yyyy-MM-dd HH:mm:ss` segun los DTO actuales.
- Algunas respuestas serializan `LocalDateTime` en formato ISO (`2026-06-15T14:30:00`) si no hay formato custom en el DTO.
- Los precios se manejan como `BigDecimal`.
- `AccessValidator` centraliza la restriccion de multiplex para `MANAGER`.
- `RoomManager`, `MovieManager`, `BuyerManager`, `SnackManager`, `SeatManager` y otros contratos en `shared.auxiliaryClass` se usan para evitar acoplamiento directo entre modulos.
