package com.gliesestudio.mc.enums;

/**
 * This class contains all the commands used in the plugin.
 * <p>
 * It is used to organize the commands and make the code more readable.
 * It also makes it easier to add new commands in the future.
 * </p>
 *
 * @author Mazidul Islam
 * @version 1.0
 * @since 1.0
 */
public class Commands {

    /**
     * This class contains all the commands related to teleporting players.
     */
    public static class TPA {
        public static final String TPA = "tpa";
        public static final String TPHERE = "tphere";
        public static final String TPACCEPT = "tpaccept";
        public static final String TPDENY = "tpdeny";
    }

    /**
     * This class contains all the commands related to setting and using warps.
     */
    public static class WARP {
        public static final String SETWARP = "setwarp";
        public static final String WARP = "warp";
        public static final String WARPS = "warps";
        public static final String DELWARP = "delwarp";
    }

    /**
     * This class contains all the commands related to teleporting back to the last location.
     */
    public static class BACK {
        public static final String BACK = "back";
    }

}
