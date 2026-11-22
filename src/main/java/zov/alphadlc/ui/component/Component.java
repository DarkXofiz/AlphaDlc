package zov.alphadlc.ui.component;

import lombok.Generated;
import zov.alphadlc.util.render.math.Animation;
import zov.alphadlc.util.render.math.Easing;

public abstract class Component implements IComponent {
   public float x;
   public float y;
   public float width;
   public float height;
   private final Animation alphaAnim = new Animation(Easing.BACK_OUT, 550L);
   private final Animation alphaAnimSetting = new Animation(Easing.CUBIC_OUT, 280L);
   private final Animation alphaAnimBack = new Animation(Easing.CUBIC_OUT, 280L);

   public boolean isVisible() {
      return true;
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
   public float getWidth() {
      return this.width;
   }

   @Generated
   public float getHeight() {
      return this.height;
   }

   @Generated
   public Animation getAlphaAnim() {
      return this.alphaAnim;
   }

   @Generated
   public Animation getAlphaAnimSetting() {
      return this.alphaAnimSetting;
   }

   @Generated
   public Animation getAlphaAnimBack() {
      return this.alphaAnimBack;
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
   public void setWidth(float width) {
      this.width = width;
   }

   @Generated
   public void setHeight(float height) {
      this.height = height;
   }
}
