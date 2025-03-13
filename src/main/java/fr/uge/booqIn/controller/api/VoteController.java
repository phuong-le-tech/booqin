package fr.uge.booqIn.controller.api;

import fr.uge.booqIn.dto.vote.VoteCount;
import fr.uge.booqIn.dto.vote.VoteCreate;
import fr.uge.booqIn.dto.vote.VoteResponse;
import fr.uge.booqIn.dto.vote.VoteUpdate;
import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.factory.ApiResponse;
import fr.uge.booqIn.service.interfaces.VoteServiceInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/votes")
public class VoteController {

    private static final Logger logger = LoggerFactory.getLogger(VoteController.class);

    private final VoteServiceInterface voteService;

    public VoteController(VoteServiceInterface voteService) {
        this.voteService = voteService;
    }

    @PostMapping("")
    @PreAuthorize("authentication.principal.id == #voteCreate.userId()")
    public ResponseEntity<ApiResponse<VoteResponse>> castVote(@RequestBody VoteCreate voteCreate) {
        logger.info("🗳️ Création d'un vote par l'utilisateur ID: {} pour l'entité ID: {} de type: {}",
                voteCreate.userId(), voteCreate.entityId(), voteCreate.entityType());

        VoteResponse voteResponse = voteService.castVote(voteCreate);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Vote cast successfully", voteResponse));
    }

    @PutMapping("/user/{userId}/entity/{entityId}/type/{entityType}")
    @PreAuthorize("authentication.principal.id == #userId")
    public ResponseEntity<ApiResponse<VoteResponse>> updateVote(
            @PathVariable Long userId,
            @PathVariable Long entityId,
            @PathVariable String entityType,
            @RequestBody VoteUpdate voteUpdate) {

        logger.info("🔄 Mise à jour du vote utilisateur ID: {} pour l'entité ID: {} de type: {}",
                userId, entityId, entityType);

        VoteResponse voteResponse = voteService.updateVote(userId, entityId, entityType, voteUpdate);

        return ResponseEntity.ok(ApiResponse.success("Vote updated successfully", voteResponse));
    }

    @DeleteMapping("/user/{userId}/entity/{entityId}/type/{entityType}")
    @PreAuthorize("authentication.principal.id == #userId or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteVote(
            @PathVariable Long userId,
            @PathVariable Long entityId,
            @PathVariable String entityType) {

        logger.info("🗑️ Suppression du vote utilisateur ID: {} pour l'entité ID: {} de type: {}",
                userId, entityId, entityType);

        voteService.deleteVote(userId, entityId, entityType);

        return ResponseEntity.ok(ApiResponse.success("Vote deleted successfully"));
    }

    @GetMapping("/user/{userId}/entity/{entityId}/type/{entityType}")
    public ResponseEntity<ApiResponse<VoteResponse>> getVote(
            @PathVariable Long userId,
            @PathVariable Long entityId,
            @PathVariable String entityType) {

        logger.info("🔍 Récupération du vote utilisateur ID: {} pour l'entité ID: {} de type: {}",
                userId, entityId, entityType);

        VoteResponse voteResponse = voteService.getVote(userId, entityId, entityType);

        return ResponseEntity.ok(ApiResponse.success("Vote found", voteResponse));
    }

    @GetMapping("/entity/{entityId}/type/{entityType}")
    public ResponseEntity<ApiResponse<List<VoteResponse>>> getVotesByEntity(
            @PathVariable Long entityId,
            @PathVariable String entityType) {

        logger.info("🔍 Récupération des votes pour l'entité ID: {} de type: {}", entityId, entityType);

        List<VoteResponse> votes = voteService.getVotesByEntity(entityId, entityType);

        return ResponseEntity.ok(ApiResponse.success("Votes found", votes));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("authentication.principal.id == #userId or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<VoteResponse>>> getVotesByUser(
            @PathVariable Long userId) {

        logger.info("🔍 Récupération des votes par l'utilisateur ID: {}", userId);

        List<VoteResponse> votes = voteService.getVotesByUser(userId);

        return ResponseEntity.ok(ApiResponse.success("Votes found", votes));
    }

    @GetMapping("/count/entity/{entityId}/type/{entityType}")
    public ResponseEntity<ApiResponse<VoteCount>> getVoteCounts(
            @PathVariable Long entityId,
            @PathVariable String entityType) {

        logger.info("🔢 Récupération du décompte des votes pour l'entité ID: {} de type: {}",
                entityId, entityType);

        VoteCount voteCount = voteService.getVoteCounts(entityId, entityType);

        return ResponseEntity.ok(ApiResponse.success("Vote counts retrieved", voteCount));
    }

    @GetMapping("/user/{userId}/entity/{entityId}/type/{entityType}/has-voted")
    public ResponseEntity<ApiResponse<Boolean>> hasVoted(
            @PathVariable Long userId,
            @PathVariable Long entityId,
            @PathVariable String entityType) {

        logger.info("❓ Vérification si l'utilisateur ID: {} a voté pour l'entité ID: {} de type: {}",
                userId, entityId, entityType);

        boolean hasVoted = voteService.hasVoted(userId, entityId, entityType);

        return ResponseEntity.ok(ApiResponse.success("Has voted status retrieved", hasVoted));
    }

    @GetMapping("/user/{userId}/entity/{entityId}/type/{entityType}/vote-type")
    public ResponseEntity<ApiResponse<VoteType>> getUserVoteType(
            @PathVariable Long userId,
            @PathVariable Long entityId,
            @PathVariable String entityType) {

        logger.info("🔍 Récupération du type de vote de l'utilisateur ID: {} pour l'entité ID: {} de type: {}",
                userId, entityId, entityType);

        VoteType voteType = voteService.getUserVoteType(userId, entityId, entityType);

        String message = voteType != null ? "Vote type retrieved" : "User has not voted";

        return ResponseEntity.ok(ApiResponse.success(message, voteType));
    }
}