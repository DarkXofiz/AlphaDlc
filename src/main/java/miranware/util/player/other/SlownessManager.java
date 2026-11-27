package miranware.util.player.other;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.Generated;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import miranware.util.IMinecraft;

public final class SlownessManager implements IMinecraft {
   private static final KeyBinding[] keybindsList = new KeyBinding[]{
      mc.options.forwardKey,
      mc.options.leftKey,
      mc.options.rightKey,
      mc.options.backKey,
      mc.options.jumpKey,
      mc.options.sprintKey,
      mc.options.sneakKey
   };
   private static final List<SlownessManager.SlowTask> tasks = new ArrayList<>();
   private static final List<SlownessManager.TimeTask> timeTasks = new ArrayList<>();
   private static SlownessManager.SlowTask lastTask = null;

   public static void addTask(SlownessManager.SlowTask task) {
      for (SlownessManager.SlowTask existing : tasks) {
         if (existing.runnable != null && task.runnable != null && existing.runnable.equals(task.runnable)) {
            return;
         }
      }

      lastTask = task;
      tasks.add(task);
   }

   public static void addTimeTask(SlownessManager.TimeTask task) {
      timeTasks.add(task);
   }

   public static void updateSlowTasks() {
      long now = System.currentTimeMillis();
      if (lastTask != null) {
         if (now - lastTask.time < lastTask.duration) {
            for (KeyBinding key : new KeyBinding[]{
               mc.options.forwardKey,
               mc.options.leftKey,
               mc.options.rightKey,
               mc.options.backKey,
               mc.options.jumpKey,
               mc.options.sprintKey
            }) {
               key.setPressed(false);
            }
         } else {
            for (KeyBinding key : keybindsList) {
               if (mc.currentScreen == null) {
                  key.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), key.getDefaultKey().getCode()));
               }
            }
         }
      }

      for (SlownessManager.SlowTask task : new ArrayList<>(tasks)) {
         if (now - task.time > task.duration - task.reflection && task.runnable != null) {
            task.runnable.run();
            task.runnable = null;
         }
      }

      Iterator<SlownessManager.SlowTask> iterator = tasks.iterator();

      while (iterator.hasNext()) {
         SlownessManager.SlowTask taskx = iterator.next();
         if (now - taskx.time > taskx.duration || !taskx.condition) {
            iterator.remove();
         }
      }
   }

   public static void updateTimeTasks(boolean ticksUpdate) {
      long now = System.currentTimeMillis();

      for (SlownessManager.TimeTask task : new ArrayList<>(timeTasks)) {
         if (ticksUpdate) {
            task.ticksExisted++;
         }

         if (task.mode == SlownessManager.TimeTask.Mode.TICKS ? task.ticksExisted >= task.time : now - task.time > task.duration && task.runnable != null) {
            task.runnable.run();
            task.runnable = null;
         }
      }

      Iterator<SlownessManager.TimeTask> iterator = timeTasks.iterator();

      while (iterator.hasNext()) {
         SlownessManager.TimeTask task = iterator.next();
         if ((task.mode == SlownessManager.TimeTask.Mode.TICKS ? task.ticksExisted >= task.time : now - task.time > task.duration) || !task.condition) {
            iterator.remove();
         }
      }
   }

   public static boolean slowTasksIsEmpty() {
      return tasks.isEmpty();
   }

   public static boolean timeTasksIsEmpty() {
      return timeTasks.isEmpty();
   }

   @Generated
   private SlownessManager() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }

   public static class SlowTask {
      public long duration;
      public long time;
      public long reflection;
      public Runnable runnable;
      public boolean condition;

      public SlowTask(long duration, long reflection, Runnable runnable) {
         this.duration = duration;
         this.reflection = reflection;
         this.runnable = runnable;
         this.time = System.currentTimeMillis();
         this.condition = true;
      }

      public SlowTask(long duration, long reflection, Runnable runnable, boolean condition) {
         this.duration = duration;
         this.reflection = reflection;
         this.runnable = runnable;
         this.time = System.currentTimeMillis();
         this.condition = condition;
      }

      public SlowTask(long duration, Runnable runnable) {
         this.duration = duration;
         this.reflection = 20L;
         this.runnable = runnable;
         this.time = System.currentTimeMillis();
         this.condition = true;
      }

      public SlowTask(long duration, Runnable runnable, boolean condition) {
         this.duration = duration;
         this.reflection = 20L;
         this.runnable = runnable;
         this.time = System.currentTimeMillis();
         this.condition = condition;
      }
   }

   public static class TimeTask {
      public long duration;
      public long time;
      public long ticksExisted;
      public Runnable runnable;
      public boolean condition;
      public SlownessManager.TimeTask.Mode mode;

      public TimeTask(long duration, Runnable runnable, boolean condition) {
         this.mode = SlownessManager.TimeTask.Mode.TIME;
         this.duration = duration;
         this.runnable = runnable;
         this.time = System.currentTimeMillis();
         this.condition = condition;
      }

      public TimeTask(SlownessManager.TimeTask.Mode mode, int ticks, Runnable runnable, boolean condition) {
         this.mode = mode;
         this.runnable = runnable;
         this.time = (long)ticks;
         this.condition = condition;
      }

      public static enum Mode {
         TIME,
         TICKS;
      }
   }
}
