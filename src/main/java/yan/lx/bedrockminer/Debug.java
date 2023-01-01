package yan.lx.bedrockminer;

import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debug {
    private static final Logger LOGGER = BedrockMinerMod.LOGGER;

    private static final boolean enable = false;

    public static void info(String msg) {
        if (enable) {
            LOGGER.info(msg);
        }
    }
}
