package pressing.app.dto;

import java.util.List;

/**
 * DTO generique pour la pagination.
 * Utilise pour retourner une page de resultats avec les metadonnees.
 */
public class PageResponse<T> {

    private List<T> contenu;
    private int page;
    private int taille;
    private long totalElements;
    private int totalPages;

    public PageResponse() {}

    public PageResponse(List<T> contenu, int page, int taille, long totalElements) {
        this.contenu = contenu;
        this.page = page;
        this.taille = taille;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / taille);
    }

    // --- Getters et Setters ---

    public List<T> getContenu() { return contenu; }
    public void setContenu(List<T> contenu) { this.contenu = contenu; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getTaille() { return taille; }
    public void setTaille(int taille) { this.taille = taille; }

    public long getTotalElements() { return totalElements; }
    public void setTotalElements(long totalElements) { this.totalElements = totalElements; }

    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
