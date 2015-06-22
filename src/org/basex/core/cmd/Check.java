package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.basex.build.DirParser;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Command;
import org.basex.core.Prop;
import org.basex.core.User;
import org.basex.core.Commands.CmdPerm;
import org.basex.data.Data;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.util.Util;

/**
 * Evaluates the 'checks' command, opens an existing database or
 * creates a new one.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Check extends Command {
  /**
   * Default constructor.
   * @param path file path
   */
  public Check(final String path) {
    super(STANDARD, path);
  }

  @Override
  protected boolean run() {
    // close existing database
    new Close().run(context);

    // get path and database name
    final String path = args[0];
    final String name = IO.get(path).dbname();

    // choose OPEN if user has no create permissions, or if database exists
    final boolean create = context.user.perm(User.CREATE);
    final Command cmd = !create || MetaData.found(path, name, mprop) ?
      new Open(name) : new CreateDB(name, path);

    // execute command
    final boolean ok = cmd.run(context);
    final String msg = cmd.info().trim();
    return ok ? info(msg) : error(msg);
  }

  /**
   * Opens the specified database; create a new one if it does not exist.
   * @param ctx database context
   * @param path document path
   * @return data reference
   * @throws IOException I/O exception
   */
  public static synchronized Data check(final Context ctx, final String path)
      throws IOException {

    // don't create new database if user has insufficient permissions
    final boolean create = ctx.user.perm(User.CREATE);

    final IO io = IO.get(path);
    final String name = io.dbname();

    // check if database is already opened
    final Data data = ctx.pin(name);
    if(data != null) {
      final IO in = IO.get(data.meta.original);
      final boolean found = !data.meta.original.isEmpty() && in.eq(io) &&
        io.timeStamp() == in.timeStamp();
      if(found && ctx.perm(User.READ, data.meta)) return data;
      Close.close(data, ctx);
      if(found) throw new BaseXException(PERM_NEEDED_X, CmdPerm.READ);
    }

    // choose OPEN if user has no create permissions, or if database exists
    if(!create || MetaData.found(path, name, ctx.mprop))
      return Open.open(name, ctx);

    // check if input is an existing file
    if(!io.exists() || io.isDir()) throw new FileNotFoundException(
        Util.info(FILE_NOT_FOUND_X, io));

    // if force flag is set to false, create a main memory instance
    if(!ctx.prop.is(Prop.FORCECREATE)) return CreateDB.mainMem(io, ctx);

    // otherwise, create a persistent database instance
    final DirParser dp = new DirParser(io, ctx.prop, ctx.mprop.dbpath(name));
    return CreateDB.create(name, dp, ctx);
  }
}
