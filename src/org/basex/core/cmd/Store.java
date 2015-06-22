package org.basex.core.cmd;

import static org.basex.core.Text.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.basex.core.CommandBuilder;
import org.basex.core.User;
import org.basex.data.MetaData;
import org.basex.io.IO;
import org.basex.io.IOContent;
import org.basex.io.IOFile;
import org.basex.io.in.BufferInput;
import org.basex.io.out.PrintOutput;
import org.xml.sax.InputSource;

/**
 * Evaluates the 'store' command and stores binary content into the database.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class Store extends ACreate {
  /**
   * Constructor, specifying a target path.
   * @param path target path
   */
  public Store(final String path) {
    this(path, null);
  }

  /**
   * Constructor, specifying a target path and an input.
   * @param path target path
   * @param input input file
   */
  public Store(final String path, final String input) {
    super(DATAREF | User.WRITE, path == null ? "" : path, input);
  }

  @Override
  protected boolean run() {
    final boolean create = context.user.perm(User.CREATE);
    String path = MetaData.normPath(args[0]);
    if(path == null || path.endsWith("."))
      return error(NAME_INVALID_X, args[0]);

    if(in == null) {
      final IO io = IO.get(args[1]);
      if(!io.exists() || io.isDir())
        return error(FILE_NOT_FOUND_X, create ? io : args[1]);
      in = io.inputSource();
      // set/add name of document
      if((path.isEmpty() || path.endsWith("/")) && !(io instanceof IOContent))
        path += io.name();
    }

    // ensure that the final name is not empty
    if(path.isEmpty()) return error(NAME_INVALID_X, path);

    // ensure that the name is not empty and contains no trailing dots
    final IOFile file = context.data().meta.binary(path);
    if(path.isEmpty() || path.endsWith(".") || file == null || file.isDir())
      return error(NAME_INVALID_X, create ? path : args[0]);

    // add directory if it does not exist anyway
    new IOFile(file.dir()).md();

    try {
      store(in, file);
    } catch(final IOException ex) {
      return error(FILE_NOT_STORED_X, ex.getMessage());
    }
    return info(QUERY_EXECUTED_X, perf);
  }

  /**
   * Stores the specified source to the specified file.
   * @param in input source
   * @param file target file
   * @throws IOException I/O exception
   */
  public static void store(final InputSource in, final IOFile file)
      throws IOException {

    // add directory if it does not exist anyway
    new IOFile(file.dir()).md();

    final PrintOutput po = new PrintOutput(file.path());
    try {
      final Reader r = in.getCharacterStream();
      final InputStream is = in.getByteStream();
      final String  id = in.getSystemId();
      if(r != null) {
        for(int c; (c = r.read()) != -1;) po.utf8(c);
      } else if(is != null) {
        for(int b; (b = is.read()) != -1;) po.write(b);
      } else if(id != null) {
        final BufferInput bi = IO.get(id).inputStream();
        try {
          for(int b; (b = bi.read()) != -1;) po.write(b);
        } finally {
          bi.close();
        }
      }
    } finally {
      po.close();
    }
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init().arg(TO, 0).arg(1);
  }
}
