package com.pdg.adventure.server.storage.service;

import com.github.f4b6a3.ulid.Ulid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

import com.pdg.adventure.model.SoundContentData;
import com.pdg.adventure.model.SoundData;
import com.pdg.adventure.server.storage.mongo.CascadeDeleteHelper;
import com.pdg.adventure.server.storage.repository.SoundContentRepository;
import com.pdg.adventure.server.storage.repository.SoundRepository;

/** Saving and deleting sounds: the metadata document and the separate document holding the audio bytes. */
@Service
public class SoundService {
    private static final Logger LOG = LoggerFactory.getLogger(SoundService.class);

    /** Every stored sound is an mp3. */
    public static final String CONTENT_TYPE = "audio/mpeg";

    private final SoundRepository soundRepository;
    private final SoundContentRepository contentRepository;
    private final CascadeDeleteHelper cascadeDeleteHelper;

    public SoundService(SoundRepository aSoundRepository, SoundContentRepository aContentRepository,
                        CascadeDeleteHelper aCascadeDeleteHelper) {
        soundRepository = aSoundRepository;
        contentRepository = aContentRepository;
        cascadeDeleteHelper = aCascadeDeleteHelper;
    }

    /**
     * Saves the sound. With new bytes, they are stored first (reusing the sound's existing content document, which
     * resolves its lazy reference once) and the size and content type are updated; with {@code null} the stored bytes
     * stay as they are, e.g. when only the name changed.
     */
    public void save(SoundData aSound, byte[] aNewContentOrNull) {
        LOG.info("Saving sound: {}", aSound.getId());
        if (aNewContentOrNull != null) {
            SoundContentData content = aSound.getContent() != null ? aSound.getContent() : new SoundContentData();
            if (content.getId() == null) {
                content.setId(Ulid.fast().toString().toLowerCase());
            }
            content.setContent(aNewContentOrNull);
            contentRepository.save(content);
            aSound.setContent(content);
            aSound.setSizeBytes(aNewContentOrNull.length);
            aSound.setContentType(CONTENT_TYPE);
        }
        soundRepository.save(aSound);
    }

    /** Deletes the sound and, through the cascade, its audio bytes. */
    public void delete(String aSoundId) {
        LOG.info("Deleting sound: {}", aSoundId);
        soundRepository.findById(aSoundId).ifPresentOrElse(sound -> {
            cascadeDeleteHelper.cascadeDelete(sound);
            soundRepository.delete(sound);
        }, () -> LOG.warn("Sound not found for deletion: {}", aSoundId));
    }

    /**
     * Whether another sound of the adventure already has this name; names are compared trimmed and ignoring case.
     *
     * @param anExceptSoundId the sound being saved, which may keep its own name
     */
    public static boolean isNameTaken(Map<String, SoundData> someSounds, String aName, String anExceptSoundId) {
        if (someSounds == null || aName == null) {
            return false;
        }
        String wanted = normalize(aName);
        return someSounds.values().stream()
                         .filter(sound -> !sound.getId().equals(anExceptSoundId))
                         .anyMatch(sound -> sound.getName() != null && normalize(sound.getName()).equals(wanted));
    }

    private static String normalize(String aName) {
        return aName.trim().toLowerCase(Locale.ROOT);
    }
}
