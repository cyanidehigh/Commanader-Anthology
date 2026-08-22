package com.commanderanthology.game.gdx;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.commanderanthology.core.commandersim.CardKind;
import com.commanderanthology.core.commandersim.GameFoundation;
import com.commanderanthology.core.commandersim.GameObject;
import com.commanderanthology.core.commandersim.Player;
import com.commanderanthology.core.commandersim.ZoneType;

import java.util.ArrayList;
import java.util.List;

final class CommanderPlaymatApplication extends ApplicationAdapter {
    private static final float WORLD_WIDTH = 1600f;
    private static final float WORLD_HEIGHT = 900f;
    private static final float RAIL_X = 16f;
    private static final float RAIL_Y = 18f;
    private static final float RAIL_W = 318f;
    private static final float RAIL_H = 854f;
    private static final float TABLE_X = 356f;
    private static final float TABLE_Y = 28f;
    private static final float TABLE_W = 1226f;
    private static final float TABLE_H = 844f;
    private static final Color BACKGROUND = new Color(0.025f, 0.022f, 0.018f, 1f);
    private static final Color MAT = new Color(0.23f, 0.30f, 0.16f, 1f);
    private static final Color MAT_INNER = new Color(0.12f, 0.18f, 0.10f, 0.82f);
    private static final Color PANEL = new Color(0.12f, 0.105f, 0.08f, 0.88f);
    private static final Color PANEL_LIGHT = new Color(0.90f, 0.84f, 0.70f, 0.96f);
    private static final Color CARD = new Color(0.055f, 0.045f, 0.035f, 1f);
    private static final Color CARD_BACK = new Color(0.12f, 0.05f, 0.025f, 1f);
    private static final Color GOLD = new Color(0.88f, 0.64f, 0.16f, 1f);
    private static final Color SOFT_GOLD = new Color(0.72f, 0.58f, 0.34f, 1f);
    private static final Color TEXT = new Color(0.96f, 0.91f, 0.80f, 1f);
    private static final Color MUTED = new Color(0.78f, 0.70f, 0.56f, 1f);

    private final GameFoundation game;
    private final String mode;
    private final String playerDeckName;
    private final String opponentDeckName;
    private OrthographicCamera camera;
    private FitViewport viewport;
    private ShapeRenderer shapes;
    private SpriteBatch batch;
    private BitmapFont font;
    private GlyphLayout layout;
    private GameCardArtRepository cardArt;
    private final ArrayList<RenderedCard> renderedCards = new ArrayList<>();
    private String hoveredCardName;
    private float previewZoom = 1f;

    CommanderPlaymatApplication(GameFoundation game, String mode, String playerDeckName, String opponentDeckName) {
        this.game = game;
        this.mode = mode;
        this.playerDeckName = playerDeckName;
        this.opponentDeckName = opponentDeckName;
    }

    @Override
    public void create() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        shapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = loadHudFont();
        layout = new GlyphLayout();
        cardArt = new GameCardArtRepository();
        Gdx.input.setInputProcessor(new InputMultiplexer(new InputAdapter() {
            @Override
            public boolean scrolled(float amountX, float amountY) {
                if (hoveredCardName == null) {
                    return false;
                }
                previewZoom = clamp(previewZoom - amountY * 0.12f, 0.75f, 1.75f);
                return true;
            }
        }));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(BACKGROUND.r, BACKGROUND.g, BACKGROUND.b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        shapes.setProjectionMatrix(camera.combined);
        batch.setProjectionMatrix(camera.combined);

        renderedCards.clear();
        drawPlaymat();
        drawHeader();
        drawTable();
        drawRail();
        drawHoverPreview();
    }

    private void drawPlaymat() {
        fill(0f, 0f, WORLD_WIDTH, WORLD_HEIGHT, BACKGROUND);
        fill(RAIL_X, RAIL_Y, RAIL_W, RAIL_H, PANEL);
        outline(RAIL_X, RAIL_Y, RAIL_W, RAIL_H, SOFT_GOLD);
        fill(TABLE_X, TABLE_Y, TABLE_W, TABLE_H, MAT);
        outline(TABLE_X, TABLE_Y, TABLE_W, TABLE_H, SOFT_GOLD);
    }

    private void drawHeader() {
        batch.begin();
        drawTextSized(mode + " | " + shortName(playerDeckName, 34) + " vs " + shortName(opponentDeckName, 34), 24f, 892f, TEXT, 1.36f);
        drawRightTextSized("Turn " + game.turnNumber() + " - " + stepName(), 1574f, 892f, GOLD, 1.36f);
        batch.end();
    }

