package ci.org.votespoc.shared.pagination;

/**
 * Request de pagination.
 *
 * Représente une demande de page pour la pagination de résultats.
 *
 * Caractéristiques :
 * - Indexation 0-based (première page = 0)
 * - Taille plafonnée à MAX_SIZE (100)
 * - Validation automatique
 *
 * Exemples :
 * <pre>
 * // Page par défaut (page 0, 20 éléments)
 * PageRequest page = PageRequest.ofDefault();
 *
 * // Page spécifique
 * PageRequest page = PageRequest.of(2, 50);
 *
 * // Première page avec taille custom
 * PageRequest page = PageRequest.first(30);
 * </pre>
 *
 * @param page Numéro de page (0-indexed)
 * @param size Taille de la page (1-100)
 */
public record PageRequest(int page, int size) {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    /**
     * Compact constructor avec validation.
     */
    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("Page number must be >= 0, got: " + page);
        }
        if (size < 1) {
            throw new IllegalArgumentException("Page size must be >= 1, got: " + size);
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE; // Plafonner automatiquement
        }
    }

    // ===== FACTORY METHODS =====

    /**
     * Crée une PageRequest par défaut (page 0, size 20).
     */
    public static PageRequest ofDefault() {
        return new PageRequest(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    /**
     * Crée une PageRequest avec page et size spécifiques.
     */
    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    /**
     * Crée une PageRequest pour la première page avec une taille custom.
     */
    public static PageRequest first(int size) {
        return new PageRequest(0, size);
    }

    /**
     * Crée une PageRequest pour la première page avec la taille par défaut.
     */
    public static PageRequest first() {
        return new PageRequest(0, DEFAULT_SIZE);
    }

    // ===== MÉTHODES UTILITAIRES =====

    /**
     * Calcule l'offset SQL (OFFSET = page * size).
     *
     * Exemple :
     * - Page 0, size 20 → offset 0
     * - Page 1, size 20 → offset 20
     * - Page 2, size 20 → offset 40
     */
    public int offset() {
        return page * size;
    }

    /**
     * Retourne la page suivante.
     */
    public PageRequest next() {
        return new PageRequest(page + 1, size);
    }

    /**
     * Retourne la page précédente (ou première page si déjà sur page 0).
     */
    public PageRequest previous() {
        return new PageRequest(Math.max(0, page - 1), size);
    }

    /**
     * Retourne une nouvelle PageRequest avec une taille différente.
     */
    public PageRequest withSize(int newSize) {
        return new PageRequest(page, newSize);
    }

    /**
     * Vérifie si c'est la première page.
     */
    public boolean isFirst() {
        return page == 0;
    }

    /**
     * Retourne la taille maximale autorisée.
     */
    public static int maxSize() {
        return MAX_SIZE;
    }

    /**
     * Retourne la taille par défaut.
     */
    public static int defaultSize() {
        return DEFAULT_SIZE;
    }
}

