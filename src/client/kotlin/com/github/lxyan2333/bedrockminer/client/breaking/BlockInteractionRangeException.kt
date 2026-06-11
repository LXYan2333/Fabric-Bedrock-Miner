package com.github.lxyan2333.bedrockminer.client.breaking

import net.minecraft.core.BlockPos

class BlockInteractionRangeException(val pos: BlockPos) :
    Exception("Block at $pos is out of interaction range")
