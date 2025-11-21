package com.pdg.adventure.view.about;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import com.pdg.adventure.view.adventure.AdventuresMainLayout;

@PageTitle("About")
@Route(value = "about", layout = AdventuresMainLayout.class)
public class AboutView extends VerticalLayout {

    public AboutView() {
        setSpacing(false);

        Image img = new Image("images/main.jpg", "Adventure Builder Logo");
        img.setWidth("300px");
        add(img);

        add(new H2("@ PDG Software 2022 - 2026"));
        add(new Paragraph("The place where your imagination can run wild! 🤗"));
        add(new Paragraph(new Html(
                "<a href=\"https://www.flaticon.com/free-animated-icons/localization\" title=\"localization animated icons\">Localization animated icons created by Freepik - Flaticon</a>)")));

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

}
