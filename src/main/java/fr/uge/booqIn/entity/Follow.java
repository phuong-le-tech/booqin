package fr.uge.booqIn.entity;

import fr.uge.booqIn.interfaces.PolymorphicAssociation;
import fr.uge.booqIn.interfaces.Followable;
import jakarta.persistence.*;
import java.time.LocalDateTime;

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
    private LocalDateTime createdAt = LocalDateTime.now();

    public Follow() {
    }

    public Follow(User follower) {
        this.follower = follower;
    }

    public static Follow create(User follower, Followable followable) {
        Follow follow = new Follow(follower);
        follow.setTargetEntity(followable);
        return follow;
    }

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