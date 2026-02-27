package com.pdg.adventure.view.message;

import com.pdg.adventure.api.Describable;

public class MessageDescriptionAdapter implements Describable {

    private final MessageViewModel messageViewModel;

    public MessageDescriptionAdapter(MessageViewModel aMessageViewModel) {
        messageViewModel = aMessageViewModel;
    }

    @Override
    public String getId() {
        return messageViewModel.getId();
    }

    @Override
    public void setId(String anId) {
        messageViewModel.setId(anId);
    }

    @Override
    public String getAdjective() {
        return "";
    }

    @Override
    public String getNoun() {
        return "";
    }

    @Override
    public String getBasicDescription() {
        return null;
    }

    @Override
    public String getEnrichedBasicDescription() {
        return null;
    }

    @Override
    public String getShortDescription() {
        return messageViewModel.getPreview(30);
    }

    @Override
    public String getLongDescription() {
        return null;
    }

    @Override
    public String getEnrichedShortDescription() {
        return null;
    }

    public int getLength() {
        return messageViewModel.getMessageText().length();
    }

    public int getUsageCount() {
        return messageViewModel.getUsageCount();
    }

    public MessageViewModel getMessageViewModel() {
        return messageViewModel;
    }
}
