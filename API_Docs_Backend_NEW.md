API Docs - Backend (versión actualizada)

Resumen: documentación de endpoints nuevos/actualizados para trabajar con reservas por función (SeatScreening).

1) Seleccionar / cambiar estado de una silla (por función)
- Endpoint: PUT /api/seats/{seatId}/changeStatus?screeningId={screeningId}
- Headers: Authorization: Bearer <token>
- Descripción: Toggle del estado de la silla para una función específica. Si la silla está AVAILABLE para esa función, se crea o actualiza un SeatScreening y pasa a BLOCKED por 10 minutos a nombre del usuario. Si ya estaba BLOCKED por el mismo usuario, se libera. Si está BLOCKED por otro usuario o SOLD para esa función, se devuelve 409.
- Uso frontend (pasos):
  1. Usuario hace click en asiento en la UI y frontend llama PUT con seatId y screeningId.
  2. Si respuesta es 200 OK con status BLOCKED, mostrar como reservado y comenzar contador local de 10 minutos.
  3. Si respuesta es 200 OK con status AVAILABLE, actualizar UI para permitir que otro usuario lo seleccione.
  4. Manejar errores 409/400 mostrando mensajes claros.

2) Crear checkout y redirigir a Stripe
- Endpoint: POST /api/checkout/stripe (invoca StripeService.checkoutProducts)
- Body: CheckoutRequest {
    screeningId: UUID,
    seats: [{ seatId: UUID }],
    snacks: [{ snackId, quantity }],
    buyerEmail?: string  // opcional: obligatorio si el actor es un empleado/manager
  }
- Headers: Authorization: Bearer <token>
- Respuesta: CheckoutSummaryResponse con sessionUrl de Stripe, paymentId y status PENDING.
- Nota para frontend: NO asumir que la compra está finalizada al recibir esta respuesta. El pago se confirma cuando Stripe notifica (webhook) o cuando el usuario vuelve a la ruta /api/checkout/stripe/success del backend (el frontend debe redirigir al usuario a Stripe y luego puede invocar success o esperar webhook).
- Notas especiales para empleados:
  - Si el actor es un empleado/manager, incluir buyerEmail en el body. El backend validará que el empleado pertenece al multiplex de la función antes de crear la factura y el pago.
  - El pago (PaymentEntity.userId) será asociado AL BUYER indicado por buyerEmail.

3) Webhook / confirmación de pago
- Endpoint sugerido: POST /api/checkout/stripe/webhook
- Seguridad: Verificar firma de Stripe (recomendado) y validar fuente.
- Acción: Cuando Stripe confirma pago, backend debe invocar StripeService.handlePaymentSuccess(checkoutRequest, paymentId, token)
  - El token es el Authorization Bearer token del actor que inició el proceso (buyer o employee). En el caso de webhook sin token, el backend debe recuperar el mapping paymentId -> checkoutRequest/buyer desde la BD (se recomienda guardar el CheckoutRequest o su referencia cuando se creó el pago).
  - handlePaymentSuccess hace:
    - Determina actor (buyer o employee) a partir del token:
      - Si actor es BUYER: se valida que las seatScreenings estén BLOCKED por ese buyer (email del token).
      - Si actor es EMPLOYEE/MANAGER: el checkoutRequest debe incluir buyerEmail; se valida que el empleado pertenece al mismo multiplex que la función y que las seatScreenings están BLOCKED por el buyerEmail.
    - Actualiza el Payment a COMPLETED
    - Marca cada SeatScreening como SOLD (solo para la función indicada)
    - Registra la venta en TicketSaleRepository
    - Registra la película en el historial del comprador (buyerManager)
    - Programa la liberación automática (scheduleRelease) para esa función (3 horas después del inicio)
- Respuesta: 200 OK a Stripe

4) Flow recomendado para frontend (resumen práctico):
- Paso 1: Cliente obtiene lista de asientos para una función y muestra estado (si se requiere estado por función, pedir al backend el endpoint que acepte screeningId para devolver estado por función).
- Paso 2: Usuario selecciona asientos -> por cada asiento llamar PUT /api/seats/{seatId}/changeStatus?screeningId={screeningId}
  - IMPORTANTE: si la operación la ejecuta un EMPLEADO en el panel del multiplex, el frontend debe usar el token del empleado para autenticarse y opcionalmente incluir buyerEmail en el CheckoutRequest (si el empleado está creando la venta para un buyer).
- Paso 3: Una vez seleccionadas todas las sillas, llamar POST /api/checkout/stripe con el body y recibir sessionUrl
  - Si actor es EMPLEADO: incluir buyerEmail en el body; el backend validará que el empleado pertenece al multiplex de la función antes de crear la factura y el pago.
- Paso 4: Redirigir al usuario a sessionUrl (Stripe)
- Paso 5: Stripe redirige a success/cancel o envía webhook. El backend procesará la confirmación en handlePaymentSuccess; el frontend debe enviar una llamada POST /api/checkout/stripe/success con el CheckoutRequest y paymentId y el token (Authorization: Bearer <token>) para indicar la finalización. Alternativamente, el backend puede consumir el webhook y notificar al frontend.

Notas técnicas:
- Se introdujo la entidad SeatScreening para administrar estado por par (silla, función). El SeatEntity sigue existiendo para información estática de la silla (número, sala, tipo).
- Los bloqueos temporales se mantienen por 10 minutos por SeatScreening y se liberan automáticamente si expiran.
- No modificar el estado global de SeatEntity para manejar ventas/bloqueos por función.

Pruebas recomendadas:
- Selección simultánea: dos usuarios intentan bloquear la misma silla para la misma función (uno debe fallar).
- Misma silla en funciones distintas: bloquear en A no impide bloquear/comprar en B.
- Pago exitoso: completar flujo Stripe y verificar TicketSale creado y SeatScreening en SOLD.
- Expiración de bloqueo: esperar 10 minutos o forzar timer y verificar liberación.

5) Escaneo de QR por empleado (entrada física)
- Endpoint: PUT /api/checkout/employee/billing/{billingId}/scan
- Headers: Authorization: Bearer <token> (token del empleado)
- Descripción: El QR generado en la factura apunta a este endpoint. Solo el personal del multiplex (EMPLOYEE o MANAGER) asignado puede escanear la factura.
- Validaciones realizadas en backend:
  - El JWT del empleado es decodificado para obtener su email.
  - Se verifica que el usuario sea EMPLOYEE o MANAGER.
  - Se verifica que el multiplex del empleado coincida con el multiplex guardado en la factura.
  - Se evita escaneo duplicado (cada factura puede escanearse una sola vez).
- Respuesta: { message: "Entrada válida. Bienvenido a CinePacho" }
- Uso frontend (panel empleado):
  1. El empleado abre la cámara en su panel y escanea el QR.
  2. El frontend hace PUT a /api/checkout/employee/billing/{billingId}/scan con Authorization Bearer del empleado.
  3. El backend valida y marca la factura como escaneada y devuelve mensaje de éxito.

Fin de documento.
