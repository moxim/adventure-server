package com.pdg.adventure.server.exception;

import com.pdg.adventure.server.storage.message.SystemMessageKey;

public class QuitException extends RuntimeException {
    public QuitException() {
        super(SystemMessageKey.SM14.defaultText());
    }
}
