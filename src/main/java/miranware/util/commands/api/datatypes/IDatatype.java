package miranware.util.commands.api.datatypes;

import java.util.stream.Stream;
import miranware.util.IMinecraft;
import miranware.util.commands.api.exception.CommandException;

public interface IDatatype extends IMinecraft {
   Stream<String> tabComplete(IDatatypeContext var1) throws CommandException;
}
