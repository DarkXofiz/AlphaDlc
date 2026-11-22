package zov.alphadlc.util.render.renderers.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexFormat.DrawMode;
import org.joml.Matrix4f;
import zov.alphadlc.util.render.builders.states.QuadColorState;
import zov.alphadlc.util.render.builders.states.QuadRadiusState;
import zov.alphadlc.util.render.builders.states.SizeState;
import zov.alphadlc.util.render.providers.ResourceProvider;
import zov.alphadlc.util.render.renderers.IRenderer;

public record BuiltGlow(SizeState size, QuadRadiusState radius, QuadColorState color, float glowRadius, float softness, float intensity, boolean additive)
   implements IRenderer {
   private static final ShaderProgramKey GLOW_SHADER_KEY = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("glow"), VertexFormats.field_1576, Defines.field_53930
   );

   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      RenderSystem.enableBlend();
      RenderSystem.disableCull();
      RenderSystem.defaultBlendFunc();
      float w = this.size.width();
      float h = this.size.height();
      ShaderProgram shader = RenderSystem.setShader(GLOW_SHADER_KEY);
      shader.method_34582("Size").method_1255(w, h);
      shader.method_34582("Radius").method_35657(this.radius.radius1(), this.radius.radius2(), this.radius.radius3(), this.radius.radius4());
      shader.method_34582("GlowRadius").method_1251(this.glowRadius);
      shader.method_34582("Softness").method_1251(this.softness);
      shader.method_34582("Intensity").method_1251(this.intensity);
      BufferBuilder bb = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1576);
      bb.method_22918(matrix, x, y, z).method_39415(this.color.color1());
      bb.method_22918(matrix, x, y + h, z).method_39415(this.color.color2());
      bb.method_22918(matrix, x + w, y + h, z).method_39415(this.color.color3());
      bb.method_22918(matrix, x + w, y, z).method_39415(this.color.color4());
      BufferRenderer.method_43433(bb.method_60800());
      RenderSystem.defaultBlendFunc();
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }
}
