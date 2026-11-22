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

public record BuiltTexture(
   SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness, float u, float v, float texWidth, float texHeight, int textureId
) implements IRenderer {
   private static final ShaderProgramKey TEXTURE_SHADER_KEY = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("texture"), VertexFormats.field_1575, Defines.field_53930
   );

   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      RenderSystem.setShaderTexture(0, this.textureId);
      float width = this.size.width();
      float height = this.size.height();
      ShaderProgram shader = RenderSystem.setShader(TEXTURE_SHADER_KEY);
      shader.method_34582("Size").method_1255(width, height);
      shader.method_34582("Radius").method_35657(this.radius.radius1(), this.radius.radius2(), this.radius.radius3(), this.radius.radius4());
      shader.method_34582("Smoothness").method_1251(this.smoothness);
      BufferBuilder builder = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1575);
      builder.method_22918(matrix, x, y, z).method_22913(this.u, this.v).method_39415(this.color.color1());
      builder.method_22918(matrix, x, y + height, z).method_22913(this.u, this.v + this.texHeight).method_39415(this.color.color2());
      builder.method_22918(matrix, x + width, y + height, z).method_22913(this.u + this.texWidth, this.v + this.texHeight).method_39415(this.color.color3());
      builder.method_22918(matrix, x + width, y, z).method_22913(this.u + this.texWidth, this.v).method_39415(this.color.color4());
      BufferRenderer.method_43433(builder.method_60800());
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }
}
