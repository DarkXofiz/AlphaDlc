package zov.alphadlc.util.render.renderers.impl;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.SimpleFramebuffer;
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

public record BuiltBlur(SizeState size, QuadRadiusState radius, QuadColorState color, float smoothness, float blurRadius) implements IRenderer {
   private static final ShaderProgramKey BLUR_SHADER_KEY = new ShaderProgramKey(
      ResourceProvider.getShaderIdentifier("blur"), VertexFormats.field_1576, Defines.field_53930
   );
   private static final Supplier<SimpleFramebuffer> TEMP_FBO_SUPPLIER = Suppliers.memoize(() -> new SimpleFramebuffer(1920, 1024, false));
   private static boolean frameSnapshotCaptured = false;

   public static void beginFrame() {
      frameSnapshotCaptured = false;
   }

   @Override
   public void render(Matrix4f matrix, float x, float y, float z) {
      MinecraftClient client = MinecraftClient.method_1551();
      Framebuffer main = client.method_1522();
      SimpleFramebuffer fbo = (SimpleFramebuffer)TEMP_FBO_SUPPLIER.get();
      boolean resized = false;
      if (fbo.field_1482 != main.field_1482 || fbo.field_1481 != main.field_1481) {
         fbo.method_1234(main.field_1482, main.field_1481);
         resized = true;
      }

      RenderSystem.enableBlend();
      RenderSystem.defaultBlendFunc();
      RenderSystem.disableCull();
      boolean canReuse = client.field_1755 == null;
      if (!canReuse || resized || !frameSnapshotCaptured) {
         fbo.method_1235(false);
         main.method_1237(fbo.field_1482, fbo.field_1481);
         frameSnapshotCaptured = canReuse;
      }

      main.method_1235(false);
      RenderSystem.setShaderTexture(0, fbo.method_30277());
      float width = this.size.width();
      float height = this.size.height();
      ShaderProgram shader = RenderSystem.setShader(BLUR_SHADER_KEY);
      shader.method_34582("Size").method_1255(width, height);
      shader.method_34582("Radius").method_35657(this.radius.radius1(), this.radius.radius2(), this.radius.radius3(), this.radius.radius4());
      shader.method_34582("Smoothness").method_1251(this.smoothness);
      shader.method_34582("BlurRadius").method_1251(this.blurRadius);
      BufferBuilder builder = Tessellator.method_1348().method_60827(DrawMode.field_27382, VertexFormats.field_1576);
      builder.method_22918(matrix, x, y, z).method_39415(this.color.color1());
      builder.method_22918(matrix, x, y + height, z).method_39415(this.color.color2());
      builder.method_22918(matrix, x + width, y + height, z).method_39415(this.color.color3());
      builder.method_22918(matrix, x + width, y, z).method_39415(this.color.color4());
      BufferRenderer.method_43433(builder.method_60800());
      RenderSystem.setShaderTexture(0, 0);
      RenderSystem.enableCull();
      RenderSystem.disableBlend();
   }
}
