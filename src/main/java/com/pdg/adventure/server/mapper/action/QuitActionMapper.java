package com.pdg.adventure.server.mapper.action;

import org.springframework.stereotype.Service;

import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.server.action.QuitAction;
import com.pdg.adventure.server.annotation.AutoRegisterMapper;
import com.pdg.adventure.server.storage.message.MessagesHolder;
import com.pdg.adventure.server.support.MapperSupporter;

@Service
@AutoRegisterMapper(priority = 30, description = "Quit action mapper")
public class QuitActionMapper extends ActionMapper<QuitActionData, QuitAction> {

    private final MessagesHolder messagesHolder;

    public QuitActionMapper(MapperSupporter aMapperSupporter, MessagesHolder aMessagesHolder) {
        super(aMapperSupporter);
        messagesHolder = aMessagesHolder;
        aMapperSupporter.registerMapper(QuitActionData.class, QuitAction.class, this);
    }

    @Override
    public QuitAction mapToBO(QuitActionData data) {
        return new QuitAction(messagesHolder);
    }

    @Override
    public QuitActionData mapToDO(QuitAction action) {
        return new QuitActionData();
    }
}
