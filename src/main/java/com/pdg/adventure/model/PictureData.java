package com.pdg.adventure.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pictures")
@Data
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PictureData extends com.pdg.adventure.model.basic.BasicData {
    private String adventureId;
    private String name;
    private byte[] content;
    private String contentType;
}
