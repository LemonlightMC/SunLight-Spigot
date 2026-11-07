package su.nightexpress.sunlight.moduleImpl.extras;

public class ExtrasPerms {

    public static final String MODULE = "sunlight.essential";
    public static final String COMMAND = MODULE + ".command";
    public static final String BYPASS = MODULE + ".bypass";

    public static final String SIGNS_COLOR = MODULE + ".signs.color";
    public static final String ANVILS_COLOR = MODULE + ".anvils.color";

    public static final String STARTER_KIT = MODULE + ".starterkit";
    public static final String STARTER_KIT_BYPASS = BYPASS + ".starterkit";

    public static final String SIGN_COMMAND_SIGN = COMMAND + ".sign";
    public static final String SIGN_COMMAND_SIGN_TEXT = COMMAND + ".sign.text";
    public static final String SIGN_COMMAND_DELETE = COMMAND + ".delete";
    public static final String SIGN_COMMAND_DELETE_OTHERS = COMMAND + ".delete.others";
    public static final String SIGN_COMMAND_LOCK = COMMAND + ".lock";
    public static final String SIGN_COMMAND_LOCK_AUTO = COMMAND + ".lock.auto";
    public static final String SIGN_COMMAND_UNLOCK = COMMAND + ".unlock";
    public static final String SIGN_COMMAND_INFO = COMMAND + ".info";
    public static final String SIGN_COMMAND_COLORS = COMMAND + ".colors";

    public static final String SIGN_BYPASS_LOCKED = BYPASS + ".locked";
    public static final String SIGN_BYPASS_BLACKLIST = BYPASS + ".blacklist";

    private ExtrasPerms() {
    }
}
