package fr.uge.booqIn.interfaces;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class PolymorphicAssociation {

    @Column(name = "entity_id", nullable = false)
    private Long entityId;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    

    public void setTargetEntity(PolymorphicEntity entity) {
        this.entityId = entity.getId();
        this.entityType = entity.getEntityType();
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }
}