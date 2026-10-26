package fr.uge.booqIn.controller.web;

import org.springframework.context.annotation.Profile;
import fr.uge.booqIn.dto.user.UserIndex;
import fr.uge.booqIn.dto.book.BookIndex;
import fr.uge.booqIn.dto.bookCollection.BookCollectionIndex;
import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.mapper.BookCollectionMapper;
import fr.uge.booqIn.mapper.BookMapper;
import fr.uge.booqIn.mapper.UserMapper;
import fr.uge.booqIn.service.interfaces.FollowServiceInterface;
import fr.uge.booqIn.service.interfaces.BookServiceInterface;
import fr.uge.booqIn.service.interfaces.UserServiceInterface;
import fr.uge.booqIn.service.interfaces.BookCollectionServiceInterface;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Profile("!demo")
@Controller
@RequestMapping("/following")
public class FollowWebController {

    private final FollowServiceInterface followService;
    private final UserServiceInterface userService;
    private final BookServiceInterface bookService;
    private final BookCollectionServiceInterface bookCollectionService;
    private final HomeWebController homeWebController;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;
    private final BookCollectionMapper bookCollectionMapper;

    public FollowWebController(
            FollowServiceInterface followService,
            UserServiceInterface userService,
            BookServiceInterface bookService,
            BookCollectionServiceInterface bookCollectionService,
            UserMapper userMapper,
            BookMapper bookMapper,
            BookCollectionMapper bookCollectionMapper,
            HomeWebController homeWebController) {
        this.followService = followService;
        this.userService = userService;
        this.bookService = bookService;
        this.bookCollectionService = bookCollectionService;
        this.userMapper = userMapper;
        this.bookMapper = bookMapper;
        this.bookCollectionMapper = bookCollectionMapper;
        this.homeWebController = homeWebController;
    }

    @GetMapping
    public String following(
            Model model,
            Principal principal,
            @AuthenticationPrincipal User currentUser,
            @RequestParam(value = "tab", defaultValue = "users") String activeTab) {

        // Prepare common model
        homeWebController.prepareModel(model, principal, "following");

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        // Set active tab
        model.addAttribute("activeTab", activeTab);

        // Load followed entities based on the active tab
        switch (activeTab) {
            case "users":
                List<Long> followedUserIds = followService.getFollowedEntities(currentUser.getId(), "USER");
                List<UserIndex> followedUsers = followedUserIds.stream()
                        .map(userService::findUserById)
                        .map(userMapper::toUserIndex)
                        .collect(Collectors.toList());
                model.addAttribute("followedUsers", followedUsers);
                break;
            case "books":
                List<Long> followedBookIds = followService.getFollowedEntities(currentUser.getId(), "BOOK");
                List<BookIndex> followedBooks = followedBookIds.stream()
                        .map(bookService::findBookById)
                        .map(bookMapper::toBookIndex)
                        .collect(Collectors.toList());
                model.addAttribute("followedBooks", followedBooks);
                break;
            case "collections":
                List<Long> followedCollectionIds = followService.getFollowedEntities(currentUser.getId(), "COLLECTION");
                List<BookCollectionIndex> followedCollections = followedCollectionIds.stream()
                        .map(bookCollectionService::findCollectionById)
                        .map(bookCollectionMapper::toBookCollectionIndex)
                        .collect(Collectors.toList());
                model.addAttribute("followedCollections", followedCollections);
                break;
        }

        model.addAttribute("title", "BooqIn - Following");
        model.addAttribute("content", "pages/follows/following");
        return "layout";
    }

    @PostMapping("/follow")
    public String followEntity(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long entityId,
            @RequestParam String entityType,
            @RequestParam(required = false) String returnUrl,
            RedirectAttributes redirectAttributes) {

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            switch (entityType) {
                case "USER":
                    followService.followEntity(currentUser.getId(), userService.findUserById(entityId));
                    break;
                case "BOOK":
                    followService.followEntity(currentUser.getId(), bookService.findBookById(entityId));
                    break;
                case "COLLECTION":
                    followService.followEntity(currentUser.getId(), bookCollectionService.findCollectionById(entityId));
                    break;
                default:
                    redirectAttributes.addFlashAttribute("error", "Invalid entity type");
                    return returnUrl != null ? "redirect:" + returnUrl : "redirect:/";
            }

            redirectAttributes.addFlashAttribute("success", "Successfully followed");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to follow: " + e.getMessage());
        }

        return returnUrl != null ? "redirect:" + returnUrl : "redirect:/following?tab=" + entityType.toLowerCase() + "s";
    }

    @PostMapping("/unfollow")
    public String unfollowEntity(
            @AuthenticationPrincipal User currentUser,
            @RequestParam Long entityId,
            @RequestParam String entityType,
            @RequestParam(required = false) String returnUrl,
            RedirectAttributes redirectAttributes) {

        if (currentUser == null) {
            return "redirect:/auth/login";
        }

        try {
            boolean success = followService.unfollowEntity(currentUser.getId(), entityId, entityType);
            if (success) {
                redirectAttributes.addFlashAttribute("success", "Successfully unfollowed");
            } else {
                redirectAttributes.addFlashAttribute("error", "Failed to unfollow");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error: " + e.getMessage());
        }

        return returnUrl != null ? "redirect:" + returnUrl : "redirect:/following?tab=" + entityType.toLowerCase() + "s";
    }
}