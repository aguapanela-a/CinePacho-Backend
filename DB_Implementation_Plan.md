# Plan de Implementación de Bases de Datos — Cine Pacho

## 1. Resumen general
Cine Pacho es una red de multiplex y puntos ágiles que venden boletas y snacks en línea desde distintos puntos físicos y multiplex en Latinoamérica.

El sistema debe soportar:
- Venta de boletería y snacks en cada punto ágil y multiplex.
- Disponibilidad de salas y asientos por función.
- Bloqueo temporal de asientos hasta que se complete el pago.
- Gestión de puntos por comprador (10 pts por boleta, 5 pts por snacks).
- Canje de boleta gratuita al llegar a 100 pts.
- Control de empleados por multiplex, con rol y cargo fijo en un único multiplex.
- Sincronización parcial entre bases locales y una base central.
- Reportes mensuales y consolidación de ventas.

## 2. Estado actual del backend
El backend actual utiliza:
- Spring Boot con `spring-boot-starter-data-jpa`.
- PostgreSQL como base de datos principal.
- Configuración en `src/main/resources/application.properties`:
  - `spring.datasource.url=${DB_URL}`
  - `spring.datasource.username=${DB_USERNAME}`
  - `spring.datasource.password=${DB_PASSWORD}`
  - `spring.jpa.hibernate.ddl-auto=update`

Esta configuración indica que el proyecto está preparado para conectarse a PostgreSQL usando JPA.

## 3. Arquitectura de base de datos propuesta

### 3.1. Diseño principal
Se propone una arquitectura de bases de datos con dos capas:

1. **Base de datos central** (centralizada)
   - Contiene datos maestros y reportes.
   - Consolida información de todos los multiplex y puntos ágiles.
   - Alimenta reportes mensuales y datos administrativos.

2. **Bases de datos locales** (por punto ágil / multiplex)
   - Cada punto ágil o multiplex puede tener su propia base PostgreSQL.
   - Administra ventas locales, bloqueo de asientos, transacciones y stock de snacks.
   - Mantiene datos fragmentados y sincroniza los datos que se requieran con la capa central.

### 3.2. Topología propuesta
- Base central en Bogotá u otro datacenter principal.
- Bases locales en cada punto ágil/multiplex: Titán, Unicentro, Plaza Central, Gran Estación, Embajador, Las Américas.
- Cada DB local puede ser un PostgreSQL independiente o un esquema aislado en un servidor centralizado.

### 3.3. Tipo de sincronización
- **Sincronización parcial y unidireccional/bi-direccional**.
- Los datos maestros pueden replicarse hacia locales.
- Las transacciones de venta se sincronizan hacia el central.
- Se recomienda usar:
  - API de sincronización programada.
  - Mensajería/cola si se agrega en el futuro.
  - Batch de sincronización diaria y eventos en tiempo real para pedidos críticos.

## 4. Datos a sincronizar

### 4.1. Datos a mantener en bases locales
- Multiplex, salas, asientos y funciones disponibles.
- Películas y programación local.
- Catálogo de snacks y precios por punto.
- Ventas de boletas y snacks del punto.
- Bloqueos de asientos y estado de asientos por screening.
- Clientes/buyers registrados y su balance de puntos local.
- Historial de tickets vendidos del punto.
- Inventario de snacks (opcional) y consumo en local.

### 4.2. Datos centralizados
- Información consolidada de ventas por multiplex.
- Reportes de ticket y snack sales.
- Movilidad y datos de empleados para estadísticas.
- Usuarios globales y roles si se desea centralizar administración.
- Canjes de puntos y recompensas.
- Estrategias y promociones globales.

### 4.3. Datos de sincronización obligatoria
- Ventas confirmadas (boletas/snacks) desde locales hacia central.
- Puntos acumulados y canjes de clientes.
- Cambios de empleados / asignaciones de multiplex.
- Nuevas funciones, precios o snacks que deben distribuirse a locales.

## 5. Modelo de datos recomendado

### 5.1. Tablas principales

#### `multiplex`
- `id` UUID
- `name` VARCHAR
- `location` VARCHAR
- `address` VARCHAR
- `city` VARCHAR
- `created_at`
- `updated_at`

#### `room`
- `id` UUID
- `multiplex_id` UUID
- `name` VARCHAR
- `type` ENUM(`GENERAL`, `PREFERENCIAL`)
- `seat_capacity` INT
- `created_at`
- `updated_at`

#### `seat`
- `id` UUID
- `room_id` UUID
- `number` VARCHAR
- `type` ENUM(`GENERAL`, `PREFERENCIAL`)
- `price` NUMERIC
- `created_at`
- `updated_at`

#### `movie`
- `id` UUID
- `title` VARCHAR
- `genre` VARCHAR
- `duration_minutes` INT
- `rating` VARCHAR
- `description` TEXT
- `created_at`
- `updated_at`

