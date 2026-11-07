package su.nightexpress.sunlight.moduleImpl.sign.config;

public class SignsPerms {

    public static final String MODULE  = "sunlight.sign";
    public static final String COMMAND = MODULE + ".command";
    public static final String BYPASS  = MODULE + ".bypass";

    public static final String COMMAND_SIGN         = COMMAND + ".sign";
    public static final String COMMAND_SIGN_TEXT    = COMMAND + ".sign.text";
    public static final String COMMAND_DELETE       = COMMAND + ".delete";
    public static final String COMMAND_DELETE_OTHERS = COMMAND + ".delete.others";
    public static final String COMMAND_LOCK         = COMMAND + ".lock";
    public static final String COMMAND_LOCK_AUTO    = COMMAND + ".lock.auto";
    public static final String COMMAND_UNLOCK       = COMMAND + ".unlock";
    public static final String COMMAND_INFO         = COMMAND + ".info";
    public static final String COMMAND_COLORS       = COMMAND + ".colors";

    public static final String BYPASS_LOCKED    = BYPASS + ".locked";
    public static final String BYPASS_BLACKLIST = BYPASS + ".blacklist";

    private SignsPerms() {
    }
}
