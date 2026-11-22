package zov.alphadlc.module.settings;

import java.util.function.Supplier;
import lombok.Generated;
import zov.alphadlc.util.render.math.Animation;
import zov.alphadlc.util.render.math.Easing;

public class BooleanSetting extends Setting {
   private final Animation animation = new Animation(Easing.QUINTIC_OUT, 400L);
   private final Animation clickAnimation = new Animation(Easing.QUINTIC_OUT, 400L);
   private boolean clicked;
   private boolean value;
   private int key = -1;

   public BooleanSetting(String name, boolean defaultValue) {
      super(name);
      this.value = defaultValue;
   }

   public boolean getValue() {
      return this.value;
   }

   @Override
   public String getValueAsString() {
      return Boolean.toString(this.value);
   }

   @Override
   public void setValueFromString(String value) {
      this.value = Boolean.parseBoolean(value);
   }

   public void toggle() {
      this.value = !this.value;
   }

   public BooleanSetting setVisible(Supplier<Boolean> visible) {
      this.visible = visible;
      return this;
   }

   @Generated
   public Animation getAnimation() {
      return this.animation;
   }

   @Generated
   public Animation getClickAnimation() {
      return this.clickAnimation;
   }

   @Generated
   public boolean isClicked() {
      return this.clicked;
   }

   @Generated
   public int getKey() {
      return this.key;
   }

   @Generated
   public void setClicked(boolean clicked) {
      this.clicked = clicked;
   }

   @Generated
   public void setValue(boolean value) {
      this.value = value;
   }

   @Generated
   public void setKey(int key) {
      this.key = key;
   }
}
