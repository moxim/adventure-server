package com.pdg.adventure.server.engine;

public class MessageConsumer implements java.util.function.Consumer<String> {

    @Override
    public void accept(String aMessage) {
        Environment.tell(aMessage);
    }
}
