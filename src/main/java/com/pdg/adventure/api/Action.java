package com.pdg.adventure.api;

import java.io.Serializable;

public interface Action extends Serializable {
    ExecutionResult execute();
    String getActionName();
}
