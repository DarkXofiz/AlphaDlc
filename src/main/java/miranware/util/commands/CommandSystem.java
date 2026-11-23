package miranware.util.commands;

import miranware.util.commands.api.ICommandSystem;
import miranware.util.commands.api.argparser.IArgParserManager;
import miranware.util.commands.argparser.ArgParserManager;

public enum CommandSystem implements ICommandSystem {
   INSTANCE;

   @Override
   public IArgParserManager getParserManager() {
      return ArgParserManager.INSTANCE;
   }
}
