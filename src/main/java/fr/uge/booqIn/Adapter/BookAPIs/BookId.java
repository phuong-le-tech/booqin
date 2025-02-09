package fr.uge.booqIn.Adapter.BookAPIs;


import java.util.Objects;
import java.io.Serializable;


public class BookId implements Serializable {
    private String ibsn10;
    private String ibsn13;

    public BookId() {
    }

    public BookId(String ibsn10, String ibsn13) {
        this.ibsn10 = ibsn10;
        this.ibsn13 = ibsn13;
    }

    public String getIbsn10() {
        return ibsn10;
    }

    public String getIbsn13() {
        return ibsn13;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BookId)) return false;
        BookId bookId = (BookId) o;
        return Objects.equals(ibsn10, bookId.ibsn10) &&
                Objects.equals(ibsn13, bookId.ibsn13);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ibsn10, ibsn13);
    }
}
