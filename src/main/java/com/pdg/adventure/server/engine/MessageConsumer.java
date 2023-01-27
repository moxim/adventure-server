package com.pdg.adventure.server.engine;

import java.util.function.Consumer;

public class MessageConsumer implements java.util.function.Consumer<String> {

    @Override
    public void accept(String aMessage) {
        Environment.tell(aMessage);
    }

    @Override
    public Consumer<String> andThen(Consumer<? super String> after) {
        return Consumer.super.andThen(after);
    }
}
