package CinePacho.demo.payment.controller;

import CinePacho.demo.payment.dto.request.CheckoutRequest;
import CinePacho.demo.payment.dto.response.CheckoutSummaryResponse;
import CinePacho.demo.payment.service.CheckoutService;
import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;

@RestController
@RequestMapping("/api/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;

    @GetMapping("/pago")
    public String pago() throws MPException, MPApiException {
        
        
        MercadoPagoConfig.setAccessToken(checkoutService.accessTokenPayment);

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
            .success("https://www.tu-sitio/success")
            .pending("https://www.tu-sitio/pending")
            .failure("https://www.tu-sitio/failure")
            .build();



        PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                .id("1234")
                .title("Games")
                .description("PS5")
                .pictureUrl("http://picture.com/PS5")
                .categoryId("games")
                .quantity(2)
                .currencyId("BRL")
                .unitPrice(new BigDecimal("4000"))
                .build();
            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
        .items(items).backUrls(backUrls).build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);




        return preference.getSandboxInitPoint();
    }    








    @PostMapping("/preview")
    public ResponseEntity<CheckoutSummaryResponse> preview(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader("Authorization") String token
    ) {
        // Calcula totales para vista previa
        token = token.replace("Bearer ", "");
        return ResponseEntity.ok(checkoutService.preview(request, token));
    }

    








    @PostMapping("/confirm")
    public ResponseEntity<CheckoutSummaryResponse> confirm(
            @Valid @RequestBody CheckoutRequest request,
            @RequestHeader("Authorization") String token
    ) {
        // Revalida disponibilidad antes de continuar al pago
        token = token.replace("Bearer ", "");
        return ResponseEntity.ok(checkoutService.confirm(request, token));
    }
}
