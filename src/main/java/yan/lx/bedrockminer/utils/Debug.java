package yan.lx.bedrockminer.utils;

import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Debug {
    private static Logger LOGGER = LoggerFactory.getLogger(Text.translatable("bedrockminer").toString());

    public static boolean enable = false;

    public static void info(String msg) {
        if (enable) {
            LOGGER.info(msg);
        }
    }
}
