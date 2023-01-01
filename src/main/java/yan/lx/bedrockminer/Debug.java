package yan.lx.bedrockminer;

import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yan.lx.bedrockminer.BedrockMinerMod;

public class Debug {
    private static final boolean enable = true;
    private static final Logger LOGGER = BedrockMinerMod.LOGGER;


    public static void info(String msg) {
        if (enable) {
            LOGGER.info(msg);
        }
    }

    public static void info(String tag, String msg) {
        info(String.format("[%s]: %s", tag, msg));
    }
}
