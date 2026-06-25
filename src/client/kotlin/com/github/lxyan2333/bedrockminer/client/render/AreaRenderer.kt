package com.github.lxyan2333.bedrockminer.client.render

import com.github.lxyan2333.bedrockminer.client.area.AreaRestriction
import com.github.lxyan2333.bedrockminer.client.config.Configs
//? if >=26.1 {
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.pipeline.RenderTarget
//?}
//? if <1.20.5 {
/*import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
*///?}
//? if >=1.21.11 {
import fi.dy.masa.malilib.MaLiLib
import fi.dy.masa.malilib.render.MaLiLibPipelines
import fi.dy.masa.malilib.render.RenderContext
//?}
import fi.dy.masa.malilib.interfaces.IRenderer
import fi.dy.masa.malilib.render.RenderUtils
import net.minecraft.client.Minecraft
//? if <1.20.5
//import net.minecraft.client.renderer.GameRenderer
//? if >=26.1 {
import net.minecraft.client.renderer.RenderBuffers
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.util.profiling.ProfilerFiller
//?}
import net.minecraft.core.BlockPos
//? if >=26.1 {
import org.joml.Matrix4fc
import org.joml.Vector4f
//?} else if >=1.20 {
/*import org.joml.Matrix4f*///?}
//? if <1.20
//import com.mojang.math.Matrix4f


object AreaRenderer : IRenderer {
    //? if >=26.1 {
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
        profiler.push("bedrock_miner_area_restriction")
        renderAreas()
        profiler.pop()
    }
    //?} else if >=1.20.5 {
    /*override fun onRenderWorldLast(
        posMatrix: Matrix4f,
        projMatrix: Matrix4f
    ) {
        renderAreas()
    }
    *///?} else {
    /*override fun onRenderWorldLast(
        matrixStack: PoseStack,
        projMatrix: Matrix4f
    ) {
        renderAreas()
    }
    *///?}

    private fun renderAreas() {
        if (!Configs.Area.AREA_RESTRICTION_ENABLED.booleanValue) return

        val player = Minecraft.getInstance().player ?: return
        val eyePos = player.eyePosition

        for (area in AreaRestriction.configuredAreas()) {
            val pos1 = area.pos1
            val pos2 = area.pos2

            if (area.distanceToSqr(eyePos) > 64 * 64) continue

            renderAreaOutline(pos1, pos2)
        }
    }

    private fun renderAreaOutline(pos1: BlockPos, pos2: BlockPos) {
        //? if >=1.21.11 {
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
        //?} else if >=1.20.5 {
        /*val color = Configs.Area.AREA_BOX_COLOR.color
        RenderUtils.renderAreaOutline(
            pos1,
            pos2,
            Configs.Area.areaBoxLineWidth,
            color,
            color,
            color,
            Minecraft.getInstance()
        )
        *///?} else {
        /*renderAreaOutlineLegacy(pos1, pos2)
        *///?}
    }

    //? if <1.20.5 {
    /*private fun renderAreaOutlineLegacy(pos1: BlockPos, pos2: BlockPos) {
        val mc = Minecraft.getInstance()
        val cameraPos = mc.gameRenderer.mainCamera.position
        val minX = minOf(pos1.x, pos2.x) - cameraPos.x
        val minY = minOf(pos1.y, pos2.y) - cameraPos.y
        val minZ = minOf(pos1.z, pos2.z) - cameraPos.z
        val maxX = maxOf(pos1.x, pos2.x) + 1 - cameraPos.x
        val maxY = maxOf(pos1.y, pos2.y) + 1 - cameraPos.y
        val maxZ = maxOf(pos1.z, pos2.z) + 1 - cameraPos.z
        val color = Configs.Area.AREA_BOX_COLOR.color
        val depthEnabled = Configs.Area.HIDE_AREA_BOX_BEHIND_BLOCKS.booleanValue

        RenderSystem.lineWidth(Configs.Area.areaBoxLineWidth)
        RenderSystem.setShader(GameRenderer::getPositionColorShader)
        if (!depthEnabled) {
            RenderSystem.disableDepthTest()
        }

        val tessellator = Tesselator.getInstance()
        val buffer = tessellator.builder
        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR)
        RenderUtils.drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, color, buffer)
        tessellator.end()

        if (!depthEnabled) {
            RenderSystem.enableDepthTest()
        }
    }
    *///?}
}
