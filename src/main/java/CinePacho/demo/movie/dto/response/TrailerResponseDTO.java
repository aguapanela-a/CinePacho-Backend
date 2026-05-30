package CinePacho.demo.movie.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TrailerResponseDTO(
        List<Results> results
) {
}
