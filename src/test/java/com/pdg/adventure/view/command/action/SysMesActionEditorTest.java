package com.pdg.adventure.view.command.action;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

import com.pdg.adventure.model.action.SysMesActionData;
import com.pdg.adventure.server.storage.message.SystemMessageKey;

class SysMesActionEditorTest {

    @Test
    void validate_withNoMessage_returnsFalse() {
        SysMesActionEditor editor = new SysMesActionEditor(new SysMesActionData());
        editor.initialize();

        assertThat(editor.validate()).isFalse();
        assertThat(editor.getActionSummary()).isEqualTo("(none)");
    }

    @Test
    void validate_withAMessage_returnsTrue_andSummarisesIt() {
        SysMesActionData data = new SysMesActionData();
        data.setMessageKeyId("15");
        SysMesActionEditor editor = new SysMesActionEditor(data);
        editor.initialize();

        assertThat(editor.validate()).isTrue();
        assertThat(editor.getActionSummary()).isEqualTo("15: OK.");
    }

    @Test
    void choices_leaveOutTheMessagesThatNeedAnArgument() {
        assertThat(SysMesActionEditor.choices()).contains(SystemMessageKey.SM15, SystemMessageKey.SM2)
                                                .doesNotContain(SystemMessageKey.SM24, SystemMessageKey.SM36);
    }
}
