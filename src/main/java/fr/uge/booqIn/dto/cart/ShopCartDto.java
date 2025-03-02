package fr.uge.booqIn.dto.cart;

import fr.uge.booqIn.dto.book.BookDto;
import fr.uge.booqIn.dto.book.BookIndex;

import java.util.List;

public record ShopCartDto(Long id, Long userId, List<BookIndex> books) {}
