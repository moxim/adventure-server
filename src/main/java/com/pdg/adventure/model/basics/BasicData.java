package com.pdg.adventure.model.basics;

import com.pdg.adventure.api.Ided;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;

import java.util.UUID;

@Data
@EqualsAndHashCode
public class BasicData implements Ided {
    @Id
    @EqualsAndHashCode.Include
    private String id = UUID.randomUUID().toString();

//    @Override
//    public String toString() {
//        return this.getClass().getSimpleName() + "[" + id + "]";
//    }
}
