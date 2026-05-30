package CinePacho.demo.snacks.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record SnackByMultiplex(
        String multiplexName,
        List<SnackResponse> snacks
) {
}