    private void drawTable() {
        Player opponent = game.players().get(game.playerOrder().get(1));
        Player player = game.players().get(game.playerOrder().get(0));

        drawZonePanel("Opponent Hand", TABLE_X + 16f, 792f, TABLE_W - 32f, 66f);
        drawOpponentHand(opponent, TABLE_X + 28f, 800f);

        drawZonePanel("Opponent Lands", TABLE_X + 16f, 702f, TABLE_W - 32f, 78f);
        drawLands(opponent, TABLE_X + 42f, 720f, false);
        drawZonePanel("Opponent Battlefield", TABLE_X + 16f, 498f, TABLE_W - 32f, 190f);
        drawBattlefield(opponent, TABLE_X + 42f, 528f, false);

        drawZonePanel("Player Battlefield", TABLE_X + 16f, 292f, TABLE_W - 32f, 190f);
        drawBattlefield(player, TABLE_X + 42f, 322f, true);
        drawZonePanel("Player Lands", TABLE_X + 16f, 202f, TABLE_W - 32f, 78f);
        drawLands(player, TABLE_X + 42f, 220f, true);

        drawZonePanel("Player Hand", TABLE_X + 16f, 42f, TABLE_W - 32f, 148f);
        drawPlayerHand(player, TABLE_X + 34f, 56f, TABLE_W - 68f);
    }

    private void drawRail() {
        Player opponent = game.players().get(game.playerOrder().get(1));
        Player player = game.players().get(game.playerOrder().get(0));

        drawPlayerHud(opponent, opponentDeckName, RAIL_X + 14f, 754f, true);
        drawZoneCountPanel(opponent, RAIL_X + 14f, 665f);
        drawCommandPanel(opponent, RAIL_X + 14f, 534f);
        drawPriorityPanel(RAIL_X + 14f, 373f);
        drawCommandPanel(player, RAIL_X + 14f, 242f);
        drawZoneCountPanel(player, RAIL_X + 14f, 153f);
        drawPlayerHud(player, playerDeckName, RAIL_X + 14f, 34f, false);
    }

    private void drawPlayerHud(Player player, String deckName, float x, float y, boolean opponent) {
        fill(x, y, RAIL_W - 28f, 104f, PANEL_LIGHT);
        outline(x, y, RAIL_W - 28f, 104f, GOLD);
        fill(x + 10f, y + 12f, 72f, 80f, opponent ? CARD_BACK : CARD);
        outline(x + 10f, y + 12f, 72f, 80f, GOLD);
        batch.begin();
        drawTextSized(opponent ? "OPPONENT" : "PLAYER", x + 94f, y + 88f, new Color(0.10f, 0.08f, 0.04f, 1f), 0.94f);
        drawTextSized(shortName(deckName, 17), x + 94f, y + 66f, new Color(0.10f, 0.08f, 0.04f, 1f), 0.82f);
        drawTextSized("LIFE", x + 94f, y + 38f, new Color(0.35f, 0.02f, 0.02f, 1f), 1.00f);
        drawRightTextSized(String.valueOf(player.life()), x + RAIL_W - 42f, y + 51f, new Color(0.35f, 0.02f, 0.02f, 1f), 1.92f);
        drawTextSized(count(player, ZoneType.HAND) + " hand", x + 94f, y + 17f, new Color(0.10f, 0.08f, 0.04f, 1f), 0.86f);
        batch.end();
    }

    private void drawPriorityPanel(float x, float y) {
        fill(x, y, RAIL_W - 28f, 146f, PANEL);
        outline(x, y, RAIL_W - 28f, 146f, SOFT_GOLD);
        batch.begin();
        drawTextSized("Stack", x + 12f, y + 124f, TEXT, 0.98f);
        drawTextSized("Empty", x + 12f, y + 97f, MUTED, 0.88f);
        drawTextSized("Turn " + game.turnNumber(), x + 12f, y + 68f, GOLD, 0.88f);
        drawTextSized(stepName(), x + 96f, y + 68f, TEXT, 0.78f);
        batch.end();
        drawButton("No response", x + 12f, y + 12f, 146f, 36f);
        drawButton("Next", x + 172f, y + 12f, 94f, 36f);
    }

