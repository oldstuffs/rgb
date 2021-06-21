package io.github.portlek.rgb.gradients;

import io.github.portlek.rgb.Gradient;
import io.github.portlek.rgb.TextColor;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * a class that represent Kyori gradient applier.
 * <p>
 * the pattern is &lt;gradient:#RRGGBB:#RRGGBB&gt;Text&lt;/gradient&gt;.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class KyoriGradient implements Gradient {

  /**
   * the instance.
   */
  public static final Gradient INSTANCE = new KyoriGradient();

  /**
   * the pattern.
   */
  private static final Pattern PATTERN = Pattern.compile("<gradient:#[0-9a-fA-F]{6}:#[0-9a-fA-F]{6}>[^<]*</gradient>");

  @NotNull
  @Override
  public String apply(@NotNull final String text, @NotNull final Boolean ignorePlaceholders) {
    if (!text.contains("<grad")) {
      return text;
    }
    var replaced = text;
    final var matcher = KyoriGradient.PATTERN.matcher(replaced);
    while (matcher.find()) {
      final var format = matcher.group();
      if (ignorePlaceholders && format.contains("%")) {
        continue;
      }
      final var start = TextColor.of(format.substring(10, 17));
      final var message = format.substring(26, format.length() - 11);
      final var end = TextColor.of(format.substring(18, 25));
      final var applied = Gradient.asGradient(start, message, end);
      replaced = replaced.replace(format, applied);
    }
    return replaced;
  }
}
