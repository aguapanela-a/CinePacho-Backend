package CinePacho.demo.movie.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

//DTO wrapper que tiene lña lista de TmdbMovieDTO para extraerlas
@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbResponseDTO(
        Integer page,

        @JsonProperty("results")
        List<TmdbMovieDTO> results,

        @JsonProperty("total_pages")
        Integer totalPages,

        @JsonProperty("total_results")
        Integer totalResults
) {}