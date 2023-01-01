package yan.lx.bedrockminer.model;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public record PistonInfo(Block block, BlockPos pos, Direction direction) {

}
