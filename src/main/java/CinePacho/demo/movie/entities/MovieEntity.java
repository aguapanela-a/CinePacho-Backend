package CinePacho.demo.movie.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "movies")
public class MovieEntity {

    @Id
    private Long id;

    @Column(name = "backdrop_path", columnDefinition = "TEXT")
    private String backdropPath;
    
    @Column(name = "original_language")
    private String originalLanguage;
    
    @Column(name = "original_title")
    private String originalTitle;
    
    @Column(name = "overview", columnDefinition = "TEXT")
    private String overview;
    
    @Column(name = "rating")
    private Double rating;
    
    @Column(name = "poster_path", columnDefinition = "TEXT")
    private String posterPath;
    
    @Column(name = "release_date")
    private String releaseDate;
    
    @Column(name = "director")
    private String director;

    //crea una tabla para los géneros (id y nombre) de las películas
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"movie_id", "genre_id"}))
    private List<GenreEmbeddable> genres = new ArrayList<>();
}