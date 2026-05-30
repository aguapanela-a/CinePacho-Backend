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
- Endpoint: POST /api/checkout/stripe (o el que ya exista que invoque StripeService.checkoutProducts)
- Body: CheckoutRequest { screeningId, seats: [{seatId}], snacks: [...] }
- Headers: Authorization: Bearer <token>
- Respuesta: CheckoutSummaryResponse con sessionUrl de Stripe y status PENDING.
- Nota para frontend: NO asumir que la compra está finalizada al recibir esta respuesta. El pago se confirma cuando Stripe notifica (webhook) o cuando el usuario vuelve a la ruta success del frontend.

3) Webhook / confirmación de pago
- Endpoint sugerido: POST /api/checkout/stripe/webhook
- Seguridad: Verificar firma de Stripe (recomendado) y validar fuente.
- Acción: Cuando Stripe confirma pago, backend debe invocar StripeService.handlePaymentSuccess(checkoutRequest, paymentId, userEmail)
  - handlePaymentSuccess valida que las seatScreenings estén BLOCKED por ese usuario
  - actualiza el Payment a COMPLETED
  - marca cada SeatScreening como SOLD (solo para la función indicada)
  - registra la venta en TicketSaleRepository
  - registra la película en el historial del comprador (buyerManager)
  - programa la liberación automática (scheduleRelease) para esa función (3 horas después del inicio)
- Respuesta: 200 OK a Stripe

4) Flow recomendado para frontend (resumen práctico):
- Paso 1: Cliente obtiene lista de asientos para una función y muestra estado (si se requiere estado por función, pedir al backend el endpoint que acepte screeningId para devolver estado por función).
- Paso 2: Usuario selecciona asientos -> por cada asiento llamar PUT /api/seats/{seatId}/changeStatus?screeningId={screeningId}
- Paso 3: Una vez seleccionadas todas las sillas, llamar POST /api/checkout/stripe con el body y recibir sessionUrl
- Paso 4: Redirigir al usuario a sessionUrl (Stripe)
- Paso 5: Stripe redirige a success/cancel o envía webhook. El backend procesará la confirmación en handlePaymentSuccess; el frontend puede preguntar al backend por el estado del pago (polling) o recibir webhooks del backend.

Notas técnicas:
- Se introdujo la entidad SeatScreening para administrar estado por par (silla, función). El SeatEntity sigue existiendo para información estática de la silla (número, sala, tipo).
- Los bloqueos temporales se mantienen por 10 minutos por SeatScreening y se liberan automáticamente si expiran.
- No modificar el estado global de SeatEntity para manejar ventas/bloqueos por función.

Pruebas recomendadas:
- Selección simultánea: dos usuarios intentan bloquear la misma silla para la misma función (uno debe fallar).
- Misma silla en funciones distintas: bloquear en A no impide bloquear/comprar en B.
- Pago exitoso: completar flujo Stripe y verificar TicketSale creado y SeatScreening en SOLD.
- Expiración de bloqueo: esperar 10 minutos o forzar timer y verificar liberación.

Fin de documento.
