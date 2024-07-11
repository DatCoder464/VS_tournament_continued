package org.valkyrienskies.tournament.blocks

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.valkyrienskies.tournament.blockentity.JetBlockEntity

class JetBlock: BaseEntityBlock(Properties.of()) {
    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity =
        JetBlockEntity(pos, state)
}