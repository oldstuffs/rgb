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

import io.github.portlek.rgb.formatters.BukkitFormatter;
import io.github.portlek.rgb.formatters.CMIFormatter;
import io.github.portlek.rgb.formatters.HtmlFormatter;
import io.github.portlek.rgb.formatters.RainbowFormatter;
import io.github.portlek.rgb.formatters.UnnamedFormatter;
import io.github.portlek.rgb.gradients.CMIGradient;
import io.github.portlek.rgb.gradients.HtmlGradient;
import io.github.portlek.rgb.gradients.IridescentGradient;
import io.github.portlek.rgb.gradients.KyoriGradient;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represent color managers.
 */
public final class ColorManager {

  /**
   * the default.
   */
  private static final ColorManager DEFAULT = ColorManager.createDefault();

  /**
   * the default pattern.
   */
  private static final Pattern DEFAULT_PATTERN = Pattern.compile("#[0-9a-fA-F]{6}");

  /**
   * the special colors.
   */
  private static final Collection<String> SPECIAL_COLORS = Set.of("&l", "&n", "&o", "&k", "&m");

  /**
   * the colors.
   */
  private final Map<String, String> colors = new HashMap<>();

  /**
   * the formatters.
   */
  private final Collection<Formatter> formatters = new ArrayList<>();

  /**
   * the gradients.
   */
  private final Collection<Gradient> gradients = new ArrayList<>();

  /**
   * the rgb supported.
   */
  private boolean rgbSupported = true;

  /**
   * checks if the text contains legacy code.
   *
   * @param text the text to check.
   * @param code the code to check.
   *
   * @return {@code true} if the text contains legacy code.
   */
  public static boolean containsLegacyCode(@NotNull final String text, final int code) {
    return text.length() - code >= 9 &&
      text.charAt(code + 7) == '|' &&
      ChatFormat.getByChar(text.charAt(code + 8)).isPresent();
  }

  /**
   * creates a default color manager.
   *
   * @return a newly created default color manager.
   */
  @NotNull
  public static ColorManager createDefault() {
    final var colorManger = new ColorManager()
      .withFormatter(BukkitFormatter.INSTANCE)
      .withFormatter(CMIFormatter.INSTANCE)
      .withFormatter(HtmlFormatter.INSTANCE)
      .withFormatter(UnnamedFormatter.INSTANCE)
      .withGradient(CMIGradient.INSTANCE)
      .withGradient(HtmlGradient.INSTANCE)
      .withGradient(IridescentGradient.INSTANCE)
      .withGradient(KyoriGradient.INSTANCE);
    colorManger.withFormatter(RainbowFormatter.of(colorManger));
    return colorManger;
  }

  /**
   * obtains the default color manager.
   *
   * @return default color manager.
   */
  @NotNull
  public static ColorManager getDefault() {
    return ColorManager.DEFAULT;
  }

  /**
   * applies the formats to the text.
   *
   * @param text the text to apply.
   * @param ignorePlaceholders the ignore placeholders.
   *
   * @return formatted text.
   */
  @NotNull
  public String applyFormats(@NotNull final String text, final boolean ignorePlaceholders) {
    var replaced = text;
    for (final var formatter : this.formatters) {
      replaced = formatter.apply(replaced);
    }
    for (final var pattern : this.gradients) {
      replaced = pattern.apply(replaced, ignorePlaceholders);
    }
    return replaced;
  }

  /**
   * converts rgb to legacy.
   *
   * @param text the text to convert.
   *
   * @return converted text.
   */
  @NotNull
  public String convertRGBtoLegacy(@NotNull final String text) {
    if (!text.contains("#")) {
      return Legacy.color(text);
    }
    final var applied = this.applyFormats(text, false);
    final var builder = new StringBuilder();
    for (var i = 0; i < applied.length(); i++) {
      final var c = applied.charAt(i);
      if (c != '#') {
        builder.append(c);
        continue;
      }
      try {
        if (ColorManager.containsLegacyCode(applied, i)) {
          builder.append(TextColor.of(applied.substring(i, i + 7), ChatFormat.getByCharOrNull(applied.charAt(i + 8))).getLegacyColor().getChatFormat());
          i += 8;
        } else {
          builder.append(TextColor.of(applied.substring(i, i + 7)).getLegacyColor().getChatFormat());
          i += 6;
        }
      } catch (final Exception e) {
        builder.append(c);
      }
    }
    return builder.toString();
  }

