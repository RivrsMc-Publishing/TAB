package me.neznamy.tab.shared.features.nametags;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import me.neznamy.tab.shared.cpu.ThreadExecutor;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.data.World;
import me.neznamy.tab.shared.features.types.*;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * Sub-feature for NameTags for managing prefix/suffix.
 */
@RequiredArgsConstructor
public class PrefixSuffixManager extends RefreshableFeature implements GroupListener, WorldSwitchListener,
        ServerSwitchListener, CustomThreaded {

    /** Parent feature */
    private final NameTag feature;

    @Override
    @NotNull
    public String getRefreshDisplayName() {
        return "Updating prefix/suffix";
    }

    @Override
    public void refresh(@NotNull TabPlayer refreshed, boolean force) {
        if (force) {
            updateProperties(refreshed);
            updateCustomTagNameVisibility(refreshed);
            updatePrefixSuffix(refreshed);
        } else {
            boolean prefix = refreshed.teamData.prefix.update();
            boolean name = refreshed.teamData.customTagName.update();
            boolean suffix = refreshed.teamData.suffix.update();
            if (prefix || name || suffix) {
                updateCustomTagNameVisibility(refreshed);
                updatePrefixSuffix(refreshed);
            }
        }
    }

    @Override
    @NotNull
    public String getFeatureName() {
        return feature.getFeatureName();
    }

    @Override
    public void onGroupChange(@NotNull TabPlayer player) {
        if (updateProperties(player)) {
            updateCustomTagNameVisibility(player);
            updatePrefixSuffix(player);
        }
    }

    @Override
    public void onServerChange(@NonNull TabPlayer player, @NotNull Server from, @NotNull Server to) {
        if (updateProperties(player)) {
            updateCustomTagNameVisibility(player);
            updatePrefixSuffix(player);
        }
    }

    @Override
    public void onWorldChange(@NotNull TabPlayer player, @NotNull World from, @NotNull World to) {
        if (updateProperties(player)) {
            updateCustomTagNameVisibility(player);
            updatePrefixSuffix(player);
        }
    }

    /**
     * Loads all properties from config and returns {@code true} if at least
     * one of them either wasn't loaded or changed value, {@code false} otherwise.
     *
     * @param   p
     *          Player to update properties of
     * @return  {@code true} if at least one property changed, {@code false} if not
     */
    private boolean updateProperties(@NotNull TabPlayer p) {
        boolean changed = p.updatePropertyFromConfig(p.teamData.prefix, "");
        if (p.updatePropertyFromConfig(p.teamData.customTagName, p.getName())) changed = true;
        if (p.updatePropertyFromConfig(p.teamData.suffix, "")) changed = true;
        return changed;
    }

    /**
     * Updates nametag visibility based on customtagname value.
     * If customtagname resolves to empty string, hides the nametag.
     * Otherwise, shows it (removes the EMPTY_CUSTOM_TAG_NAME reason).
     *
     * @param   player
     *          Player to update visibility for
     */
    private void updateCustomTagNameVisibility(@NotNull TabPlayer player) {
        boolean empty = player.teamData.customTagName.get().isEmpty();
        if (empty) {
            if (player.teamData.hideNametag(NameTagInvisibilityReason.EMPTY_CUSTOM_TAG_NAME)) {
                feature.getVisibilityManager().updateVisibility(player);
            }
        } else {
            if (player.teamData.showNametag(NameTagInvisibilityReason.EMPTY_CUSTOM_TAG_NAME)) {
                feature.getVisibilityManager().updateVisibility(player);
            }
        }
    }

    /**
     * Updates team prefix and suffix of given player.
     *
     * @param   player
     *          Player to update prefix/suffix of
     */
    public void updatePrefixSuffix(@NonNull TabPlayer player) {
        for (TabPlayer viewer : feature.getOnlinePlayers().getPlayers()) {
            if (viewer.teamData.hasTeamRegistered(player)) {
                viewer.getScoreboard().updateTeam(
                        player.teamData.teamName,
                        feature.getPrefixCache().get(player.teamData.prefix.getFormat(viewer)),
                        feature.getSuffixCache().get(player.teamData.suffix.getFormat(viewer)),
                        feature.getLastColorCache().get(player.teamData.prefix.getFormat(viewer)).getLastStyle().toEnumChatFormat()
                );
            }
        }
        feature.getProxyHandler().sendProxyMessage(player);
    }

    /**
     * Loads properties from config.
     *
     * @param   player
     *          Player to load properties for
     */
    public void loadProperties(@NotNull TabPlayer player) {
        player.teamData.prefix = player.loadPropertyFromConfig(this, "tagprefix", "");
        player.teamData.customTagName = player.loadPropertyFromConfig(this, "customtagname", player.getName());
        player.teamData.suffix = player.loadPropertyFromConfig(this, "tagsuffix", "");
        updateCustomTagNameVisibility(player);
    }

    @Override
    @NotNull
    public ThreadExecutor getCustomThread() {
        return feature.getCustomThread();
    }
}
