package org.yabogvk.color.impl;

import org.jetbrains.annotations.Nullable;
import org.yabogvk.color.Colorizer;

public final class LegacyColorizer implements Colorizer {

    private static final char COLOR_CHAR = '§';
    private static final char ALT_COLOR_CHAR = '&';

    private static final boolean[] HEX = new boolean[128];
    private static final boolean[] COLOR = new boolean[128];

    static {
        for (int c = '0'; c <= '9'; c++) {
            HEX[c] = true;
            COLOR[c] = true;
        }
        for (int c = 'a'; c <= 'f'; c++) {
            HEX[c] = true;
            COLOR[c] = true;
        }
        for (int c = 'A'; c <= 'F'; c++) {
            HEX[c] = true;
            COLOR[c] = true;
        }
        for (int c : new int[]{'r', 'R', 'k', 'K', 'l', 'L', 'm', 'M', 'n', 'N', 'o', 'O'}) {
            COLOR[c] = true;
        }
    }

    @Override
    public String colorize(@Nullable final String message) {
        if (message == null) {
            return null;
        }

        final char[] source = message.toCharArray();
        final int length = source.length;
        if (length == 0) {
            return message;
        }

        final int firstAmpersand = message.indexOf(ALT_COLOR_CHAR);
        if (firstAmpersand < 0) {
            return message;
        }

        final char[] destination = new char[length * 2];
        int writeIndex = 0;
        int readIndex = 0;

        if (firstAmpersand > 8) {
            System.arraycopy(source, 0, destination, 0, firstAmpersand);
            writeIndex = firstAmpersand;
            readIndex = firstAmpersand;
        } else {
            while (readIndex < firstAmpersand) {
                destination[writeIndex++] = source[readIndex++];
            }
        }

        while (readIndex < length) {
            final char currentChar = source[readIndex];
            if (currentChar != ALT_COLOR_CHAR) {
                destination[writeIndex++] = currentChar;
                readIndex++;
                continue;
            }

            final int remaining = length - readIndex;

            if (remaining >= 8 && source[readIndex + 1] == '#') {
                final char h0 = source[readIndex + 2];
                final char h1 = source[readIndex + 3];
                final char h2 = source[readIndex + 4];
                final char h3 = source[readIndex + 5];
                final char h4 = source[readIndex + 6];
                final char h5 = source[readIndex + 7];
                if (h0 < 128 && HEX[h0] && h1 < 128 && HEX[h1] && h2 < 128 && HEX[h2]
                    && h3 < 128 && HEX[h3] && h4 < 128 && HEX[h4] && h5 < 128 && HEX[h5]) {
                    destination[writeIndex] = COLOR_CHAR;
                    destination[writeIndex + 1] = 'x';
                    destination[writeIndex + 2] = COLOR_CHAR;
                    destination[writeIndex + 3] = h0;
                    destination[writeIndex + 4] = COLOR_CHAR;
                    destination[writeIndex + 5] = h1;
                    destination[writeIndex + 6] = COLOR_CHAR;
                    destination[writeIndex + 7] = h2;
                    destination[writeIndex + 8] = COLOR_CHAR;
                    destination[writeIndex + 9] = h3;
                    destination[writeIndex + 10] = COLOR_CHAR;
                    destination[writeIndex + 11] = h4;
                    destination[writeIndex + 12] = COLOR_CHAR;
                    destination[writeIndex + 13] = h5;
                    writeIndex += 14;
                    readIndex += 8;
                    continue;
                }
            }

            if (remaining >= 14 && (source[readIndex + 1] == 'x' || source[readIndex + 1] == 'X')) {
                boolean isHex = true;
                for (int hexIndex = 0; hexIndex < 6; hexIndex++) {
                    if (source[readIndex + 2 + (hexIndex * 2)] != ALT_COLOR_CHAR) {
                        isHex = false;
                        break;
                    }
                    final char hexChar = source[readIndex + 3 + (hexIndex * 2)];
                    if (hexChar >= 128 || !HEX[hexChar]) {
                        isHex = false;
                        break;
                    }
                }

                if (isHex) {
                    destination[writeIndex++] = COLOR_CHAR;
                    destination[writeIndex++] = 'x';
                    for (int hexIndex = 0; hexIndex < 6; hexIndex++) {
                        destination[writeIndex++] = COLOR_CHAR;
                        destination[writeIndex++] = source[readIndex + 3 + (hexIndex * 2)];
                    }
                    readIndex += 14;
                    continue;
                }
            }

            if (remaining >= 2) {
                final char next = source[readIndex + 1];
                if (next < 128 && COLOR[next]) {
                    destination[writeIndex] = COLOR_CHAR;
                    destination[writeIndex + 1] = next;
                    writeIndex += 2;
                    readIndex += 2;
                    continue;
                }
            }

            destination[writeIndex++] = ALT_COLOR_CHAR;
            readIndex++;
        }

        return new String(destination, 0, writeIndex);
    }
}
