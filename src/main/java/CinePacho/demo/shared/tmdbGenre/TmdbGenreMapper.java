package CinePacho.demo.shared.tmdbGenre;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TmdbGenreMapper {

    //Atributo estático que mapea por id los género disponibles de la API externa
    private static final Map<Integer, String> GENRES = Map.ofEntries(
            Map.entry(28, "Acción"),
            Map.entry(12, "Aventura"),
            Map.entry(16, "Animación"),
            Map.entry(35, "Comedia"),
            Map.entry(80, "Crimen"),
            Map.entry(99, "Documental"),
            Map.entry(18, "Drama"),
            Map.entry(10751, "Familia"),
            Map.entry(14, "Fantasía"),
            Map.entry(36, "Historia"),
            Map.entry(27, "Terror"),
            Map.entry(10402, "Música"),
            Map.entry(9648, "Misterio"),
            Map.entry(10749, "Romance"),
            Map.entry(878, "Ciencia ficción"),
            Map.entry(10770, "Película de TV"),
            Map.entry(53, "Suspense"),
            Map.entry(10752, "Bélica"),
            Map.entry(37, "Western")
    );

    public String getGenreName(Integer genreId) {
        return GENRES.getOrDefault(genreId, "Desconocido");
    }
}
