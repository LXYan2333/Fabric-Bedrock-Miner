package yan.lx.bedrockminer;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BedrockMinerMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Bedrock Miner");

    @Override
    public void onInitialize() {
        Debug.info("模组初始化成功");
    }
}
