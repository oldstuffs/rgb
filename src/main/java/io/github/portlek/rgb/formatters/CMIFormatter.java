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

import io.github.portlek.rgb.Formatter;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represent CMI-like formatter.
 * <p>
 * the pattern is {@literal {#RRGGBB}}.
 */
public final class CMIFormatter implements Formatter {

  /**
   * the pattern.
   */
  private static final Pattern PATTERN = Pattern.compile("\\{#[0-9a-fA-F]{6}}");

  @NotNull
  @Override
  public String apply(@NotNull final String text) {
    if (!text.contains("{#")) {
      return text;
    }
    var replaced = text;
    final var matcher = CMIFormatter.PATTERN.matcher(replaced);
    while (matcher.find()) {
      final var hexCode = matcher.group();
      final var fixed = hexCode.substring(2, 8);
      replaced = replaced.replace(hexCode, "#" + fixed);
    }
    return replaced;
  }
}
