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

import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

/**
 * a class that contains utility methods for legacy support.
 */
public final class Legacy {

  /**
   * the magic codes 1.
   */
  private static final String MAGIC_CODES_1 = "0123456789AaBbCcDdEeFfRr";

  /**
   * the magic codes 2.
   */
  private static final String MAGIC_CODES_2 = Legacy.MAGIC_CODES_1 + "KkLlMmNnOo";

  /**
   * the magic codes 3.
   */
  private static final String MAGIC_CODES_3 = Legacy.MAGIC_CODES_2 + "Xx";

  /**
   * ctor.
   */
  private Legacy() {
  }

  /**
   * colorizes the text.
   *
   * @param text the text to colorize.
   *
   * @return colorized text.
   */
  @NotNull
  public static String color(@NotNull final String text) {
    if (!text.contains("&")) {
      return text;
    }
    final var chars = text.toCharArray();
    IntStream.range(0, chars.length - 1)
      .filter(i -> chars[i] == '&' && Legacy.MAGIC_CODES_3.indexOf(chars[i + 1]) > -1)
      .forEach(i -> {
        chars[i] = '\u00a7';
        chars[i + 1] = Character.toLowerCase(chars[i + 1]);
      });
    return new String(chars);
  }

  /**
   * gets last colors of the text.
   *
   * @param text the text to get.
   *
   * @return obtained last colors of the text.
   */
  @NotNull
  public static String getLastColors(@NotNull final String text) {
    final var builder = new StringBuilder();
    final var length = text.length();
    for (var index = length - 1; index > -1; index--) {
      final var section = text.charAt(index);
      if (section != '\u00a7' && section != '&' || index >= length - 1) {
        continue;
      }
      final var c = text.charAt(index + 1);
      if (!Legacy.MAGIC_CODES_2.contains(String.valueOf(c))) {
        continue;
      }
      builder.insert(0, "\u00a7" + c);
      if (Legacy.MAGIC_CODES_1.contains(String.valueOf(c))) {
        break;
      }
    }
    return builder.toString();
  }
}
