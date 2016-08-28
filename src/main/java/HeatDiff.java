package io.eclair.heatdiff;

import java.util.stream.IntStream;
import java.awt.image.BufferedImage;

public class HeatDiff {
    public static BufferedImage getHeatMap(final BufferedImage imageA, final BufferedImage imageB) {
        if (imageA.getHeight() != imageB.getHeight() || imageA.getWidth() != imageB.getWidth()) {
            // TODO: Handle this instead with a null object
            return null;
        }

        final int height = imageA.getHeight();
        final int width = imageB.getWidth();

        int[] pixelsA = imageA.getRaster().getPixels(0, 0, width, height, new int[width * height * 3]);
        int[] pixelsB = imageB.getRaster().getPixels(0, 0, width, height, new int[width * height * 3]);

        IntStream.range(0, pixelsA.length)
            .map(i -> {
                return pixelsA[i] - pixelsB[i];
            });

        BufferedImage heatMap = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        return heatMap;
    }
}
