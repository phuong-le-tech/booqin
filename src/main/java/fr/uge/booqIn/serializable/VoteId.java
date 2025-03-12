package fr.uge.booqIn.serializable;

import fr.uge.booqIn.entity.User;
import fr.uge.booqIn.entity.Votable;

import java.io.Serializable;
import java.util.Objects;

public class VoteId implements Serializable {
    private User user;
    private Votable votable;

    public VoteId() {
    }

    public VoteId(User user, Votable votable) {
        this.user = user;
        this.votable = votable;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Votable getVotable() {
        return votable;
    }

    public void setVotable(Votable votable) {
        this.votable = votable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoteId voteId = (VoteId) o;
        return Objects.equals(user, voteId.user) &&
                Objects.equals(votable, voteId.votable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, votable);
    }
}