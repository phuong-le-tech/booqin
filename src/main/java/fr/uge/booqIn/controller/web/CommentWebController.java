package fr.uge.booqIn.controller.web;

import fr.uge.booqIn.dto.comment.CommentCreateDTO;
import fr.uge.booqIn.dto.comment.CommentDTO;
import fr.uge.booqIn.dto.comment.CommentUpdateDTO;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.service.interfaces.CommentServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/comments")
public class CommentWebController {
    private static final Logger logger = LoggerFactory.getLogger(CommentWebController.class);

    private final CommentServiceInterface commentService;

  public CommentWebController(CommentServiceInterface commentService) {
    this.commentService = commentService;
  }

  @PostMapping
    public String addComment(
        @RequestParam("content") String content,
        @RequestParam("entityId") Long entityId,
        @RequestParam("entityType") String entityType,
        @RequestParam(value = "parentId", required = false) Long parentId,
        @RequestParam(value = "redirectParams", required = false) String redirectParams,
        @AuthenticationPrincipal User currentUser,
        RedirectAttributes redirectAttributes) {

        try {
            CommentCreateDTO commentDTO = new CommentCreateDTO(
                currentUser.getId(),
                entityId,
                entityType,
                content,
                parentId);

            CommentDTO savedComment = commentService.create(commentDTO);
            redirectAttributes.addFlashAttribute("success", "Votre commentaire a été ajouté avec succès");

            
            String redirectUrl = getEntityRedirectUrl(entityType, entityId);

            if (redirectParams != null && !redirectParams.isEmpty()) {
                
                redirectParams = redirectParams.replace("{newCommentId}", savedComment.id().toString());
                redirectUrl += "?" + redirectParams;
            } else {
                
                redirectUrl += "?highlight=" + savedComment.id();
            }

            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            logger.error("Error creating comment", e);
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue lors de l'ajout du commentaire: " + e.getMessage());
            return "redirect:" + getEntityRedirectUrl(entityType, entityId);
        }
    }

    @PostMapping("/reply")
    public String addReply(
        @RequestParam("comment") String content,
        @RequestParam("entityId") Long entityId,
        @RequestParam("entityType") String entityType,
        @RequestParam("parentId") Long parentId,
        @RequestParam(value = "redirectParams", required = false) String redirectParams,
        @AuthenticationPrincipal User currentUser,
        RedirectAttributes redirectAttributes) {

        try {
            
            CommentCreateDTO commentDTO = new CommentCreateDTO(
                currentUser.getId(),
                entityId,
                entityType,
                content,
                parentId
            );
            CommentDTO savedComment = commentService.create(commentDTO);
            redirectAttributes.addFlashAttribute("success", "Votre réponse a été ajoutée avec succès.");
            String redirectUrl = getEntityRedirectUrl(entityType, entityId);
            if (redirectParams != null && !redirectParams.isEmpty()) {
                redirectParams = redirectParams.replace("{newCommentId}", savedComment.id().toString());
                redirectUrl += "?" + redirectParams;
            } else {
                
                redirectUrl += "?highlight=" + savedComment.id() + "&parent=" + parentId;
            }
            return "redirect:" + redirectUrl;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'ajout de votre réponse: " + e.getMessage());
            return "redirect:" + getEntityRedirectUrl(entityType, entityId);
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(
        @PathVariable Long id,
        @RequestParam("entityId") Long entityId,
        @RequestParam("entityType") String entityType,
        @AuthenticationPrincipal User currentUser,
        Model model,
        RedirectAttributes redirectAttributes) {

        try {
            CommentDTO comment = commentService.getById(id);

            if (!comment.userId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à modifier ce commentaire");
                return "redirect:" + getEntityRedirectUrl(entityType, entityId);
            }
            model.addAttribute("comment", comment);
            model.addAttribute("entityId", entityId);
            model.addAttribute("entityType", entityType);
            return "comment/edit";
        } catch (Exception e) {
            logger.error("Error showing edit form", e);
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue: " + e.getMessage());
            return "redirect:" + getEntityRedirectUrl(entityType, entityId);
        }
    }
    @PostMapping("/{id}/update")
    public String updateComment(
        @PathVariable Long id,
        @RequestParam("content") String content,
        @AuthenticationPrincipal User currentUser,
        RedirectAttributes redirectAttributes) {
        try {
            CommentDTO comment = commentService.getById(id);
            String redirectUrl = getEntityRedirectUrl(comment.entityType(), comment.entityId());
            if (!comment.userId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Vous n'êtes pas autorisé à modifier ce commentaire");
                return "redirect:" + redirectUrl;
            }
            CommentUpdateDTO updateDTO = new CommentUpdateDTO(content);
            commentService.update(id, currentUser.getId(), updateDTO);
            redirectAttributes.addFlashAttribute("success", "Commentaire mis à jour avec succès");
            return "redirect:" + redirectUrl + "?highlight=" + id;
        } catch (Exception e) {
            logger.error("Error updating comment", e);
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue lors de la mise à jour: " + e.getMessage());
            try {
                CommentDTO comment = commentService.getById(id);
                return "redirect:" + getEntityRedirectUrl(comment.entityType(), comment.entityId());
            } catch (Exception ex) {
                
                return "redirect:/";
            }
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteComment(
        @PathVariable Long id,
        @RequestParam("entityId") Long entityId,
        @RequestParam("entityType") String entityType,
        @AuthenticationPrincipal User currentUser,
        RedirectAttributes redirectAttributes) {

        try {
            commentService.delete(id, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Commentaire supprimé avec succès");
        } catch (Exception e) {
            logger.error("Error deleting comment", e);
            redirectAttributes.addFlashAttribute("error", "Une erreur est survenue lors de la suppression: " + e.getMessage());
        }

        return "redirect:" + getEntityRedirectUrl(entityType, entityId);
    }

    
    private String getEntityRedirectUrl(String entityType, Long entityId) {
      return switch (entityType.toUpperCase()) {
        case "BOOK" -> "/books/" + entityId;
        case "COLLECTION" -> "/collections/" + entityId;
        case "AUTHOR" -> "/authors/" + entityId;
        case "USER" -> "/users/" + entityId;
        default -> "/";
      };
    }
}