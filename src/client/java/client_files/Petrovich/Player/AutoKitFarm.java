package client_files.Petrovich.Player;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.ClientikUtils.ChatHelper;
import client_files.ClientikUtils.ModeListSetting;
import client_files.ClientikUtils.SliderSetting;
import client_files.ClientikUtils.StopWatch;
import client_files.Module;
import client_files.ModuleManager;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class AutoKitFarm extends Module {

    private static final long KIT_PERIOD_MS = 60L * 60L * 1000L;
    private static final long TAKE_TIMEOUT_MS = 3000;
    private static final long OPEN_TIMEOUT_MS = 1500;
    private static final long SEARCH_RETRY_MS = 3000;
    private static final long CLICK_DELAY_MS = 50;
    private static final long WATCHDOG_MS = 30000;
    private static final double CHEST_RANGE = 4.5;
    private static final long CI_DELAY_MS = 700;
    private static final long KIT_SETTLE_MIN_MS = 900;
    private static final long KIT_SETTLE_QUIET_MS = 400;

    private static final Pattern P_HOURS = Pattern.compile("(\\d+)\\s*(?:ч(?:ас|ас[а-я]*)|h(?:ours?)?)");
    private static final Pattern P_MINUTES = Pattern.compile("(\\d+)\\s*(?:мин(?:ут|ут[а-я]*)?|m(?:inutes?)?)");
    private static final Pattern P_SECONDS = Pattern.compile("(\\d+)\\s*(?:сек(?:унд|унд[а-я]*)?|s(?:econds?)?)");
    private static final Pattern P_MMSS = Pattern.compile("\\b(\\d{1,2}):(\\d{2})\\b");

    private final ModeListSetting kits = addSetting(new ModeListSetting("Киты",
            new BooleanSetting("free", true),
            new BooleanSetting("dragon", true),
            new BooleanSetting("tiger", true),
            new BooleanSetting("bunny", true),
            new BooleanSetting("winner", true),
            new BooleanSetting("delta", false),
            new BooleanSetting("sponsor", false),
            new BooleanSetting("universal", false),
            new BooleanSetting("cobra", false),
            new BooleanSetting("slime", false),
            new BooleanSetting("god", false),
            new BooleanSetting("snowman", false),
            new BooleanSetting("fame", false)));

    private final BooleanSetting armor = addSetting(new BooleanSetting("Броня", true));
    private final BooleanSetting enchGold = addSetting(new BooleanSetting("Зачарованные яблоки", true));
    private final BooleanSetting goldApple = addSetting(new BooleanSetting("Золотые яблоки", true));
    private final BooleanSetting vikingPotions = addSetting(new BooleanSetting("Зелья Викинга", true));
    private final BooleanSetting baboonPotions = addSetting(new BooleanSetting("Зелья Бабуина", true));
    private final BooleanSetting lawyerPotions = addSetting(new BooleanSetting("Зелья Юриста", true));
    private final BooleanSetting turtlePowerPotions = addSetting((BooleanSetting) new BooleanSetting("Зелье Черепашьей Мощи", true)
            .setVisible(() -> kits.isEnabled("bunny") || kits.isEnabled("tiger")));
    private final BooleanSetting wood = addSetting((BooleanSetting) new BooleanSetting("Дерево", true)
            .setVisible(() -> kits.isEnabled("free") || kits.isEnabled("dragon")));
    private final BooleanSetting dragonidTotems = addSetting((BooleanSetting) new BooleanSetting("Тотемы Драконида", true)
            .setVisible(() -> kits.isEnabled("delta") || kits.isEnabled("sponsor") || kits.isEnabled("universal")
                    || kits.isEnabled("fame") || kits.isEnabled("free") || kits.isEnabled("dragon")
                    || kits.isEnabled("tiger") || kits.isEnabled("bunny")));
    private final BooleanSetting arrows = addSetting((BooleanSetting) new BooleanSetting("Стрелы", false)
            .setVisible(() -> kits.isEnabled("cobra") || kits.isEnabled("slime") || kits.isEnabled("god")
                    || kits.isEnabled("fame") || kits.isEnabled("snowman")));
    private final ModeListSetting arrowTypes = addSetting((ModeListSetting) new ModeListSetting("Типы стрел",
            new BooleanSetting("Плавного Падения", false),
            new BooleanSetting("Клирик", false),
            new BooleanSetting("Вреда", false),
            new BooleanSetting("Левитации", false),
            new BooleanSetting("Шрека", false),
            new BooleanSetting("Обморожения", false))
            .setVisible(() -> arrows.getValue() && (kits.isEnabled("cobra") || kits.isEnabled("slime")
                    || kits.isEnabled("god") || kits.isEnabled("fame") || kits.isEnabled("snowman"))));
    private final BooleanSetting livalka = addSetting((BooleanSetting) new BooleanSetting("Ливалка", true)
            .setVisible(() -> kits.isEnabled("fame")));
    private final BooleanSetting fireworks = addSetting((BooleanSetting) new BooleanSetting("Фейерверки", true)
            .setVisible(() -> kits.isEnabled("winner") || kits.isEnabled("fame")));
    private final BooleanSetting spheres = addSetting(new BooleanSetting("Шары", true));
    private final BooleanSetting onlyFameSpheres = addSetting((BooleanSetting) new BooleanSetting("Только шары фейма", false)
            .setVisible(() -> spheres.getValue()));

    private final SliderSetting cdCheck = addSetting(new SliderSetting("Проверка КД (сек)", 30.0f, 5.0f, 300.0f, 5.0f));

    private enum State {WAITING, TAKING, FIND_CHEST, OPENING, DEPOSIT}

    private State state = State.WAITING;
    private String currentKit = null;
    private final Map<String, Long> nextAttempt = new HashMap<>();
    private long takeSentAt = 0;
    private int preCount = 0;
    private long phaseStart = 0;
    private long nextSearchAt = 0;
    private BlockPos targetChest = null;
    private final Set<BlockPos> fullChests = new HashSet<>();
    private long ciAt = 0;
    private int lastSeenCount = -1;
    private long lastCountChangeAt = 0;
    private final StopWatch clickTimer = new StopWatch();

    public AutoKitFarm() {
        super("AutoKitFarm", "Автоматически берёт киты и складывает их в сундуки", Category.PLAYER);
    }

    @Override
    public void onEnable() {
        super.onEnable();
        resetCycle();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.player != null && mc.gui.screen() instanceof AbstractContainerScreen) {
            closeScreen();
        }
        resetCycle();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.level == null) return;

        if (state != State.WAITING && System.currentTimeMillis() - phaseStart > WATCHDOG_MS) {
            chat("\u00a7eСброс зависшего состояния");
            if (mc.gui.screen() instanceof AbstractContainerScreen) closeScreen();
            setState(State.WAITING);
            currentKit = null;
        }

        switch (state) {
            case WAITING -> handleWaiting();
            case TAKING -> handleTaking();
            case FIND_CHEST -> handleFindChest();
            case OPENING -> handleOpening();
            case DEPOSIT -> handleDeposit();
        }
    }

    public static void onGameMessage(String text) {
        AutoKitFarm mod = ModuleManager.getInstance().get(AutoKitFarm.class);
        if (mod == null || !mod.isEnabled()) return;
        mod.handleChat(text);
    }

    private void handleChat(String text) {
        if (currentKit == null) return;
        String lower = text.toLowerCase(Locale.ROOT);

        boolean kitCtx = lower.contains("кит") || lower.contains("kit") || lower.contains(currentKit);
        if (!kitCtx) return;

        boolean cdPhrase = lower.contains("через") || lower.contains("подожд") || lower.contains("жд")
                || lower.contains("wait") || lower.contains("cooldown") || lower.contains("доступ");

        if (cdPhrase) {
            long parsed = parseCooldown(lower);
            long ms = parsed > 0 ? parsed : (long) (cdCheck.getValue() * 1000.0);
            long candidate = System.currentTimeMillis() + ms;
            nextAttempt.merge(currentKit, candidate, Math::max);
            chat("\u00a7bКД кита \"" + currentKit + "\": " + formatDuration(ms));
        }
    }

    private void handleWaiting() {
        long now = System.currentTimeMillis();

        if (ciAt > 0) {
            if (now < ciAt) return;
            mc.player.connection.sendCommand("ci");
            chat("\u00a77Инвентарь очищен (/ci)");
            ciAt = 0;
            return;
        }

        for (String kit : new HashSet<>(nextAttempt.keySet())) {
            if (nextAttempt.get(kit) > now) continue;
            if (!kits.isEnabled(kit)) {
                nextAttempt.remove(kit);
                return;
            }
            currentKit = kit;
            preCount = countInventory();
            lastSeenCount = preCount;
            lastCountChangeAt = now;
            mc.player.connection.sendCommand("kit " + kit);
            takeSentAt = now;
            setState(State.TAKING);
            return;
        }

        for (BooleanSetting ks : kits.getSettings()) {
            if (ks.getValue()) nextAttempt.putIfAbsent(ks.getName().toLowerCase(Locale.ROOT), 0L);
        }
    }

    private void handleTaking() {
        long now = System.currentTimeMillis();
        int cnt = countInventory();

        if (cnt != lastSeenCount) {
            lastSeenCount = cnt;
            lastCountChangeAt = now;
        }

        boolean settled = now - takeSentAt >= KIT_SETTLE_MIN_MS
                && now - lastCountChangeAt >= KIT_SETTLE_QUIET_MS;

        if (cnt > preCount && settled) {
            nextAttempt.put(currentKit, now + KIT_PERIOD_MS);
            chat("\u00a7aКит \"" + currentKit + "\" взят, ищу сундук...");
            fullChests.clear();
            setState(State.FIND_CHEST);
            return;
        }

        if (now - takeSentAt > TAKE_TIMEOUT_MS) {
            long cdMs = Math.round(cdCheck.getValue() * 1000L);
            nextAttempt.put(currentKit, now + cdMs);
            chat("\u00a7eКит \"" + currentKit + "\" на кулдауне, проверю через " + Math.round(cdCheck.getValue()) + " сек.");
            currentKit = null;
            setState(State.WAITING);
        }
    }

    private void handleFindChest() {
        long now = System.currentTimeMillis();
        if (isContainerOpen()) {
            setState(State.DEPOSIT);
            clickTimer.reset();
            return;
        }
        if (now < nextSearchAt) return;

        Player player = mc.player;
        BlockPos center = player.blockPosition();
        BlockPos best = null;
        double bestDist = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(center.getX() - 4, center.getY() - 3, center.getZ() - 4,
                center.getX() + 4, center.getY() + 3, center.getZ() + 4)) {
            if (fullChests.contains(p)) continue;
            BlockState bs = mc.level.getBlockState(p);
            if (!(bs.getBlock() instanceof ChestBlock)) continue;
            double d = distSqr(player, p);
            if (d < bestDist) {
                bestDist = d;
                best = p.immutable();
            }
        }

        if (best == null || bestDist > CHEST_RANGE * CHEST_RANGE) {
            nextSearchAt = now + SEARCH_RETRY_MS;
            chat("\u00a7cСвободных сундуков рядом не найдено, повтор через 10 сек.");
            return;
        }

        Vec3 v = new Vec3(best.getX() + 0.5, best.getY() + 1.0, best.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(v, Direction.UP, best, false);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, hit);
        mc.player.swing(InteractionHand.MAIN_HAND);

        targetChest = best;
        setState(State.OPENING);
    }

    private void handleOpening() {
        if (isContainerOpen()) {
            setState(State.DEPOSIT);
            clickTimer.reset();
            return;
        }
        if (System.currentTimeMillis() - phaseStart > OPEN_TIMEOUT_MS) {
            fullChests.add(targetChest);
            targetChest = null;
            setState(State.FIND_CHEST);
        }
    }

    private void handleDeposit() {
        if (!isContainerOpen()) {
            setState(State.FIND_CHEST);
            return;
        }

        AbstractContainerMenu menu = currentMenu();
        if (menu == null) {
            setState(State.FIND_CHEST);
            return;
        }
        int containerSlots = menu.slots.size() - 36;
        if (containerSlots <= 0) {
            setState(State.FIND_CHEST);
            return;
        }

        Integer from = null;
        for (int i = containerSlots; i < menu.slots.size(); i++) {
            if (shouldStore(menu.slots.get(i).getItem())) {
                from = i;
                break;
            }
        }

        if (from == null) {
            chat("\u00a7aКит \"" + currentKit + "\" разложен"
                    + (targetChest != null ? " (сундук " + targetChest.getX() + " " + targetChest.getY() + " " + targetChest.getZ() + ")" : ""));
            closeScreen();
            targetChest = null;
            currentKit = null;
            ciAt = System.currentTimeMillis() + CI_DELAY_MS;
            setState(State.WAITING);
            return;
        }

        ItemStack stack = menu.slots.get(from).getItem();
        if (!chestAccepts(menu, containerSlots, stack)) {
            fullChests.add(targetChest);
            chat("\u00a7eСундук заполнен, ищу следующий...");
            closeScreen();
            targetChest = null;
            setState(State.FIND_CHEST);
            return;
        }

        if (clickTimer.isReached(CLICK_DELAY_MS)) {
            int syncId = menu.containerId;
            int slotIndex = menu.slots.get(from).index;
            mc.gameMode.handleContainerInput(syncId, slotIndex, 0, ContainerInput.QUICK_MOVE, mc.player);
            clickTimer.reset();
        }
    }

    private boolean isContainerOpen() {
        return mc.gui.screen() instanceof AbstractContainerScreen;
    }

    private AbstractContainerMenu currentMenu() {
        if (mc.gui.screen() instanceof AbstractContainerScreen<?> screen) {
            return screen.getMenu();
        }
        return null;
    }

    private void closeScreen() {
        if (mc.player == null || mc.player.connection == null) return;
        AbstractContainerMenu menu = currentMenu();
        if (menu != null) {
            mc.player.connection.send(new ServerboundContainerClosePacket(menu.containerId));
        }
        mc.setScreenAndShow(null);
    }

    private long parseCooldown(String lower) {
        long totalSec = 0;

        Matcher mm = P_MMSS.matcher(lower);
        if (mm.find()) {
            return Integer.parseInt(mm.group(1)) * 60L + Integer.parseInt(mm.group(2));
        }

        Matcher h = P_HOURS.matcher(lower);
        if (h.find()) totalSec += Integer.parseInt(h.group(1)) * 3600L;
        Matcher m = P_MINUTES.matcher(lower);
        if (m.find()) totalSec += Integer.parseInt(m.group(1)) * 60L;
        Matcher s = P_SECONDS.matcher(lower);
        if (s.find()) totalSec += Integer.parseInt(s.group(1));

        return totalSec * 1000L;
    }

    private String formatDuration(long ms) {
        long totalSec = ms / 1000;
        long h = totalSec / 3600;
        long m = (totalSec % 3600) / 60;
        long s = totalSec % 60;
        if (h > 0) return String.format("%d:%02d:%02d", h, m, s);
        return String.format("%d:%02d", m, s);
    }

    private boolean shouldStore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        String name = stack.getHoverName().getString().toLowerCase(Locale.ROOT);

        if (stack.get(DataComponents.EQUIPPABLE) != null) return armor.getValue();
        if (item == Items.ENCHANTED_GOLDEN_APPLE) return enchGold.getValue();
        if (item == Items.GOLDEN_APPLE) return goldApple.getValue();
        if (name.contains("викинг")) return vikingPotions.getValue();
        if (name.contains("бабуин")) return baboonPotions.getValue();
        if (name.contains("юрист")) return lawyerPotions.getValue();
        if (name.contains("черепаш")) return turtlePowerPotions.getValue();
        if (name.contains("драконид")) return dragonidTotems.getValue();
        if (isWoodItem(item) || name.contains("дерев")) {
            if (!wood.getValue()) return false;
            return name.contains("дерев") || isWoodItem(item);
        }
        if (item == Items.FEATHER && name.contains("лива")) return livalka.getValue();
        if (item instanceof ArrowItem) {
            if (!arrows.getValue()) return false;
            return matchArrowType(name);
        }
        if (item == Items.FIREWORK_ROCKET) return fireworks.getValue();
        if (item == Items.PLAYER_HEAD) {
            if (!spheres.getValue()) return false;
            if (onlyFameSpheres.getValue()) return name.contains("фейм");
            return true;
        }
        return false;
    }

    private boolean matchArrowType(String name) {
        boolean anyEnabled = false;
        for (BooleanSetting t : arrowTypes.getSettings()) {
            if (t.getValue()) {
                anyEnabled = true;
                break;
            }
        }
        if (!anyEnabled) return true;

        if (name.contains("паден")) return arrowTypes.isEnabled("Плавного Падения");
        if (name.contains("клирик")) return arrowTypes.isEnabled("Клирик");
        if (name.contains("вред")) return arrowTypes.isEnabled("Вреда");
        if (name.contains("левита")) return arrowTypes.isEnabled("Левитации");
        if (name.contains("шрек")) return arrowTypes.isEnabled("Шрека");
        if (name.contains("обморо")) return arrowTypes.isEnabled("Обморожения");
        return false;
    }

    private boolean isWoodItem(Item item) {
        try {
            String path = BuiltInRegistries.ITEM.getKey(item).getPath();
            return path.contains("log") || path.contains("planks") || path.contains("_wood")
                    || path.contains("stem") || path.contains("hyphae");
        } catch (Exception ignored) {
            return false;
        }
    }

    private boolean chestAccepts(AbstractContainerMenu menu, int containerSlots, ItemStack stack) {
        for (int i = 0; i < containerSlots; i++) {
            ItemStack s = menu.slots.get(i).getItem();
            if (s.isEmpty()) return true;
            if (ItemStack.isSameItem(s, stack) && s.getCount() + stack.getCount() <= Item.DEFAULT_MAX_STACK_SIZE) {
                return true;
            }
        }
        return false;
    }

    private int countInventory() {
        int c = 0;
        for (ItemStack st : mc.player.getInventory().getNonEquipmentItems()) {
            if (!st.isEmpty()) c++;
        }
        return c;
    }

    private double distSqr(Player player, BlockPos p) {
        double dx = p.getX() + 0.5 - player.getX();
        double dz = p.getZ() + 0.5 - player.getZ();
        double dy = p.getY() - player.getY();
        return dx * dx + dy * dy + dz * dz;
    }

    private void setState(State s) {
        state = s;
        phaseStart = System.currentTimeMillis();
    }

    private void resetCycle() {
        state = State.WAITING;
        currentKit = null;
        nextAttempt.clear();
        fullChests.clear();
        targetChest = null;
        ciAt = 0;
        lastSeenCount = -1;
        lastCountChangeAt = 0;
    }

    private void chat(String message) {
        ChatHelper.print(message);
    }
}