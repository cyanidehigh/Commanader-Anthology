package com.commanderanthology.core.commandersim;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.Set;

public final class GameFoundation {
    private final LinkedHashMap<Integer, Player> players = new LinkedHashMap<>();
    private final ArrayList<Integer> playerOrder = new ArrayList<>();
    private final LinkedHashMap<Integer, Zone> zones = new LinkedHashMap<>();
    private final EnumMap<ZoneType, Integer> sharedZoneIds = new EnumMap<>(ZoneType.class);
    private final LinkedHashMap<Integer, GameObject> objects = new LinkedHashMap<>();
    private final HashMap<Integer, StackResolution> stackResolutions = new HashMap<>();
    private int nextPlayerId = 1;
    private int nextZoneId = 1;
    private int nextObjectId = 1;
    private int nextEntityId = 1;
    private int turnNumber = 1;
    private OptionalInt activePlayerId = OptionalInt.empty();
    private TurnStep currentStep = TurnStep.UNTAP;
    private OptionalInt priorityPlayerId = OptionalInt.empty();
    private int consecutivePasses;

    public static GameFoundation buildGame(List<String> playerNames) {
        return buildGame(playerNames, 40);
    }

    public static GameFoundation buildGame(List<String> playerNames, int startingLife) {
        GameFoundation game = new GameFoundation();
        for (ZoneType zoneType : List.of(ZoneType.BATTLEFIELD, ZoneType.STACK, ZoneType.EXILE, ZoneType.COMMAND)) {
            Zone zone = game.addZone(zoneType, OptionalInt.empty());
            game.sharedZoneIds.put(zoneType, zone.zoneId());
        }
        for (String playerName : playerNames) {
            game.addPlayer(playerName, startingLife);
        }
        return game;
    }

    public Player addPlayer(String name) {
        return addPlayer(name, 40);
    }

    public Player addPlayer(String name, int life) {
        String normalizedName = requireText(name, "Player name");
        if (life <= 0) {
            throw new IllegalArgumentException("Starting life must be positive.");
        }

        Player player = new Player(nextPlayerId++, normalizedName, life);
        players.put(player.playerId(), player);
        playerOrder.add(player.playerId());
        for (ZoneType zoneType : List.of(ZoneType.LIBRARY, ZoneType.HAND, ZoneType.GRAVEYARD)) {
            Zone zone = addZone(zoneType, OptionalInt.of(player.playerId()));
            player.putZone(zoneType, zone.zoneId());
        }
        if (activePlayerId.isEmpty()) {
            activePlayerId = OptionalInt.of(player.playerId());
        }
        return player;
    }

    public GameObject addObject(
            String name,
            ObjectType objectType,
            int ownerId,
            int zoneId,
            Optional<CardKind> cardKind,
            int manaCost,
            int power,
            int toughness
    ) {
        requirePlayer(ownerId);
        requireZone(zoneId);
        if (manaCost < 0 || power < 0 || toughness < 0) {
            throw new IllegalArgumentException("Mana cost, power, and toughness cannot be negative.");
        }
        GameObject gameObject = new GameObject(
                nextObjectId++,
                nextEntityId++,
                requireText(name, "Object name"),
                Objects.requireNonNull(objectType, "objectType"),
                ownerId,
                ownerId,
                zoneId,
                Objects.requireNonNull(cardKind, "cardKind"),
                manaCost,
                power,
                toughness,
                false,
                false,
                false,
                false
        );
        objects.put(gameObject.objectId(), gameObject);
        zones.get(zoneId).addObject(gameObject.objectId());
        return gameObject;
    }

    public GameObject addCommander(
            String name,
            int ownerId,
            Optional<CardKind> cardKind,
            int manaCost,
            int power,
            int toughness
    ) {
        requirePlayer(ownerId);
        if (manaCost < 0 || power < 0 || toughness < 0) {
            throw new IllegalArgumentException("Mana cost, power, and toughness cannot be negative.");
        }
        int commandZoneId = sharedZoneIds.get(ZoneType.COMMAND);
        GameObject gameObject = new GameObject(
                nextObjectId++,
                nextEntityId++,
                requireText(name, "Commander name"),
                ObjectType.CARD,
                ownerId,
                ownerId,
                commandZoneId,
                Objects.requireNonNull(cardKind, "cardKind"),
                manaCost,
                power,
                toughness,
                false,
                false,
                true,
                true
        );
        objects.put(gameObject.objectId(), gameObject);
        zones.get(commandZoneId).addObject(gameObject.objectId());
        return gameObject;
    }

