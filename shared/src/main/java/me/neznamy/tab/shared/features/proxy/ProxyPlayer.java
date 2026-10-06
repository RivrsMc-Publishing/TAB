package me.neznamy.tab.shared.features.proxy;

import lombok.Getter;
import lombok.Setter;
import me.neznamy.tab.shared.ProtocolVersion;
import me.neznamy.tab.shared.TabConstants.Permission;
import me.neznamy.tab.shared.data.Server;
import me.neznamy.tab.shared.features.belowname.BelowNameProxyPlayerData;
import me.neznamy.tab.shared.features.nametags.NameTagProxyPlayerData;
import me.neznamy.tab.shared.features.playerlist.PlayerListProxyPlayerData;
import me.neznamy.tab.shared.features.playerlistobjective.PlayerListObjectiveProxyPlayerData;
import me.neznamy.tab.shared.features.sorting.Sorting;
import me.neznamy.tab.shared.platform.TabList;
import me.neznamy.tab.shared.platform.TabPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * Class holding information about a player connected to another proxy.
 */
@Getter
@Setter
public class ProxyPlayer {

    /** Real UUID of the player */
    @NotNull
    private final UUID uniqueId;

    /** Tablist UUID of the player */
    @NotNull
    private final UUID tablistId;

    /** Player's real name */
    @NotNull
    private final String name;

    /** Player's name as seen in game profile */
    @NotNull
    private String nickname;

    /** Name of server the player is connected to */
    @NotNull
    public Server server;

    /** Whether player is vanished or not */
    private boolean vanished;

    /** Whether player is staff or not */
    private final boolean staff;

    /** Belowname data */
    @Nullable
    private BelowNameProxyPlayerData belowname;

    /** Playerlist objective data */
    @Nullable
    private PlayerListObjectiveProxyPlayerData playerlist;

    /** Player's skin for global playerlist */
    @Nullable
    private final TabList.Skin skin;

    /** Tablist display name */
    @Nullable
    private PlayerListProxyPlayerData tabFormat;

    /** Nametag data */
    @Nullable
    private NameTagProxyPlayerData nametag;

    /** Player's connection state */
    @NotNull
    private ConnectionState connectionState = ConnectionState.QUEUED;

    /**
     * Constructs new instance with given parameters.
     *
     * @param   uniqueId
     *          Player's real UUID
     * @param   tablistId
     *          Player's tablist UUID
     * @param   name
     *          Player's real name
     * @param   server
     *          Player's server
     * @param   vanished
     *          Whether player is vanished or not
     * @param   staff
     *          Whether player has {@link Permission#STAFF} permission or not
     * @param   skin
     *          Player's skin for global playerlist, null if not set
     */
    public ProxyPlayer(@NotNull UUID uniqueId, @NotNull UUID tablistId, @NotNull String name,
                       @NotNull Server server, boolean vanished, boolean staff, @Nullable TabList.Skin skin) {
        this.uniqueId = uniqueId;
        this.tablistId = tablistId;
        this.name = name;
        nickname = name;
        this.server = server;
        this.vanished = vanished;
        this.staff = staff;
        this.skin = skin;
    }

    /**
     * Creates a new entry for this player to be used in tablist.
     *
     * @param   viewer
     *          Player who will see the entry
     * @return  TabList.Entry representing this player
     */
    @NotNull
    public TabList.Entry asEntry(@NotNull TabPlayer viewer) {
        return new TabList.Entry(uniqueId, nickname, skin, true, 0, 0, tabFormat == null ? null : tabFormat.getFormatComponent(),
                getListOrder(viewer), true);
    }

    /**
     * Fork: returns list order of this player for specified viewer, computed from the team name
     * received from player's proxy the same way as for local players (see {@link Sorting#computeListOrder(String)}).
     * Without this, players connected to another proxy would have list order 0 and end up below
     * all local players on 1.21.11+ clients regardless of their sorting.
     *
     * @param   viewer
     *          Player viewing this player's tablist entry
     * @return  List order for specified viewer, 0 if viewer is below 1.21.11 or nametag data was not received yet
     */
    public int getListOrder(@NotNull TabPlayer viewer) {
        if (nametag == null || viewer.getVersion().getNetworkId() < ProtocolVersion.V1_21_11.getNetworkId()) return 0;
        return Sorting.computeListOrder(nametag.getTeamName());
    }

    /**
     * Enum representing connection state of a proxy player.
     */
    public enum ConnectionState {

        /** The connection is accepted and player is being displayed to others */
        CONNECTED,

        /** The player is queued, because the actual player is still connected to this server */
        QUEUED,

        /** The player has disconnected */
        DISCONNECTED
    }
}