  /**
   * converts the text to bukkit format.
   *
   * @param text the text to convert.
   *
   * @return converted text.
   */
  @NotNull
  public String convertToBukkitFormat(@NotNull final String text) {
    if (!this.rgbSupported) {
      return ChatComponent.fromColoredText(text).toLegacyText();
    }
    var replaced = this.applyFormats(text, false);
    final var matcher = ColorManager.DEFAULT_PATTERN.matcher(replaced);
    while (matcher.find()) {
      final var hexCode = matcher.group();
      final var fixed = "&x" +
        "&" + hexCode.charAt(1) +
        "&" + hexCode.charAt(2) +
        "&" + hexCode.charAt(3) +
        "&" + hexCode.charAt(4) +
        "&" + hexCode.charAt(5) +
        "&" + hexCode.charAt(6);
      replaced = replaced.replace(hexCode, fixed.replace('&', Legacy.COLOR_CHAR));
    }
    return replaced;
  }

  /**
   * rainbows the text.
   *
   * @param text the text to rainbow.
   * @param saturation the saturation to rainbow.
   *
   * @return rainbow text.
   */
  @NotNull
  public String rainbow(@NotNull final String text, final float saturation) {
    var rainbow = text;
    final var builder = new StringBuilder();
    for (final var color : ColorManager.SPECIAL_COLORS) {
      if (rainbow.contains(color)) {
        builder.append(color);
        rainbow = rainbow.replace(color, "");
      }
    }
    final var colors = this.createRainbow(rainbow.length(), saturation);
    final var characters = rainbow.split("");
    return IntStream.range(0, rainbow.length())
      .mapToObj(i -> this.convertToBukkitFormat(colors[i].getHexCode()) + builder + characters[i])
      .collect(Collectors.joining());
  }

  /**
   * registers a custom color.
   *
   * @param id the id to register.
   * @param color the color to register.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ColorManager withColor(@NotNull final String id, @NotNull final String color) {
    this.colors.put(id, color);
    return this;
  }

  /**
   * registers the formatter.
   *
   * @param formatter the formatter to register.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ColorManager withFormatter(@NotNull final Formatter formatter) {
    this.formatters.add(formatter);
    return this;
  }

  /**
   * registers the gradient.
   *
   * @param gradient the gradient to register.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ColorManager withGradient(@NotNull final Gradient gradient) {
    this.gradients.add(gradient);
    return this;
  }

  /**
   * sets the rgb supported.
   *
   * @param rgbSupported the rgb supported to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ColorManager withRgbSupport(final boolean rgbSupported) {
    this.rgbSupported = rgbSupported;
    return this;
  }

  /**
   * unregisters a custom color.
   *
   * @param id the id to unregister.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ColorManager withoutColor(@NotNull final String id) {
    this.colors.remove(id);
    return this;
  }

  /**
   * unregisters the formatter.
   *
   * @param formatter the formatter to unregisters.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ColorManager withoutFormatter(@NotNull final Formatter formatter) {
    this.formatters.add(formatter);
    return this;
  }

  /**
   * unregisters the gradient.
   *
   * @param gradient the gradient to unregisters.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ColorManager withoutGradient(@NotNull final Gradient gradient) {
    this.gradients.add(gradient);
    return this;
  }

  /**
   * creates rainbow.
   *
   * @param step the step to create.
   * @param saturation the saturation to create.
   *
   * @return created rainbow.
   */
  @NotNull
  private TextColor[] createRainbow(final int step, final float saturation) {
    final var colors = new TextColor[step];
    final var colorStep = 1.00 / step;
    for (var i = 0; i < step; i++) {
      final var color = Color.getHSBColor((float) (colorStep * i), saturation, saturation);
      if (this.rgbSupported) {
        colors[i] = TextColor.of(color);
      } else {
        colors[i] = TextColor.of(TextColor.getClosestColor(color));
      }
    }
    return colors;
  }
}
