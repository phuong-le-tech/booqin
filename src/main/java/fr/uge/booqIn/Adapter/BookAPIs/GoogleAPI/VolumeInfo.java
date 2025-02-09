package fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI;

import java.util.List;

public class VolumeInfo {
    public String title;
    public List<String> authors;
    public String publishedDate;
    public String description;

    @Override
    public String toString() {
        return "Titre: " + title + ", Auteurs: " + authors + ", Date de publication: " + publishedDate;
    }
}
