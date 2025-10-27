package com.pdg.adventure.server;

class BaseDTO {
    private final String name;
    private String id;

    public BaseDTO(String id, String name) {
        this.name = name;
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}

class DtoRecordMapper {
    public BaseDTO toDTO(BaseRecord record) {
        return new BaseDTO(record.id(), record.name());
    }

    public BaseRecord toRecord(BaseDTO dto) {
        return new BaseRecord(dto.getId(), dto.getName());
    }
}
