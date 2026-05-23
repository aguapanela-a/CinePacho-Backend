# 🎬 API Documentation - Cine Backend

---

# 🎬 1. Multiplex

## 🔹 GET /admin/multiplexes

**Descripción:** Obtener todos los multiplex

```json
[
  {
    "idMultiplex": "string",
    "nameMultiplex": "string",
    "cityMultiplex": "string"
  }
]
```

---

## 🔹 GET /admin/multiplexes/{id}

**Descripción:** Obtener multiplex por ID

```json
{
  "idMultiplex": "string",
  "nameMultiplex": "string",
  "addressMultiplex": "string",
  "cityMultiplex": "string",
  "rooms": [
    {
      "idRoom": "string",
      "numberRoom": 0,
      "generalCapacity": 40,
      "preferentialCapacity": 20,
      "isRoomActive": true
    }
  ]
}
```

---

## 🔹 POST /admin/multiplexes

**Request**

```json
{
  "nameMultiplex": "string",
  "addressMultiplex": "string",
  "cityMultiplex": "string"
}
```

**Response**

```json
{
  "idMultiplex": "string",
  "nameMultiplex": "string",
  "addressMultiplex": "string",
  "cityMultiplex": "string",
  "rooms": []
}
```

---

## 🔹 PUT /admin/multiplexes/{id}

**Request**

```json
{
  "nameMultiplex": "string",
  "addressMultiplex": "string",
  "cityMultiplex": "string"
}
```

**Response**

```json
{
  "idMultiplex": "string",
  "nameMultiplex": "string",
  "addressMultiplex": "string",
  "cityMultiplex": "string",
  "rooms": []
}
```

---

## 🔹 DELETE /admin/multiplexes/{id}

---

# 🎥 2. Salas (Rooms)

## 🔹 GET /admin/rooms

```json
[
  {
    "idRoom": "string",
    "generalCapacity": 0,
    "preferentialCapacity": 0,
    "isRoomActive": true
  }
]
```

---

## 🔹 GET /admin/rooms/{id}

```json
{
  "idRoom": "string",
  "numberRoom": 0,
  "isRoomActive": true,
  "seats": {
    "availableGeneral": 0,
    "availablePreferential": 0,
    "totalAvailable": 0
  }
}
```

---

## 🔹 POST /admin/rooms

**Request**

```json
{
  "multiplexId": "string",
  "numberRoom": 0
}
```

**Response**

```json
{
  "idRoom": "string",
  "numberRoom": 0,
  "isRoomActive": true,
  "seats": {
    "availableGeneral": 0,
    "availablePreferential": 0,
    "totalAvailable": 0
  }
}
```

---

## 🔹 DELETE /admin/rooms/{id}

---

# 💺 3. Sillas (Seats)

## 🔹 PUT /seats/{seatId}/changeStatus

**Descripción:** Cambia el estado de la silla al estado contrario si no está reservada por otro usuario. (Uso compartido por BUYER y EMPLOYEE)

**Request**

No requiere body. El `seatId` se envía como parámetro de ruta.

**Response**

```json
{
  "idSeat": "string",
  "roomId": "string",
  "seatNumber": 0,
  "type": "GENERAL",
  "SeatStatus" : "STATUS"
}
```

**Valores posibles para `type`:**

* GENERAL
* PREFERENTIAL

**Valores posibles para `SeatStatus`:**

* AVAILABLE
* BLOCKED
* SOLD

---

# 🎬 4. Películas

## 🔹 GET /admin/movie/search?query={text}

**Descripción:** Búsqueda dinámica (TMDB)

```json
[
  {
    "id": 0,
    "backdropPath": "string",
    "genreIds": [0],
    "originalLanguage": "string",
    "originalTitle": "string",
    "overview": "string",
    "posterPath": "string",
    "releaseDate": "yyyy-MM-dd"
  }
]
```

---

## 🔹 POST /admin/movie/select/{movieId}

```json
{
  "originalTitle": "string",
  "director": "string",
  "message": "string"
}
```

---

## 🔹 POST /admin/movie/createScreening

**Request**

```json
{
  "movieId": 0,
  "roomId": "uuid",
  "dateTime": "yyyy-MM-dd HH:mm:ss"
}
```

**Response**

```json
{
  "screeningId": "uuid",
  "dateTime": "yyyy-MM-dd HH:mm:ss",
  "originalLanguage": "string",
  "originalTitle": "string",
  "overview": "string",
  "rating": 0.0,
  "director": "string",
  "status": "ACTIVE",
  "genres": ["string"]
}
```

---

## 🔹 PUT /admin/movie/changeStatus/{idScreening}

**Request**

```json
{
  "status": "ACTIVE"
}
```

**Valores posibles:**

* ACTIVE
* CANCELLED
* COMPLETED

**Response**

```json
{
  "screeningStatus": "ACTIVE",
  "screeningId": "uuid"
}
```

---

# 🍿 5. Snacks

## 🔹 GET /admin/snacks

```json
[
  {
    "idSnack": "string",
    "nameSnack": "string",
    "descriptionSnack": "string",
    "priceSnack": 0.0,
    "quantitySnack": 0
  }
]
```

---

## 🔹 GET /admin/snacks/{id}

```json
{
  "idSnack": "string",
  "nameSnack": "string",
  "descriptionSnack": "string",
  "priceSnack": 0.0,
  "quantitySnack": 0
}
```

---

## 🔹 POST /admin/snacks

```json
{
  "nameSnack": "string",
  "descriptionSnack": "string",
  "priceSnack": 0.0,
  "quantitySnack": 0
}
```

---

## 🔹 PUT /admin/snacks/{id}

```json
{
  "nameSnack": "string",
  "descriptionSnack": "string",
  "priceSnack": 0.0,
  "quantitySnack": 0
}
```

---

# ⚠️ Notas

* Todas las fechas usan formato: `yyyy-MM-dd HH:mm:ss`
* UUID se maneja como string
* Las listas siempre vienen en formato `[ ]`
* Los objetos en `{ }`
