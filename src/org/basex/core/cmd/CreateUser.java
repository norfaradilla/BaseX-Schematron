package org.basex.core.cmd;

import static org.basex.core.Text.*;

import org.basex.core.CommandBuilder;
import org.basex.core.Commands.Cmd;
import org.basex.core.Commands.CmdCreate;
import org.basex.data.MetaData;

/**
 * Evaluates the 'create user' command and creates a new user.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class CreateUser extends AUser {
  /**
   * Default constructor.
   * @param name user name
   * @param pw password
   */
  public CreateUser(final String name, final String pw) {
    super(name, pw);
  }

  @Override
  protected boolean run() {
    final String user = args[0];
    final String pass = args[1];
    if(!MetaData.validName(user, false)) return error(NAME_INVALID_X, user);
    return !isMD5(pass) ? error(PW_NOT_VALID) :
      context.users.create(user, pass) ? info(USER_CREATED_X, user) :
        error(USER_EXISTS_X, user);
  }

  @Override
  public void build(final CommandBuilder cb) {
    cb.init(Cmd.CREATE + " " + CmdCreate.USER).args();
  }
}
