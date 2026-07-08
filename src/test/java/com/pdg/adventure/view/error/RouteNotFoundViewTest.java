package com.pdg.adventure.view.error;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.InvalidLocationException;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteNotFoundViewTest {

    @Mock
    private BeforeEnterEvent beforeEnterEvent;

    private RouteNotFoundView view;

    @BeforeEach
    void setUp() {
        view = new RouteNotFoundView();
    }

    @Test
    void view_isAnnotatedAnonymousAllowed() {
        assertThat(RouteNotFoundView.class.isAnnotationPresent(AnonymousAllowed.class)).isTrue();
    }

    @Test
    void view_hasNoParentLayoutAnnotation() {
        assertThat(RouteNotFoundView.class.isAnnotationPresent(com.vaadin.flow.router.ParentLayout.class)).isFalse();
    }

    @Test
    void setErrorParameter_returnsNotFoundStatus() throws InvalidLocationException {
        when(beforeEnterEvent.getLocation()).thenReturn(new Location("some/bad/path"));

        int status = view.setErrorParameter(beforeEnterEvent,
                new ErrorParameter<>(NotFoundException.class, new NotFoundException()));

        assertThat(status).isEqualTo(HttpServletResponse.SC_NOT_FOUND);
    }

    @Test
    void setErrorParameter_messageContainsAttemptedPath() throws InvalidLocationException {
        when(beforeEnterEvent.getLocation()).thenReturn(new Location("some/bad/path"));

        view.setErrorParameter(beforeEnterEvent,
                new ErrorParameter<>(NotFoundException.class, new NotFoundException()));

        Paragraph message = view.getChildren()
                .filter(Paragraph.class::isInstance).map(Paragraph.class::cast)
                .findFirst().orElseThrow();
        assertThat(message.getText()).contains("some/bad/path")
                .contains("There's no path here");
    }

    @Test
    void view_containsPuzzledDragonImageWithAltText() {
        Image image = view.getChildren()
                .filter(Image.class::isInstance).map(Image.class::cast)
                .findFirst().orElseThrow();
        assertThat(image.getSrc()).contains("main_puzzled.jpg");
        assertThat(image.getAlt()).isPresent().get()
                .isEqualTo("A giant puzzled dragon towers over a small armored knight");
    }

    @Test
    void view_containsBackToSafetyButton() {
        Button button = view.getChildren()
                .filter(Button.class::isInstance).map(Button.class::cast)
                .findFirst().orElseThrow();
        assertThat(button.getText()).isEqualTo("Back to safety");
    }

    @Test
    void view_containsPuzzledDragonHeading() {
        H2 heading = view.getChildren()
                .filter(H2.class::isInstance).map(H2.class::cast)
                .findFirst().orElseThrow();
        assertThat(heading.getText()).isEqualTo("Even the dragon is puzzled.");
    }
}
