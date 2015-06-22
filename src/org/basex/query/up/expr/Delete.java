package org.basex.query.up.expr;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.iter.Iter;
import org.basex.query.up.primitives.DeleteNode;
import org.basex.util.InputInfo;

/**
 * Delete expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class Delete extends Update {
  /**
   * Constructor.
   * @param ii input info
   * @param r return expression
   */
  public Delete(final InputInfo ii, final Expr r) {
    super(ii, r);
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Iter t = ctx.iter(expr[0]);
    for(Item i; (i = t.next()) != null;) {
      if(!(i instanceof ANode)) UPTRGDELEMPT.thrw(input);
      final ANode n = (ANode) i;
      // nodes without parents are ignored
      if(n.parent() == null) continue;
      final DBNode dbn = ctx.updates.determineDataRef(n, ctx);
      ctx.updates.add(new DeleteNode(dbn.pre, dbn.data, input), ctx);
    }
    return null;
  }

  @Override
  public String toString() {
      
    evaluateDelete(expr);
    
    return DELETE + ' ' + NODES + ' ' + expr[0];
  }
  
  public static void evaluateDelete(Expr[] expr){
      
        String output ="";
        String path = expr[0].toString();          
        
        if(path.contains("position()")||path.contains("descendant")||path.contains(":")||path.contains("*")||path.contains("/null")||path.contains("-or-selfnode()")||path.contains("ancestor")){
            path = path.replace("position() = ", "");
            path = path.replace(":", "");
            path = path.replace("*", "");
            path = path.replace("descendant", "");
            path = path.replace("-or-selfnode()", "");
            path = path.replace("ancestor", "");
            path = path.replace("/null", "");

        }
        

        
        output = "null" + "~"+path + "~null";
        
               try{
        //BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        Writer out; 
        out = new BufferedWriter(new FileWriter("pre_del.txt", true));
        out.write(output+"\n");
        out.close();
        }catch(IOException e){
            System.err.print("Unable to create file");
        }
        
        //System.out.println(null+"~"+path+"~"+null);
            
  }
}
