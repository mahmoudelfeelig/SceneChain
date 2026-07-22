package de.scenechain.protocol;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class MarkerOverlay {
    private MarkerOverlay() {}

    public static List<Integer> generate(SecureRandom random) {
        var markers = new ArrayList<Integer>(96);
        for (int marker = 0; marker < Protocol.MARKERS; marker++) {
            for (int n = 0; n < 12; n++) markers.add(marker);
        }
        Collections.shuffle(markers, random);
        return List.copyOf(markers);
    }

    public static int expected(List<Integer> overlay, int cellId) {
        if (overlay.size() != 96) throw new IllegalArgumentException("Invalid overlay");
        return overlay.get(Protocol.challengeTile(cellId));
    }
}