    private void drawOpponentHand(Player player, float x, float y) {
        int count = count(player, ZoneType.HAND);
        float width = 44f;
        for (int index = 0; index < count; index++) {
            drawCardBack(x + index * 34f, y, width, 54f);
        }
        batch.begin();
        drawRightTextSized(count + " cards", TABLE_X + TABLE_W - 38f, y + 35f, TEXT, 1f);
        batch.end();
    }

    private void drawPlayerHand(Player player, float x, float y, float width) {
        List<GameObject> hand = game.objectsInZone(player.zoneIds().get(ZoneType.HAND));
        float cardW = 92f;
        float cardH = 128f;
        float gap = 12f;
        float total = hand.size() * cardW + Math.max(0, hand.size() - 1) * gap;
        if (total > width) {
            gap = -Math.min(34f, (total - width) / Math.max(1, hand.size() - 1));
            total = hand.size() * cardW + Math.max(0, hand.size() - 1) * gap;
        }
        float startX = x + (width - total) / 2f;
        for (int index = 0; index < hand.size(); index++) {
            drawCard(hand.get(index).name(), startX + index * (cardW + gap), y, cardW, cardH, true);
        }
    }

    private void drawZoneCountPanel(Player player, float x, float y) {
        fill(x, y, RAIL_W - 28f, 74f, PANEL);
        outline(x, y, RAIL_W - 28f, 74f, SOFT_GOLD);
        drawPile("Exile", countOwnedShared(player.playerId(), ZoneType.EXILE), x + 10f, y + 12f, 78f, 50f);
        drawPile("Deck", count(player, ZoneType.LIBRARY), x + 106f, y + 12f, 78f, 50f);
        drawPile("Grave", count(player, ZoneType.GRAVEYARD), x + 202f, y + 12f, 78f, 50f);
    }

    private void drawCommandPanel(Player player, float x, float y) {
        List<GameObject> commanders = game.objectsInZone(game.sharedZoneIds().get(ZoneType.COMMAND)).stream()
                .filter(object -> object.ownerId() == player.playerId())
                .toList();
        fill(x, y, RAIL_W - 28f, 116f, PANEL);
        outline(x, y, RAIL_W - 28f, 116f, SOFT_GOLD);
        batch.begin();
        drawTextSized("Command", x + 10f, y + 100f, TEXT, 1.06f);
        batch.end();
        for (int index = 0; index < 2; index++) {
            float slotX = x + 12f + index * 88f;
            if (index < commanders.size()) {
                drawCard(commanders.get(index).name(), slotX, y + 14f, 72f, 62f, false);
            } else {
                drawEmptyCard(slotX, y + 14f, 72f, 62f);
            }
        }
        batch.begin();
        drawTextSized("Tax", x + 198f, y + 66f, MUTED, 0.70f);
        drawTextSized("0", x + 248f, y + 66f, GOLD, 0.94f);
        drawTextSized("Tax", x + 198f, y + 38f, MUTED, 0.70f);
        drawTextSized("0", x + 248f, y + 38f, GOLD, 0.94f);
        batch.end();
    }

    private void drawBattlefield(Player player, float x, float y, boolean playerSide) {
        List<GameObject> permanents = battlefieldObjects(player, false);
        if (permanents.isEmpty()) {
            batch.begin();
            drawCenteredTextSized("Empty battlefield", x, y + 88f, 1130f, MUTED, 1.12f);
            batch.end();
            return;
        }
        drawCardRow(permanents, x, y, 1130f, 118f, playerSide);
    }

    private void drawLands(Player player, float x, float y, boolean playerSide) {
        List<GameObject> lands = battlefieldObjects(player, true);
        if (lands.isEmpty()) {
            batch.begin();
            drawTextSized("Lands - 0", x, y + 28f, MUTED, 1.00f);
            batch.end();
            return;
        }
        drawCardRow(lands, x, y, 1130f, 58f, false);
    }

    private void drawCardRow(List<GameObject> cards, float x, float y, float maxWidth, float cardHeight, boolean names) {
        float cardW = cardHeight * 0.714f;
        float gap = 10f;
        if (cards.size() * (cardW + gap) > maxWidth) {
            gap = -Math.min(cardW * 0.55f, (cards.size() * cardW - maxWidth) / Math.max(1, cards.size() - 1));
        }
        for (int index = 0; index < cards.size(); index++) {
            drawCard(names ? cards.get(index).name() : "", x + index * (cardW + gap), y, cardW, cardHeight, names);
        }
    }

