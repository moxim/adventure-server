package com.pdg.adventure.server.storage.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * The version of this adventure builder (from Spring Boot's build info, i.e. the pom version). Unknown when the
 * application runs without generated build info; callers then leave stamps alone.
 */
@Component
public class BuilderVersion {

    private final String version;

    @Autowired
    public BuilderVersion(ObjectProvider<BuildProperties> aBuildProperties) {
        BuildProperties properties = aBuildProperties.getIfAvailable();
        version = properties == null ? null : properties.getVersion();
    }

    private BuilderVersion(String aVersion) {
        version = aVersion;
    }

    public static BuilderVersion of(String aVersion) {
        return new BuilderVersion(aVersion);
    }

    public Optional<String> current() {
        return Optional.ofNullable(version);
    }
}
