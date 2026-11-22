package zov.alphadlc.util.draggable;

import com.google.gson.annotations.Expose;
import lombok.Generated;
import net.minecraft.client.gui.screen.ChatScreen;
import zov.alphadlc.module.Module;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.render.math.MathUtil;

public class Draggable implements IMinecraft {
   @Expose
   private float x;
   @Expose
   private float y;
   @Expose
   public float initialXVal;
   @Expose
   public float initialYVal;
   private float startX;
   private float startY;
   private boolean dragging;
   private float width;
   private float height;
   private float wobbleAngle;
   private float wobbleVel;
   private float prevX;
   private boolean wobbleInit;
   @Expose
   private final String name;
   private final Module module;

   public Draggable(Module module, String name, float initialXVal, float initialYVal) {
      this.module = module;
      this.name = name;
      this.x = this.roundToHalf(initialXVal);
      this.y = this.roundToHalf(initialYVal);
      this.initialXVal = initialXVal;
      this.initialYVal = initialYVal;
   }

   public final void onDraw() {
      if (!(mc.currentScreen instanceof ChatScreen)) {
         this.updateWobble();
      } else {
         if (this.dragging) {
            int mouseX = this.normaliseX();
            int mouseY = this.normaliseY();
            this.x = this.roundToHalf((float)mouseX - this.startX);
            this.y = this.roundToHalf((float)mouseY - this.startY);
            int screenWidth = (int)MathUtil.calc((double)mc.getWindow().getScaledWidth());
            int screenHeight = (int)MathUtil.calc((double)mc.getWindow().getScaledHeight());
            this.x = Math.max(0.0F, Math.min(this.x, (float)screenWidth - this.width));
            this.y = Math.max(0.0F, Math.min(this.y, (float)screenHeight - this.height));
         }

         this.updateWobble();
      }
   }

   private void updateWobble() {
      this.prevX = this.x;
      this.wobbleAngle = 0.0F;
      this.wobbleVel = 0.0F;
   }

   public final void onClick(int button) {
      if (button == 0) {
         this.dragging = true;
         this.startX = (float)((int)((float)this.normaliseX() - this.x));
         this.startY = (float)((int)((float)this.normaliseY() - this.y));
      }
   }

   public boolean isHovering() {
      return (float)this.normaliseX() > Math.min(this.x, this.x + this.width)
         && (float)this.normaliseX() < Math.max(this.x, this.x + this.width)
         && (float)this.normaliseY() > Math.min(this.y, this.y + this.height)
         && (float)this.normaliseY() < Math.max(this.y, this.y + this.height);
   }

   public int normaliseX() {
      return (int)(mc.mouse.getX() / mc.getWindow().getScaleFactor());
   }

   public int normaliseY() {
      return (int)(mc.mouse.getY() / mc.getWindow().getScaleFactor());
   }

   public final void onRelease(int button) {
      if (button == 0) {
         this.dragging = false;
      }
   }

   private float roundToHalf(float value) {
      return (float)Math.round(value * 2.0F) / 2.0F;
   }

   @Generated
   public float getX() {
      return this.x;
   }

   @Generated
   public float getY() {
      return this.y;
   }

   @Generated
   public float getInitialXVal() {
      return this.initialXVal;
   }

   @Generated
   public float getInitialYVal() {
      return this.initialYVal;
   }

   @Generated
   public float getStartX() {
      return this.startX;
   }

   @Generated
   public float getStartY() {
      return this.startY;
   }

   @Generated
   public boolean isDragging() {
      return this.dragging;
   }

   @Generated
   public float getWidth() {
      return this.width;
   }

   @Generated
   public float getHeight() {
      return this.height;
   }

   @Generated
   public float getWobbleAngle() {
      return this.wobbleAngle;
   }

   @Generated
   public float getWobbleVel() {
      return this.wobbleVel;
   }

   @Generated
   public float getPrevX() {
      return this.prevX;
   }

   @Generated
   public boolean isWobbleInit() {
      return this.wobbleInit;
   }

   @Generated
   public String getName() {
      return this.name;
   }

   @Generated
   public Module getModule() {
      return this.module;
   }

   @Generated
   public void setX(float x) {
      this.x = x;
   }

   @Generated
   public void setY(float y) {
      this.y = y;
   }

   @Generated
   public void setInitialXVal(float initialXVal) {
      this.initialXVal = initialXVal;
   }

   @Generated
   public void setInitialYVal(float initialYVal) {
      this.initialYVal = initialYVal;
   }

   @Generated
   public void setStartX(float startX) {
      this.startX = startX;
   }

   @Generated
   public void setStartY(float startY) {
      this.startY = startY;
   }

   @Generated
   public void setDragging(boolean dragging) {
      this.dragging = dragging;
   }

   @Generated
   public void setWidth(float width) {
      this.width = width;
   }

   @Generated
   public void setHeight(float height) {
      this.height = height;
   }

   @Generated
   public void setWobbleAngle(float wobbleAngle) {
      this.wobbleAngle = wobbleAngle;
   }

   @Generated
   public void setWobbleVel(float wobbleVel) {
      this.wobbleVel = wobbleVel;
   }

   @Generated
   public void setPrevX(float prevX) {
      this.prevX = prevX;
   }

   @Generated
   public void setWobbleInit(boolean wobbleInit) {
      this.wobbleInit = wobbleInit;
   }
}
