package fr.uge.booqIn.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import fr.uge.booqIn.entity.Book;
import fr.uge.booqIn.entity.BookLending;
import fr.uge.booqIn.entity.User;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PdfCreator {

  public byte[] generateShippingTicketPDF(BookLending bookLending) {
    Book book = bookLending.getBook();
    User lender = bookLending.getLender();
    User borrower = bookLending.getBorrower();
    try {

      String trackingNumber = "BQ-" + bookLending.getId() + "-" + System.currentTimeMillis();


      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      Document document = new Document(PageSize.A4);
      PdfWriter.getInstance(document, baos);

      document.open();

      Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
      Paragraph title = new Paragraph("BOOQIN - SHIPPING LABEL", titleFont);
      title.setAlignment(Element.ALIGN_CENTER);
      document.add(title);
      document.add(new Paragraph(" ")); // Space

      Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
      Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

      document.add(new Paragraph("BOOK: ", boldFont));
      document.add(new Paragraph(book.getTitle(), normalFont));
      document.add(new Paragraph(" ")); // Space

      // Date and tracking number
      document.add(new Paragraph("DATE: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), normalFont));
      document.add(new Paragraph("TRACKING NUMBER: " + trackingNumber, normalFont));
      document.add(new Paragraph(" ")); // Space

      // Sender information
      document.add(new Paragraph("SENDER:", boldFont));
      document.add(new Paragraph(lender.getFirstName() + " " + lender.getLastName(), normalFont));
      document.add(new Paragraph(String.valueOf(lender.getAddress()), normalFont));
      document.add(new Paragraph(" ")); // Space

      // Recipient information
      document.add(new Paragraph("RECIPIENT:", boldFont));
      document.add(new Paragraph(borrower.getFirstName() + " " + borrower.getLastName(), normalFont));
      document.add(new Paragraph(String.valueOf(borrower.getAddress()), normalFont));
      document.add(new Paragraph(" ")); // Space

      // Instructions
      document.add(new Paragraph("INSTRUCTIONS:", boldFont));
      document.add(new Paragraph("1. Carefully pack the book", normalFont));
      document.add(new Paragraph("2. Attach this label to the package", normalFont));
      document.add(new Paragraph("3. Drop off the package at the post office", normalFont));
      document.add(new Paragraph(" ")); // Space

      // Footer
      Paragraph footer = new Paragraph("Thank you for using BooqIn!", boldFont);
      footer.setAlignment(Element.ALIGN_CENTER);
      document.add(footer);

      document.close();

      return baos.toByteArray();
    } catch (Exception e) {
      return null;
    }
  }
}