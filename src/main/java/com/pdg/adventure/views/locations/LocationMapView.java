package com.pdg.adventure.views.locations;

import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.github.legioth.imagemap.ImageMap;

@PageTitle("Your World")
@Route(value = "map", layout = LocationsMainLayout.class)
public class LocationMapView extends FormLayout {

    public LocationMapView() {
        Div div = new Div();
        ImageMap imageMap =
//                new ImageMap("https://thelordsofmidnight.com/blog/wp-content/uploads/2012/12/overview_map"
//                                                 + ".png", "World map");
        new ImageMap(new StreamResource("islandMap.jpg",
                                        () -> getClass().getResourceAsStream("/META-INF/resources/images/islandMap"
                                                                                     + ".jpg")), "islandMap");
        for (int x = 0; x < 2451; x+=100) {
            for (int y = 0; y < 2628; y+=100) {
                int finalX = x;
                int finalY = y;
                imageMap.addArea(x, y, 100, 100).addClickListener(event ->
                      Notification.show(
                              "Location " + (finalX / 100) + " : " + (finalY / 100)));
            }
        }
        div.add(imageMap);
        div.setWidth("1000px");
        div.setHeight("1000px");
        setMaxWidth("1200px");
        add(div);
    }

    /*
    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 500;

    private CanvasRenderingContext2D ctx;

    public LocationMapView() {
        Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.getStyle().set("border", "1px solid");

        ctx = canvas.getContext();

        Div buttons = new Div();
        buttons.add(new NativeButton("Draw random circle",
                e -> drawRandomCircle()));
        buttons.add(new NativeButton("Draw house", e -> drawHouse()));
        buttons.add(new NativeButton("Clear canvas",
                e -> ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT)));

        add(canvas, buttons);

        Input input = new Input();
        input.setValue("resources/vaadin-logo.svg");
        NativeButton drawImageButton = new NativeButton("Draw image",
                e -> ctx.drawImage(input.getValue(), 0, 0));
        add(new Label("Image src: "), input, drawImageButton);
    }

    private void drawHouse() {
        ctx.save();

        ctx.setFillStyle("yellow");
        ctx.strokeRect(200, 200, 100, 100);
        ctx.fillRect(200, 200, 100, 100);

        ctx.beginPath();
        ctx.moveTo(180, 200);
        ctx.lineTo(250, 150);
        ctx.lineTo(320, 200);
        ctx.closePath();
        ctx.stroke();
        ctx.setFillStyle("orange");
        ctx.fill();

        ctx.restore();
    }

    private void drawRandomCircle() {
        ctx.save();
        ctx.setLineWidth(2);
        ctx.setFillStyle(getRandomColor());
        ctx.beginPath();
        ctx.arc(Math.random() * CANVAS_WIDTH, Math.random() * CANVAS_HEIGHT,
                10 + Math.random() * 90, 0, 2 * Math.PI, false);
        ctx.closePath();
        ctx.stroke();
        ctx.fill();
        ctx.restore();
    }

    private String getRandomColor() {
        return String.format("rgb(%s, %s, %s)", (int) (Math.random() * 256),
                (int) (Math.random() * 256), (int) (Math.random() * 256));
    }
*/
}
