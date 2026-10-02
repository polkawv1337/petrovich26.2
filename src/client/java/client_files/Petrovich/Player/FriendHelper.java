package client_files.Petrovich.Player;

import client_files.ClientikUtils.BooleanSetting;
import client_files.ClientikUtils.Category;
import client_files.Module;
import client_files.Petrovich.Render.FriendMarkerLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.EntityTypes;

import java.util.HashSet;
import java.util.Set;

public class FriendHelper extends Module {

    private static final Set<String> FRIENDS = new HashSet<>();

    public final BooleanSetting highlightFriends = addSetting(new BooleanSetting("Выделять друзей", true));

    @SuppressWarnings("unchecked")
    public FriendHelper() {
        super("FriendHelper", "Помощь вашим друнам которые никогда сами не помогут :)", Category.PLAYER);

        LivingEntityRenderLayerRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
            if (entityType == EntityTypes.PLAYER) {
                registrationHelper.register(new FriendMarkerLayer((RenderLayerParent<AvatarRenderState, PlayerModel>) entityRenderer));
            }
        });
    }

    public static boolean isFriend(String name) {
        return FRIENDS.contains(name.toLowerCase());
    }

    public static void addFriend(String name) {
        FRIENDS.add(name.toLowerCase());
    }

    public static void removeFriend(String name) {
        FRIENDS.remove(name.toLowerCase());
    }

    public static Set<String> getFriends() {
        return FRIENDS;
    }

    public static int friendCount() {
        return FRIENDS.size();
    }
}