package com.pdg.adventure.view.error;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.ErrorParameter;
import com.vaadin.flow.router.HasErrorParameter;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletResponse;

import com.pdg.adventure.view.RootView;

/**
 * Standalone (no parent layout) so it renders for anonymous and authenticated
 * visitors alike — AdventuresMainLayout is @PermitAll and would otherwise
 * re-block anonymous access at the layout level.
 */
@AnonymousAllowed
public class RouteNotFoundView extends VerticalLayout implements HasErrorParameter<NotFoundException> {

    private final Paragraph message = new Paragraph();

    public RouteNotFoundView() {
        setSpacing(false);

        Image img = new Image("images/main_puzzled.jpg",
                "A giant puzzled dragon towers over a small armored knight");
        img.setWidth("300px");
        add(img);

        add(new H2("Even the dragon is puzzled."));
        add(message);

        Button backButton = new Button("Back to safety",
                e -> UI.getCurrent().navigate(RootView.class));
        add(backButton);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        message.setText("There's no path here — '" + event.getLocation().getPath()
                + "' doesn't lead anywhere in this world.");
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
