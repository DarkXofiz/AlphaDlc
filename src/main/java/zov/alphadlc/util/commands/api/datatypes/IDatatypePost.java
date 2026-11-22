package zov.alphadlc.util.commands.api.datatypes;

import zov.alphadlc.util.commands.api.exception.CommandException;

public interface IDatatypePost<T, O> extends IDatatype {
   T apply(IDatatypeContext var1, O var2) throws CommandException;
}
