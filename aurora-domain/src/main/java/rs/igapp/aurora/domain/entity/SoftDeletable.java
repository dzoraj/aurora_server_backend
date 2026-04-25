package rs.igapp.aurora.domain.entity;

import java.time.LocalDateTime;

public interface SoftDeletable {
    Boolean getIsDeleted();
    void setIsDeleted(Boolean isDeleted);

    LocalDateTime getDeletedAt();
    void setDeletedAt(LocalDateTime deletedAt);
}