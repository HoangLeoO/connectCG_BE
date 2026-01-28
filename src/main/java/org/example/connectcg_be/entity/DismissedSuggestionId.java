package org.example.connectcg_be.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Embeddable
public class DismissedSuggestionId implements Serializable {
    private static final long serialVersionUID = 5939690412480797546L;
    @NotNull
    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @NotNull
    @Column(name = "dismissed_user_id", nullable = false)
    private Integer dismissedUserId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        DismissedSuggestionId entity = (DismissedSuggestionId) o;
        return Objects.equals(this.dismissedUserId, entity.dismissedUserId) &&
                Objects.equals(this.userId, entity.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dismissedUserId, userId);
    }

}
