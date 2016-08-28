package io.eclair.heatdiff;

import java.util.Arrays;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.Color;

import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;

// TODO: Clean everything, exceptions etc
public class HeatCompare {
    public static void main(String[] args) throws Exception {
        System.out.println("Starting");

        File imageRoot = new File("/home/tw/Documents");

        BufferedImage imageA = ImageIO.read(new File(imageRoot, "A.jpg"));
        BufferedImage imageB = ImageIO.read(new File(imageRoot, "B.jpg"));

        //BufferedImage heatMap = makeHeatMap(imageA, imageB);
        BufferedImage heatMap = fastMakeHeatMap(imageA, imageB);

        System.out.println("Writing file");

        ImageIO.write(heatMap, "BMP", new File(imageRoot, "Heat.bmp"));
    }

    public static BufferedImage makeHeatMap(BufferedImage imageA, BufferedImage imageB) {
        int width = imageA.getWidth();
        int height = imageA.getHeight();

        BufferedImage heatMap = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int black = (new Color(0, 0 ,0)).getRGB();
        int white = (new Color(255, 255 ,255)).getRGB();

        double range = white - black;

        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                int rgbA = imageA.getRGB(w, h);
                int rgbB = imageB.getRGB(w, h);

                int diff = 0;

                if (rgbA > rgbB) {
                    diff = rgbA - rgbB;
                } else {
                    diff = rgbB - rgbA;
                }

                int diffAmp = (int) (255 * ((double) diff)/range);

                int diffRGB = (new Color(diffAmp, diffAmp, diffAmp)).getRGB();

                heatMap.setRGB(w, h, diffRGB);
            }
        }

        return heatMap;
    }

    public static BufferedImage fastMakeHeatMap(BufferedImage imageA, BufferedImage imageB) {
        int width = imageA.getWidth();
        int height = imageA.getHeight();

        BufferedImage heatMap = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        int[] pixelsA = imageA.getRaster().getPixels(0, 0, width, height, new int[width * height * 3]);
        int[] pixelsB = imageB.getRaster().getPixels(0, 0, width, height, new int[width * height * 3]);

        int[] heatPixels = getHeatValues(pixelsDiff(pixelsA, pixelsB));

        heatMap.getRaster().setPixels(0, 0, width, height, heatPixels);

        return heatMap;
    }

    public static int[] pixelsDiff(int[] pixelsA, int[] pixelsB) {
        int[] diff = new int[pixelsA.length];

        for (int i = 0; i < pixelsA.length; i++) {
            if (pixelsA[i] > pixelsB[i]) {
                diff[i] = pixelsA[i] - pixelsB[i];
            } else {
                diff[i] = pixelsB[i] - pixelsA[i];
            }
        }

        return diff;
    }

    public static int[] getHeatValues(int[] pixels) {
        int[] heat = new int[pixels.length];

        for (int i = 0; i < pixels.length; i += 3) {
            double ratioValue =
                ((double) pixels[i] +
                (double) pixels[i+1] +
                (double) pixels[i+2]) / (255);

            int[] num = translateToHeat(ratioValue);

            //System.out.println(ratioValue + "->" + num[0] + "|" + num[1] + "|" + num[2]);

            heat[i] = num[0];
            heat[i+1] = num[1];
            heat[i+2] = num[2];
        }

        return heat;
    }

    public static int[] translateToHeat(double normalValue) {
        //black->blue->cyan->green->yellow->red->white
        int[] black = { 0, 0, 0 };
        int[] blue = { 0, 0, 255 };
        int[] cyan = { 0, 255, 255 };
        int[] green = { 0, 255, 0 };
        int[] yellow = { 255, 255, 0 };
        int[] red = { 255, 0, 0 };
        int[] white = { 255, 255, 255 };

        double increment = (double) 1/6;

        // TODO: Binary search
        if (0 <= normalValue && normalValue < increment) {
            return getGradientValue(normalValue, black, blue);
        } else if (1 * increment <= normalValue && normalValue < 2 * increment) {
            return getGradientValue(normalValue, blue, cyan);
        } else if (2 * increment <= normalValue && normalValue < 3 * increment) {
            return getGradientValue(normalValue, cyan, green);
        } else if (3 * increment <= normalValue && normalValue < 4 * increment) {
            return getGradientValue(normalValue, green, yellow);
        } else if (4 * increment <= normalValue && normalValue < 5 * increment) {
            return getGradientValue(normalValue, yellow, red);
        } else if (5 * increment <= normalValue && normalValue <= 6 * increment) {
            return getGradientValue(normalValue, red, white);
        }

        return getGradientValue(normalValue, red, white);
    }

    public static int[] getGradientValue(double normalValue, int[] from, int[] to) {
        int r = (int) ((double) (to[0] - from[0]) * normalValue + from[0]);
        int g = (int) ((double) (to[1] - from[1]) * normalValue + from[1]);
        int b = (int) ((double) (to[2] - from[2]) * normalValue + from[2]);

        int[] num = {r, g, b};

        return num;

    }
}
