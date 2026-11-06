package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import com.pdg.adventure.model.basic.BasicData;
import com.pdg.adventure.server.storage.mongo.CascadeDelete;

/**
 * One mp3 sound of an adventure, as the author uploads it. The adventure references the sounds eagerly (they are small
 * here); the bytes sit behind the lazy {@link #content} reference and go away with the sound.
 */
@Document(collection = "sounds")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class SoundData extends BasicData {
    private String adventureId;
    private String name;
    private String contentType;
    private long sizeBytes;

    @DBRef(lazy = true)
    @CascadeDelete
    @ToString.Exclude
    private SoundContentData content;

    /** The audio bytes, which resolves the lazy {@link #content} reference; {@code null} when there is none. */
    public byte[] audioBytes() {
        return content == null ? null : content.getContent();
    }
}
