package com.pdg.adventure.view.sound;

import com.vaadin.browserless.BrowserlessTest;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.streams.DownloadEvent;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SoundPlayButtonTest extends BrowserlessTest {

    @Test
    void audioHandler_writesTheBytesInlineAsMpeg() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DownloadEvent event = mock(DownloadEvent.class);
        when(event.getOutputStream()).thenReturn(out);

        SoundPlayButton.audioHandler(() -> new byte[] {1, 2, 3}).handleDownloadRequest(event);

        verify(event).inline();
        verify(event).setContentType("audio/mpeg");
        assertThat(out.toByteArray()).containsExactly(1, 2, 3);
    }

    @Test
    void audioHandler_withoutBytes_writesNothing() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DownloadEvent event = mock(DownloadEvent.class);
        when(event.getOutputStream()).thenReturn(out);

        SoundPlayButton.audioHandler(() -> null).handleDownloadRequest(event);

        assertThat(out.toByteArray()).isEmpty();
    }

    @Test
    void theButtonHoldsAnAudioElementAndShowsAPlayIcon() {
        SoundPlayButton button = new SoundPlayButton(() -> new byte[] {1});

        assertThat(button.getElement().getChildren().anyMatch(child -> "audio".equals(child.getTag()))).isTrue();
        assertThat(button.getIcon()).isNotNull();
    }

    private static DownloadEvent eventWithRange(String aRangeHeader, ByteArrayOutputStream anOut,
                                                VaadinResponse aResponse) throws IOException {
        VaadinRequest request = mock(VaadinRequest.class);
        when(request.getHeader("Range")).thenReturn(aRangeHeader);
        DownloadEvent event = mock(DownloadEvent.class);
        when(event.getRequest()).thenReturn(request);
        when(event.getResponse()).thenReturn(aResponse);
        when(event.getOutputStream()).thenReturn(anOut);
        return event;
    }

    @Test
    void audioHandler_withoutARangeHeader_sendsTheWholeFile_andAdvertisesRanges() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        VaadinResponse response = mock(VaadinResponse.class);
        DownloadEvent event = eventWithRange(null, out, response);

        SoundPlayButton.audioHandler(() -> new byte[] {10, 11, 12, 13}).handleDownloadRequest(event);

        verify(response).setHeader("Accept-Ranges", "bytes");
        verify(event).setContentLength(4);
        verify(response, never()).setStatus(anyInt());
        assertThat(out.toByteArray()).containsExactly(10, 11, 12, 13);
    }

    @Test
    void audioHandler_withARangeHeader_answers206WithJustThatSlice() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        VaadinResponse response = mock(VaadinResponse.class);
        DownloadEvent event = eventWithRange("bytes=1-2", out, response);

        SoundPlayButton.audioHandler(() -> new byte[] {10, 11, 12, 13}).handleDownloadRequest(event);

        verify(response).setStatus(206);
        verify(response).setHeader("Content-Range", "bytes 1-2/4");
        verify(event).setContentLength(2);
        assertThat(out.toByteArray()).containsExactly(11, 12);
    }

    @Test
    void audioHandler_withARangeBeyondTheFile_answers416AndWritesNothing() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        VaadinResponse response = mock(VaadinResponse.class);
        DownloadEvent event = eventWithRange("bytes=10-", out, response);

        SoundPlayButton.audioHandler(() -> new byte[] {10, 11, 12, 13}).handleDownloadRequest(event);

        verify(response).setStatus(416);
        verify(response).setHeader("Content-Range", "bytes */4");
        assertThat(out.toByteArray()).isEmpty();
    }
}
