package su.nightexpress.sunlight.moduleImpl.essential;

public class EssentialPerms {

    public static final String MODULE = "sunlight.essential";
    public static final String COMMAND = MODULE + ".command";
    public static final String BYPASS = MODULE + ".bypass";

    public static final String COMMAND_INVULNERABILITY = COMMAND + ".invulnerability";
    public static final String COMMAND_INVULNERABILITY_OTHERS = COMMAND + ".invulnerability.others";
    public static final String COMMAND_GOD = COMMAND + ".god";
    public static final String COMMAND_GOD_OTHERS = COMMAND + ".god.others";
    
    public static final String BYPASS_GOD_WORLD = BYPASS + ".invulnerability.world";
    public static final String BYPASS_INVULNERABILITY_WORLD = BYPASS + ".invulnerability.world";
    public static final String BYPASS_INVULNERABILITY_DAMAGE = BYPASS + ".invulnerability.damage";

    public static final String COMMAND_AGGRO = COMMAND + ".aggro";
    public static final String COMMAND_AGGRO_OTHERS = COMMAND + ".aggro.others";

    public static final String COMMAND_RENAME = COMMAND + ".rename";
    public static final String COMMAND_RENAME_COLORS = COMMAND + ".rename.colors";

    private EssentialPerms() {
    }
}
