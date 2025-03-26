package fr.uge.booqIn.pdf;

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
      Paragraph title = new Paragraph("BOOQIN - ÉTIQUETTE D'EXPÉDITION", titleFont);
      title.setAlignment(Element.ALIGN_CENTER);
      document.add(title);
      document.add(new Paragraph(" ")); // Espace
      
      Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
      Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
      
      document.add(new Paragraph("LIVRE: ", boldFont));
      document.add(new Paragraph(book.getTitle(), normalFont));
      document.add(new Paragraph(" ")); // Espace
      
      // Date et numéro de suivi
      document.add(new Paragraph("DATE: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()), normalFont));
      document.add(new Paragraph("NUMÉRO DE SUIVI: " + trackingNumber, normalFont));
      document.add(new Paragraph(" ")); // Espace
      
      // Informations expéditeur
      document.add(new Paragraph("EXPÉDITEUR:", boldFont));
      document.add(new Paragraph(lender.getFirstName() + " " + lender.getLastName(), normalFont));
      document.add(new Paragraph(String.valueOf(lender.getAddress()), normalFont));
      document.add(new Paragraph(" ")); // Espace
      
      // Informations destinataire
      document.add(new Paragraph("DESTINATAIRE:", boldFont));
      document.add(new Paragraph(borrower.getFirstName() + " " + borrower.getLastName(), normalFont));
      document.add(new Paragraph(String.valueOf(borrower.getAddress()), normalFont));
      document.add(new Paragraph(" ")); // Espace
      
      // Instructions
      document.add(new Paragraph("INSTRUCTIONS:", boldFont));
      document.add(new Paragraph("1. Emballez soigneusement le livre", normalFont));
      document.add(new Paragraph("2. Collez cette étiquette sur le colis", normalFont));
      document.add(new Paragraph("3. Déposez le colis à La Poste", normalFont));
      document.add(new Paragraph(" ")); // Espace
      
      // Pied de page
      Paragraph footer = new Paragraph("Merci d'utiliser BooqIn !", boldFont);
      footer.setAlignment(Element.ALIGN_CENTER);
      document.add(footer);
      
      document.close();
      
      return baos.toByteArray();
    } catch (Exception e) {
      return null;
    }
  }
}
