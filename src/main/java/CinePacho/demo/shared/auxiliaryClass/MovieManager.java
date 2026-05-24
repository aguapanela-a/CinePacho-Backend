package CinePacho.demo.shared.auxiliaryClass;

import org.springframework.stereotype.Component;

@Component
public interface MovieManager {
    boolean existsById(Long id);
}