#### `screening` (MovieScreening)
- `id` UUID
- `movie_id` UUID
- `room_id` UUID
- `multiplex_id` UUID
- `screening_time` TIMESTAMP
- `status` ENUM(`ACTIVE`,`CANCELLED`,`FINISHED`)
- `created_at`
- `updated_at`

#### `seat_screening`
- `id` UUID
- `seat_id` UUID
- `screening_id` UUID
- `status` ENUM(`AVAILABLE`,`BLOCKED`,`SOLD`)
- `blocked_by` VARCHAR
- `blocked_until` TIMESTAMP
- `created_at`
- `updated_at`

#### `snack`
- `id` UUID
- `name` VARCHAR
- `category` VARCHAR
- `price` NUMERIC
- `available` BOOLEAN
- `created_at`
- `updated_at`

#### `buyer`
- `id` UUID
- `email` VARCHAR
- `name` VARCHAR
- `phone` VARCHAR
- `points` INT
- `created_at`
- `updated_at`

#### `employee`
- `id` UUID
- `identity_card` VARCHAR
- `name` VARCHAR
- `phone` VARCHAR
- `hire_date` DATE
- `salary` NUMERIC
- `multiplex_id` UUID
- `role` ENUM(`DIRECTOR`,`CAJERO`,`DESPACHADOR`,`ENCARGADO_SALA`,`ASEADOR`,`MANAGER`)
- `position_updated_at` TIMESTAMP
- `created_at`
- `updated_at`

#### `user` / `auth_user`
- `id` UUID
- `email` VARCHAR
- `password_hash` VARCHAR
- `user_type` ENUM(`BUYER`,`EMPLOYEE`,`MANAGER`,`ADMIN`)
- `employee_id` UUID nullable
- `created_at`
- `updated_at`

#### `payment`
- `id` UUID
- `buyer_id` UUID
- `amount` NUMERIC
- `method` VARCHAR
- `status` ENUM(`PENDING`,`COMPLETED`,`CANCELLED`)
- `created_at`
- `updated_at`

#### `billing`
- `id` UUID
- `payment_id` UUID
- `multiplex_id` UUID
- `buyer_id` UUID
- `employee_id` UUID
- `total` NUMERIC
- `scanned` BOOLEAN
- `created_at`
- `updated_at`

#### `ticket_sale`
- `id` UUID
- `billing_id` UUID
- `screening_id` UUID
- `seat_id` UUID
- `price` NUMERIC
- `created_at`
- `updated_at`

#### `snack_sale`
- `id` UUID
- `billing_id` UUID
- `snack_id` UUID
- `quantity` INT
- `unit_price` NUMERIC
- `created_at`
- `updated_at`

#### `points_transaction`
- `id` UUID
- `buyer_id` UUID
- `points` INT
- `type` ENUM(`EARN`,`REDEEM`,`ADJUSTMENT`)
- `description` VARCHAR
- `created_at`

### 5.2. Tablas de reporte
- `monthly_sales_report`
- `employee_mobility_report`
- `multiplex_performance_report`

Estas tablas pueden generarse como vistas/materialized views en la DB central.

## 6. Sincronización de datos

### 6.1. Qué sincronizar desde locales hacia central
- Ventas confirmadas de boletas y snacks.
- Canjes de puntos y transacciones de buyer.
- Escaneos de facturas / validaciones de ticket.
- Cambios de inventario o stock relevante del punto.

### 6.2. Qué sincronizar desde central hacia locales
- Catálogo de multiplex, salas y asientos.
- Programación de funciones y películas.
- Lista de snacks y precios.
- Datos de empleados y roles si se gestionan centralmente.

### 6.3. Estrategia de sincronización
1. **Sincronización en tiempo cercano**
   - Cada venta local se registra en la DB local.
   - Un servicio de sincronización lee nuevas transacciones y las envía a la DB central.
   - Puede ejecutarse con un job cron cada 1-5 minutos.

2. **Sincronización por eventos**
   - Cuando se confirme un pago, el backend local publica un evento de venta.
   - Un consumer central procesa el evento y actualiza la DB central.

3. **Resolución de conflictos**
   - Las ventas locales son autoritativas para cada punto.
   - La central debe validar duplicados por `payment_id` o `billing_id`.

### 6.4. Mecanismos recomendados
- API REST para push/pull de datos.
- Tabla de `sync_log` o `outbox` para transacciones locales pendientes.
- Hash / checksum de datos maestros para detectar cambios.
- Job de reconciliación nocturno.

## 7. Recomendaciones técnicas

### 7.1. Configuración de Spring Boot
- Mantener `spring.datasource.url` apuntando al PostgreSQL local/central según perfil.
- Definir perfiles:
  - `local` para desarrollo.
  - `branch` o `edge` para puntos ágiles.
  - `central` para la DB de consolidación.
- Evitar `spring.jpa.hibernate.ddl-auto=update` en producción; usar migraciones con Flyway o Liquibase.

