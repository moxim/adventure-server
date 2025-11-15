package com.pdg.adventure.model.basic;

import com.github.f4b6a3.ulid.Ulid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

import com.pdg.adventure.api.Ided;

@Data
@EqualsAndHashCode
public class BasicData implements Ided {
    @Id
    @EqualsAndHashCode.Include
    private String id;

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

    public BasicData() {
        id = Ulid.fast().toLowerCase();
//        id = UUID.randomUUID().toString();
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
