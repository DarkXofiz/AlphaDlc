package miranware.util.text;

import java.util.List;
import lombok.Generated;

public class BetterText {
   private List<String> texts;
   public final StringBuilder output = new StringBuilder();
   private int delay;
   private int textIndex = 0;
   private int charIndex = 0;
   private boolean forward = true;
   private long lastUpdateTime = System.currentTimeMillis();

   public BetterText(List<String> texts, int delay) {
      if (texts != null && !texts.isEmpty()) {
         this.texts = texts;
         this.delay = delay;
      } else {
         throw new IllegalArgumentException("Text list cannot be null or empty");
      }
   }

   public void update() {
      long currentTime = System.currentTimeMillis();
      if (currentTime - this.lastUpdateTime >= 100L) {
         this.lastUpdateTime = currentTime;
         String currentText = this.texts.get(this.textIndex);
         if (currentText == null || currentText.isEmpty()) {
            return;
         }

         if (this.forward) {
            if (this.charIndex < currentText.length()) {
               this.output.append(currentText.charAt(this.charIndex));
               this.charIndex++;
            } else {
               this.forward = false;
               this.lastUpdateTime = currentTime + (long)this.delay;
            }
         } else if (this.charIndex > 0) {
            this.output.deleteCharAt(this.charIndex - 1);
            this.charIndex--;
         } else {
            this.forward = true;
            this.textIndex = (this.textIndex + 1) % this.texts.size();
         }
      }
   }

   @Generated
   public List<String> getTexts() {
      return this.texts;
   }

   @Generated
   public StringBuilder getOutput() {
      return this.output;
   }

   @Generated
   public int getDelay() {
      return this.delay;
   }

   @Generated
   public int getTextIndex() {
      return this.textIndex;
   }

   @Generated
   public int getCharIndex() {
      return this.charIndex;
   }

   @Generated
   public boolean isForward() {
      return this.forward;
   }

   @Generated
   public long getLastUpdateTime() {
      return this.lastUpdateTime;
   }

   @Generated
   public void setTexts(List<String> texts) {
      this.texts = texts;
   }

   @Generated
   public void setDelay(int delay) {
      this.delay = delay;
   }

   @Generated
   public void setTextIndex(int textIndex) {
      this.textIndex = textIndex;
   }

   @Generated
   public void setCharIndex(int charIndex) {
      this.charIndex = charIndex;
   }

   @Generated
   public void setForward(boolean forward) {
      this.forward = forward;
   }

   @Generated
   public void setLastUpdateTime(long lastUpdateTime) {
      this.lastUpdateTime = lastUpdateTime;
   }

   @Generated
   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof BetterText other)) {
         return false;
      } else if (!other.canEqual(this)) {
         return false;
      } else if (this.getDelay() != other.getDelay()) {
         return false;
      } else if (this.getTextIndex() != other.getTextIndex()) {
         return false;
      } else if (this.getCharIndex() != other.getCharIndex()) {
         return false;
      } else if (this.isForward() != other.isForward()) {
         return false;
      } else if (this.getLastUpdateTime() != other.getLastUpdateTime()) {
         return false;
      } else {
         Object this$texts = this.getTexts();
         Object other$texts = other.getTexts();
         if (this$texts == null ? other$texts == null : this$texts.equals(other$texts)) {
            Object this$output = this.getOutput();
            Object other$output = other.getOutput();
            return this$output == null ? other$output == null : this$output.equals(other$output);
         } else {
            return false;
         }
      }
   }

   @Generated
   protected boolean canEqual(Object other) {
      return other instanceof BetterText;
   }

   @Generated
   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getDelay();
      result = result * 59 + this.getTextIndex();
      result = result * 59 + this.getCharIndex();
      result = result * 59 + (this.isForward() ? 79 : 97);
      long $lastUpdateTime = this.getLastUpdateTime();
      result = result * 59 + (int)($lastUpdateTime >>> 32 ^ $lastUpdateTime);
      Object $texts = this.getTexts();
      result = result * 59 + ($texts == null ? 43 : $texts.hashCode());
      Object $output = this.getOutput();
      return result * 59 + ($output == null ? 43 : $output.hashCode());
   }

   @Generated
   @Override
   public String toString() {
      return "BetterText(texts="
         + this.getTexts()
         + ", output="
         + this.getOutput()
         + ", delay="
         + this.getDelay()
         + ", textIndex="
         + this.getTextIndex()
         + ", charIndex="
         + this.getCharIndex()
         + ", forward="
         + this.isForward()
         + ", lastUpdateTime="
         + this.getLastUpdateTime()
         + ")";
   }
}