### 7.2. Deployment
- Cada punto ágil debe tener su servicio backend con DB local PostgreSQL.
- El backend central puede correr en un servidor separado con su propia DB.
- Configurar variables de entorno:
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
  - `APP_BASE_URL`, `STRIPE_API_KEY`, `JWT_SECRET`

### 7.3. Consistencia de datos
- Local: consumo rápido, bloqueo de asientos y venta en tiempo real.
- Central: reporte y análisis, no es necesaria la consistencia fuerte inmediata.
- Usar IDs UUID para permitir merge seguro entre bases.

## 8. Pasos de implementación

1. Diseñar el esquema físico para la DB central y el DB local.
2. Implementar entidades JPA para los objetos del modelo.
3. Crear repositorios Spring Data para operaciones.
4. Construir migraciones SQL o Flyway.
5. Definir el flujo de sincronización y tablas `sync_queue` / `outbox`.
6. Desplegar una DB local por punto ágil y una DB central.
7. Probar ventas locales, sincronización y reportes.
8. Verificar la consolidación de puntos, asignación de boleta gratis y reporte mensual.

### 8.1. Paso a paso práctico

1. En el backend, crea o actualiza el archivo de configuración para PostgreSQL local y central.
   - `src/main/resources/application-local.properties`
   - `src/main/resources/application-central.properties`
   - Asegura variables de entorno: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.

2. Crear las tablas del modelo en PostgreSQL usando migraciones.
   - Si usas Flyway, añade `src/main/resources/db/migration/V1__create_schema.sql`.
   - Si usas SQL directo, ejecuta en psql:
     ```sql
     CREATE TABLE multiplex (...);
     CREATE TABLE room (...);
     CREATE TABLE seat (...);
     CREATE TABLE movie (...);
     CREATE TABLE screening (...);
     CREATE TABLE seat_screening (...);
     CREATE TABLE snack (...);
     CREATE TABLE buyer (...);
     CREATE TABLE employee (...);
     CREATE TABLE auth_user (...);
     CREATE TABLE payment (...);
     CREATE TABLE billing (...);
     CREATE TABLE ticket_sale (...);
     CREATE TABLE snack_sale (...);
     CREATE TABLE points_transaction (...);
     ```

3. Implementar las entidades JPA correspondientes.
   - Define cada entidad con `@Entity`, `@Table`, `@Id` y relaciones básicas (`@ManyToOne`, `@OneToMany`).
   - Usa `UUID` para los identificadores y `@GeneratedValue` con `UUIDGenerator` si es necesario.

4. Crear repositorios Spring Data.
   - `interface MultiplexRepository extends JpaRepository<Multiplex, UUID>`
   - `interface ScreeningRepository extends JpaRepository<Screening, UUID>`
   - `interface BillingRepository extends JpaRepository<Billing, UUID>`
   - `interface PointsTransactionRepository extends JpaRepository<PointsTransaction, UUID>`

5. Añadir servicios para las operaciones locales.
   - Servicio de bloqueo y liberación de asientos.
   - Servicio de creación de pago y factura.
   - Servicio de acumulación y canje de puntos.

6. Crear un servicio de sincronización básico.
   - Añade una entidad `SyncLog` o `OutboxMessage`.
   - Cada vez que una venta se confirme, guarda un registro en `sync_log`.
   - Crea un job programado con `@Scheduled(fixedDelay = 300000)` para leer `sync_log` y enviar los datos a la DB central.

7. Desplegar la DB local y la DB central.
   - Inicia PostgreSQL local con la configuración adecuada.
   - Crea la base de datos central en otro servidor o instancia.
   - Asegura que el backend local use `application-local` y el backend central use `application-central`.

8. Probar el flujo completo.
   - Registra una venta de boleta y snacks en la DB local.
   - Verifica que los asientos cambian de `AVAILABLE` a `BLOCKED` y luego a `SOLD`.
   - Confirma el pago o la factura.
   - Ejecuta el job de sincronización para replicar la venta al central.
   - Comprueba en la DB central que la venta existe y que el cliente recibió los puntos.

9. Validar reportes y consolidación.
   - Genera una consulta de ventas mensual en la DB central.
   - Revisa la tabla o vista `monthly_sales_report`.
   - Verifica que los puntos `REDEEM` y `EARN` se reflejen correctamente.

10. Repetir para un segundo punto ágil.
    - Añade otra DB local con su propio esquema.
    - Crea un nuevo backend local apuntando a esa DB.
    - Sincroniza ventas al central para validar consolidación entre múltiples puntos.

## 8.2. Ejemplo real: multiplex Plaza Central

Este ejemplo usa la estructura actual del backend con tablas como `multiplex`, `rooms`, `seats`, `movie_screenings`, `seat_screenings`, `snacks`, `buyers`, `employees`, `payments` y `billings`.

