package fr.uge.booqIn.entity;

import fr.uge.booqIn.enums.VoteType;
import fr.uge.booqIn.interfaces.PolymorphicAssociation;
import fr.uge.booqIn.interfaces.Votable;
import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "entity_id", "entity_type"})
})
public class Vote extends PolymorphicAssociation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoteType voteType;

    public Vote() {}

    public Vote(User user, VoteType voteType) {
        this.user = user;
        this.voteType = voteType;
    }

    public static Vote create(User user, Votable votable, VoteType voteType) {
        Vote vote = new Vote(user, voteType);
        vote.setTargetEntity(votable);
        return vote;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }
}