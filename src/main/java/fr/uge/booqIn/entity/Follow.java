package fr.uge.booqIn.entity;

import fr.uge.booqIn.model.PolymorphicAssociation;
import fr.uge.booqIn.model.Followable;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a follow relationship.
 * A user can follow other users, books, collections, etc.
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"follower_id", "entity_id", "entity_type"})
})
public class Follow extends PolymorphicAssociation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    private User follower;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Default constructor required by JPA
    public Follow() {
    }

    public Follow(User follower) {
        this.follower = follower;
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Factory method to create a follow relationship
     */
    public static Follow create(User follower, Followable followable) {
        Follow follow = new Follow(follower);
        follow.setTargetEntity(followable);
        return follow;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}