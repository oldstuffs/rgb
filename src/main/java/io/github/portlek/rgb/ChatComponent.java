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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import lombok.Getter;
import lombok.extern.java.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * a class that represents char components.
 */
@Log
@SuppressWarnings("unchecked")
public final class ChatComponent {

  /**
   * the empty text.
   */
  private static final String EMPTY_TEXT = "{\"text\":\"\"}";

  /**
   * the empty translatable.
   */
  private static final String EMPTY_TRANSLATABLE = "{\"translate\":\"\"}";

  /**
   * the bold.
   */
  @Nullable
  private Boolean bold;

  /**
   * the click action.
   */
  @Nullable
  @Getter
  private ClickAction clickAction;

  /**
   * the click value.
   */
  @Nullable
  @Getter
  private String clickValue;

  /**
   * the color.
   */
  @Nullable
  @Getter
  private TextColor color;

  /**
   * the extra.
   */
  @Nullable
  private List<ChatComponent> extra;

  /**
   * the hover action.
   */
  @Nullable
  @Getter
  private HoverAction hoverAction;

  /**
   * the hover value.
   */
  @Nullable
  private Object hoverValue;

  /**
   * the italic.
   */
  @Nullable
  private Boolean italic;

  /**
   * the obfucated.
   */
  @Nullable
  private Boolean obfuscated;

  /**
   * the strikethrough.
   */
  @Nullable
  private Boolean strikethrough;

  /**
   * the text.
   */
  @Nullable
  @Getter
  private String text;

  /**
   * the underlined.
   */
  @Nullable
  private Boolean underlined;

  /**
   * ctor.
   *
   * @param text the text.
   */
  public ChatComponent(@Nullable final String text) {
    this.text = text;
  }

  /**
   * ctor.
   */
  public ChatComponent() {
  }

  /**
   * creates a chat component from colored text.
   *
   * @param originalText the original text to create.
   *
   * @return a newly created chat component from colored text.
   */
  @NotNull
  public static ChatComponent fromColoredText(@NotNull final String originalText) {
    final var text = ColorManager.getDefault().applyFormats(Legacy.color(originalText), false);
    final var components = new ArrayList<ChatComponent>();
    var builder = new StringBuilder();
    var component = new ChatComponent();
    for (var i = 0; i < text.length(); i++) {
      var c = text.charAt(i);
      if (c == '\u00a7') {
        i++;
        if (i >= text.length()) {
          break;
        }
        c = text.charAt(i);
        if (c >= 'A' && c <= 'Z') {
          c = (char) (c + ' ');
        }
        final var format = ChatFormat.getByCharOrNull(c);
        if (format != null) {
          if (builder.length() > 0) {
            component.withText(builder.toString());
            components.add(component);
            component = component.copyFormatting();
            builder = new StringBuilder();
          }
          switch (format) {
            case BOLD:
              component.withBold(true);
              break;
            case ITALIC:
              component.withItalic(true);
              break;
            case UNDERLINE:
              component.withUnderlined(true);
              break;
            case STRIKETHROUGH:
              component.withStrikethrough(true);
              break;
            case OBFUSCATED:
              component.withObfuscated(true);
              break;
            case RESET:
              component = new ChatComponent();
              component.withColor(TextColor.of(ChatFormat.WHITE));
              break;
            default:
              component = new ChatComponent();
              component.withColor(TextColor.of(format));
              break;
          }
        }
      } else if (c == '#') {
        try {
          final var hex = text.substring(i, i + 7);
          Integer.parseInt(hex.substring(1), 16);
          final TextColor color;
          if (ColorManager.containsLegacyCode(text, i)) {
            color = TextColor.of(hex, ChatFormat.getByCharOrNull(text.charAt(i + 8)));
            i += 8;
          } else {
            color = TextColor.of(hex);
            i += 6;
          }
          if (builder.length() > 0) {
            component.withText(builder.toString());
            components.add(component);
            builder = new StringBuilder();
          }
          component = new ChatComponent();
          component.withColor(color);
        } catch (final Exception e) {
          builder.append(c);
        }
      } else {
        builder.append(c);
      }
    }
    component.withText(builder.toString());
    components.add(component);
    return new ChatComponent("").withExtra(components);
  }