    private void drawZonePanel(String label, float x, float y, float width, float height) {
        fill(x, y, width, height, MAT_INNER);
        outline(x, y, width, height, new Color(SOFT_GOLD.r, SOFT_GOLD.g, SOFT_GOLD.b, 0.50f));
        batch.begin();
        drawTextSized(label, x + 12f, y + height - 14f, MUTED, 0.92f);
        batch.end();
    }

    private void drawPile(String label, int count, float x, float y, float width, float height) {
        fill(x, y, width, height, PANEL);
        outline(x, y, width, height, SOFT_GOLD);
        batch.begin();
        drawCenteredTextSized(label, x, y + height - 8f, width, TEXT, 0.82f);
        drawCenteredTextSized(String.valueOf(count), x, y + 20f, width, TEXT, 1.18f);
        batch.end();
    }

    private void drawCard(String label, float x, float y, float width, float height, boolean showLabel) {
        fill(x, y, width, height, CARD);
        outline(x, y, width, height, GOLD);
        Texture art = label == null || label.isBlank() ? null : cardArt.textureFor(label);
        if (art == null) {
            fill(x + 5f, y + height * 0.38f, width - 10f, height * 0.54f, new Color(0.20f, 0.15f, 0.10f, 1f));
            if (showLabel && width >= 82f) {
                fill(x + 6f, y + 8f, width - 12f, 40f, new Color(0.18f, 0.12f, 0.07f, 0.96f));
                batch.begin();
                drawWrappedSized(shortName(label, 18), x + 8f, y + 40f, width - 16f, TEXT, 0.70f);
                batch.end();
            }
        } else {
            batch.begin();
            drawTextureFit(art, x + 5f, y + 5f, width - 10f, height - 10f);
            batch.end();
            renderedCards.add(new RenderedCard(label, art, new Rectangle(x, y, width, height)));
        }
    }

    private void drawEmptyCard(float x, float y, float width, float height) {
        outline(x, y, width, height, SOFT_GOLD);
    }

    private void drawCardBack(float x, float y, float width, float height) {
        fill(x, y, width, height, CARD_BACK);
        outline(x, y, width, height, GOLD);
        batch.begin();
        drawCenteredTextSized("CA", x, y + height * 0.60f, width, GOLD, 0.82f);
        batch.end();
    }

    private void drawButton(String label, float x, float y, float width, float height) {
        fill(x, y, width, height, new Color(0.20f, 0.14f, 0.06f, 1f));
        outline(x, y, width, height, GOLD);
        batch.begin();
        drawCenteredTextSized(label, x, y + 25f, width, TEXT, 1.00f);
        batch.end();
    }

