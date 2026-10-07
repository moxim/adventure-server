package com.pdg.adventure.support;

import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.Scope;

import java.util.HashMap;
import java.util.Map;

/**
 * Test stand-in for Vaadin's "vaadin-session" scope: one bean store per fake session id, switched with
 * {@link #useSession(String)}. Single-threaded on purpose - tests interleave "sessions" by switching.
 */
public class FakeSessionScope implements Scope {

    public static final String NAME = "vaadin-session";

    private final Map<String, Map<String, Object>> storesBySession = new HashMap<>();
    private String currentSession = "default";

    public void useSession(String aSessionId) {
        currentSession = aSessionId;
    }

    @Override
    public Object get(String aName, ObjectFactory<?> anObjectFactory) {
        Map<String, Object> store = storesBySession.computeIfAbsent(currentSession, _ -> new HashMap<>());
        Object bean = store.get(aName);
        if (bean == null) {
            bean = anObjectFactory.getObject();
            store.put(aName, bean);
        }
        return bean;
    }

    @Override
    public Object remove(String aName) {
        Map<String, Object> store = storesBySession.get(currentSession);
        return store == null ? null : store.remove(aName);
    }

    @Override
    public void registerDestructionCallback(String aName, Runnable aCallback) {
        // destruction is not simulated
    }

    @Override
    public Object resolveContextualObject(String aKey) {
        return null;
    }

    @Override
    public String getConversationId() {
        return currentSession;
    }
}