### Configuración de perfil local para Plaza Central

- `application-local.properties`
  - `spring.datasource.url=jdbc:postgresql://localhost:5432/cine_plaza_central`
  - `spring.datasource.username=plaza_central_user`
  - `spring.datasource.password=secret`
  - `spring.profiles.active=local`
  - `app.base-url=http://localhost:8010`

- `application-central.properties`
  - `spring.datasource.url=jdbc:postgresql://central-db:5432/cine_central`
  - `spring.datasource.username=central_user`
  - `spring.datasource.password=secret`
  - `spring.profiles.active=central`

### Datos de ejemplo para Plaza Central

1. Multiplex `Plaza Central`
   - `name`: Plaza Central
   - `city`: Bogotá
   - `address`: Cra 7 #45-60
   - `general_seat_price`: 11000
   - `preferential_seat_price`: 15000

2. Salas locales
   - Sala A: `room_number` = A, `general_capacity` = 80, `preferential_capacity` = 20
   - Sala B: `room_number` = B, `general_capacity` = 70, `preferential_capacity` = 15

3. Película y función
   - Película: `Super Cine 2026`
   - Función: `screen_date_time` = 2026-06-10 19:30, `status` = ACTIVE

4. Snacks disponibles
   - `Popcorn Grande`, `precio` = 15000, `cantidad` = 50
   - `Refresco 500ml`, `precio` = 8000, `cantidad` = 80

5. Comprador de prueba
   - `email`: cliente@ejemplo.com
   - `points`: 45

6. Empleado de Plaza Central
   - `uniqueCode`: 1001
   - `rol`: CAJERO
   - `multiplex`: Plaza Central

### Flujo de venta local para Plaza Central

1. El comprador selecciona la función de `Super Cine 2026` en Sala A.
2. El sistema bloquea las sillas en `seat_screenings`:
   - `status` = BLOCKED
   - `blocked_by_user_email` = cliente@ejemplo.com
   - `blocked_until` = ahora + 15 minutos
3. Se crea un `PaymentEntity` con `status` = PENDING y se genera un `BillingEntity`.
4. Al confirmarse el pago con Stripe, el pago pasa a `COMPLETED` y se guardan totales en `billings`.
5. Las sillas se marcan como vendidas (`status` = SOLD) en `seat_screenings`.
6. Se registra la acumulación de puntos del comprador en `buyers`.
7. El servicio de sincronización envía esta venta a la base central.

### Ejemplo de datos SQL simplificado

```sql
INSERT INTO multiplex (id, name, city, address, general_seat_price, preferential_seat_price)
VALUES ('00000000-0000-0000-0000-000000000001', 'Plaza Central', 'Bogotá', 'Cra 7 #45-60', 11000, 15000);

INSERT INTO rooms (id, room_number, multiplex_id, general_capacity, preferential_capacity, active, created_at)
VALUES
  ('00000000-0000-0000-0000-000000000011', 'A', '00000000-0000-0000-0000-000000000001', 80, 20, true, NOW()),
  ('00000000-0000-0000-0000-000000000012', 'B', '00000000-0000-0000-0000-000000000001', 70, 15, true, NOW());

INSERT INTO snacks (id, nombre, descripcion, precio, cantidad, points, multiplex_id)
VALUES
  ('00000000-0000-0000-0000-000000000021', 'Popcorn Grande', 'Palomitas grandes', 15000, 50, 10, '00000000-0000-0000-0000-000000000001'),
  ('00000000-0000-0000-0000-000000000022', 'Refresco 500ml', 'Refresco cola', 8000, 80, 5, '00000000-0000-0000-0000-000000000001');

INSERT INTO buyers (buyer_id, user_id, points, correo)
VALUES ('00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000041', 45, 'cliente@ejemplo.com');
```

### Cómo usar el ejemplo

- Levanta la aplicación local de Plaza Central con el perfil `local`.
- Usa el plan para poblar los datos iniciales en la DB local.
- Ejecuta compras de prueba y revisa `seat_screenings`, `payments` y `billings`.
- Confirma que la venta se sincroniza al servidor central y aparece en el consolidado.

## 8.3. Creación de multiplexes por admin y su BD asociada

El backend actual ya soporta la creación de multiplexes desde el admin con `POST /api/admin/multiplexes`.

### Payload de creación de multiplex por admin

```json
{
  "nameMultiplex": "Plaza Central",
  "addressMultiplex": "Cra 7 #45-60",
  "cityMultiplex": "Bogotá",
  "numberOfRooms": 8,
  "generalSeatPrice": 11000,
  "preferentialSeatPrice": 15000
}
```

### Comportamiento del backend

1. Valida que no exista otro multiplex con el mismo nombre y ciudad.
2. Crea un registro `multiplex` en la base de datos.
3. Si `generalSeatPrice` o `preferentialSeatPrice` no llegan, se usan los valores por defecto 11000/15000.
4. Crea automáticamente entre 5 y 15 salas (`rooms`) usando `RoomManager.createRoom(multiplex)`.
5. Devuelve los datos del multiplex con el listado de salas creado.