    public GameObject moveObject(int objectId, int destinationZoneId, ObjectType objectType, int controllerId) {
        GameObject gameObject = requireObject(objectId);
        Zone origin = zones.get(gameObject.zoneId());
        Zone destination = requireZone(destinationZoneId);
        requirePlayer(controllerId);
        if (origin.zoneId() == destination.zoneId()) {
            return gameObject;
        }

        origin.removeObject(objectId);
        destination.addObject(objectId);
        boolean summoningSick = destination.zoneType() == ZoneType.BATTLEFIELD
                && gameObject.cardKind().orElse(null) == CardKind.CREATURE;
        gameObject.moveTo(destinationZoneId, objectType, false, summoningSick);
        stackResolutions.remove(objectId);
        return gameObject;
    }

    public GameObject drawCard(int playerId) {
        Player player = requirePlayer(playerId);
        List<GameObject> library = objectsInZone(player.zoneId(ZoneType.LIBRARY));
        if (library.isEmpty()) {
            throw new IllegalStateException("Cannot draw from an empty library.");
        }
        GameObject topCard = library.get(library.size() - 1);
        return moveObject(topCard.objectId(), player.zoneId(ZoneType.HAND), ObjectType.CARD, playerId);
    }

    public List<GameObject> drawCards(int playerId, int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Draw count cannot be negative.");
        }
        ArrayList<GameObject> drawn = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            drawn.add(drawCard(playerId));
        }
        return List.copyOf(drawn);
    }

    public void shuffleLibrary(int playerId, Random random) {
        Player player = requirePlayer(playerId);
        Objects.requireNonNull(random, "random");
        zones.get(player.zoneId(ZoneType.LIBRARY)).shuffle(random);
        validate();
    }

    public void prepareOpeningHands(Random random, int handSize) {
        Objects.requireNonNull(random, "random");
        if (handSize < 0) {
            throw new IllegalArgumentException("Opening hand size cannot be negative.");
        }
        requireNoPriority();
        for (int playerId : playerOrder) {
            shuffleLibrary(playerId, random);
        }
        for (int playerId : playerOrder) {
            Player player = players.get(playerId);
            int availableCards = objectsInZone(player.zoneId(ZoneType.LIBRARY)).size();
            drawCards(playerId, Math.min(handSize, availableCards));
        }
        validate();
    }

    public TurnStep advanceStep() {
        requireNoPriority();
        if (!stackObjectIds().isEmpty()) {
            throw new IllegalStateException("Cannot advance while the stack is not empty.");
        }
        players.values().forEach(Player::emptyManaPool);

        if (currentStep == TurnStep.END) {
            advanceTurn();
            currentStep = TurnStep.UNTAP;
            performUntapStep();
            return currentStep;
        }

        int currentIndex = TurnStep.SEQUENCE.indexOf(currentStep);
        if (currentIndex < 0 || currentIndex + 1 >= TurnStep.SEQUENCE.size()) {
            throw new IllegalStateException("Invalid turn step.");
        }
        currentStep = TurnStep.SEQUENCE.get(currentIndex + 1);
        if (currentStep == TurnStep.DRAW) {
            performDrawStep();
        }
        if (currentStep != TurnStep.UNTAP) {
            openPriority();
        }
        return currentStep;
    }

    public int openPriority() {
        if (currentStep == TurnStep.UNTAP) {
            throw new IllegalStateException("Players do not receive priority during untap.");
        }
        if (priorityPlayerId.isPresent()) {
            throw new IllegalStateException("A priority window is already open.");
        }
        int first = firstEligibleFrom(activePlayerId.orElseThrow());
        priorityPlayerId = OptionalInt.of(first);
        consecutivePasses = 0;
        return first;
    }

    public PriorityResult passPriority(int playerId) {
        if (priorityPlayerId.isEmpty()) {
            throw new IllegalStateException("No priority window is open.");
        }
        if (priorityPlayerId.getAsInt() != playerId) {
            throw new IllegalArgumentException("Only the player with priority may pass.");
        }
        List<Integer> eligible = eligiblePriorityPlayers();
        consecutivePasses += 1;
        if (consecutivePasses < eligible.size()) {
            int currentIndex = eligible.indexOf(playerId);
            int nextPlayer = eligible.get((currentIndex + 1) % eligible.size());
            priorityPlayerId = OptionalInt.of(nextPlayer);
            return PriorityResult.passed(nextPlayer);
        }

        consecutivePasses = 0;
        List<Integer> stack = stackObjectIds();
        if (!stack.isEmpty()) {
            int resolved = resolveTopOfStack(stack.get(stack.size() - 1));
            int nextPlayer = firstEligibleFrom(activePlayerId.orElseThrow());
            priorityPlayerId = OptionalInt.of(nextPlayer);
            return PriorityResult.stackResolved(nextPlayer, resolved);
        }

        priorityPlayerId = OptionalInt.empty();
        return PriorityResult.windowClosed();
    }

    public List<Integer> checkStateBasedActions() {
        ArrayList<Integer> newlyLostPlayers = new ArrayList<>();
        for (Player player : players.values()) {
            if (!player.lost() && player.life() <= 0) {
                player.loseGame();
                newlyLostPlayers.add(player.playerId());
            }
        }
        if (!newlyLostPlayers.isEmpty() && priorityPlayerId.isPresent() && players.get(priorityPlayerId.getAsInt()).lost()) {
            List<Integer> eligible = eligiblePriorityPlayers();
            priorityPlayerId = eligible.isEmpty() || gameOver()
                    ? OptionalInt.empty()
                    : OptionalInt.of(firstEligibleFrom(activePlayerId.orElse(eligible.get(0))));
            consecutivePasses = 0;
        }
        validate();
        return List.copyOf(newlyLostPlayers);
    }

    public boolean gameOver() {
        return players.values().stream().filter(player -> !player.lost()).count() <= 1;
    }

    public GameObject playLand(int playerId, int objectId) {
        requirePriorityPlayer(playerId);
        requireSorceryTiming(playerId);
        GameObject card = requireCardInHand(playerId, objectId);
        if (card.cardKind().orElse(null) != CardKind.LAND) {
            throw new IllegalArgumentException("Only a land card may be played as a land.");
        }
        Player player = players.get(playerId);
        if (player.landsPlayedThisTurn() >= 1) {
            throw new IllegalStateException("The player has already played a land this turn.");
        }
        GameObject permanent = moveObject(objectId, sharedZoneIds.get(ZoneType.BATTLEFIELD), ObjectType.PERMANENT, playerId);
        player.recordLandPlayed();
        consecutivePasses = 0;
        return permanent;
    }

    public GameObject castSpell(int playerId, int objectId) {
        requirePriorityPlayer(playerId);
        GameObject card = requireCardInHand(playerId, objectId);
        CardKind kind = card.cardKind().orElseThrow(() -> new IllegalArgumentException("The object is not a supported spell card."));
        if (!Set.of(CardKind.CREATURE, CardKind.SORCERY, CardKind.INSTANT).contains(kind)) {
            throw new IllegalArgumentException("The object is not a supported spell card.");
        }
        if (kind != CardKind.INSTANT) {
            requireSorceryTiming(playerId);
        }
        Player player = players.get(playerId);
        ManaCost manaCost = ManaCost.generic(card.manaCost());
        ManaPaymentResult payment = new ManaPaymentEngine().evaluate(player.manaPoolDetails(), manaCost);
        if (!payment.payable()) {
            throw new IllegalStateException("The player cannot pay the spell's mana cost.");
        }

        int destinationZoneId = kind == CardKind.CREATURE
                ? sharedZoneIds.get(ZoneType.BATTLEFIELD)
                : player.zoneId(ZoneType.GRAVEYARD);
        ObjectType resolvedType = kind == CardKind.CREATURE ? ObjectType.PERMANENT : ObjectType.CARD;
        player.payMana(manaCost);
        GameObject spell = moveObject(objectId, sharedZoneIds.get(ZoneType.STACK), ObjectType.SPELL, playerId);
        stackResolutions.put(spell.objectId(), new StackResolution(OptionalInt.of(destinationZoneId), resolvedType));
        consecutivePasses = 0;
        return spell;
    }

    public GameObject activateManaAbility(int playerId, int objectId) {
        requirePriorityPlayer(playerId);
        GameObject land = requireObject(objectId);
        if (land.controllerId() != playerId || land.zoneId() != sharedZoneIds.get(ZoneType.BATTLEFIELD)) {
            throw new IllegalArgumentException("The player does not control that battlefield object.");
        }
        if (land.objectType() != ObjectType.PERMANENT || land.cardKind().orElse(null) != CardKind.LAND) {
            throw new IllegalArgumentException("Only land permanents can use this basic mana ability.");
        }
        if (land.tapped()) {
            throw new IllegalStateException("The land is already tapped.");
        }
        land.moveTo(land.zoneId(), land.objectType(), true, land.summoningSick());
        addMana(playerId, 1);
        consecutivePasses = 0;
        return land;
    }

    public int addMana(int playerId, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Mana amount must be positive.");
        }
        Player player = requirePlayer(playerId);
        player.addMana(amount);
        return player.manaPool();
    }

    public int addMana(int playerId, ManaType manaType, int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Mana amount must be positive.");
        }
        Player player = requirePlayer(playerId);
        player.addMana(manaType, amount);
        return player.manaPool();
    }

    public int changeLife(int playerId, int amount) {
        Player player = requirePlayer(playerId);
        player.changeLife(amount);
        checkStateBasedActions();
        return player.life();
    }

    public void validate() {
        Set<Integer> seenObjectIds = new HashSet<>();
        for (Zone zone : zones.values()) {
            for (int objectId : zone.objectIds()) {
                if (!objects.containsKey(objectId)) {
                    throw new IllegalStateException("Zone references unknown object: " + objectId);
                }
                if (!seenObjectIds.add(objectId)) {
                    throw new IllegalStateException("Object appears in multiple zones: " + objectId);
                }
                GameObject object = objects.get(objectId);
                if (object.zoneId() != zone.zoneId()) {
                    throw new IllegalStateException("Object zone pointer is stale: " + objectId);
                }
            }
        }
        if (!seenObjectIds.equals(objects.keySet())) {
            throw new IllegalStateException("Objects map and zones disagree.");
        }
    }

    public Map<Integer, Player> players() { return Map.copyOf(players); }
    public List<Integer> playerOrder() { return List.copyOf(playerOrder); }
    public Map<Integer, Zone> zones() { return Map.copyOf(zones); }
    public Map<ZoneType, Integer> sharedZoneIds() { return Map.copyOf(sharedZoneIds); }
    public Map<Integer, GameObject> objects() { return Map.copyOf(objects); }
    public int turnNumber() { return turnNumber; }
    public OptionalInt activePlayerId() { return activePlayerId; }
    public TurnStep currentStep() { return currentStep; }
    public OptionalInt priorityPlayerId() { return priorityPlayerId; }

    public List<GameObject> objectsInZone(int zoneId) {
        Zone zone = requireZone(zoneId);
        return zone.objectIds().stream().map(objects::get).toList();
    }

    boolean canPlayLand(int playerId, int objectId) {
        return priorityPlayerId.isPresent()
                && priorityPlayerId.getAsInt() == playerId
                && isMainPhaseSorceryWindow(playerId)
                && requireObject(objectId).cardKind().orElse(null) == CardKind.LAND
                && players.get(playerId).landsPlayedThisTurn() == 0
                && requireObject(objectId).zoneId() == players.get(playerId).zoneId(ZoneType.HAND);
    }

    boolean canCastSpell(int playerId, int objectId) {
        if (priorityPlayerId.isEmpty() || priorityPlayerId.getAsInt() != playerId) {
            return false;
        }
        GameObject object = requireObject(objectId);
        if (object.zoneId() != players.get(playerId).zoneId(ZoneType.HAND)) {
            return false;
        }
        CardKind kind = object.cardKind().orElse(null);
        if (!Set.of(CardKind.CREATURE, CardKind.SORCERY, CardKind.INSTANT).contains(kind)) {
            return false;
        }
        if (kind != CardKind.INSTANT && !isMainPhaseSorceryWindow(playerId)) {
            return false;
        }
        return new ManaPaymentEngine().evaluate(players.get(playerId).manaPoolDetails(), ManaCost.generic(object.manaCost())).payable();
    }

    boolean canActivateManaAbility(int playerId, int objectId) {
        if (priorityPlayerId.isEmpty() || priorityPlayerId.getAsInt() != playerId) {
            return false;
        }
        GameObject object = requireObject(objectId);
        return object.controllerId() == playerId
                && object.zoneId() == sharedZoneIds.get(ZoneType.BATTLEFIELD)
                && object.objectType() == ObjectType.PERMANENT
                && object.cardKind().orElse(null) == CardKind.LAND
                && !object.tapped();
    }

    private Zone addZone(ZoneType zoneType, OptionalInt ownerId) {
        if (ownerId.isPresent()) {
            requirePlayer(ownerId.getAsInt());
        }
        Zone zone = new Zone(nextZoneId++, zoneType, ownerId);
        zones.put(zone.zoneId(), zone);
        return zone;
    }

    private void advanceTurn() {
        int currentIndex = playerOrder.indexOf(activePlayerId.orElseThrow());
        int nextIndex = (currentIndex + 1) % playerOrder.size();
        activePlayerId = OptionalInt.of(playerOrder.get(nextIndex));
        turnNumber += 1;
        players.get(activePlayerId.getAsInt()).resetTurnCounters();
    }

    private void performUntapStep() {
        int active = activePlayerId.orElseThrow();
        for (GameObject object : objects.values()) {
            if (object.controllerId() == active && object.objectType() == ObjectType.PERMANENT) {
                object.moveTo(object.zoneId(), object.objectType(), false, false);
            }
        }
    }

    private void performDrawStep() {
        int active = activePlayerId.orElseThrow();
        Player player = players.get(active);
        if (!objectsInZone(player.zoneId(ZoneType.LIBRARY)).isEmpty()) {
            drawCard(active);
        }
    }

    private int resolveTopOfStack(int objectId) {
        StackResolution resolution = stackResolutions.get(objectId);
        if (resolution == null || resolution.destinationZoneId().isEmpty()) {
            return objectId;
        }
        moveObject(objectId, resolution.destinationZoneId().getAsInt(), resolution.destinationObjectType(), requireObject(objectId).ownerId());
        return objectId;
    }

    private void requirePriorityPlayer(int playerId) {
        if (priorityPlayerId.isEmpty() || priorityPlayerId.getAsInt() != playerId) {
            throw new IllegalArgumentException("Only the player with priority may act.");
        }
    }

    private void requireSorceryTiming(int playerId) {
        if (!isMainPhaseSorceryWindow(playerId)) {
            throw new IllegalStateException("This action requires the active player's main phase and an empty stack.");
        }
    }

    public boolean isMainPhaseSorceryWindow(int playerId) {
        return activePlayerId.isPresent()
                && activePlayerId.getAsInt() == playerId
                && (currentStep == TurnStep.PRECOMBAT_MAIN || currentStep == TurnStep.POSTCOMBAT_MAIN)
                && stackObjectIds().isEmpty();
    }

    private GameObject requireCardInHand(int playerId, int objectId) {
        GameObject object = requireObject(objectId);
        if (object.objectType() != ObjectType.CARD || object.zoneId() != requirePlayer(playerId).zoneId(ZoneType.HAND)) {
            throw new IllegalArgumentException("The object must be a card in that player's hand.");
        }
        return object;
    }

    private void requireNoPriority() {
        if (priorityPlayerId.isPresent()) {
            throw new IllegalStateException("Cannot advance while a player has priority.");
        }
    }

    private Player requirePlayer(int playerId) {
        Player player = players.get(playerId);
        if (player == null) {
            throw new IllegalArgumentException("Unknown player: " + playerId);
        }
        return player;
    }

    private Zone requireZone(int zoneId) {
        Zone zone = zones.get(zoneId);
        if (zone == null) {
            throw new IllegalArgumentException("Unknown zone: " + zoneId);
        }
        return zone;
    }

    private GameObject requireObject(int objectId) {
        GameObject object = objects.get(objectId);
        if (object == null) {
            throw new IllegalArgumentException("Unknown object: " + objectId);
        }
        return object;
    }

    private List<Integer> stackObjectIds() {
        return zones.get(sharedZoneIds.get(ZoneType.STACK)).objectIds();
    }

    private List<Integer> eligiblePriorityPlayers() {
        return playerOrder.stream().filter(playerId -> !players.get(playerId).lost()).toList();
    }

    private int firstEligibleFrom(int playerId) {
        List<Integer> eligible = eligiblePriorityPlayers();
        int startIndex = playerOrder.indexOf(playerId);
        for (int offset = 0; offset < playerOrder.size(); offset++) {
            int candidate = playerOrder.get((startIndex + offset) % playerOrder.size());
            if (eligible.contains(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException("No player is eligible to receive priority.");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }
}
