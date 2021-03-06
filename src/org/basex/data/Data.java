package org.basex.data;

import static org.basex.util.Token.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.basex.core.cmd.InfoStorage;
import org.basex.index.IdPreMap;
import org.basex.index.Index;
import org.basex.index.IndexIterator;
import org.basex.index.IndexToken;
import org.basex.index.IndexToken.IndexType;
import org.basex.index.Names;
import org.basex.index.Resources;
import org.basex.index.path.PathSummary;
import org.basex.io.IO;
import org.basex.io.random.TableAccess;
import org.basex.util.Atts;
import org.basex.util.TokenBuilder;
import org.basex.util.Util;
import org.basex.util.hash.TokenMap;
import org.basex.util.list.IntList;

/**
 * This class provides access to the database storage.
 * Note that the methods of this class are optimized for performance.
 * They will not check if correct data is requested, i.e. if a text is
 * requested, a pre value must points to a text node.
 *
 * All nodes in the table are accessed by their
 * implicit pre value. The following restrictions are imposed on the data:
 * <ul>
 * <li>The table is limited to 2^31 entries (pre values are signed int's)</li>
 * <li>A maximum of 2^15 different tag and attribute names is allowed</li>
 * <li>A maximum of 2^8 different namespaces is allowed</li>
 * <li>A tag can have a maximum of 32 attributes</li>
 * </ul>
 * Each node occupies 128 bits. The current storage layout looks as follows:
 *
 * <pre>
 * COMMON ATTRIBUTES:
 * - Byte     0:  KIND: Node kind (bits: 2-0)
 * - Byte 12-15:  UNID: Unique Node ID
 * DOCUMENT NODES (kind = 0):
 * - Byte  3- 7:  TEXT: Text reference
 * - Byte  8-11:  SIZE: Number of descendants
 * ELEMENT NODES (kind = 1):
 * - Byte     0:  ATTS: Number of attributes (bits: 7-3).
 *                      Calculated in real-time, if bit range is too small
 * - Byte  1- 2:  NAME: Namespace Flag (bit: 15), Name (bits: 14-0)
 * - Byte     3:  NURI: Namespace URI
 * - Byte  4- 7:  DIST: Distance to parent node
 * - Byte  8-11:  SIZE: Number of descendants
 * TEXT, COMMENT, PI NODES (kind = 2, 4, 5):
 * - Byte  3- 7:  TEXT: Text reference
 * - Byte  8-11:  DIST: Distance to parent node
 * ATTRIBUTE NODES (kind = 3):
 * - Byte     0:  DIST: Distance to parent node (bits: 7-3)
 *                      Calculated in real-time, if bit range is too small
 * - Byte  1- 2:  NAME: Namespace Flag (bit: 15), Name (bits: 14-0)
 * - Byte  3- 7:  TEXT: Attribute value reference
 * - Byte    11:  NURI: Namespace (bits: 7-3)
 * </pre>
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public abstract class Data {
  /** Node kind: Document. */
  public static final byte DOC = 0x00;
  /** Node kind: Element. */
  public static final byte ELEM = 0x01;
  /** Node kind: Text. */
  public static final byte TEXT = 0x02;
  /** Node kind: Attribute. */
  public static final byte ATTR = 0x03;
  /** Node kind: Comment. */
  public static final byte COMM = 0x04;
  /** Node kind: Processing Instruction. */
  public static final byte PI = 0x05;

  /** Meta data. */
  public MetaData meta;
  /** Tag index. */
  public Names tagindex;
  /** Attribute name index. */
  public Names atnindex;
  /** Namespace index. */
  public Namespaces nspaces;
  /** Path summary index. */
  public PathSummary paths;
  /** Resource index. */
  public final Resources resources = new Resources(this);
  /** Text index. */
  public Index txtindex;
  /** Attribute value index. */
  public Index atvindex;
  /** Full-text index instance. */
  public Index ftxindex;

  /** Index reference for a name attribute. */
  public int nameID;
  /** Index reference for a size attribute. */
  public int sizeID;

  /** Table access file. */
  TableAccess table;
  /** ID->PRE mapping. */
  IdPreMap idmap;

  /**
   * Dissolves the references to often used tag names and attributes.
   * @throws IOException I/O exception
   */
  @SuppressWarnings("unused")
  public void init() throws IOException {
    nameID = atnindex.id(DataText.T_NAME);
    sizeID = atnindex.id(DataText.T_SIZE);
  }

  /**
   * Closes the current database.
   * @throws IOException I/O exception
   */
  public abstract void close() throws IOException;

  /**
   * Flushes the database.
   */
  public abstract void flush();

  /**
   * Checks if the database contains no documents.
   * Empty databases can be recognized by a single document node.
   * @return result of check
   */
  public final boolean isEmpty() {
    return meta.size == 1 && kind(0) == DOC;
  }

  /**
   * Checks if the database contains a single document.
   * @return result of check
   */
  public final boolean single() {
    return meta.size == size(0, DOC);
  }

  /**
   * Closes the specified index.
   * @param type index to be closed
   * @throws IOException I/O exception
   */
  public abstract void closeIndex(IndexType type) throws IOException;

  /**
   * Assigns the specified index.
   * @param type index to be opened
   * @param index index instance
   */
  public abstract void setIndex(IndexType type, Index index);

  /**
   * Marks a database as updating.
   * @param updating updating flag
   * @return result flag
   */
  public abstract boolean updating(final boolean updating);

  /**
   * Returns the indexed pre references for the specified token.
   * @param token index token reference
   * @return array of sorted pre values
   */
  public final IndexIterator iter(final IndexToken token) {
    return index(token.type()).iter(token);
  }

  /**
   * Returns the number of indexed pre references for the specified token.
   * @param token text to be found
   * @return number of hits
   */
  public final int count(final IndexToken token) {
    return index(token.type()).count(token);
  }

  /**
   * Returns info on the specified index structure.
   * @param type index type
   * @return info
   */
  public final byte[] info(final IndexType type) {
    return index(type).info();
  }

  /**
   * Returns the index reference for the specified index type.
   * @param type index type
   * @return index
   */
  final Index index(final IndexType type) {
    switch(type) {
      case TAG:       return tagindex;
      case ATTNAME:   return atnindex;
      case TEXT:      return txtindex;
      case ATTRIBUTE: return atvindex;
      case FULLTEXT:  return ftxindex;
      case PATH:      return paths;
      default:        throw Util.notexpected();
    }
  }

  /**
   * Returns an atomized content for any node kind.
   * The atomized value can be an attribute value or XML content.
   * @param pre pre value
   * @return atomized value
   */
  public final byte[] atom(final int pre) {
    switch(kind(pre)) {
      case TEXT: case COMM:
        return text(pre, true);
      case ATTR:
        return text(pre, false);
      case PI:
        byte[] txt = text(pre, true);
        final int i = indexOf(txt, ' ');
        return i == -1 ? EMPTY : substring(txt, i + 1);
      default:
        // create atomized text node
        TokenBuilder tb = null;
        byte[] t = EMPTY;
        int p = pre;
        final int s = p + size(p, kind(p));
        while(p != s) {
          final int k = kind(p);
          if(k == TEXT) {
            txt = text(p, true);
            if(t == EMPTY) {
              t = txt;
            } else {
              if(tb == null) tb = new TokenBuilder(t);
              tb.add(txt);
            }
          }
          p += attSize(p, k);
        }
        return tb == null ? t : tb.finish();
    }
  }

  // RETRIEVING VALUES ========================================================

  /**
   * Returns a pre value.
   * @param id unique node id
   * @return pre value or -1 if id was not found
   */
  final int preold(final int id) {
    // find pre value in table
    for(int p = Math.max(0, id); p < meta.size; ++p)
      if(id == id(p)) return p;
    for(int p = 0, ps = Math.min(meta.size, id); p < ps; ++p)
      if(id == id(p)) return p;

    // id not found
    return -1;
  }

  /**
   * Returns a pre value.
   * @param id unique node id
   * @return pre value or -1 if id was not found
   */
  public final int pre(final int id) {
    if(meta.updindex) return idmap.pre(id);
    return preold(id);
  }

  /**
   * Returns pre values.
   * @param ids unique node ids
   * @param off start offset
   * @param len number of ids
   * @return sorted pre values
   */
  public final int[] pre(final int[] ids, final int off, final int len) {
    if(meta.updindex) return idmap.pre(ids, off, len);

    final IntList p = new IntList(ids.length);
    for(int i = off; i < len; ++i) p.add(preold(ids[i]));
    return p.sort().toArray();
  }

  /**
   * Returns a unique node id.
   * @param pre pre value
   * @return node id
   */
  public final int id(final int pre) {
    return table.read4(pre, 12);
  }

  /**
   * Returns a node kind.
   * @param pre pre value
   * @return node kind
   */
  public final int kind(final int pre) {
    return table.read1(pre, 0) & 0x07;
  }

  /**
   * Returns a pre value of the parent node.
   * @param pre pre value
   * @param kind node kind
   * @return pre value of the parent node
   */
  public final int parent(final int pre, final int kind) {
    return pre - dist(pre, kind);
  }

  /**
   * Returns the distance of the specified node.
   * @param pre pre value
   * @param kind node kind
   * @return distance
   */
  private int dist(final int pre, final int kind) {
    switch(kind) {
      case ELEM:
        return table.read4(pre, 4);
      case TEXT:
      case COMM:
      case PI:
        return table.read4(pre, 8);
      case ATTR:
        int d = table.read1(pre, 0) >> 3 & IO.MAXATTS;
        // skip additional attributes, if value is larger than maximum range
        if(d >= IO.MAXATTS) while(d < pre && kind(pre - d) == ATTR) d++;
        return d;
      default:
        return pre + 1;
    }
  }

  /**
   * Returns a size value (number of descendant table entries).
   * @param pre pre value
   * @param kind node kind
   * @return size value
   */
  public final int size(final int pre, final int kind) {
    return kind == ELEM || kind == DOC ? table.read4(pre, 8) : 1;
  }

  /**
   * Returns a number of attributes.
   * @param pre pre value
   * @param kind node kind
   * @return number of attributes
   */
  public final int attSize(final int pre, final int kind) {
    int s = kind == ELEM ? table.read1(pre, 0) >> 3 & IO.MAXATTS : 1;
    // skip additional attributes, if value is larger than maximum range
    if(s >= IO.MAXATTS) while(s < meta.size - pre && kind(pre + s) == ATTR) s++;
    return s;
  }

  /**
   * Finds the specified attribute and returns its value.
   * @param att the attribute id of the attribute to be found
   * @param pre pre value
   * @return attribute value
   */
  public final byte[] attValue(final int att, final int pre) {
    final int a = pre + attSize(pre, kind(pre));
    int p = pre;
    while(++p != a) if(name(p) == att) return text(p, false);
    return null;
  }

  /**
   * Returns a reference to the tag or attribute name id.
   * @param pre pre value
   * @return token reference
   */
  public final int name(final int pre) {
    return table.read2(pre, 1) & 0x7FFF;
  }

  /**
   * Returns a tag, attribute or pi name.
   * @param pre pre value
   * @param kind node kind
   * @return name reference
   */
  public final byte[] name(final int pre, final int kind) {
    if(kind == PI) {
      final byte[] name = text(pre, true);
      final int i = indexOf(name, ' ');
      return i == -1 ? name : substring(name, 0, i);
    }
    return (kind == ELEM ? tagindex : atnindex).key(name(pre));
  }

  /**
   * Returns a reference to the namespace of the addressed element or attribute.
   * @param pre pre value
   * @param kind node kind
   * @return namespace URI
   */
  public final int uri(final int pre, final int kind) {
    return kind == ELEM || kind == ATTR ?
        table.read1(pre, kind == ELEM ? 3 : 11) & 0xFF : 0;
  }

  /**
   * Returns the namespace flag of the addressed element.
   * @param pre pre value
   * @return namespace flag
   */
  public final boolean nsFlag(final int pre) {
    return (table.read1(pre, 1) & 0x80) != 0;
  }

  /**
   * Returns all namespace keys and values.
   * Should be only called for element nodes.
   * @param pre pre value
   * @return key and value ids
   */
  public final Atts ns(final int pre) {
    final Atts as = new Atts();
    if(nsFlag(pre)) {
      final int[] nsp = nspaces.get(pre);
      for(int n = 0; n < nsp.length; n += 2)
        as.add(nspaces.prefix(nsp[n]), nspaces.uri(nsp[n + 1]));
    }
    return as;
  }

  /**
   * Returns the disk offset of a text (text, comment, pi) or attribute value.
   * @param pre pre value
   * @return disk offset
   */
  final long textOff(final int pre) {
    return table.read5(pre, 3);
  }

  /**
   * Returns a text (text, comment, pi) or attribute value.
   * @param pre pre value
   * @param text text/attribute flag
   * @return atomized value
   */
  public abstract byte[] text(int pre, boolean text);

  /**
   * Returns a text (text, comment, pi) or attribute value as integer value.
   * @param pre pre value
   * @param text text/attribute flag
   * @return numeric value
   */
  public abstract long textItr(int pre, boolean text);

  /**
   * Returns a text (text, comment, pi) or attribute value as double value.
   * @param pre pre value
   * @param text text/attribute flag
   * @return numeric value
   */
  public abstract double textDbl(int pre, boolean text);

  /**
   * Returns the byte length of a text (text, comment, pi).
   * @param pre pre value
   * @param text text/attribute flag
   * @return length
   */
  public abstract int textLen(int pre, boolean text);

  // UPDATE OPERATIONS ========================================================

  /**
   * Updates (renames) an element, attribute or pi name.
   * @param pre pre value
   * @param kind node kind
   * @param name new tag, attribute or pi name
   * @param uri uri
   */
  public final void update(final int pre, final int kind, final byte[] name,
      final byte[] uri) {

    meta.update();

    if(kind == PI) {
      updateText(pre, trim(concat(name, SPACE, atom(pre))), kind);
    } else {
      // update/set namespace reference
      final int ouri = nspaces.uri(name, pre);
      final boolean ne = ouri == 0 && uri.length != 0;
      final int npre = kind == ATTR ? parent(pre, kind) : pre;
      final int nuri = ne ? nspaces.add(npre, npre, prefix(name), uri) :
        ouri != 0 && eq(nspaces.uri(ouri), uri) ? ouri : 0;

      // write namespace uri reference
      table.write1(pre, kind == ELEM ? 3 : 11, nuri);
      // write name reference
      table.write2(pre, 1, (nsFlag(pre) ? 1 << 15 : 0) |
        (kind == ELEM ? tagindex : atnindex).index(name, null, false));
      // write namespace flag
      table.write2(npre, 1, (ne || nsFlag(npre) ? 1 << 15 : 0) | name(npre));
    }
  }

  /**
   * Updates (replaces) the value of a single text, comment, pi or
   * attribute node.
   * @param pre pre value to be replaced
   * @param kind node kind
   * @param value value to be updated (tag name, text, comment, pi)
   */
  public final void update(final int pre, final int kind, final byte[] value) {
    meta.update();

    updateText(pre, kind == PI ? trim(concat(name(pre, kind), SPACE, value)) :
      value, kind);

    if(kind == DOC) resources.rename(pre, value);
  }

  /**
   * Replaces parts of the database with the specified data instance.
   * @param rpre pre value to be replaced
   * @param data replace data
   */
  public final void replace(final int rpre, final Data data) {
    meta.update();

    final int dsize = data.meta.size;
    final int rkind = kind(rpre);
    final int rsize = size(rpre, rkind);
    final int rpar = parent(rpre, rkind);
    final int diff = dsize - rsize;
    buffer(dsize);
    resources.replace(rpre, rsize, data);

    if(meta.updindex) {
      // update index
      indexDelete(rpre, rsize);
      indexBegin();
    }

    for(int dpre = 0; dpre < dsize; dpre++) {
      final int dkind = data.kind(dpre);
      final int dpar = data.parent(dpre, dkind);
      final int pre = rpre + dpre;
      final int dis = dpar >= 0 ? dpre - dpar : pre - parent(rpre, rkind);

      switch(dkind) {
        case DOC:
          // add document
          doc(pre, data.size(dpre, dkind), data.text(dpre, true));
          meta.ndocs++;
          break;
        case ELEM:
          // add element
          byte[] nm = data.name(dpre, dkind);
          elem(dis, tagindex.index(nm, null, false), data.attSize(dpre, dkind),
              data.size(dpre, dkind), nspaces.uri(nm, true), false);
          break;
        case TEXT:
        case COMM:
        case PI:
          // add text
          text(pre, dis, data.text(dpre, true), dkind);
          break;
        case ATTR:
          // add attribute
          nm = data.name(dpre, dkind);
          attr(pre, dis, atnindex.index(nm, null, false),
              data.text(dpre, false), nspaces.uri(nm, false), false);
          break;
      }
    }

    if(meta.updindex) {
      indexEnd();
      // update ID -> PRE map:
      idmap.delete(rpre, id(rpre), -rsize);
      idmap.insert(rpre, meta.lastid - dsize + 1, dsize);
    }

    // update table:
    table.replace(rpre, buffer(), rsize);
    buffer(1);

    // no distance/size update if the two subtrees are of equal size
    if(diff == 0) return;

    // increase/decrease size of ancestors, adjust distances of siblings
    int p = rpar;
    while(p >= 0) {
      final int k = kind(p);
      size(p, k, size(p, k) + diff);
      p = parent(p, k);
    }
    updateDist(rpre + dsize, diff);

    // adjust attribute size of parent if attributes inserted. attribute size
    // of parent cannot be reduced via a replace expression.
    if(data.kind(0) == ATTR) {
      int d = 0, i = 0;
      while(i < dsize && data.kind(i++) == ATTR) d++;
      if(d > 1) attSize(rpar, kind(rpar), d + 1);
    }
  }

  /**
   * Deletes a node and its descendants.
   * @param pre pre value of the node to delete
   */
  public final void delete(final int pre) {
    meta.update();

    // size of the subtree to delete
    int k = kind(pre);
    int s = size(pre, k);
    // indicates if database is empty
    final boolean empty = pre == 0 && s == meta.size;
    // update document index: delete specified entry
    if(!empty) resources.delete(pre, s);

    if(meta.updindex) {
      // delete child records from indexes
      indexDelete(pre, s);
    }

    /// explicitly delete text or attribute value
    if(k != DOC && k != ELEM) delete(pre, k != ATTR);

    // update namespaces
    nspaces.delete(pre, s);

    // reduce size of ancestors
    int par = pre;
    // check if we are an attribute (different size counters)
    if(k == ATTR) {
      par = parent(par, ATTR);
      attSize(par, ELEM, attSize(par, ELEM) - 1);
      size(par, ELEM, size(par, ELEM) - 1);
      k = kind(par);
    }

    // reduce size of ancestors
    while(par > 0 && k != DOC) {
      par = parent(par, k);
      k = kind(par);
      size(par, k, size(par, k) - s);
    }

    // preserve empty root node
    int p = pre;
    if(empty) {
      ++p;
      --s;
    } else if(kind(p) == DOC) {
      --meta.ndocs;
    }

    if(meta.updindex) {
      // delete node and descendants from ID -> PRE map:
      idmap.delete(pre, id(pre), -s);
    }

    // delete node from table structure and reduce document size
    table.delete(pre, s);
    updateDist(p, -s);

    // NSNodes have to be checked for pre value shifts after delete
    nspaces.update(pre, s, false, null);

    // restore empty document node
    if(empty) {
      doc(pre, 1, EMPTY);
      table.set(0, buffer());
      if(meta.updindex) idmap.insert(0, id(0), 1);
    }
  }

  /**
   * Inserts attributes.
   * @param pre pre value
   * @param par parent of node
   * @param data data instance to copy from
   */
  public final void insertAttr(final int pre, final int par, final Data data) {
    insert(pre, par, data);
    attSize(par, ELEM, attSize(par, ELEM) + data.meta.size);
  }

  /**
   * Inserts a data instance at the specified pre value.
   * Note that the specified data instance must differ from this instance.
   * @param ipre value at which to insert new data
   * @param ipar parent pre value of node
   * @param data data instance to copy from
   */
  public final void insert(final int ipre, final int ipar, final Data data) {
    meta.update();

    // update value and document indexes
    if(meta.updindex) indexBegin();
    resources.insert(ipre, data);

    // indicates if database only contains a dummy node
    final boolean dummy = isEmpty() && data.kind(0) == DOC;
    final int dsize = data.meta.size;

    final int buf = Math.min(dsize, IO.BLOCKSIZE >> IO.NODEPOWER);
    // resize buffer to cache more entries
    buffer(buf);

    // find all namespaces in scope to avoid duplicate declarations
    final TokenMap nsScope = new TokenMap();
    NSNode n = nspaces.current;
    do {
      for(int i = 0; i < n.vals.length; i += 2)
        nsScope.add(nspaces.prefix(n.vals[i]), nspaces.uri(n.vals[i + 1]));
      final int pos = n.fnd(ipar);
      if(pos < 0) break;
      n = n.ch[pos];
    } while(n.pre <= ipar && ipar < n.pre + size(n.pre, ELEM));

    // loop through all entries
    final IntList preStack = new IntList();
    int dpre = -1;
    final NSNode t = nspaces.current;
    final Set<NSNode> newNodes = new HashSet<NSNode>();
    final IntList flagPres = new IntList();

    while(++dpre != dsize) {
      if(dpre != 0 && dpre % buf == 0) insert(ipre + dpre - buf);

      final int pre = ipre + dpre;
      final int dkind = data.kind(dpre);
      final int dpar = data.parent(dpre, dkind);
      final int dis = dpar >= 0 ? dpre - dpar : ipar >= 0 ? pre - ipar : 0;
      final int par = dis == 0 ? -1 : pre - dis;

      // find nearest namespace node on the ancestor axis of the insert
      // location. possible candidates for this node are collected and
      // the match with the highest pre value between ancestors and candidates
      // is determined.
      if(dpre == 0) {
        // collect possible candidates for namespace root
        final List<NSNode> cand = new LinkedList<NSNode>();
        NSNode cn = nspaces.root;
        cand.add(cn);
        for(int cI; (cI = cn.fnd(par)) > -1;) {
          // add candidate to stack
          cn = cn.ch[cI];
          cand.add(0, cn);
        }

        cn = nspaces.root;
        if(cand.size() > 1) {
          // compare candidates to ancestors of par
          int ancPre = par;
          // take first candidate from stack
          NSNode curr = cand.remove(0);
          while(ancPre > -1 && cn == nspaces.root) {
            // this is the new root
            if(curr.pre == ancPre) cn = curr;
            // if the current candidate's pre value is lower than the current
            // ancestor of par or par itself we have to look for a potential
            // match for this candidate. therefore we iterate through ancestors
            // till we find one with a lower than or the same pre value as the
            // current candidate.
            else if(curr.pre < ancPre) {
              while((ancPre = parent(ancPre, kind(ancPre))) > curr.pre);
              if(curr.pre == ancPre) cn = curr;
            }
            // no potential for infinite loop, cause dummy root always a match,
            // in this case ancPre ends iteration
            if(!cand.isEmpty()) curr = cand.remove(0);
          }
        }
        nspaces.setNearestRoot(cn, par);
      }

      while(preStack.size() != 0 && preStack.peek() > par)
        nspaces.close(preStack.pop());

      switch(dkind) {
        case DOC:
          // add document
          final int s = data.size(dpre, dkind);
          doc(pre, s, data.text(dpre, true));
          meta.ndocs++;
          nspaces.open();
          preStack.push(pre);
          break;
        case ELEM:
          // add element
          boolean ne = false;
          if(data.nsFlag(dpre)) {
            final Atts at = data.ns(dpre);
            for(int a = 0; a < at.size(); ++a) {
              // see if prefix has been declared/ is part of current ns scope
              final byte[] old = nsScope.get(at.name(a));
              if(old == null || !eq(old, at.string(a))) {
                // we have to keep track of all new NSNodes that are added
                // to the Namespace structure, as their pre values must not
                // be updated. I.e. if an NSNode N with pre value 3 existed
                // prior to inserting and two new nodes are inserted at
                // location pre == 3 we have to make sure N and only N gets
                // updated.
                newNodes.add(nspaces.add(at.name(a), at.string(a), pre));
                ne = true;
              }
            }
          }
          nspaces.open();
          byte[] nm = data.name(dpre, dkind);
          elem(dis, tagindex.index(nm, null, false), data.attSize(dpre, dkind),
              data.size(dpre, dkind), nspaces.uri(nm, true), ne);
          preStack.push(pre);
          break;
        case TEXT:
        case COMM:
        case PI:
          // add text
          text(pre, dis, data.text(dpre, true), dkind);
          break;
        case ATTR:
          // add attribute
          nm = data.name(dpre, dkind);
          // check if prefix already in nsScope or not
          final byte[] attPref = prefix(nm);
          // check if prefix of attribute has already been declared, otherwise
          // add declaration to parent node
          if(data.nsFlag(dpre) && nsScope.get(attPref) == null) {
            nspaces.add(par, preStack.size() == 0 ? -1 : preStack.peek(),
                attPref, data.nspaces.uri(data.uri(dpre, dkind)));
            // save pre value to set ns flag later for this node. can't be done
            // here as direct table access would interfere with the buffer
            flagPres.add(par);
          }
          attr(pre, dis, atnindex.index(nm, null, false),
              data.text(dpre, false), nspaces.uri(nm, false), false);
          break;
      }
    }

    while(preStack.size() != 0) nspaces.close(preStack.pop());
    nspaces.setRoot(t);

    if(bp != 0) insert(ipre + dpre - 1 - (dpre - 1) % buf);
    // reset buffer to old size
    buffer(1);

    // set ns flags
    for(final int toFlag : flagPres.toArray())
      table.write2(toFlag, 1, name(toFlag) | 1 << 15);

    // increase size of ancestors
    int p = ipar;
    while(p >= 0) {
      final int k = kind(p);
      size(p, k, size(p, k) + dsize);
      p = parent(p, k);
    }
    updateDist(ipre + dsize, dsize);

    // NSNodes have to be checked for pre value shifts after insert
    nspaces.update(ipre, dsize, true, newNodes);

    if(meta.updindex) {
      // add the entries to the ID -> PRE mapping:
      idmap.insert(ipre, id(ipre), dsize);
      indexEnd();
    }

    // delete old empty root node
    if(dummy) delete(0);
  }

  /**
   * This method updates the distance values of the specified pre value
   * and the following siblings of all ancestor-or-self nodes.
   * @param pre root node
   * @param size size to be added/removed
   */
  private void updateDist(final int pre, final int size) {
    int p = pre;
    while(p < meta.size) {
      final int k = kind(p);
      dist(p, k, dist(p, k) + size);
      p += size(p, k);
    }
  }

  /**
   * Sets the size value.
   * @param pre pre reference
   * @param kind node kind
   * @param value value to be stored
   */
  public final void size(final int pre, final int kind, final int value) {
    if(kind == ELEM || kind == DOC) table.write4(pre, 8, value);
  }

  /**
   * Sets the disk offset of a text/attribute value.
   * @param pre pre value
   * @param off offset
   */
  final void textOff(final int pre, final long off) {
    table.write5(pre, 3, off);
  }

  /**
   * Updates the specified text or attribute value.
   * @param pre pre value
   * @param value content
   * @param kind node kind
   */
  protected abstract void updateText(final int pre, final byte[] value,
      final int kind);

  /**
   * Sets the distance.
   * @param pre pre value
   * @param kind node kind
   * @param value value
   */
  private void dist(final int pre, final int kind, final int value) {
    if(kind == ATTR) table.write1(pre, 0, value << 3 | ATTR);
    else if(kind != DOC) table.write4(pre, kind == ELEM ? 4 : 8, value);
  }

  /**
   * Sets the attribute size.
   * @param pre pre value
   * @param kind node kind
   * @param value value
   */
  private void attSize(final int pre, final int kind, final int value) {
    if(kind == ELEM) table.write1(pre, 0, value << 3 | ELEM);
  }

  /**
   * Sets the namespace flag.
   * Should be only called for element nodes.
   * @param pre pre value
   * @param ne namespace flag
   */
  public final void nsFlag(final int pre, final boolean ne) {
    table.write1(pre, 1, table.read1(pre, 1) & 0x7F | (ne ? 0x80 : 0));
  }

  /**
   * Inserts the internal buffer to the storage
   * without updating the table structure.
   * @param pre insert position
   */
  public final void insert(final int pre) {
    table.insert(pre, buffer());
  }

  /**
   * Deletes the specified text entry.
   * @param pre pre value
   * @param text text (text, comment or pi) or attribute flag
   */
  protected abstract void delete(final int pre, final boolean text);

  // INSERTS WITHOUT TABLE UPDATES ============================================

  /** Buffer for caching new table entries. */
  private byte[] b = new byte[IO.NODESIZE];
  /** Buffer position. */
  private int bp;

  /**
   * Sets the update buffer to a new size.
   * @param size number of table entries
   */
  final void buffer(final int size) {
    final int bs = size << IO.NODEPOWER;
    if(b.length != bs) b = new byte[bs];
  }

  /**
   * Adds a document entry to the internal update buffer.
   * @param pre pre value
   * @param size node size
   * @param value document name
   */
  public final void doc(final int pre, final int size, final byte[] value) {
    final int i = newID();
    final long v = index(pre, i, value, DOC);
    s(DOC); s(0); s(0); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(size >> 24); s(size >> 16); s(size >> 8); s(size);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Adds an element entry to the internal update buffer.
   * @param dist parent distance
   * @param name tag name index
   * @param asize number of attributes
   * @param size node size
   * @param uri namespace uri reference
   * @param ne namespace flag
   */
  public final void elem(final int dist, final int name, final int asize,
      final int size, final int uri, final boolean ne) {

    // build and insert new entry
    final int i = newID();
    final int n = ne ? 1 << 7 : 0;
    s(Math.min(IO.MAXATTS, asize) << 3 | ELEM);
    s(n | (byte) (name >> 8)); s(name); s(uri);
    s(dist >> 24); s(dist >> 16); s(dist >> 8); s(dist);
    s(size >> 24); s(size >> 16); s(size >> 8); s(size);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Adds a text entry to the internal update buffer.
   * @param pre pre value
   * @param dist parent distance
   * @param value string value
   * @param kind node kind
   */
  public final void text(final int pre, final int dist, final byte[] value,
      final int kind) {

    // build and insert new entry
    final int i = newID();
    final long v = index(pre, i, value, kind);
    s(kind); s(0); s(0); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(dist >> 24); s(dist >> 16); s(dist >> 8); s(dist);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Adds an attribute entry to the internal update buffer.
   * @param pre pre value
   * @param dist parent distance
   * @param name attribute name
   * @param value attribute value
   * @param uri namespace uri reference
   * @param ne namespace flag
   */
  public final void attr(final int pre, final int dist, final int name,
      final byte[] value, final int uri, final boolean ne) {

    // add attribute to text storage
    final int i = newID();
    final long v = index(pre, i, value, ATTR);
    final int n = ne ? 1 << 7 : 0;
    s(Math.min(IO.MAXATTS, dist) << 3 | ATTR);
    s(n | (byte) (name >> 8)); s(name); s(v >> 32);
    s(v >> 24); s(v >> 16); s(v >> 8); s(v);
    s(0); s(0); s(0); s(uri);
    s(i >> 24); s(i >> 16); s(i >> 8); s(i);
  }

  /**
   * Stores the specified value in the update buffer.
   * @param value value to be stored
   */
  private void s(final int value) {
    b[bp++] = (byte) value;
  }

  /**
   * Generates a new id.
   * @return id
   */
  private int newID() {
    return ++meta.lastid;
  }

  /**
   * Stores the specified value in the update buffer.
   * @param value value to be stored
   */
  private void s(final long value) {
    b[bp++] = (byte) value;
  }

  /**
   * Returns the byte buffer.
   * @return byte buffer
   */
  private byte[] buffer() {
    final byte[] bb = bp == b.length ? b : Arrays.copyOf(b, bp);
    bp = 0;
    return bb;
  }

  /**
   * Indexes a text and returns the reference.
   * @param pre pre value
   * @param id id value
   * @param value text to be indexed
   * @param kind node kind
   * @return reference
   */
  protected abstract long index(final int pre, final int id, final byte[] value,
      final int kind);

  /** Notify the index structures that an update operation is started. */
  void indexBegin() { }

  /** Notify the index structures that an update operation is finished. */
  void indexEnd() { }

  /**
   * Delete a node and its descendants from the corresponding indexes.
   * @param pre pre value of the node to delete
   * @param size number of descendants
   */
  protected abstract void indexDelete(final int pre, final int size);

  /**
   * Returns a string representation of the specified table range. Can be called
   * for debugging.
   * @param start start pre value
   * @param end end pre value
   * @return table
   */
  final String toString(final int start, final int end) {
    return string(InfoStorage.table(this, start, end));
  }

  @Override
  public final String toString() {
    return toString(0, meta.size);
  }
}
