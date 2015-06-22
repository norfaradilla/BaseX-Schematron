package org.basex.query.func;

import static org.basex.query.QueryText.*;

import java.io.*;

import org.basex.io.serial.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.item.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.util.*;

/**
 * Inline function.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class InlineFunc extends UserFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param r return type
   * @param v arguments
   * @param e function body
   * @param a annotations
   */
  public InlineFunc(final InputInfo ii, final SeqType r, final Var[] v,
      final Expr e, final Ann a) {
    super(ii, null, v, r, a);
    expr = e;
  }

  @Override
  public Expr comp(final QueryContext ctx) throws QueryException {
    comp(ctx, false);
    // only evaluate if the closure is empty, so we don't lose variables
    return expr.hasFreeVars(ctx) ? this : optPre(item(ctx, input), ctx);
  }

  @Override
  public FuncItem item(final QueryContext ctx, final InputInfo ii) {
    final FuncType ft = FuncType.get(this);
    final boolean c = ft.ret != null && !expr.type().instance(ft.ret);
    return new FuncItem(args, expr, ft, ctx.vars.locals(), c);
  }

  @Override
  public Value value(final QueryContext ctx) {
    return item(ctx, input);
  }

  @Override
  public ValueIter iter(final QueryContext ctx) {
    return value(ctx).iter();
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 || super.uses(u);
  }

  @Override
  public boolean removable(final Var v) {
    return false;
  }

  @Override
  public Expr remove(final Var v) {
    throw Util.notexpected(v);
  }

  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(int i = 0; i < args.length; ++i) {
      ser.attribute(Token.token(ARG + i), args[i].name.string());
    }
    expr.plan(ser);
    ser.closeElement();
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder(FUNCTION).append(PAR1);
    for(int i = 0; i < args.length; i++) {
      if(i > 0) sb.append(", ");
      sb.append(args[i].toString());
    }
    sb.append(PAR2).append(' ');
    if(ret != null) sb.append("as ").append(ret.toString()).append(' ');
    return sb.append("{ ").append(expr).append(" }").toString();
  }

  @Override
  boolean tco() {
    return false;
  }
}
