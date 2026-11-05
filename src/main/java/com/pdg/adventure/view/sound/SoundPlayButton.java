package com.pdg.adventure.view.sound;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.streams.DownloadHandler;

import java.util.function.Supplier;

import com.pdg.adventure.server.storage.service.SoundService;

/**
 * A button that plays a sound in the browser so the author can check it: the first click plays it, a click while it
 * plays stops it. The audio sits in a hidden {@code audio} element; its bytes are only requested when the button is
 * used, from the supplier, so a list of many sounds loads no audio.
 */
public class SoundPlayButton extends Button {

    // $0 = the audio element. Returns whether it is playing afterwards.
    private static final String TOGGLE_SCRIPT = """
            const audio = $0;
            if (audio.paused || audio.ended) {
              audio.load();
              audio.currentTime = 0;
              audio.play().catch(() => {});
              return true;
            }
            audio.pause();
            audio.currentTime = 0;
            return false;
            """;

    private final Element audio = new Element("audio");
    private final Supplier<byte[]> bytes;

    public SoundPlayButton(Supplier<byte[]> someBytes) {
        super(new Icon(VaadinIcon.PLAY));
        bytes = someBytes;
        addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        setTooltipText("Play the sound; click again to stop it");
        setAriaLabel("Play sound");

        audio.setAttribute("preload", "none");
        audio.addEventListener("ended", _ -> setIcon(new Icon(VaadinIcon.PLAY)));
        getElement().appendChild(audio);
        refresh();
        addClickListener(_ -> toggle());
    }

    /** Points the audio at the supplier's current bytes again; call after the sound's file changed. */
    public void refresh() {
        audio.setAttribute("src", audioHandler(bytes));
        setIcon(new Icon(VaadinIcon.PLAY));
    }

    /**
     * Streams the bytes inline as mp3; writes nothing when there are none. Answers byte-range requests (206, or 416 for
     * a range beyond the file) and says it can: Safari plays audio only from servers that do.
     */
    public static DownloadHandler audioHandler(Supplier<byte[]> someBytes) {
        return event -> {
            event.inline();
            event.setContentType(SoundService.CONTENT_TYPE);
            byte[] content = someBytes.get();
            if (content == null) {
                return;
            }
            VaadinResponse response = event.getResponse();
            if (response != null) {
                response.setHeader("Accept-Ranges", "bytes");
            }
            VaadinRequest request = event.getRequest();
            ByteRange range = ByteRange.parse(request == null ? null : request.getHeader("Range"), content.length);
            if (range == null || response == null) {
                event.setContentLength(content.length);
                event.getOutputStream().write(content);
            } else if (range == ByteRange.UNSATISFIABLE) {
                response.setStatus(416);
                response.setHeader("Content-Range", "bytes */" + content.length);
            } else {
                response.setStatus(206);
                response.setHeader("Content-Range", "bytes " + range.start() + "-" + range.end() + "/" + content.length);
                event.setContentLength(range.length());
                event.getOutputStream().write(content, (int) range.start(), (int) range.length());
            }
        };
    }

    private void toggle() {
        audio.executeJs(TOGGLE_SCRIPT, audio).then(Boolean.class, playing ->
                setIcon(new Icon(Boolean.TRUE.equals(playing) ? VaadinIcon.STOP : VaadinIcon.PLAY)));
    }
}
