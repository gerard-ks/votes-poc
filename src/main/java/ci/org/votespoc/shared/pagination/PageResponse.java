package ci.org.votespoc.shared.pagination;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Response paginée générique.
 *
 * Contient les données paginées + métadonnées de pagination.
 *
 * Exemples :
 * <pre>
 * // Créer une PageResponse
 * List<Todo> todos = todoRepository.findAll(pageRequest);
 * long total = todoRepository.count();
 * PageResponse<Todo> page = PageResponse.of(todos, pageRequest, total);
 *
 * // Mapper vers un DTO
 * PageResponse<TodoDTO> dtoPage = page.map(TodoMapper::toDTO);
 *
 * // Page vide
 * PageResponse<Todo> empty = PageResponse.empty(pageRequest);
 * </pre>
 *
 * @param <T> Type des éléments
 */
public record PageResponse<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last,
        boolean empty
) {

    /**
     * Compact constructor avec validation.
     */
    public PageResponse {
        if (content == null) {
            content = Collections.emptyList();
        }
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number must be >= 0");
        }
        if (pageSize < 1) {
            throw new IllegalArgumentException("Page size must be >= 1");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("Total elements must be >= 0");
        }
    }

    // ===== FACTORY METHODS =====

    /**
     * Créer une PageResponse à partir d'une liste et du total.
     */
    public static <T> PageResponse<T> of(
            List<T> content,
            PageRequest pageRequest,
            long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / pageRequest.size());
        int pageNumber = pageRequest.page();

        return new PageResponse<>(
                content != null ? content : Collections.emptyList(),
                pageNumber,
                pageRequest.size(),
                totalElements,
                Math.max(1, totalPages), // Au moins 1 page même si vide
                pageNumber == 0,
                pageNumber >= totalPages - 1,
                content == null || content.isEmpty()
        );
    }

    /**
     * Créer une PageResponse vide.
     */
    public static <T> PageResponse<T> empty(PageRequest pageRequest) {
        return new PageResponse<>(
                Collections.emptyList(),
                pageRequest.page(),
                pageRequest.size(),
                0,
                1,
                true,
                true,
                true
        );
    }

    /**
     * Créer une PageResponse avec un seul élément.
     */
    public static <T> PageResponse<T> single(T element, PageRequest pageRequest) {
        return of(List.of(element), pageRequest, 1);
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Vérifie s'il y a une page suivante.
     */
    public boolean hasNext() {
        return pageNumber < totalPages - 1;
    }

    /**
     * Vérifie s'il y a une page précédente.
     */
    public boolean hasPrevious() {
        return pageNumber > 0;
    }

    /**
     * Retourne le nombre d'éléments dans cette page.
     */
    public int numberOfElements() {
        return content.size();
    }

    /**
     * Transforme les éléments de la page avec une fonction de mapping.
     *
     * Utile pour convertir des entités domain en DTOs.
     *
     * Exemple :
     * <pre>
     * PageResponse<User> userPage = ...;
     * PageResponse<UserDTO> dtoPage = userPage.map(UserMapper::toDTO);
     * </pre>
     */
    public <U> PageResponse<U> map(Function<T, U> mapper) {
        List<U> mappedContent = content.stream()
                .map(mapper)
                .toList();

        return new PageResponse<>(
                mappedContent,
                pageNumber,
                pageSize,
                totalElements,
                totalPages,
                first,
                last,
                empty
        );
    }

    /**
     * Retourne le numéro de la page suivante (1-based pour l'affichage).
     */
    public int nextPageNumber() {
        return hasNext() ? pageNumber + 2 : pageNumber + 1;
    }

    /**
     * Retourne le numéro de la page précédente (1-based pour l'affichage).
     */
    public int previousPageNumber() {
        return hasPrevious() ? pageNumber : 1;
    }

    /**
     * Retourne le numéro de page courant (1-based pour l'affichage).
     */
    public int displayPageNumber() {
        return pageNumber + 1;
    }
}