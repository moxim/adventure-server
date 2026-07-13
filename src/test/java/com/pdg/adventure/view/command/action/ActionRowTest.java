package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.QuitActionData;
import com.pdg.adventure.model.action.SetVariableActionData;

class ActionRowTest {

    @Test
    void summaryText_withTargetSummary_showsTypeAndTarget() {
        SetVariableActionData data = new SetVariableActionData("score", "10");
        SetVariableActionEditor editor = new SetVariableActionEditor(data);
        editor.initialize();

        ActionRow row = new ActionRow(editor);

        assertThat(row.getSummaryText()).isEqualTo("SetVariable: score = 10");
    }

    @Test
    void summaryText_withEmptyTarget_showsTypeAndNone() {
        SetVariableActionData data = new SetVariableActionData(null, null);
        SetVariableActionEditor editor = new SetVariableActionEditor(data);
        editor.initialize();

        ActionRow row = new ActionRow(editor);

        assertThat(row.getSummaryText()).isEqualTo("SetVariable: (none)");
    }

    @Test
    void summaryText_withNoTargetAction_showsTypeOnly() {
        QuitActionData data = new QuitActionData();
        QuitActionEditor editor = new QuitActionEditor(data);
        editor.initialize();

        ActionRow row = new ActionRow(editor);

        assertThat(row.getSummaryText()).isEqualTo("Quit");
    }
}