### Cómo queda la base de datos cuando el admin crea el multiplex

- Se inserta un registro en `multiplex`.
- Se insertan `numberOfRooms` registros en `rooms` con `multiplex_id` apuntando al multiplex.
- Cada sala creada queda disponible para que luego el admin/manager agregue funciones, asientos y snacks.

Ejemplo simplificado de estado final:

```sql
-- Multiplex creado por admin
INSERT INTO multiplex (id, name, city, address, general_seat_price, preferential_seat_price)
VALUES ('00000000-0000-0000-0000-000000000001', 'Plaza Central', 'Bogotá', 'Cra 7 #45-60', 11000, 15000);

-- Salas generadas automáticamente
INSERT INTO rooms (id, room_number, multiplex_id, general_capacity, preferential_capacity, active, created_at)
VALUES
  ('00000000-0000-0000-0000-000000000011', 'A', '00000000-0000-0000-0000-000000000001', 80, 20, true, NOW()),
  ('00000000-0000-0000-0000-000000000012', 'B', '00000000-0000-0000-0000-000000000001', 80, 20, true, NOW()),
  ('00000000-0000-0000-0000-000000000013', 'C', '00000000-0000-0000-0000-000000000001', 80, 20, true, NOW());
```

### Qué más necesita el flujo de admin

- Validación de datos en el request: ya usa `jakarta.validation` en `MultiplexRequest`.
- Seguridad: solo `ADMIN` puede crear y eliminar multiplexes; `MANAGER` puede actualizar el multiplex asignado.
- Manejo de errores: lanzar excepción clara si el multiplex ya existe.
- Creación inmediata de la estructura básica de la BD para que el multiplex quede en funcionamiento desde el primer registro.

## 8.4. Extensión a más multiplexes y estructuras locales

### Multiplexes adicionales

Para cada nuevo multiplex se debe:
- Crear el registro en la base de datos local del punto.
- Generar automáticamente las salas asociadas.
- Crear entradas de snacks, funciones y asientos en esa base local.
- Conectar el backend local con la DB local correspondiente.

### Ejemplo de otros multiplexes

- `Gran Estación`, `Bogotá`, `Av. 26 #20-50`
- `Unicentro`, `Bogotá`, `Cl. 127 #12-34`
- `Embajador`, `Bogotá`, `Av. Suba #56-78`

Para cada uno, el admin puede usar el mismo endpoint y la BD quedará lista con su lista de salas.

### Sincronización y consolidación

- Si el backend local es la fuente de datos operativa, el registro del multiplex se almacena allí.
- Luego, el servicio de sincronización envía el registro al central o la central fusiona el nuevo multiplex.
- Si el central es la fuente maestra, el local puede replicar el registro a partir de esa base.

## 8.5. Tecnologías y elementos adicionales a meter

Además de JPA y PostgreSQL, el plan debe incluir:

- `Flyway` o `Liquibase` para migraciones de esquema en producción.
- `jakarta.validation` / `hibernate-validator` para validar DTOs de admin y checkout.
- `Spring Security` con JWT para proteger endpoints `admin`, `manager` y `buyer`.
- `@Scheduled` o un job scheduler para:
  - Liberar bloqueos de asientos expirados.
  - Ejecutar sincronización periódica hacia el central.
- `Outbox` / `sync_log` para asegurar que eventos de venta se envían correctamente.
- Monitoreo y logs estructurados para detectar fallos de sincronización.
- Backup/restore de PostgreSQL (`pg_dump`, replicación, snapshots).
- Una capa de deploy: Docker / Docker Compose / Kubernetes para componentes backend y bases.
- Cache si la carga aumenta: Redis para datos maestros que no cambian con frecuencia.
- Un sistema de colas (Kafka / RabbitMQ) si se quiere escalar la sincronización en tiempo real.

## 8.6. Ejemplo de DDL para tablas clave

Estas definiciones son una base práctica compatible con el backend actual y permiten que la DB se genere de forma ordenada.

