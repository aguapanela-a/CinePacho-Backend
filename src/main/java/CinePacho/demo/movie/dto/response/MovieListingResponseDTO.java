package CinePacho.demo.movie.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
public class MovieListingResponseDTO {
    
    private Long idMovie;
    private String originalTitle;
    private List<String> genres;
    private Double rating;
    private Integer year;
    private String posterPath;
    private String backdropPath;

}

