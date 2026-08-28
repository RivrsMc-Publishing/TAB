package me.neznamy.tab.shared.features.nametags;

/**
 * Enum tracking all possible reasons why a player's nametag is globally hidden for everyone.
 */
public enum NameTagInvisibilityReason {

    /** API function for nametag hiding (#hideNameTag) */
    API_HIDE,

    /** invisible-nametags option returned true for the player */
    MEETING_CONFIGURED_CONDITION,

    /** /tab nametag hide command */
    HIDE_COMMAND,

    /** customtagname property resolved to empty string */
    EMPTY_CUSTOM_TAG_NAME,

    /** /tab nametag opaque command hides nametag from viewers without line of sight */
    OPAQUE_OCCLUSION
}
