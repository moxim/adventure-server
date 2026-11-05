package com.pdg.adventure.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * The audio bytes of one {@link SoundData}, kept in a document of their own so that loading an adventure never loads
 * audio: {@link SoundData#getContent()} is a lazy reference that is only resolved when a sound is played or previewed.
 */
@Document(collection = "soundcontent")
@Data
public class SoundContentData {
    @Id
    private String id;
    private byte[] content;
}
