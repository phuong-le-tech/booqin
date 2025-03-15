package fr.uge.booqIn.mapper;

import fr.uge.booqIn.dto.lending.BookLendingShow;
import fr.uge.booqIn.entity.BookLending;
import org.springframework.stereotype.Component;

@Component
public class BookLendingMapper {
  
  private final BookMapper bookMapper;
  private final UserMapper userMapper;
  
  public BookLendingMapper(BookMapper bookMapper, UserMapper userMapper) {
    this.bookMapper = bookMapper;
    this.userMapper = userMapper;
  }
  
  /**
   * Convertit une entité BookLendingPage en DTO BookLendingShow
   */
  public BookLendingShow toBookLendingShow(BookLending lending) {
    if (lending == null) {
      return null;
    }
    return new BookLendingShow(
            lending.getId(),
            lending.getBook() != null ? bookMapper.toBookShow(lending.getBook()) : null,
            lending.getBorrower() != null ? userMapper.toUserIndex(lending.getBorrower()) : null,
            lending.getLender() != null ? userMapper.toUserIndex(lending.getLender()) : null,
            lending.getStatus(),
            lending.getReservedAt(),
            lending.getStartDate(),
            lending.getDueDate(),
            lending.getReturnDate()
    );
  }
}