    private void drawHoverPreview() {
        if (renderedCards.isEmpty()) {
            return;
        }
        Vector3 pointer = viewport.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0f));
        RenderedCard hovered = null;
        for (int index = renderedCards.size() - 1; index >= 0; index--) {
            RenderedCard card = renderedCards.get(index);
            if (card.bounds().contains(pointer.x, pointer.y)) {
                hovered = card;
                break;
            }
        }
        if (hovered == null) {
            hoveredCardName = null;
            previewZoom = 1f;
            return;
        }
        if (!hovered.label().equals(hoveredCardName)) {
            hoveredCardName = hovered.label();
            previewZoom = 1f;
        }

        float previewW = 286f * previewZoom;
        float previewH = 400f * previewZoom;
        float x = pointer.x + 24f;
        float y = pointer.y - previewH / 2f;
        if (x + previewW > WORLD_WIDTH - 18f) {
            x = pointer.x - previewW - 24f;
        }
        if (y < 18f) {
            y = 18f;
        }
        if (y + previewH > WORLD_HEIGHT - 18f) {
            y = WORLD_HEIGHT - previewH - 18f;
        }

        Texture previewTexture = cardArt.previewTextureFor(hovered.label());
        if (previewTexture == null) {
            previewTexture = hovered.texture();
        }
        fill(x - 8f, y - 8f, previewW + 16f, previewH + 16f, new Color(0.02f, 0.018f, 0.014f, 0.96f));
        outline(x - 8f, y - 8f, previewW + 16f, previewH + 16f, GOLD);
        batch.begin();
        drawTextureFit(previewTexture, x, y, previewW, previewH);
        batch.end();
    }

    private void drawTextureFit(Texture texture, float x, float y, float width, float height) {
        float textureAspect = (float) texture.getWidth() / (float) texture.getHeight();
        float frameAspect = width / height;
        float drawW = width;
        float drawH = height;
        if (textureAspect > frameAspect) {
            drawH = width / textureAspect;
        } else {
            drawW = height * textureAspect;
        }
        float drawX = x + (width - drawW) / 2f;
        float drawY = y + (height - drawH) / 2f;
        batch.draw(texture, drawX, drawY, drawW, drawH);
    }

    private String stepName() {
        return game.currentStep().name().toLowerCase().replace('_', ' ');
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private BitmapFont loadHudFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/beleren-bold_P1.01.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 18;
        parameter.color = TEXT;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.minFilter = Texture.TextureFilter.Linear;
        BitmapFont generated = generator.generateFont(parameter);
        generator.dispose();
        generated.getData().setScale(1f);
        return generated;
    }

    private record RenderedCard(String label, Texture texture, Rectangle bounds) {
    }

    private String shortName(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        int comma = trimmed.indexOf(',');
        if (comma > 0 && comma <= maxLength) {
            return trimmed.substring(0, comma);
        }
        int cut = trimmed.lastIndexOf(' ', maxLength - 1);
        if (cut < maxLength / 2) {
            cut = maxLength - 1;
        }
        return trimmed.substring(0, cut).trim() + "...";
    }

    private List<GameObject> battlefieldObjects(Player player, boolean lands) {
        return game.objectsInZone(game.sharedZoneIds().get(ZoneType.BATTLEFIELD)).stream()
                .filter(object -> object.controllerId() == player.playerId())
                .filter(object -> (object.cardKind().orElse(null) == CardKind.LAND) == lands)
                .toList();
    }

    private int count(Player player, ZoneType zoneType) {
        return game.zones().get(player.zoneIds().get(zoneType)).objectIds().size();
    }

    private int countOwnedShared(int playerId, ZoneType zoneType) {
        return (int) game.objectsInZone(game.sharedZoneIds().get(zoneType)).stream()
                .filter(object -> object.ownerId() == playerId)
                .count();
    }

    private void fill(float x, float y, float width, float height, Color color) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(color);
        shapes.rect(x, y, width, height);
        shapes.end();
    }

    private void outline(float x, float y, float width, float height, Color color) {
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(color);
        shapes.rect(x, y, width, height);
        shapes.end();
    }

    private void line(float x1, float y1, float x2, float y2, Color color) {
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(color);
        shapes.line(x1, y1, x2, y2);
        shapes.end();
    }

    private void drawText(String text, float x, float y, Color color) {
        font.setColor(color);
        font.draw(batch, text, x, y);
    }

    private void drawTextSized(String text, float x, float y, Color color, float scale) {
        float previousScaleX = font.getData().scaleX;
        float previousScaleY = font.getData().scaleY;
        font.getData().setScale(scale);
        drawText(text, x, y, color);
        font.getData().setScale(previousScaleX, previousScaleY);
    }

    private void drawRightText(String text, float x, float y, Color color) {
        layout.setText(font, text);
        drawText(text, x - layout.width, y, color);
    }

    private void drawRightTextSized(String text, float x, float y, Color color, float scale) {
        float previousScaleX = font.getData().scaleX;
        float previousScaleY = font.getData().scaleY;
        font.getData().setScale(scale);
        layout.setText(font, text);
        drawText(text, x - layout.width, y, color);
        font.getData().setScale(previousScaleX, previousScaleY);
    }

    private void drawCenteredText(String text, float x, float y, float width, Color color) {
        layout.setText(font, text);
        drawText(text, x + (width - layout.width) / 2f, y, color);
    }

    private void drawCenteredTextSized(String text, float x, float y, float width, Color color, float scale) {
        float previousScaleX = font.getData().scaleX;
        float previousScaleY = font.getData().scaleY;
        font.getData().setScale(scale);
        layout.setText(font, text);
        drawText(text, x + (width - layout.width) / 2f, y, color);
        font.getData().setScale(previousScaleX, previousScaleY);
    }

    private void drawWrapped(String text, float x, float y, float width, Color color) {
        font.setColor(color);
        font.draw(batch, text, x, y, width, 1, true);
    }

    private void drawWrappedSized(String text, float x, float y, float width, Color color, float scale) {
        float previousScaleX = font.getData().scaleX;
        float previousScaleY = font.getData().scaleY;
        font.getData().setScale(scale);
        drawWrapped(text, x, y, width, color);
        font.getData().setScale(previousScaleX, previousScaleY);
    }

    @Override
    public void dispose() {
        shapes.dispose();
        batch.dispose();
        font.dispose();
        cardArt.dispose();
    }
}