```sql
CREATE TABLE multiplex (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(100) NOT NULL,
  city VARCHAR(100) NOT NULL,
  address VARCHAR(200) NOT NULL,
  general_seat_price NUMERIC(10,2) NOT NULL,
  preferential_seat_price NUMERIC(10,2) NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE rooms (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  multiplex_id UUID NOT NULL REFERENCES multiplex(id),
  room_number VARCHAR(10) NOT NULL,
  general_capacity INT NOT NULL,
  preferential_capacity INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE seats (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  room_id UUID NOT NULL REFERENCES rooms(id),
  seat_number INT NOT NULL,
  type VARCHAR(20) NOT NULL,
  status VARCHAR(20) NOT NULL,
  blocked_by_user_email VARCHAR(255),
  blocked_until TIMESTAMP WITHOUT TIME ZONE,
  UNIQUE (room_id, seat_number, type)
);

CREATE TABLE movie_screenings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  movie_id UUID NOT NULL,
  room_id UUID NOT NULL REFERENCES rooms(id),
  screen_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  status VARCHAR(20) NOT NULL
);

CREATE TABLE seat_screenings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  seat_id UUID NOT NULL REFERENCES seats(id),
  screening_id UUID NOT NULL REFERENCES movie_screenings(id),
  status VARCHAR(20) NOT NULL,
  blocked_by_user_email VARCHAR(255),
  blocked_until TIMESTAMP WITHOUT TIME ZONE,
  points INT,
  UNIQUE (seat_id, screening_id)
);

CREATE TABLE snacks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre VARCHAR(100) NOT NULL,
  descripcion VARCHAR(500),
  precio NUMERIC(10,2) NOT NULL,
  cantidad INT NOT NULL,
  points INT,
  multiplex_id UUID NOT NULL REFERENCES multiplex(id)
);

CREATE TABLE buyers (
  buyer_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID UNIQUE,
  points INT NOT NULL DEFAULT 0,
  correo VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE auth_user (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  user_type VARCHAR(20) NOT NULL,
  employee_id UUID
);

CREATE TABLE payment (
  payment_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  amount NUMERIC(10,2) NOT NULL,
  payment_method VARCHAR(100) NOT NULL,
  status VARCHAR(20) NOT NULL
);

CREATE TABLE billings (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  payment_id UUID REFERENCES payment(payment_id),
  buyer_id UUID REFERENCES buyers(buyer_id),
  employee_id UUID,
  multiplex_id UUID REFERENCES multiplex(id),
  total_seats NUMERIC(10,2),
  total_snacks NUMERIC(10,2),
  total_purchase NUMERIC(10,2),
  qr_base64 TEXT,
  qr_status VARCHAR(20),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  scanned_at TIMESTAMP WITHOUT TIME ZONE,
  room_number VARCHAR(50),
  movie_title VARCHAR(255),
  screening_date VARCHAR(100),
  screening_id UUID
);

CREATE TABLE points_transaction (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  buyer_id UUID REFERENCES buyers(buyer_id),
  points INT NOT NULL,
  type VARCHAR(20) NOT NULL,
  description VARCHAR(255),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);
```

## 8.7. Migraciones y bootstrap de datos

### Migraciones

- Usa Flyway o Liquibase para mantener el esquema controlado.
- Crea un primer script `V1__create_schema.sql` con las tablas anteriores.
- Crea scripts posteriores para cambios de columnas, índices, datos maestros y mejoras.
- Añade un script de datos iniciales `V2__seed_initial_data.sql` para:
  - Multiplexes base.
  - Roles y usuarios administrativos.
  - Configuración de precios por defecto.

### Bootstrap de la estructura creada por admin

- El admin crea un multiplex vía `POST /api/admin/multiplexes`.
- La API inserta el multiplex y dispara la creación de `numberOfRooms` en `rooms`.
- Luego el manager puede completar con:
  - funciones (`movie_screenings`)
  - asientos (`seats` y `seat_screenings`)
  - snacks (`snacks`)

## 8.8. Configuración de entornos y perfiles

- `application-local.properties`: Dev local y pruebas.
- `application-central.properties`: Consolidación central.
- `application-prod.properties`: Producción con seguridad adicional.
- Variables clave:
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
  - `STRIPE_API_KEY`, `JWT_SECRET`, `APP_BASE_URL`
  - `SPRING_PROFILES_ACTIVE`

- Cada instancia backend debe apuntar a su DB correspondiente.
- Las bases locales pueden usar nombres como `cine_plaza_central`, `cine_unicentro`, `cine_gran_estacion`.

## 8.9. Ejemplo de flujo de creación de multiplex en DB local

1. El admin crea el multiplex `Plaza Central`.
2. El backend inserta un registro en `multiplex`.
3. Si el request indica 8 salas, se crean 8 registros `rooms`.
4. El manager agrega funciones y snacks sobre ese multiplex.
5. Los asientos se cargan y se vinculan a salas.
6. El frontend consume la configuración para mostrar la programación y catálogo.

## 8.10. Recomendaciones de operaciones y pruebas

- Probar la creación de multiplex desde `POST /api/admin/multiplexes`.
- Verificar en DB local que los `rooms` se generaron automáticamente.
- Probar la creación de asientos y la reserva de `seat_screenings`.
- Validar que al pagar se guarda `payment` y `billings`, y que el estado de asiento cambia.
- Probar la liberación automática de asientos bloqueados con `@Scheduled`.
- Simular la sincronización al central y verificar que `payment_id` no se duplica.

## 8.11. Ejemplo de request / response para `POST /api/admin/multiplexes`

