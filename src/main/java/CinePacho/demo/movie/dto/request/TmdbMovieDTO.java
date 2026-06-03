package CinePacho.demo.movie.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record TmdbMovieDTO(
        Long id,

        @JsonProperty("backdrop_path")
        String backdropPath,

        @JsonProperty("genres")
        List<GenreDto> genreIds,

        @JsonProperty("original_language")
        String originalLanguage,

        @JsonProperty("title")
        String originalTitle,

        String overview,

        @JsonProperty("poster_path")
        String posterPath,

        @JsonProperty("release_date")
        String releaseDate,

        // Campos para el frontend
        String director,
        String cast

) {
}