package miranware.util.commands.api;

import miranware.util.commands.api.argparser.IArgParserManager;

public interface ICommandSystem {
   IArgParserManager getParserManager();
}
