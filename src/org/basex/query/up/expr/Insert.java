package org.basex.query.up.expr;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.StringTokenizer;
import static org.basex.query.QueryText.*;
import static org.basex.query.util.Err.*;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Constr;
import org.basex.query.expr.Expr;
import org.basex.query.item.ANode;
import org.basex.query.item.DBNode;
import org.basex.query.item.Item;
import org.basex.query.item.NodeType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeCache;
import org.basex.query.up.primitives.InsertAfter;
import org.basex.query.up.primitives.InsertAttribute;
import org.basex.query.up.primitives.InsertBefore;
import org.basex.query.up.primitives.InsertInto;
import org.basex.query.up.primitives.InsertIntoFirst;
import org.basex.query.up.primitives.UpdatePrimitive;
import org.basex.util.InputInfo;
import org.basex.util.Util;

/**
 * Insert expression.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class Insert extends Update {
  /** First flag. */
  private final boolean first;
  /** Last flag. */
  private final boolean last;
  /** Before flag. */
  private final boolean before;
  /** After flag. */
  private final boolean after;

  /**
   * Constructor.
   * @param ii input info
   * @param src source expression
   * @param f first flag
   * @param l last
   * @param b before
   * @param a after
   * @param trg target expression
   */
  public Insert(final InputInfo ii, final Expr src, final boolean f,
      final boolean l, final boolean b, final boolean a, final Expr trg) {
    super(ii, trg, src);
    first = f;
    last = l;
    before = b;
    after = a;
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {

    final Constr c = new Constr(ii, ctx).add(expr[1]);
    final NodeCache cList = c.children;
    final NodeCache aList = c.atts;
    if(c.errAtt) UPNOATTRPER.thrw(input);
    if(c.duplAtt != null) UPATTDUPL.thrw(input, c.duplAtt);

    // check target constraints
    final Iter t = ctx.iter(expr[0]);
    final Item i = t.next();
    if(i == null) UPSEQEMP.thrw(input, Util.name(this));
    if(!(i instanceof ANode) || t.next() != null)
      (before || after ? UPTRGTYP2 : UPTRGTYP).thrw(input);

    final ANode n = (ANode) i;
    final ANode par = n.parent();
    if(before || after) {
      if(n.type == NodeType.ATT || n.type == NodeType.DOC)
        UPTRGTYP2.thrw(input);
      if(par == null) UPPAREMPTY.thrw(input);
    } else {
      if(n.type != NodeType.ELM && n.type != NodeType.DOC)
        UPTRGTYP.thrw(input);
    }

    UpdatePrimitive up;
    DBNode dbn;
    // no update primitive is created if node list is empty
    if(aList.size() > 0) {
      final ANode targ = before || after ? par : n;
      if(targ.type != NodeType.ELM)
        (before || after ? UPATTELM : UPATTELM2).thrw(input);

      dbn = ctx.updates.determineDataRef(targ, ctx);
      up = new InsertAttribute(dbn.pre, dbn.data, input,
          checkNS(aList, targ, ctx));
      ctx.updates.add(up, ctx);
    }

    // no update primitive is created if node list is empty
    if(cList.size() > 0) {
      dbn = ctx.updates.determineDataRef(n, ctx);
      if(before) up = new InsertBefore(dbn.pre, dbn.data, input, cList);
      else if(after) up = new InsertAfter(dbn.pre, dbn.data, input, cList);
      else if(first) up = new InsertIntoFirst(dbn.pre, dbn.data, input, cList);
      else up = new InsertInto(dbn.pre, dbn.data, input, cList, last);
      ctx.updates.add(up, ctx);
    }
    return null;
  }

  
  @Override
  public String toString() {
    
   
             
    evaluateInsert(expr);
      
      //System.out.println(expr[1]+"    AND  "+expr[0]);

    return INSERT + ' ' + NODE + ' ' + expr[1] + ' ' + INTO + ' ' + expr[0];
  }
  
  
  
  
  public static void evaluateInsert(Expr[] expr){
      
         String output = "";
        //code to get path
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
            
            
            
      
        //code to get TARGETNAME and VALUE
        int ind =0;
        int z=0;
        
        //String insert= "element { \"Member\" } { attribute { \"Level\" } { \"gold\" }, element { \"Name\" } { \"Dilla\" }, element { \"FavoriteColor\" } { \"purple\" } } ";
        String insert= expr[1].toString();
        StringTokenizer initial  = new StringTokenizer(insert, "{");
        
                
        if(initial.countTokens() > 3 ){        
        //if(insert.charAt(0) == 'e' ){
            ind = insert.indexOf("}");  
        
            String firstele = insert.substring(insert.indexOf("\""), ind);
            firstele=firstele.replaceAll("\"", "").trim();
            path = path+"/"+firstele;
        
            insert = insert.substring(ind+4, insert.length()-1);
             
            StringTokenizer st = new StringTokenizer(insert, ",");

            String[] arr= new String[st.countTokens()];

                int i=0;
            while(st.hasMoreTokens()){
                arr[i]= st.nextToken();
                i++;
            }

            StringTokenizer st_sub= null;
            String[] arr_sub = null;
            String[] arr_subF = new String[i * 2 + 5];
            //arr_subF[0]= first_target;
            //arr_subF[1]= first_value;
            int y=0;
            z=0;
            for(int x= 0; x < i; x++){
                st_sub = new StringTokenizer(arr[x],"}");
                arr_sub = new String[st_sub.countTokens()];

                 y=0;
                while(st_sub.hasMoreTokens()){
                    arr_sub[y] =st_sub.nextToken();                
                    arr_subF[z] = arr_sub[y]; z++;  
                    y++;
                }
            }
            for(int x = 0; x< z-1 ; x=x+2){

                String targetname= arr_subF[x];
                String value = arr_subF[x+1];

                if(targetname.contains("attribute")||targetname.contains("element")){

                    targetname =  targetname.replaceAll("attribute", "").trim();
                    targetname =  targetname.replaceAll("element", "").trim();

                }
                targetname = targetname.substring(targetname.indexOf("\"")+1, targetname.length()-1);
                value = value.substring(value.indexOf("\"")+1, value.length()-2);

                //System.out.println(targetname+"~"+path+"~"+value);
                
                 output= targetname+"~"+path +"~"+value ;
                              System.out.println(output);


            }

        }
        else{
                        
            StringTokenizer st = new StringTokenizer(insert, "{");

            String[] arr= new String[st.countTokens()];
            String targetname="", value = "";

            int i=0;
            while(st.hasMoreTokens()){
                arr[i]= st.nextToken().trim();
                
                if(i==1){
                   targetname= arr[i];
                }
                if(i==2){
                   value = arr[i];

                }
                i++;
            }    
            
            if(targetname.contains("\"")||targetname.contains("}")){
                
                targetname =  targetname.replaceAll("\"", "").trim();
                targetname =  targetname.replaceAll("}", "").trim();

            }
            if(value.contains("\"")||value.contains("}")){
                
                value =  value.replaceAll("\"", "").trim();
                value =  value.replaceAll("}", "").trim();

            }
            
             output= targetname+"~"+path +"~"+value ;
             
             //System.out.println(output);
            //System.out.println(targetname+"~"+path+"~"+value);
                //System.out.println(arr[i]);
            
        }
        
        try{
        //BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                         //System.out.println(output);


        Writer out; 
        out = new BufferedWriter(new FileWriter("pre_ins.txt", true));
        out.write(output+"\n");
        out.close();
        }catch(IOException e){
            System.err.print("Unable to create file");
        }
      
  }
}