  /**
   * parses the json and converts it into chat component.
   *
   * @param json the json to parse.
   *
   * @return chat component from json value.
   */
  @NotNull
  public static ChatComponent fromString(@NotNull final String json) {
    try {
      if (json.startsWith("\"") && json.endsWith("\"") && json.length() > 1) {
        return new ChatComponent(json.substring(1, json.length() - 1));
      }
      final var jsonObject = (JSONObject) new JSONParser().parse(json);
      final var component = new ChatComponent()
        .withText((String) jsonObject.get("text"))
        .withBold(ChatComponent.getBoolean(jsonObject, "bold"))
        .withItalic(ChatComponent.getBoolean(jsonObject, "italic"))
        .withUnderlined(ChatComponent.getBoolean(jsonObject, "underlined"))
        .withStrikethrough(ChatComponent.getBoolean(jsonObject, "strikethrough"))
        .withObfuscated(ChatComponent.getBoolean(jsonObject, "obfuscated"))
        .withColor(TextColor.getByText((String) jsonObject.get("color")));
      if (jsonObject.containsKey("clickEvent")) {
        final var clickEvent = (JSONObject) jsonObject.get("clickEvent");
        final var action = (String) clickEvent.get("action");
        final var value = clickEvent.get("value").toString();
        component.withClick(ClickAction.valueOf(action.toUpperCase(Locale.ROOT)), value);
      }
      if (jsonObject.containsKey("hoverEvent")) {
        final var hoverEvent = (JSONObject) jsonObject.get("hoverEvent");
        final var action = (String) hoverEvent.get("action");
        final var value = (String) hoverEvent.get("value");
        component.withHover(HoverAction.valueOf(action.toUpperCase(Locale.ROOT)), value);
      }
      if (jsonObject.containsKey("extra")) {
        final var list = (List<Object>) jsonObject.get("extra");
        for (final var extra : list) {
          var string = extra.toString();
          if (!string.startsWith("{")) {
            string = "\"" + string + "\"";
          }
          component.addExtra(ChatComponent.fromString(string));
        }
      }
      return component;
    } catch (final ParseException e) {
      ChatComponent.log.warning("Failed to parse json object: " + json);
      return ChatComponent.fromColoredText(json);
    } catch (final Exception e) {
      ChatComponent.log.log(Level.SEVERE, "Failed to read component: " + json, e);
      return ChatComponent.fromColoredText(json);
    }
  }

  /**
   * creates a optimized chat component from the text.
   *
   * @param text the text to create.
   *
   * @return chat component.
   */
  @NotNull
  public static ChatComponent optimizedComponent(@NotNull final String text) {
    if (text.contains("#") || text.contains("&x") || text.contains('\u00a7' + "x")) {
      return ChatComponent.fromColoredText(text);
    }
    return new ChatComponent(text);
  }

  /**
   * gets boolean value from json.
   *
   * @param jsonObject the json object to get.
   * @param key the key to get.
   *
   * @return boolean value at key or null.
   */
  @Nullable
  private static Boolean getBoolean(@NotNull final JSONObject jsonObject, @NotNull final String key) {
    if (jsonObject.containsKey(key)) {
      return Boolean.parseBoolean(String.valueOf(jsonObject.get(key)));
    }
    return null;
  }

