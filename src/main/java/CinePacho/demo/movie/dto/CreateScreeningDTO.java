package CinePacho.demo.movie.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateScreeningDTO(
        @NotBlank(message = "El id de la película es obligatorio para crear la función")
        Long movieId,           // id de la peli ya seleccionada

        @NotBlank(message = "El UUID de la sala es obligatorio para crear la función")
        UUID salaId,            // sala donde se proyecta

        @NotBlank(message = "La hora de la función es obligatoria para crearla")
        LocalDateTime fechaHora,

        @NotBlank(message = "El precio de la función es obligatorio para crearla")
        BigDecimal precio
) {}
