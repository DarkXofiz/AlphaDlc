package zov.alphadlc.util.commands.api.datatypes;

import java.util.stream.Stream;
import zov.alphadlc.util.IMinecraft;
import zov.alphadlc.util.commands.api.exception.CommandException;

public interface IDatatype extends IMinecraft {
   Stream<String> tabComplete(IDatatypeContext var1) throws CommandException;
}
