package com.pdg.adventure.server.parser;

import java.util.List;

/**
 * The parser's output for one full turn of raw input, which may contain more than one
 * conjunction- or period-separated sub-command (e.g. "take sword and kill ogre",
 * "take sword. kill ogre."), in execution order.
 * <p>
 * Not to be confused with {@link GenericCommandChain}, which groups multiple candidate
 * implementations of the SAME verb/adjective/noun tried in turn by CommandExecutor - this type
 * instead holds multiple distinct commands, each resolved and executed independently in order.
 */
public record CommandSequence(List<GenericCommandDescription> commands) {

    public CommandSequence {
        commands = List.copyOf(commands);
    }
}
