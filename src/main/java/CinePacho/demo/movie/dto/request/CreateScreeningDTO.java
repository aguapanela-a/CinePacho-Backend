package CinePacho.demo.movie.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CreateScreeningDTO(
        @NotNull(message = "El id de la película es obligatorio para crear la función")
        Long movieId,           // id de la peli ya seleccionada

        @NotNull(message = "El UUID de la sala es obligatorio para crear la función")
        UUID roomId,            // sala donde se proyecta

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @NotNull(message = "La hora de la función es obligatoria para crearla")
        LocalDateTime dateTime

) {}
