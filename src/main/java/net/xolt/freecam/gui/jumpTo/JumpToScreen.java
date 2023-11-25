package net.xolt.freecam.gui.jumpTo;

import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.client.input.KeyCodes;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.xolt.freecam.Freecam;
import net.xolt.freecam.config.ModConfig;
import net.xolt.freecam.gui.textures.Texture;
import net.xolt.freecam.util.FreeCamera;
import org.lwjgl.glfw.GLFW;

import java.util.*;
import java.util.stream.Collectors;

public class JumpToScreen extends Screen {
    private static final int GUI_WIDTH = 236;
    private static final int GUI_TOP = 50;
    private static final int LIST_TOP = GUI_TOP + 8;
    private static final int LIST_ITEM_HEIGHT = 36;
    private static final Identifier SEARCH_ICON_TEXTURE = new Identifier("icon/search");
    private static final Text SEARCH_TEXT = Text.translatable("gui.recipebook.search_hint").formatted(Formatting.ITALIC).formatted(Formatting.GRAY);
    private static final Texture JUMP_BACKGROUND = Texture.getTexture(new Identifier(Freecam.ID, "textures/gui/jump_background.png"));
    private static final Texture JUMP_LIST_BACKGROUND = Texture.getTexture(new Identifier(Freecam.ID, "textures/gui/jump_list_background.png"));

    private Tab tab = Tab.PLAYER;
    private ListWidget list;
    private boolean initialized;
    private ButtonWidget buttonBack;
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

        if (!this.initialized) {
            this.list = new ListWidget(this, this.client, this.width, this.height, 0, 0, LIST_ITEM_HEIGHT);
            this.searchBox = new TextFieldWidget(this.textRenderer, 0, 15, SEARCH_TEXT);
            this.searchBox.setPlaceholder(SEARCH_TEXT);
            this.searchBox.setMaxLength(16);
            this.searchBox.setVisible(true);
            this.searchBox.setEditableColor(0xFFFFFF);
            this.searchBox.setChangedListener(this::onSearchChange);

            this.buttonJump = ButtonWidget.builder(Text.translatable("gui.freecam.jumpTo.button.jump"), button -> this.jump())
                    .tooltip(Tooltip.of(Text.translatable("gui.freecam.jumpTo.button.jump.@Tooltip")))
                    .width(48)
                    .build();

            this.buttonPerspective = CyclingButtonWidget
                    .builder(ModConfig.Perspective::getName)
                    .values(ModConfig.Perspective.values())
                    .initially(ModConfig.INSTANCE.hidden.jumpToPerspective)
                    .tooltip(value -> Tooltip.of(Text.translatable("gui.freecam.jumpTo.button.perspective.@Tooltip", value)))
                    .omitKeyText()
                    .build(0, 0, 80, 20, null, (button, value) -> {
                        ModConfig.INSTANCE.hidden.jumpToPerspective = value;
                        AutoConfig.getConfigHolder(ModConfig.class).save();
                    });

            this.buttonBack = ButtonWidget.builder(ScreenTexts.BACK, button -> this.close()).width(48).build();
        }

        int listTop = LIST_TOP + 16;
        int listBottom = this.getListBottom();
        int innerWidth = GUI_WIDTH - 10;
        int innerX = (this.width - innerWidth) / 2;

        this.list.updateSize(this.width, this.height, listTop, listBottom);
        this.searchBox.setPosition(innerX + 20, LIST_TOP + 1);
        this.searchBox.setWidth(this.list.getRowWidth() - 19);

        SimplePositioningWidget positioner = new SimplePositioningWidget(innerX, listBottom + 3, innerWidth, 0);
        positioner.getMainPositioner()
                .alignBottom()
                .alignRight();
        DirectionalLayoutWidget layout = positioner.add(DirectionalLayoutWidget.horizontal());
        layout.getMainPositioner()
                .alignBottom()
                .marginX(2);

        layout.add(this.buttonBack);
        if (tab == Tab.PLAYER) {
            layout.add(this.buttonPerspective);
        }
        layout.add(this.buttonJump);

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
        JUMP_BACKGROUND.draw(context, left, GUI_TOP, GUI_WIDTH, this.getGuiHeight());
        JUMP_LIST_BACKGROUND.draw(context, left + 7, LIST_TOP - 1, this.list.getRowWidth() + 2, this.getListHeight() + 2);
        context.drawGuiTexture(SEARCH_ICON_TEXTURE, left + 10, LIST_TOP + 3, 12, 12);
    }

    // GUI height
    private int getGuiHeight() {
        return Math.max(52, this.height - (GUI_TOP * 2));
    }

    // List height including search bar
    private int getListHeight() {
        return this.getGuiHeight() - 29 - 8;
    }

    private int getListBottom() {
        return LIST_TOP + this.getListHeight();
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
        this.currentSearch = search.toLowerCase(Locale.ROOT);
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