  /**
   * adds the extra.
   *
   * @param extra the extra to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent addExtra(@NotNull final ChatComponent extra) {
    if (this.extra == null) {
      this.extra = new ArrayList<>();
    }
    this.extra.add(extra);
    return this;
  }

  @Override
  @NotNull
  public ChatComponent clone() {
    final var component = new ChatComponent(this.text)
      .withBold(this.bold)
      .withColor(this.color)
      .withItalic(this.italic)
      .withObfuscated(this.obfuscated)
      .withStrikethrough(this.strikethrough)
      .withUnderlined(this.underlined);
    if (this.hoverAction != null) {
      component.withHover(this.hoverAction, this.hoverValue);
    }
    if (this.clickAction != null) {
      component.withClick(this.clickAction, this.clickValue);
    }
    this.getExtra().stream()
      .map(ChatComponent::clone)
      .forEach(component::addExtra);
    return component;
  }

  @Override
  public String toString() {
    final var json = new JSONObject();
    if (this.text != null) {
      json.put("text", this.text);
    }
    if (this.color != null) {
      json.put("color", this.color.toString());
    }
    if (this.bold != null) {
      json.put("bold", this.bold);
    }
    if (this.italic != null) {
      json.put("italic", this.italic);
    }
    if (this.underlined != null) {
      json.put("underlined", this.underlined);
    }
    if (this.strikethrough != null) {
      json.put("strikethrough", this.strikethrough);
    }
    if (this.obfuscated != null) {
      json.put("obfuscated", this.obfuscated);
    }
    if (this.clickAction != null) {
      final var click = new JSONObject();
      click.put("action", this.clickAction.toString().toLowerCase(Locale.ROOT));
      click.put("value", this.clickValue);
      json.put("clickEvent", click);
    }
    if (this.hoverAction != null) {
      final var hover = new JSONObject();
      hover.put("action", this.hoverAction.toString().toLowerCase(Locale.ROOT));
      hover.put("value", this.hoverValue);
      json.put("hoverEvent", hover);
    }
    if (this.extra != null) {
      json.put("extra", this.extra);
    }
    return json.toString();
  }

  /**
   * copies the formatting.
   *
   * @return chat component with copied formatting.
   */
  @NotNull
  public ChatComponent copyFormatting() {
    return new ChatComponent()
      .withBold(this.bold)
      .withColor(this.color)
      .withItalic(this.italic)
      .withObfuscated(this.obfuscated)
      .withStrikethrough(this.strikethrough)
      .withUnderlined(this.underlined);
  }

  /**
   * obtains the bold.
   *
   * @return bold or null.
   */
  @Nullable
  public Boolean getBold() {
    return this.bold;
  }

