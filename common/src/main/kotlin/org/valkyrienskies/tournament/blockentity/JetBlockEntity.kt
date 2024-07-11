package org.valkyrienskies.tournament.blockentity

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import org.valkyrienskies.tournament.TournamentBlockEntities

class JetBlockEntity(pos: BlockPos, blockState: BlockState) :
    BlockEntity(TournamentBlockEntities.JET.get(), pos, blockState) {

}