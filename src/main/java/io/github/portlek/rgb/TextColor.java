/*
 * MIT License
 *
 * Copyright (c) 2021 Hasan Demirtaş
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.github.portlek.rgb;

import java.util.Locale;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * a class that represent text colors.
 */
@Getter
public final class TextColor {

  /**
   * the blue.
   */
  private final int blue;

  /**
   * the green.
   */
  private final int green;

  /**
   * the hex code.
   */
  @NotNull
  private final String hexCode;

  /**
   * the legacy color.
   */
  @NotNull
  private final ChatFormat legacyColor;

  /**
   * the legacy color forced.
   */
  private final boolean legacyColorForced;

  /**
   * the red.
   */
  private final int red;

  /**
   * the return legacy.
   */
  @Setter
  private boolean returnLegacy;

  /**
   * ctor.
   *
   * @param red the red.
   * @param green the green.
   * @param blue the blue.
   * @param hexCode the hex code.
   * @param legacyColor the legacy color.
   * @param legacyColorForced the legacy color forced.
   */
  private TextColor(final int red, final int green, final int blue, @NotNull final String hexCode,
                    @NotNull final ChatFormat legacyColor, final boolean legacyColorForced) {
    this.blue = blue;
    this.green = green;
    this.hexCode = hexCode;
    this.legacyColor = legacyColor;
    this.legacyColorForced = legacyColorForced;
    this.red = red;
  }

  /**
   * gets text color by text.
   *
   * @param text the text to get.
   *
   * @return text color.
   */
  @NotNull
  public static TextColor getByText(@NotNull final String text) {
    if (text.startsWith("#")) {
      return TextColor.of(text);
    }
    return TextColor.of(ChatFormat.valueOf(text.toUpperCase(Locale.ROOT)));
  }

  /**
   * creates a text color.
   *
   * @param legacyColor the legacy color to create.
   *
   * @return a newly create text color.
   */
  @NotNull
  public static TextColor of(@NotNull final ChatFormat legacyColor) {
    return new TextColor(legacyColor.getRed(), legacyColor.getGreen(), legacyColor.getBlue(), legacyColor.getHexCode(),
      legacyColor, false);
  }

  /**
   * creates a text color.
   *
   * @param hexCode the hex code to create.
   * @param legacyColor the legacy color to create.
   *
   * @return a newly create text color.
   */
  @NotNull
  public static TextColor of(@NotNull final String hexCode, @Nullable final ChatFormat legacyColor) {
    final var hexColor = Integer.parseInt(hexCode.substring(1), 16);
    final var red = hexColor >> 16 & 0xFF;
    final var green = hexColor >> 8 & 0xFF;
    final var blue = hexColor & 0xFF;
    final var notNullLegacyColor = legacyColor == null
      ? TextColor.getClosestColor(red, green, blue)
      : legacyColor;
    return new TextColor(red, green, blue, hexCode, notNullLegacyColor, true);
  }

  /**
   * creates a text color.
   *
   * @param red the red to create.
   * @param green the green to create.
   * @param blue the blue to create.
   *
   * @return a newly create text color.
   */
  @NotNull
  public static TextColor of(final int red, final int green, final int blue) {
    return new TextColor(red, green, blue, String.format("#%06X", (red << 16) + (green << 8) + blue),
      TextColor.getClosestColor(red, green, blue), false);
  }

  /**
   * creates a text color.
   *
   * @param hexCode the hex code to create.
   *
   * @return a newly create text color.
   */
  @NotNull
  public static TextColor of(@NotNull final String hexCode) {
    return TextColor.of(hexCode, null);
  }

  /**
   * gets closest color.
   *
   * @param red the red to get.
   * @param green the green to get.
   * @param blue the blue to get.
   *
   * @return closest color.
   */
  @NotNull
  private static ChatFormat getClosestColor(final int red, final int green, final int blue) {
    var minMaxDist = 9999.0d;
    var maxDist = 0.0d;
    var legacyColor = ChatFormat.WHITE;
    for (final var color : ChatFormat.values) {
      var rDiff = color.getRed() - red;
      var gDiff = color.getGreen() - green;
      var bDiff = color.getBlue() - blue;
      if (rDiff < 0) {
        rDiff = -rDiff;
      }
      if (gDiff < 0) {
        gDiff = -gDiff;
      }
      if (bDiff < 0) {
        bDiff = -bDiff;
      }
      maxDist = rDiff;
      if (gDiff > maxDist) {
        maxDist = gDiff;
      }
      if (bDiff > maxDist) {
        maxDist = bDiff;
      }
      if (maxDist < minMaxDist) {
        minMaxDist = maxDist;
        legacyColor = color;
      }
    }
    return legacyColor;
  }

  @NotNull
  public String toString() {
    if (this.returnLegacy) {
      return this.getLegacyColor().toString().toLowerCase(Locale.ROOT);
    }
    return ChatFormat.fromRGBExact(this.getRed(), this.getGreen(), this.getBlue())
      .map(chatFormat -> chatFormat.toString().toLowerCase(Locale.ROOT))
      .orElse(this.hexCode);
  }
}
