package com.pdg.adventure.model.basic;

import com.github.f4b6a3.ulid.Ulid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;

import com.pdg.adventure.api.Ided;

@Data
@EqualsAndHashCode
public class BasicData implements Ided {
    @Id
    @EqualsAndHashCode.Include
    private String id = Ulid.fast().toString();
}
