package org.basex.test.server;

import static org.basex.core.Text.*;

import java.io.IOException;

import org.basex.BaseXServer;
import org.basex.core.MainProp;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateUser;
import org.basex.core.cmd.Kill;
import org.basex.server.ClientSession;
import org.basex.util.Performance;
import org.basex.util.Token;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This class tests the database commands with the client/server
 * architecture.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class ServerCommandTest extends CommandTest {
  /** Server instance. */
  private static BaseXServer server;

  /**
   * Starts the server.
   * @throws IOException I/O exception
   */
  @BeforeClass
  public static void start() throws IOException {
    CONTEXT.mprop.set(MainProp.DBPATH, sandbox().path());
    server = new BaseXServer(CONTEXT, "-z", "-p9999", "-e9998");
    session = new ClientSession(LOCALHOST, 9999, ADMIN, ADMIN);
    cleanUp();
  }

  /**
   * Stops the server.
   * @throws IOException I/O exception
   */
  @AfterClass
  public static void finish() throws IOException {
    try {
      if(session != null) session.close();
    } catch(final Exception ex) {
      fail(Util.message(ex));
    }
    // stop server instance
    if(server != null) server.stop();

    assertTrue(sandbox().delete());
    CONTEXT.close();
  }

  /**
   * Kill test.
   * @throws IOException on server error
   */
  @Test
  public void kill() throws IOException {
    ok(new Kill(ADMIN));
    ok(new Kill(ADMIN + '2'));
    ok(new Kill(Prop.NAME + '*'));
    ok(new CreateUser(NAME2, Token.md5("test")));
    final ClientSession cs = new ClientSession(LOCALHOST, 9999, NAME2, "test");
    ok(new Kill(NAME2));
    ok(new Kill(NAME2 + '?'));
    cs.close();
    // may be superfluous
    Performance.sleep(100);
  }
}
