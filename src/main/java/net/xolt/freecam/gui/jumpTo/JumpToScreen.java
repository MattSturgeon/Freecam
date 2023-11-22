package net.xolt.freecam.gui.jumpTo;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.util.FreeCamera;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

public class JumpToScreen extends Screen {
    private static final int GUI_WIDTH = 236;
    private static final int GUI_TOP = 50;
    private static final int LIST_TOP = GUI_TOP + 24;
    private static final int LIST_ITEM_HEIGHT = 36;
    private static final int GUI_BUTTON_ROW = 24;

    //FIXME do we need our own translation keys, or is this ok?
    private static final Text SEARCH_TEXT = Text.translatable("gui.socialInteractions.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
    static final Text EMPTY_SEARCH_TEXT = Text.translatable("gui.socialInteractions.search_empty").formatted(Formatting.GRAY);
    private static final Identifier SEARCH_ICON_TEXTURE = new Identifier("icon/search");

    private Tab tab = Tab.PLAYER;
    private ListWidget list;
    private boolean initialized;
    private ButtonWidget buttonJump;
    private CyclingButtonWidget<ModConfig.Perspective> buttonPerspective;
    private TextFieldWidget searchBox;
    private String currentSearch;

    public JumpToScreen() {
        super(Text.translatable("gui.freecam.jumpTo.title"));
    }

    @Override
    protected void init() {
        super.init();
        int listBottom = GUI_TOP + this.getGuiHeight() - GUI_BUTTON_ROW - 2;

        if (this.initialized) {
            this.list.updateSize(this.width, this.height, LIST_TOP, listBottom);
        } else {
            this.list = new ListWidget(this, this.client, this.width, this.height, LIST_TOP, listBottom, LIST_ITEM_HEIGHT);
        }

        int innerWidth = GUI_WIDTH - 10;
        int innerX = (this.width - innerWidth) / 2;

        String string = this.searchBox != null ? this.searchBox.getText() : "";
        this.searchBox = new TextFieldWidget(this.textRenderer, innerX + 20, GUI_TOP + 9,  this.list.getRowWidth() - 20, 15, SEARCH_TEXT){

            @Override
            protected MutableText getNarrationMessage() {
                if (!JumpToScreen.this.searchBox.getText().isEmpty() && JumpToScreen.this.list.children().isEmpty()) {
                    return super.getNarrationMessage().append(", ").append(EMPTY_SEARCH_TEXT);
                }
                return super.getNarrationMessage();
            }
        };
        this.searchBox.setMaxLength(16);
        this.searchBox.setVisible(true);
        this.searchBox.setEditableColor(0xFFFFFF);
        this.searchBox.setText(string);
        this.searchBox.setPlaceholder(SEARCH_TEXT);
        this.searchBox.setChangedListener(this::onSearchChange);

        SimplePositioningWidget positioner = new SimplePositioningWidget(innerX, listBottom, innerWidth, 0);
        positioner.getMainPositioner()
                .alignBottom()
                .alignRight();
        DirectionalLayoutWidget layout = positioner.add(DirectionalLayoutWidget.horizontal());
        layout.getMainPositioner()
                .alignBottom()
                .margin(2);

        layout.add(ButtonWidget.builder(Text.translatable("gui.freecam.jumpTo.button.back"), button -> this.close()).width(48).build());
        this.buttonPerspective = switch (tab) {
            case COORDS -> null;
            case PLAYER -> layout.add(CyclingButtonWidget
                    .builder(ModConfig.Perspective::getName)
                    .values(ModConfig.Perspective.values())
                    .initially(ModConfig.INSTANCE.hidden.jumpToPerspective)
                    .tooltip(value -> Tooltip.of(Text.translatable("gui.freecam.jumpTo.button.perspective.@Tooltip", value)))
                    .omitKeyText()
                    .build(0, 0, 80, 20, null, (button, value) -> {
                        ModConfig.INSTANCE.hidden.jumpToPerspective = value;
                        AutoConfig.getConfigHolder(ModConfig.class).save();
                    }));
        };
        this.buttonJump = layout.add(ButtonWidget.builder(Text.translatable("gui.freecam.jumpTo.button.jump"), button -> this.jump())
                        .tooltip(Tooltip.of(Text.translatable("gui.freecam.jumpTo.button.jump.@Tooltip")))
                        .width(48)
                        .build());


        positioner.refreshPositions();
        positioner.forEachChild(this::addDrawableChild);

        List.of(this.searchBox, this.list).forEach(this::addDrawableChild);
        this.setInitialFocus(this.list);

        this.initialized = true;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        super.renderBackground(context, mouseX, mouseY, delta);
        int left = (this.width - GUI_WIDTH) / 2;
        BackgroundTexture.render(context, left, GUI_TOP, GUI_WIDTH, this.getGuiHeight());
        context.drawGuiTexture(SEARCH_ICON_TEXTURE, left + 10, GUI_TOP + 11, 12, 12);
    }

    // GUI height
    private int getGuiHeight() {
        return Math.max(52, this.height - (GUI_TOP * 2));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.initialized) {
            this.updateEntries();
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.searchBox.isFocused()) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.focusOn(null);
                return true;
            }
        } else {
            if (Freecam.getJumpToBind().matchesKey(keyCode, scanCode)) {
                this.close();
                return true;
            }
        }
        if (this.list.getSelectedOrNull() != null) {
            if (KeyCodes.isToggle(keyCode)) {
                this.jump();
                return true;
            }
            if (this.list.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public void updateEntries() {
        List<ListEntry> entries = (switch (tab) {
            case PLAYER -> updatePlayerEntries();
            case COORDS -> updateCoordEntries();
        }).stream()
                .filter(entry -> this.currentSearch == null
                        || this.currentSearch.isEmpty()
                        || entry.matches(this.currentSearch))
                .sorted()
                .toList();

        // Update only if the list has changed
        if (!Objects.equals(this.list.children(), entries)) {
            this.list.updateEntries(entries);
            this.updateButtonState();
        }
    }

    private List<ListEntry> updateCoordEntries() {
        // TODO
        return Collections.emptyList();
    }

    private List<ListEntry> updatePlayerEntries() {
        // Store the existing entries in a UUID map for easy lookup
        Map<UUID, PlayerListEntry> currentEntries = this.list.children()
                .parallelStream()
                .filter(PlayerListEntry.class::isInstance)
                .map(PlayerListEntry.class::cast)
                .collect(Collectors.toUnmodifiableMap(PlayerListEntry::getUUID, entry -> entry));

        // Map the in-range players into PlayerListEntries
        // Use existing entries if possible
        return this.client.world.getPlayers()
                .parallelStream()
                .filter(player -> !(player instanceof FreeCamera))
                .map(player -> {
                    PlayerListEntry entry = currentEntries.get(player.getUuid());
                    if (entry == null) {
                        return new PlayerListEntry(this.client, this, player);
                    } else {
                        entry.update(player);
                        return entry;
                    }
                })
                .map(ListEntry.class::cast)
                .toList();
    }

    private void onSearchChange(String search) {
        String newSearch = search.toLowerCase(Locale.ROOT);
        if (!newSearch.equals(this.currentSearch)) {
            this.currentSearch = newSearch;
        }
    }

    public void select(ListEntry entry) {
        this.list.setSelected(entry);
        this.updateButtonState();
    }

    public void updateButtonState() {
        ListEntry selected = this.list.getSelectedOrNull();
        this.buttonJump.active = selected != null;
    }

    public void jump() {
        Optional.ofNullable(this.list.getSelectedOrNull())
                .ifPresent(listEntry -> {
                    boolean perspective = this.buttonPerspective != null && this.buttonPerspective.active;
                    this.close();
                    Freecam.jumpTo(listEntry.getPosition(), listEntry.getName(), perspective);
                });
    }

    private enum Tab {
        PLAYER, COORDS;
    }
}
