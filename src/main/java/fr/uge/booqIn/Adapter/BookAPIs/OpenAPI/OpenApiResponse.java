package fr.uge.booqIn.Adapter.BookAPIs.OpenAPI;

import fr.uge.booqIn.Adapter.BookAPIs.GoogleAPI.BookItem;

import java.util.List;

public record OpenApiResponse(List<BookItem> items) {
}
