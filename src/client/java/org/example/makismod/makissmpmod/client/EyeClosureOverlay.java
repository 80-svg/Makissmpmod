package org.example.makismod.makissmpmod.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class EyeClosureOverlay {
    private static final int SOLID_BLACK = 0xFF000000;
    private static final int[] SMEAR_ALPHAS = {0x22000000, 0x16000000};
    private static final int[] FEATHER_ALPHAS = {0x70000000, 0x38000000};

    private EyeClosureOverlay() {
    }

    public static void render(GuiGraphics guiGraphics, float closure, boolean closeCompletely) {
        if (closure <= 0.0F) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        float clampedClosure = Mth.clamp(closure, 0.0F, 1.0F);
        float shapeProgress = Mth.clamp((clampedClosure - 0.12F) / 0.88F, 0.0F, 1.0F);
        float borderProgress = Mth.clamp((clampedClosure - 0.22F) / 0.78F, 0.0F, 1.0F);
        float openness = 1.0F - clampedClosure;
        if (openness <= 0.01F) {
            guiGraphics.fill(0, 0, width, height, SOLID_BLACK);
            return;
        }

        float centerX = width * 0.5F;
        float centerY = height * 0.5F;
        float horizontalRadius = width * 0.42F;
        float maxVerticalRadius = height * (closeCompletely ? 0.24F : 0.18F);
        float startVerticalRadius = height * (closeCompletely ? 0.30F : 0.26F);
        float verticalRadius = Mth.lerp(shapeProgress, startVerticalRadius, maxVerticalRadius * openness);
        int stripWidth = Math.max(1, width / 320);
        int featherSize = Math.max(1, height / 240);
        int smearSize = Math.max(1, height / 300);
        int softShade = ((int) (Mth.clamp(clampedClosure * 0.45F, 0.0F, 0.45F) * 255.0F)) << 24;

        for (int x = 0; x < width; x += stripWidth) {
            float stripCenterX = x + stripWidth * 0.5F;
            float normalizedX = (stripCenterX - centerX) / horizontalRadius;
            int stripRight = Math.min(width, x + stripWidth);
            int openTop = height / 2;
            int openBottom = height / 2;

            if (Math.abs(normalizedX) < 1.0F) {
                float curve = Mth.sqrt(1.0F - normalizedX * normalizedX);
                int halfOpenHeight = Math.max(0, Mth.floor(verticalRadius * curve));
                openTop = Mth.clamp(Mth.floor(centerY - halfOpenHeight), 0, height);
                openBottom = Mth.clamp(Mth.ceil(centerY + halfOpenHeight), 0, height);
            }

            if (openTop > 0) {
                guiGraphics.fill(x, 0, stripRight, openTop, SOLID_BLACK);
                if (softShade != 0) {
                    guiGraphics.fill(x, Math.max(0, openTop - featherSize * 3), stripRight, openTop, softShade);
                }
            }

            if (openBottom < height) {
                guiGraphics.fill(x, openBottom, stripRight, height, SOLID_BLACK);
                if (softShade != 0) {
                    guiGraphics.fill(x, openBottom, stripRight, Math.min(height, openBottom + featherSize * 3), softShade);
                }
            }

            if (borderProgress <= 0.0F) {
                continue;
            }

            for (int i = 0; i < SMEAR_ALPHAS.length; i++) {
                int band = smearSize * (i + 1);
                int alpha = scaleAlpha(SMEAR_ALPHAS[i], borderProgress);
                int smearTop = Math.max(0, openTop - band - smearSize);
                int smearTopInner = Math.max(0, openTop - band);
                if (smearTopInner > smearTop) {
                    guiGraphics.fill(x, smearTop, stripRight, smearTopInner, alpha);
                }

                int smearBottomInner = Math.min(height, openBottom + band);
                int smearBottom = Math.min(height, openBottom + band + smearSize);
                if (smearBottom > smearBottomInner) {
                    guiGraphics.fill(x, smearBottomInner, stripRight, smearBottom, alpha);
                }
            }

            for (int i = 0; i < FEATHER_ALPHAS.length; i++) {
                int band = featherSize * (i + 1);
                int alpha = scaleAlpha(FEATHER_ALPHAS[i], borderProgress);
                int featherTop = Math.max(0, openTop - band);
                int featherTopInner = Math.max(0, openTop - band + featherSize);
                if (featherTopInner > featherTop) {
                    guiGraphics.fill(x, featherTop, stripRight, featherTopInner, alpha);
                }

                int featherBottomInner = Math.min(height, openBottom + band - featherSize);
                int featherBottom = Math.min(height, openBottom + band);
                if (featherBottom > featherBottomInner) {
                    guiGraphics.fill(x, featherBottomInner, stripRight, featherBottom, alpha);
                }
            }
        }
    }

    private static int scaleAlpha(int color, float scale) {
        int alpha = color >>> 24;
        int scaledAlpha = Mth.clamp(Math.round(alpha * scale), 0, 255);
        return (scaledAlpha << 24) | (color & 0x00FFFFFF);
    }
}
