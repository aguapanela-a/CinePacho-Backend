package CinePacho.demo.movie.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TmdbMovieDTO(
        Long id,

        @JsonProperty("backdrop_path")
        String backdropPath,

        @JsonProperty("genre_ids")
        List<Integer> genreIds, // ← lista de los ids de los géneros

        @JsonProperty("original_language")
        String originalLanguage,

        @JsonProperty("title")
        String originalTitle,

        String overview,

        @JsonProperty("poster_path")
        String posterPath,

        @JsonProperty("release_date")
        String releaseDate
) {
}
