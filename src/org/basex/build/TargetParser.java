package org.basex.build;

import org.basex.io.IO;

/**
 * This class defines an abstract parser, specifying a target path.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
abstract class TargetParser extends Parser {
  /** Target path (empty, or suffixed with a single slash). */
  final String trg;

  /**
   * Constructor.
   * @param source document source
   * @param path target path
   */
  TargetParser(final IO source, final String path) {
    super(source);
    trg = path.isEmpty() ? "" : (path + '/').replaceAll("//+", "/");
  }
}
