package CinePacho.demo.movie.dto.response;

import CinePacho.demo.movie.dto.request.TmdbMovieDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record MovieSelectorDTO(
        TmdbMovieDTO movieInfo,
        Double rating,
        String key,
        List<ScreeningInfoDTO> screenings
) { }
