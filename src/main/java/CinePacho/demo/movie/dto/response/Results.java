package CinePacho.demo.movie.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)

public record Results(
        String key,
        String type,
        String iso_3166_1
) {
}