El admin debe poder crear un multiplex con un único request y obtener la estructura básica lista en la DB local.

### Request ejemplo

```json
{
  "name": "Plaza Central",
  "city": "Bogotá",
  "address": "Av. Caracas #45-30",
  "generalSeatPrice": 15000.00,
  "preferentialSeatPrice": 22000.00,
  "numberOfRooms": 8,
  "initialSnackList": [
    { "nombre": "Combo Crispetas", "descripcion": "Crispetas con soda mediana", "precio": 22000.00, "cantidad": 100, "points": 15 },
    { "nombre": "Hot Dog", "descripcion": "Hot dog con papas", "precio": 18000.00, "cantidad": 60, "points": 10 }
  ]
}
```

### Response ejemplo

```json
{
  "multiplexId": "00000000-0000-0000-0000-000000000021",
  "name": "Plaza Central",
  "city": "Bogotá",
  "address": "Av. Caracas #45-30",
  "roomsCreated": 8,
  "roomIds": [
    "00000000-0000-0000-0000-000000000031",
    "00000000-0000-0000-0000-000000000032",
    "00000000-0000-0000-0000-000000000033"
  ],
  "snacksCreated": 2,
  "status": "CREATED"
}
```

## 8.12. Ejemplo de cómo queda la BD local creada por el admin

Cuando el admin crea `Plaza Central` se espera una base de datos local preparada con:

- Un registro en `multiplex`.
- Salas generadas en `rooms`.
- Snacks iniciales en `snacks`.
- Espacio para funciones en `movie_screenings`.
- Espacio para asientos en `seats` y `seat_screenings`.

### Ejemplo de creación automática

```sql
INSERT INTO multiplex (id, name, city, address, general_seat_price, preferential_seat_price)
VALUES ('00000000-0000-0000-0000-000000000021', 'Plaza Central', 'Bogotá', 'Av. Caracas #45-30', 15000.00, 22000.00);

INSERT INTO rooms (id, multiplex_id, room_number, general_capacity, preferential_capacity, active)
VALUES
  ('00000000-0000-0000-0000-000000000031', '00000000-0000-0000-0000-000000000021', 'A', 120, 30, true),
  ('00000000-0000-0000-0000-000000000032', '00000000-0000-0000-0000-000000000021', 'B', 120, 30, true);

INSERT INTO snacks (id, nombre, descripcion, precio, cantidad, points, multiplex_id)
VALUES
  ('00000000-0000-0000-0000-000000000041', 'Combo Crispetas', 'Crispetas con soda mediana', 22000.00, 100, 15, '00000000-0000-0000-0000-000000000021'),
  ('00000000-0000-0000-0000-000000000042', 'Hot Dog', 'Hot dog con papas', 18000.00, 60, 10, '00000000-0000-0000-0000-000000000021');
```

### Nota

- La creación automática de `rooms` debe ser parte del flujo admin.
- La creación de asientos puede ser diferida hasta que el manager defina la configuración precisa de la sala.
- Si el proyecto requiere, se puede generar un layout por defecto para `seats` y `seat_screenings`.

## 8.13. Ejemplos de multiplexes adicionales listos para la misma lógica

El mismo flujo debe funcionar para otros multiplexes y puntos ágiles:

- `Gran Estación`, `Bogotá`, `Av. 26 #20-50`
- `Unicentro`, `Bogotá`, `Cl. 127 #12-34`
- `Embajador`, `Bogotá`, `Av. Suba #56-78`
- `Las Américas`, `Medellín`, `Cra. 43 #12-89`

Cada uno tendrá su propia base local o su propio esquema, pero se comporta igual:

1. El admin crea el multiplex.
2. Se inserta `multiplex`.
3. Se crean `rooms` automáticamente.
4. Se insertan snacks iniciales.
5. El manager completa programación y asiento por screening.

## 8.14. Qué conviene meter además de JPA y PostgreSQL

Este plan no se queda solo en JPA y Postgres. También requiere:

- Spring Boot con Spring Data JPA.
- PostgreSQL como base de datos relacional.
- Flyway o Liquibase para controlar versiones de esquema.
- Spring Security con JWT para roles `ADMIN`, `MANAGER`, `BUYER`.
- Jakarta Validation / Hibernate Validator para validar payloads.
- Mecanismos de sincronización local-central:
  - API de sincronización.
  - Outbox (`sync_log` / `event_log`).
  - Scheduler `@Scheduled` para sincronizaciones periódicas.
- Logging estructurado y monitoreo (Prometheus/Grafana, ELK, Sentry).
- Docker / Docker Compose para desarrollo y despliegue.
- Backup/restore de base de datos y pruebas de recovery.
- Redis o cache local para datos maestros si la carga aumenta.
- Kafka o RabbitMQ si se decide escalar la sincronización a eventos.

