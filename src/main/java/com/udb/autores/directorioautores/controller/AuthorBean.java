package com.udb.autores.directorioautores.controller;

import com.udb.autores.directorioautores.model.AuthorModel;
import com.udb.autores.directorioautores.model.LiteraryGenreModel;
import com.udb.autores.directorioautores.model.pojos.Author;
import com.udb.autores.directorioautores.model.pojos.LiteraryGenre;

import javax.annotation.PostConstruct; // Importante para inicializar
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;


@ManagedBean // Registra el Bean en JSF
@ViewScoped   // El bean vive mientras estemos en la misma página
public class AuthorBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- Modelos ---
    // Clases que se conectan a la BD
    private AuthorModel authorModel;
    private LiteraryGenreModel genreModel;

    // --- Propiedades para la VISTA ---

    // 1. Para el Formulario
    private Author author; // Objeto para vincular el formulario (agregar/editar)
    private int selectedGenreId; // ID del género seleccionado en el dropdown del formulario

    // 2. Para la Tabla
    private List<Author> authorList; // Lista de autores para el <h:dataTable>

    // 3. Para los Filtros y Opciones
    private List<LiteraryGenre> genreList; // Lista de géneros para los <h:selectOneMenu>
    private int filterGenreId; // ID del género seleccionado en el dropdown de FILTRO
    private int authorCount; // Para mostrar el resultado del botón CONTAR


    public AuthorBean() {
    }


    @PostConstruct
    public void init() {
        // Inicializamos los objetos
        authorModel = new AuthorModel();
        genreModel = new LiteraryGenreModel();
        author = new Author(); // Objeto vacío para el formulario de "Agregar"

        // Cargamos los datos iniciales desde la BD
        loadGenres();  // Carga la lista de géneros para los dropdowns
        loadAuthors(); // Carga la lista de autores para la tabla
    }

    // --- MÉTODOS DE ACCIÓN (CRUD) ---


    public void saveOrUpdateAuthor() {
        // 1. Validación de autor duplicado [cite: 67]
        Author existing = authorModel.findAuthorByName(author.getName());
        // Si existe Y estamos agregando (id=0), mostramos advertencia [cite: 68]
        if (existing != null && author.getId() == 0) {
            addMessage(FacesMessage.SEVERITY_WARN, "Advertencia",
                    "Un autor con este nombre ya existe, pero se agregará.");
        }

        // 2. Asignar el objeto Género completo
        LiteraryGenre selectedGenre = genreModel.findGenreById(selectedGenreId);
        author.setLiteraryGenre(selectedGenre);

        try {
            // 3. Decidir si Guardar (Nuevo) o Actualizar (Editar)
            if (author.getId() == 0) {
                // Es un autor nuevo
                authorModel.saveAuthor(author);
                addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Autor agregado correctamente.");
            } else {
                // Es una actualización
                authorModel.updateAuthor(author);
                addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Autor actualizado correctamente.");
            }

            // 4. Recargar la lista y limpiar el formulario
            loadAuthors(); // Actualiza la tabla
            resetForm();   // Limpia los campos del formulario

        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo guardar el autor: " + e.getMessage());
        }
    }


    public void deleteAuthor(Author authorToDelete) {
        try {
            authorModel.deleteAuthor(authorToDelete);
            loadAuthors(); // Recarga la lista
            addMessage(FacesMessage.SEVERITY_INFO, "Éxito", "Autor eliminado.");
        } catch (Exception e) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "No se pudo eliminar el autor.");
        }
    }


    public void prepareEdit(Author authorToEdit) {
        // 1. Pone el autor en el formulario
        this.author = authorToEdit;

        // 2. Selecciona el género correcto en el dropdown del formulario
        if (authorToEdit.getLiteraryGenre() != null) {
            this.selectedGenreId = authorToEdit.getLiteraryGenre().getId();
        } else {
            this.selectedGenreId = 0;
        }
    }


    public void resetForm() {
        this.author = new Author();
        this.selectedGenreId = 0; // O el ID de "Seleccionar"
    }

    // --- MÉTODOS AJAX ---


    public void filterAuthorsByGenre() {
        if (filterGenreId == 0) {
            // Si selecciona "Todos", cargar todos
            loadAuthors();
        } else {
            // Si selecciona un género, filtrar
            this.authorList = authorModel.findAuthorsByGenre(filterGenreId);
        }
        // Actualiza el contador AJAX cada vez que filtramos [cite: 73]
        countAuthorsInTable();
    }


    public void countAuthorsInTable() {
        if (this.authorList != null) {
            this.authorCount = this.authorList.size();
        } else {
            this.authorCount = 0;
        }
    }

    // --- MÉTODOS PRIVADOS (Helpers) ---


    private void loadAuthors() {
        this.authorList = authorModel.getAllAuthors();
        // Actualiza el contador cada vez que se carga la lista
        countAuthorsInTable();
    }


    private void loadGenres() {
        this.genreList = genreModel.getAllGenres();
    }


    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(severity, summary, detail));
    }


    // --- GETTERS Y SETTERS ---
    // (JSF los necesita OBLIGATORIAMENTE para acceder a las propiedades)

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public int getSelectedGenreId() {
        return selectedGenreId;
    }

    public void setSelectedGenreId(int selectedGenreId) {
        this.selectedGenreId = selectedGenreId;
    }

    public List<Author> getAuthorList() {
        return authorList;
    }

    public void setAuthorList(List<Author> authorList) {
        this.authorList = authorList;
    }

    public List<LiteraryGenre> getGenreList() {
        return genreList;
    }

    public void setGenreList(List<LiteraryGenre> genreList) {
        this.genreList = genreList;
    }

    public int getFilterGenreId() {
        return filterGenreId;
    }

    public void setFilterGenreId(int filterGenreId) {
        this.filterGenreId = filterGenreId;
    }

    public int getAuthorCount() {
        return authorCount;
    }

    public void setAuthorCount(int authorCount) {
        this.authorCount = authorCount;
    }
}