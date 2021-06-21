package io.github.portlek.rgb.gradients;

import io.github.portlek.rgb.Gradient;
import io.github.portlek.rgb.TextColor;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represent CMI-like gradient applier.
 * <p>
 * the pattern is {@literal <#RRGGBB>Text</#RRGGBB>}.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HtmlGradient implements Gradient {

  /**
   * the instance.
   */
  public static final Gradient INSTANCE = new HtmlGradient();

  /**
   * the pattern.
   */
  private static final Pattern PATTERN = Pattern.compile("<#[0-9a-fA-F]{6}>[^<]*</#[0-9a-fA-F]{6}>");

  @NotNull
  @Override
  public String apply(@NotNull final String text, @NotNull final Boolean ignorePlaceholders) {
    if (!text.contains("<#")) {
      return text;
    }
    var replaced = text;
    final var matcher = HtmlGradient.PATTERN.matcher(replaced);
    while (matcher.find()) {
      final var format = matcher.group();
      if (ignorePlaceholders && format.contains("%")) {
        continue;
      }
      final var start = TextColor.of(format.substring(1, 8));
      final var message = format.substring(9, format.length() - 10);
      final var end = TextColor.of(format.substring(format.length() - 8, format.length() - 1));
      final var applied = Gradient.asGradient(start, message, end);
      replaced = replaced.replace(format, applied);
    }
    return replaced;
  }
}
