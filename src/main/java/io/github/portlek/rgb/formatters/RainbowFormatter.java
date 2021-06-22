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

package io.github.portlek.rgb.formatters;

import io.github.portlek.rgb.ColorManager;
import io.github.portlek.rgb.Formatter;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represent Bukkit formatter.
 * <p>
 * the pattern is &lt;rainbow000&gt;Text&lt;/rainbow&gt;.
 * <p>
 * the number is saturation.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RainbowFormatter implements Formatter {

  /**
   * the pattern.
   */
  private static final Pattern PATTERN = Pattern.compile("<rainbow([0-9]{1,3})>(.*?)</rainbow>");

  /**
   * the color manager.
   */
  @NotNull
  private final ColorManager colorManager;

  /**
   * creates a rainbow formatter.
   *
   * @param colorManager the color manager to create.
   *
   * @return a newly created rainbow formatter.
   */
  @NotNull
  public static Formatter of(@NotNull final ColorManager colorManager) {
    return new RainbowFormatter(colorManager);
  }

  @NotNull
  @Override
  public String apply(@NotNull final String text) {
    if (!text.contains("<rainbow")) {
      return text;
    }
    var replaced = text;
    final var matcher = RainbowFormatter.PATTERN.matcher(replaced);
    while (matcher.find()) {
      final var saturation = matcher.group(1);
      final var content = matcher.group(2);
      replaced = replaced.replace(matcher.group(), this.colorManager.rainbow(content, Float.parseFloat(saturation)));
    }
    return replaced;
  }
}
