package com.pdg.adventure.model.basic;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper=true, onlyExplicitlyIncluded = true)
public class DatedData extends BasicData {

    /**
     * Timestamp when the message was created.
     */
    @CreatedDate
    @Field("createdAt")
    private Instant createdAt;

    /**
     * Timestamp when the message was last modified.
     */
    @LastModifiedDate
    @Field("updatedAt")
    private Instant updatedAt;

    public DatedData() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    /**
     * Update the modified timestamp.
     * Should be called before saving updates.
     */
    public void touch() {
        updatedAt = Instant.now();
    }
}
