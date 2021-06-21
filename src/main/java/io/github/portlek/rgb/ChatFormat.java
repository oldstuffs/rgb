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

import java.util.Arrays;
import java.util.Optional;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * an enum class that contains chat formats.
 */
public enum ChatFormat {
  /**
   * the black.
   */
  BLACK(0, '0', "#000000"),
  /**
   * the dark blue.
   */
  DARK_BLUE(1, '1', "#0000AA"),
  /**
   * the dark green.
   */
  DARK_GREEN(2, '2', "#00AA00"),
  /**
   * the dark aqua.
   */
  DARK_AQUA(3, '3', "#00AAAA"),
  /**
   * the dark red.
   */
  DARK_RED(4, '4', "#AA0000"),
  /**
   * the dark purple.
   */
  DARK_PURPLE(5, '5', "#AA00AA"),
  /**
   * the gold.
   */
  GOLD(6, '6', "#FFAA00"),
  /**
   * the gray.
   */
  GRAY(7, '7', "#AAAAAA"),
  /**
   * the dark gray.
   */
  DARK_GRAY(8, '8', "#555555"),
  /**
   * the blue.
   */
  BLUE(9, '9', "#5555FF"),
  /**
   * the green.
   */
  GREEN(10, 'a', "#55FF55"),
  /**
   * the aqua.
   */
  AQUA(11, 'b', "#55FFFF"),
  /**
   * the red.
   */
  RED(12, 'c', "#FF5555"),
  /**
   * the light purple.
   */
  LIGHT_PURPLE(13, 'd', "#FF55FF"),
  /**
   * the yellow.
   */
  YELLOW(14, 'e', "#FFFF55"),
  /**
   * the white.
   */
  WHITE(15, 'f', "#FFFFFF"),
  /**
   * the obfuscated.
   */
  OBFUSCATED(16, 'k'),
  /**
   * the bold.
   */
  BOLD(17, 'l'),
  /**
   * the strikethrough.
   */
  STRIKETHROUGH(18, 'm'),
  /**
   * the underline.
   */
  UNDERLINE(19, 'n'),
  /**
   * the italic.
   */
  ITALIC(20, 'o'),
  /**
   * the reset.
   */
  RESET(21, 'r');

  /**
   * the values.
   */
  public static final ChatFormat[] values = ChatFormat.values();

  /**
   * the character.
   */
  @Getter
  private final char character;

  /**
   * the chat format.
   */
  @Getter
  @NotNull
  private final String chatFormat;

  /**
   * the network id.
   */
  @Getter
  private final int networkId;

  /**
   * the blue.
   */
  @Getter
  private int blue;

  /**
   * the green.
   */
  @Getter
  private int green;

  /**
   * the hex code.
   */
  @Getter
  private String hexCode;

  /**
   * the red.
   */
  @Getter
  private int red;

  /**
   * ctor.
   *
   * @param networkId the network id.
   * @param character the character id.
   * @param hexCode the hex code.
   */
  ChatFormat(final int networkId, final char character, final String hexCode) {
    this(networkId, character);
    this.hexCode = hexCode;
    final var hexColor = Integer.parseInt(hexCode.substring(1), 16);
    this.red = hexColor >> 16 & 0xFF;
    this.green = hexColor >> 8 & 0xFF;
    this.blue = hexColor & 0xFF;
  }

  /**
   * ctor.
   *
   * @param networkId the network id.
   * @param character the character.
   */
  ChatFormat(final int networkId, final char character) {
    this.networkId = networkId;
    this.character = character;
    this.chatFormat = "\u00a7" + character;
  }

  /**
   * gets chat format by rgb.
   *
   * @param red the red to get.
   * @param green the green to get.
   * @param blue the blue to get.
   *
   * @return char format.
   */
  @NotNull
  public static Optional<ChatFormat> fromRGBExact(final int red, final int green, final int blue) {
    return Arrays.stream(ChatFormat.values)
      .filter(format -> format.red == red && format.green == green && format.blue == blue)
      .findFirst();
  }

  /**
   * gets chat format by character.
   *
   * @param character the character to get.
   *
   * @return char format by character.
   */
  @NotNull
  public static Optional<ChatFormat> getByChar(final char character) {
    return Arrays.stream(ChatFormat.values)
      .filter(format -> format.character == character)
      .findFirst();
  }

  /**
   * gets chat format by character.
   *
   * @param character the character to get.
   *
   * @return char format by character.
   */
  @Nullable
  public static ChatFormat getByCharOrNull(final char character) {
    return ChatFormat.getByChar(character).orElse(null);
  }

  /**
   * obtains the last colors of the text.
   *
   * @param text the text to get.
   *
   * @return obtained last colors of the text.
   */
  @NotNull
  public static ChatFormat getLastColors(@NotNull final String text) {
    return ChatFormat.getLastColors(text, ColorManager.getDefault());
  }

  /**
   * obtains the last colors of the text.
   *
   * @param text the text to get.
   * @param manager the manager to get.
   *
   * @return obtained last colors of the text.
   */
  @NotNull
  public static ChatFormat getLastColors(@NotNull final String text, @NotNull final ColorManager manager) {
    if (text.isEmpty()) {
      return ChatFormat.WHITE;
    }
    final var legacyText = manager.convertRGBtoLegacy(text);
    final var last = Legacy.getLastColors(legacyText);
    if (last.length() <= 0) {
      return ChatFormat.WHITE;
    }
    final var c = last.toCharArray()[1];
    return Arrays.stream(ChatFormat.values)
      .filter(e -> e.character == c)
      .findFirst()
      .orElse(ChatFormat.WHITE);
  }
}