  /**
   * obtains the extra.
   *
   * @return extra.
   */
  @NotNull
  public List<ChatComponent> getExtra() {
    if (this.extra == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(this.extra);
  }

  /**
   * obtains the hover value.
   *
   * @return hover value.
   */
  @Nullable
  public Object getHoverValue() {
    if (this.hoverValue instanceof String) {
      return "\"" + this.hoverValue + "\"";
    }
    return this.hoverValue;
  }

  /**
   * obtains the italic.
   *
   * @return italic or null.
   */
  @Nullable
  public Boolean getItalic() {
    return this.italic;
  }

  /**
   * obtains the obfuscated.
   *
   * @return obfuscated or null.
   */
  @Nullable
  public Boolean getObfuscated() {
    return this.obfuscated;
  }

  /**
   * obtains the strikethrough.
   *
   * @return strikethrough or null.
   */
  @Nullable
  public Boolean getStrikethrough() {
    return this.strikethrough;
  }

  /**
   * obtains the underlined.
   *
   * @return underlined or null.
   */
  @Nullable
  public Boolean getUnderlined() {
    return this.underlined;
  }

  /**
   * obtains the bold.
   *
   * @return bold.
   */
  public boolean isBold() {
    return this.bold != null && this.bold;
  }

  /**
   * obtains the italic.
   *
   * @return italic.
   */
  public boolean isItalic() {
    return this.italic != null && this.italic;
  }

  /**
   * obtains the obfuscated.
   *
   * @return obfuscated.
   */
  public boolean isObfuscated() {
    return this.obfuscated != null && this.obfuscated;
  }

  /**
   * obtains the strikethrough.
   *
   * @return strikethrough.
   */
  public boolean isStrikethrough() {
    return this.strikethrough != null && this.strikethrough;
  }

  /**
   * obtains the underlined.
   *
   * @return underlined.
   */
  public boolean isUnderlined() {
    return this.underlined != null && this.underlined;
  }

  /**
   * converts the components into a flat text.
   *
   * @return flat text.
   */
  @NotNull
  public String toFlatText() {
    final var builder = new StringBuilder();
    if (this.color != null) {
      builder.append(this.color.getHexCode());
    }
    this.putFormats(builder);
    if (this.text != null) {
      builder.append(this.text);
    }
    this.getExtra().stream()
      .map(ChatComponent::toFlatText)
      .forEach(builder::append);
    return builder.toString();
  }

  /**
   * converts the component to legacy text.
   *
   * @return legacy text.
   */
  @NotNull
  public String toLegacyText() {
    final var builder = new StringBuilder();
    this.append(builder, "");
    return builder.toString();
  }

  /**
   * converts the component to raw text.
   *
   * @return raw text.
   */
  @NotNull
  public String toRawText() {
    final var builder = new StringBuilder();
    if (this.text != null) {
      builder.append(this.text);
    }
    this.getExtra().stream()
      .filter(extra -> extra.text != null)
      .map(extra -> extra.text).forEach(builder::append);
    return builder.toString();
  }

  /**
   * converts component to string.
   *
   * @param rgbSupported the rgb supported to convert..
   * @param sendTranslatableIfEmpty the send translatable if empty to convert.
   *
   * @return string.
   */
  @Nullable
  public String toString(final boolean rgbSupported, final boolean sendTranslatableIfEmpty) {
    if (this.extra == null) {
      if (this.text == null) {
        return null;
      }
      if (this.text.length() == 0) {
        if (sendTranslatableIfEmpty) {
          return ChatComponent.EMPTY_TRANSLATABLE;
        }
        return ChatComponent.EMPTY_TEXT;
      }
    }
    if (!rgbSupported) {
      this.convertColorsToLegacy();
    }
    return this.toString();
  }

  /**
   * sets the bold.
   *
   * @param bold the bold to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withBold(@Nullable final Boolean bold) {
    this.bold = bold;
    return this;
  }

  /**
   * sets the click.
   *
   * @param action the action to set.
   * @param value the value to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withClick(@Nullable final ClickAction action, @Nullable final String value) {
    this.clickAction = action;
    this.clickValue = value;
    return this;
  }

  /**
   * sets the click for change page.
   *
   * @param newPage the new page to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withClickChangePage(final int newPage) {
    return this.withClick(ClickAction.CHANGE_PAGE, String.valueOf(newPage));
  }

  /**
   * sets the click for copy to clipboard.
   *
   * @param clipboard the clipboard to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withClickCopyToClipboard(@NotNull final String clipboard) {
    return this.withClick(ClickAction.COPY_TO_CLIPBOARD, clipboard);
  }

  /**
   * sets the click for open url.
   *
   * @param url the url to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withClickOpenUrl(@NotNull final String url) {
    return this.withClick(ClickAction.OPEN_URL, url);
  }

  /**
   * sets the click for run command.
   *
   * @param command the command to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withClickRunCommand(@NotNull final String command) {
    return this.withClick(ClickAction.RUN_COMMAND, command);
  }

  /**
   * sets the click for suggest command.
   *
   * @param command the command to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withClickSuggestCommand(@NotNull final String command) {
    return this.withClick(ClickAction.SUGGEST_COMMAND, command);
  }

  /**
   * sets the color.
   *
   * @param color the color to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withColor(@Nullable final TextColor color) {
    this.color = color;
    return this;
  }

  /**
   * sets the extra.
   *
   * @param components the components to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withExtra(@Nullable final List<ChatComponent> components) {
    this.extra = components;
    return this;
  }

  /**
   * sets the hover.
   *
   * @param action the action to set.
   * @param value the value to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withHover(@Nullable final HoverAction action, @Nullable final Object value) {
    this.hoverAction = action;
    this.hoverValue = value;
    return this;
  }

  /**
   * sets the hover for show entity.
   *
   * @param id the id to set.
   * @param type the type to set.
   * @param customName the custom name to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withHoverShowEntity(@NotNull final UUID id, @NotNull final String type,
                                           @NotNull final String customName) {
    return this.withHover(HoverAction.SHOW_ENTITY, String.format("{id:%s,type:%s,name:%s}", id, type, customName));
  }

  /**
   * sets the hover for show item.
   *
   * @param serializedItem the serialized item to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withHoverShowItem(@NotNull final String serializedItem) {
    return this.withHover(HoverAction.SHOW_ITEM, serializedItem);
  }

  /**
   * sets the hover for show text.
   *
   * @param text the text to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withHoverShowText(@NotNull final String text) {
    return this.withHoverShowText(ChatComponent.optimizedComponent(text));
  }

  /**
   * sets the hover for show text.
   *
   * @param text the text to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withHoverShowText(@NotNull final ChatComponent text) {
    return this.withHover(HoverAction.SHOW_TEXT, text);
  }

  /**
   * sets the italic.
   *
   * @param italic the italic to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withItalic(@Nullable final Boolean italic) {
    this.italic = italic;
    return this;
  }

  /**
   * sets the obfuscated.
   *
   * @param obfuscated the obfuscated to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withObfuscated(@Nullable final Boolean obfuscated) {
    this.obfuscated = obfuscated;
    return this;
  }

  /**
   * sets the strikethrough.
   *
   * @param strikethrough the strikethrough to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withStrikethrough(@Nullable final Boolean strikethrough) {
    this.strikethrough = strikethrough;
    return this;
  }

  /**
   * sets the text.
   *
   * @param text the text to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withText(@Nullable final String text) {
    this.text = text;
    return this;
  }

  /**
   * sets the underlined.
   *
   * @param underlined the underlined to set.
   *
   * @return {@code this} for builder chain.
   */
  @NotNull
  public ChatComponent withUnderlined(@Nullable final Boolean underlined) {
    this.underlined = underlined;
    return this;
  }

  /**
   * adds text to the builder.
   *
   * @param builder the builder to append.
   * @param previousFormatting the previous formatting to append.
   *
   * @return appended text.
   */
  @NotNull
  private String append(@NotNull final StringBuilder builder, @NotNull final String previousFormatting) {
    var formatting = previousFormatting;
    if (this.text != null) {
      formatting = this.getFormatting();
      if (!formatting.equals(previousFormatting)) {
        builder.append(formatting);
      }
      builder.append(this.text);
    }
    for (final var component : this.getExtra()) {
      formatting = component.append(builder, formatting);
    }
    return formatting;
  }

  /**
   * converts colors to legacy.
   */
  private void convertColorsToLegacy() {
    if (this.color != null) {
      this.color.setReturnLegacy(true);
    }
    this.getExtra().forEach(ChatComponent::convertColorsToLegacy);
    if (this.hoverValue instanceof ChatComponent) {
      ((ChatComponent) this.hoverValue).convertColorsToLegacy();
    }
  }

  /**
   * obtains the formatting.
   *
   * @return formatting.
   */
  @NotNull
  private String getFormatting() {
    final var builder = new StringBuilder();
    if (this.color != null) {
      if (this.color.getLegacyColor() == ChatFormat.WHITE) {
        builder.append(ChatFormat.RESET.getChatFormat());
      } else {
        builder.append(this.color.getLegacyColor().getChatFormat());
      }
    }
    this.putFormats(builder);
    return builder.toString();
  }

  /**
   * puts the formats into the builder.
   *
   * @param builder the builder to put.
   */
  private void putFormats(@NotNull final StringBuilder builder) {
    if (this.isBold()) {
      builder.append(ChatFormat.BOLD.getChatFormat());
    }
    if (this.isItalic()) {
      builder.append(ChatFormat.ITALIC.getChatFormat());
    }
    if (this.isUnderlined()) {
      builder.append(ChatFormat.UNDERLINE.getChatFormat());
    }
    if (this.isStrikethrough()) {
      builder.append(ChatFormat.STRIKETHROUGH.getChatFormat());
    }
    if (this.isObfuscated()) {
      builder.append(ChatFormat.OBFUSCATED.getChatFormat());
    }
  }

  /**
   * an enum class that contains click actions.
   */
  public enum ClickAction {
    /**
     * the open url.
     */
    OPEN_URL,
    /**
     * the run command.
     */
    RUN_COMMAND,
    /**
     * the change page.
     */
    CHANGE_PAGE,
    /**
     * the suggest command.
     */
    SUGGEST_COMMAND,
    /**
     * the copy to clipboard.
     */
    COPY_TO_CLIPBOARD
  }

  /**
   * an enum class that contains hover actions.
   */
  public enum HoverAction {
    /**
     * the show text.
     */
    SHOW_TEXT,
    /**
     * the show item.
     */
    SHOW_ITEM,
    /**
     * the show entity.
     */
    SHOW_ENTITY;

    /**
     * gets the hover action from type.
     *
     * @param type type to get.
     *
     * @return hover action.
     */
    @NotNull
    public static Optional<HoverAction> fromString(@NotNull final String type) {
      return Arrays.stream(HoverAction.values())
        .filter(action -> type.toUpperCase(Locale.ROOT).contains(action.toString()))
        .findFirst();
    }
  }
}
