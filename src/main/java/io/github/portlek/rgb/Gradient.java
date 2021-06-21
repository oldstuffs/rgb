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

import java.util.function.BiFunction;
import org.jetbrains.annotations.NotNull;

/**
 * an interface to determine gradients.
 */
public interface Gradient extends BiFunction<@NotNull String, @NotNull Boolean, @NotNull String> {

  /**
   * gradients text based on start color, text and end color.
   *
   * @param start the start to gradient.
   * @param text the text to gradient.
   * @param end the end to gradient.
   *
   * @return reformatted text.
   */
  @NotNull
  static String asGradient(@NotNull final TextColor start, @NotNull final String text, @NotNull final TextColor end) {
    final var magicCodes = Legacy.getLastColors(text);
    final var deColorized = text.substring(magicCodes.length());
    final var builder = new StringBuilder();
    final var length = deColorized.length();
    for (var i = 0; i < length; i++) {
      final var red = (int) (start.getRed() + (float) (end.getRed() - start.getRed()) / (length - 1) * i);
      final var green = (int) (start.getGreen() + (float) (end.getGreen() - start.getGreen()) / (length - 1) * i);
      final var blue = (int) (start.getBlue() + (float) (end.getBlue() - start.getBlue()) / (length - 1) * i);
      builder.append(TextColor.of(red, green, blue).getHexCode());
      if (start.isLegacyColorForced()) {
        builder
          .append("|")
          .append(start.getLegacyColor().getCharacter());
      }
      builder
        .append(magicCodes)
        .append(deColorized.charAt(i));
    }
    return builder.toString();
  }
}
