package com.github.lxyan2333.bedrockminer.client.render

import com.github.lxyan2333.bedrockminer.client.area.AreaRestriction
import com.github.lxyan2333.bedrockminer.client.config.Configs
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.pipeline.RenderTarget
import fi.dy.masa.malilib.MaLiLib
import fi.dy.masa.malilib.interfaces.IRenderer
import fi.dy.masa.malilib.render.MaLiLibPipelines
import fi.dy.masa.malilib.render.RenderContext
import fi.dy.masa.malilib.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderBuffers
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.core.BlockPos
import net.minecraft.util.profiling.ProfilerFiller
import org.joml.Matrix4fc
import org.joml.Vector4f

object AreaRenderer : IRenderer {
    override fun onRenderWorldLast(
        fb: RenderTarget,
        modelViewMatrix: Matrix4fc,
        cameraState: CameraRenderState,
        culling: Frustum,
        buffers: RenderBuffers,
        terrainFog: GpuBufferSlice,
        fogColor: Vector4f,
        profiler: ProfilerFiller
    ) {
        if (!Configs.Area.AREA_RESTRICTION_ENABLED.booleanValue) return

        val player = Minecraft.getInstance().player ?: return
        val eyePos = player.eyePosition

        profiler.push("bedrock_miner_area_restriction")
        for (area in AreaRestriction.configuredAreas()) {
            val pos1 = area.pos1
            val pos2 = area.pos2

            if (area.distanceToSqr(eyePos) > 64 * 64) continue

            renderAreaOutline(pos1, pos2)
        }
        profiler.pop()
    }

    private fun renderAreaOutline(pos1: BlockPos, pos2: BlockPos) {
        val cameraPos = RenderUtils.camPos()
        val minX = (minOf(pos1.x, pos2.x) - cameraPos.x).toFloat()
        val minY = (minOf(pos1.y, pos2.y) - cameraPos.y).toFloat()
        val minZ = (minOf(pos1.z, pos2.z) - cameraPos.z).toFloat()
        val maxX = (maxOf(pos1.x, pos2.x) + 1 - cameraPos.x).toFloat()
        val maxY = (maxOf(pos1.y, pos2.y) + 1 - cameraPos.y).toFloat()
        val maxZ = (maxOf(pos1.z, pos2.z) + 1 - cameraPos.z).toFloat()
        val color = Configs.Area.AREA_BOX_COLOR.color
        val pipeline = if (Configs.Area.HIDE_AREA_BOX_BEHIND_BLOCKS.booleanValue) {
            MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_LEQUAL_DEPTH
        } else {
            MaLiLibPipelines.DEBUG_LINES_MASA_SIMPLE_NO_DEPTH_NO_CULL
        }

        val ctx = RenderContext({ "bedrock-miner:area_restriction_outline" }, pipeline)
        try {
            val buffer = ctx.builder
            RenderUtils.drawBoxAllEdgesBatchedLines(
                minX, minY, minZ, maxX, maxY, maxZ, color, Configs.Area.AREA_BOX_LINE_WIDTH.floatValue, buffer
            )

            val meshData = buffer.build()
            if (meshData != null) {
                ctx.draw(meshData, false, true)
                meshData.close()
            }
        } catch (err: Exception) {
            MaLiLib.LOGGER.error("AreaRenderer.renderAreaOutline(): Draw exception; {}", err.message)
        } finally {
            ctx.close()
        }
    }
}
