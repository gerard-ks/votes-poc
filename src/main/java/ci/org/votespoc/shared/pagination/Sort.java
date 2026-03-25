package ci.org.votespoc.shared.pagination;

/**
 * Représente un tri simple (1 seul champ).
 *
 * Pour TodoApp, on n'a besoin QUE de trier par 1 champ à la fois.
 * Si besoin de tri multiple plus tard, on étendra.
 *
 * Exemples :
 * <pre>
 * Sort sort = Sort.by("createdAt", Sort.Direction.DESC);
 * Sort sort = Sort.desc("createdAt");
 * Sort sort = Sort.asc("title");
 * </pre>
 */
public record Sort(String property, Direction direction) {

    public enum Direction {
        ASC, DESC
    }

    /**
     * Tri ascendant.
     */
    public static Sort asc(String property) {
        return new Sort(property, Direction.ASC);
    }

    /**
     * Tri descendant.
     */
    public static Sort desc(String property) {
        return new Sort(property, Direction.DESC);
    }

    /**
     * Tri par défaut pour les Todos (createdAt DESC).
     */
    public static Sort defaultTodoSort() {
        return desc("createdAt");
    }

    /**
     * Retourne la clause SQL ORDER BY.
     */
    public String toSql() {
        return property + " " + direction.name();
    }
}
