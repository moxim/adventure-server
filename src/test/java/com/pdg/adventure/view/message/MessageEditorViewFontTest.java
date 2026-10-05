package com.pdg.adventure.view.message;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.RouteParam;
import com.vaadin.flow.router.RouteParameters;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pdg.adventure.model.AdventureData;
import com.pdg.adventure.model.AdventureFont;
import com.pdg.adventure.model.MessageData;
import com.pdg.adventure.security.model.UserData;
import com.pdg.adventure.server.security.service.AdventureAccessService;
import com.pdg.adventure.server.storage.service.AdventureService;
import com.pdg.adventure.view.support.RouteIds;

class MessageEditorViewFontTest extends BrowserlessTest {

    private AdventureService adventureService;
    private AdventureAccessService accessService;
    private MessageEditorView view;
    private AdventureData adventure;

    @BeforeEach
    void setUp() {
        adventureService = mock(AdventureService.class);
        accessService = mock(AdventureAccessService.class);
        UserData testUser = new UserData();
        testUser.setUsername("test-author");
        testUser.setRoles(Set.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(testUser, null, testUser.getAuthorities()));
        view = new MessageEditorView(adventureService, accessService);
        UI.getCurrent().add(view);

        adventure = new AdventureData();
        adventure.setId("adv-1");
        when(accessService.findAdventureById(eq("adv-1"), any(UserData.class))).thenReturn(Optional.of(adventure));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private MessageData enterExistingMessage(AdventureFont font) {
        MessageData message = new MessageData("The note", "Meet me at midnight.");
        message.setFont(font);
        adventure.getMessages().put(message.getId(), message);
        enter(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"),
              new RouteParam(RouteIds.MESSAGE_ID.getValue(), message.getId()));
        return message;
    }

    private void enter(RouteParam... params) {
        BeforeEnterEvent event = mock(BeforeEnterEvent.class);
        when(event.getRouteParameters()).thenReturn(new RouteParameters(params));
        view.beforeEnter(event);
    }

    @SuppressWarnings("unchecked")
    private Select<AdventureFont> fontSelect() {
        return (Select<AdventureFont>) find(Select.class, view).withLabel("Message Font").single();
    }

    private Button button(String text) {
        return find(Button.class, view).withText(text).single();
    }

    @Test
    void offersEveryFont_andDescribesTheDefaultAsTheAdventuresFont() {
        enterExistingMessage(AdventureFont.DEFAULT);

        Select<AdventureFont> select = fontSelect();

        assertThat(select.getListDataView().getItems()).containsExactly(AdventureFont.values());
        assertThat(select.isEmptySelectionAllowed()).isFalse();
        assertThat(select.getItemLabelGenerator().apply(AdventureFont.DEFAULT)).isEqualTo("Same as adventure");
        assertThat(select.getItemLabelGenerator().apply(AdventureFont.CINZEL)).isEqualTo(AdventureFont.CINZEL.label());
    }

    @Test
    void showsTheFontOfTheLoadedMessage_andANewMessageStartsWithTheDefault() {
        enterExistingMessage(AdventureFont.LORA);
        assertThat(fontSelect().getValue()).isEqualTo(AdventureFont.LORA);

        enter(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"));
        assertThat(fontSelect().getValue()).isEqualTo(AdventureFont.DEFAULT);
    }

    @Test
    void choosingAFontEnablesSaveAndResetButNothingIsChangedBeforeSaving() {
        MessageData message = enterExistingMessage(AdventureFont.DEFAULT);
        assertThat(button("Save").isEnabled()).isFalse();

        test(fontSelect()).selectItem(AdventureFont.SPECIAL_ELITE.label());

        assertThat(button("Save").isEnabled()).isTrue();
        assertThat(button("Reset").isEnabled()).isTrue();
        assertThat(message.getFont()).as("buffered until Save").isEqualTo(AdventureFont.DEFAULT);
    }

    @Test
    void savingStoresTheChosenFontOnTheExistingMessage() {
        MessageData message = enterExistingMessage(AdventureFont.DEFAULT);

        test(fontSelect()).selectItem(AdventureFont.SPECIAL_ELITE.label());
        test(button("Save")).click();

        assertThat(message.getFont()).isEqualTo(AdventureFont.SPECIAL_ELITE);
        verify(adventureService).saveAdventureData(adventure);
    }

    @Test
    void savingANewMessageStoresItsFont() {
        enter(new RouteParam(RouteIds.ADVENTURE_ID.getValue(), "adv-1"));

        test(find(TextField.class, view).single()).setValue("The note");
        test(find(TextArea.class, view).single()).setValue("Meet me at midnight.");
        test(fontSelect()).selectItem(AdventureFont.COURIER_PRIME.label());
        test(button("Save")).click();

        assertThat(adventure.getMessages().values()).singleElement()
                .satisfies(saved -> assertThat(saved.getFont()).isEqualTo(AdventureFont.COURIER_PRIME));
    }

    @Test
    void resetRestoresTheSavedFont() {
        enterExistingMessage(AdventureFont.LORA);
        test(fontSelect()).selectItem(AdventureFont.CINZEL.label());

        test(button("Reset")).click();

        assertThat(fontSelect().getValue()).isEqualTo(AdventureFont.LORA);
    }

    @Test
    void thePreviewIsShownInTheChosenFont() {
        enterExistingMessage(AdventureFont.DEFAULT);
        Div preview = find(Div.class, view).withId("message-preview").single();
        assertThat(preview.getStyle().get("font-family")).isNull();

        test(fontSelect()).selectItem(AdventureFont.SPECIAL_ELITE.label());
        assertThat(preview.getStyle().get("font-family"))
                .isEqualTo(AdventureFont.SPECIAL_ELITE.cssFontFamily().orElseThrow());

        test(fontSelect()).selectItem("Same as adventure");
        assertThat(preview.getStyle().get("font-family")).as("back to the app font").isNull();
    }

    @Test
    void aMessageThatIsTheSameAsTheAdventureIsPreviewedInTheAdventuresFont() {
        adventure.setFont(AdventureFont.LORA);
        enterExistingMessage(AdventureFont.DEFAULT);
        Div preview = find(Div.class, view).withId("message-preview").single();

        assertThat(preview.getStyle().get("font-family")).isEqualTo(AdventureFont.LORA.cssFontFamily().orElseThrow());

        test(fontSelect()).selectItem(AdventureFont.CINZEL.label());
        assertThat(preview.getStyle().get("font-family")).as("its own font wins")
                .isEqualTo(AdventureFont.CINZEL.cssFontFamily().orElseThrow());
    }
}