## 9. Recomendación para el proyecto actual
- El backend ya usa PostgreSQL y JPA, por lo que la base del plan es compatible con el proyecto.
- Conviene comenzar con una sola base de datos local y una base central de prueba.
- Luego, extender el diseño a múltiples instancias PostgreSQL para cada punto ágil.

## 10. Sugerencia de datos fragmentados
Los siguientes datos son buenos candidatos para fragmentarse/localizarse:
- `seat_screening` y estado de asientos por función.
- `ticket_sale` y `snack_sale` del punto.
- `billing` local y su escaneo.
- Inventario local de snacks.

Y estos datos se comparten o sincronizan:
- `multiplex`, `room`, `movie`, `snack`, `employee`.
- `buyer` y `points_transaction` (con central como fuente maestra de la acumulación total).

## 11. Plan urgente para entregar en menos de 24h

### 11.1. Objetivos clave para la primera entrega
- Tener un endpoint `POST /api/admin/multiplexes` que cree un multiplex y genere salas.
- Que el backend local almacene `multiplex`, `rooms` y `snacks` iniciales.
- Que el modelo de datos funcione con PostgreSQL y Flyway.
- Que haya validación básica de payload y control de roles `ADMIN`.
- Que exista un flujo de pruebas unitarias/integración para creación de multiplex.

### 11.2. Tareas inmediatas y ordenadas
1. `Configurar Flyway` en `pom.xml` y crear `V1__create_schema.sql`.
2. `Modelar entidades JPA` mínimo: `MultiplexEntity`, `RoomEntity`, `SnackEntity`, `BuyerEntity`, `PaymentEntity`, `BillingEntity`, `SeatScreeningEntity`.
3. `Crear DTO` `MultiplexRequest` con validaciones `@NotBlank`, `@NotNull`, `@Positive`.
4. `Implementar servicio admin` para guardar multiplex y generar `n` salas.
5. `Crear controller admin` con endpoint `POST /api/admin/multiplexes`.
6. `Agregar seguridad básica` JWT + roles, de modo que solo `ADMIN` pueda ejecutar este endpoint.
7. `Crear tests` que verifiquen:
   - la inserción del multiplex,
   - la generación de rooms,
   - los snacks iniciales.

### 11.3. Estructura de trabajo sugerida para el equipo
- Dev 1: esquema de BD y Flyway.
- Dev 2: entidades JPA + DTOs + validación.
- Dev 3: endpoint admin + servicio de creación.
- Dev 4: tests y verificación contra PostgreSQL local.

### 11.4. Prioridad mínima viable
- Backend desde el request admin debe crear el multiplex y sus salas.
- No es necesario tener sincronización central completa en la primera versión; basta con dejar el esquema listo y un stub de sincronización.
- El enfoque debe ser local-first: la base local debe quedar funcional por sí sola.

### 11.5. Scripts de ejemplo para entrega rápida

#### `V1__create_schema.sql`
```sql
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE multiplex (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name VARCHAR(100) NOT NULL,
  city VARCHAR(100) NOT NULL,
  address VARCHAR(200) NOT NULL,
  general_seat_price NUMERIC(10,2) NOT NULL,
  preferential_seat_price NUMERIC(10,2) NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE rooms (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  multiplex_id UUID NOT NULL REFERENCES multiplex(id),
  room_number VARCHAR(10) NOT NULL,
  general_capacity INT NOT NULL,
  preferential_capacity INT NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE TABLE snacks (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre VARCHAR(100) NOT NULL,
  descripcion VARCHAR(500),
  precio NUMERIC(10,2) NOT NULL,
  cantidad INT NOT NULL,
  points INT,
  multiplex_id UUID NOT NULL REFERENCES multiplex(id)
);
```

#### `V2__seed_initial_data.sql`
```sql
INSERT INTO multiplex (name, city, address, general_seat_price, preferential_seat_price)
VALUES ('Plaza Central', 'Bogotá', 'Av. Caracas #45-30', 15000.00, 22000.00);
```

### 11.6. Endpoint y payload mínimo viable
- Endpoint: `POST /api/admin/multiplexes`
- Payload mínimo:
  - `name`
  - `city`
  - `address`
  - `generalSeatPrice`
  - `preferentialSeatPrice`
  - `numberOfRooms`

### 11.7. Cómo ayudar a la IA a acelerar
- Proveer códigos de ejemplos de entidades y DTOs.
- Generar automáticamente los tests unitarios con la estructura de request/response.
- Usar la IA para completar los repositories y el servicio de creación.

### 11.8. Resultado esperado para 24h
- Un backend con las tablas esenciales y el endpoint admin funcionando.
- Un `DB_Implementation_Plan.md` con la guía y ejemplo final de creación.
- Una base local con al menos el multiplex `Plaza Central` creado y sus salas insertadas.

---

Este plan deja claro cómo estructurar la base de datos del proyecto Cine Pacho, cómo usar PostgreSQL en locales y central, y cómo sincronizar la información que afecta ventas, puntos y reportes.