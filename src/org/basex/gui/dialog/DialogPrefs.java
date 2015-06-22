package org.basex.gui.dialog;

import static org.basex.core.Text.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.basex.core.MainProp;
import org.basex.core.Lang;
import org.basex.core.cmd.Close;
import org.basex.data.Data;
import org.basex.gui.GUI;
import org.basex.gui.GUIProp;
import org.basex.gui.layout.BaseXBack;
import org.basex.gui.layout.BaseXButton;
import org.basex.gui.layout.BaseXCheckBox;
import org.basex.gui.layout.BaseXCombo;
import org.basex.gui.layout.BaseXFileChooser;
import org.basex.gui.layout.BaseXFileChooser.Mode;
import org.basex.gui.layout.BaseXLabel;
import org.basex.gui.layout.BaseXTextField;
import org.basex.gui.layout.TableLayout;
import org.basex.io.IOFile;

/**
 * Dialog window for changing some project's preferences.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class DialogPrefs extends Dialog {
  /** Information on available languages. */
  private static final String[][] LANGS = Lang.parse();

  /** Directory path. */
  final BaseXTextField path;

  /** Language label. */
  private final BaseXLabel creds;
  /** Language combobox. */
  private final BaseXCombo lang;
  /** Focus checkbox. */
  private final BaseXCheckBox focus;
  /** Show names checkbox. */
  private final BaseXCheckBox names;
  /** Simple file dialog checkbox. */
  private final BaseXCheckBox simpfd;
  /** Simple file dialog checkbox. */
  private final BaseXCheckBox javalook;

  /**
   * Default constructor.
   * @param main reference to the main window
   */
  public DialogPrefs(final GUI main) {
    super(main, PREFERENCES);

    // create checkboxes
    final BaseXBack pp = new BaseXBack(new TableLayout(11, 1));
    pp.add(new BaseXLabel(DATABASE_PATH, true, true));

    BaseXBack p = new BaseXBack(new TableLayout(1, 2, 8, 0));

    final MainProp mprop = gui.context.mprop;
    final GUIProp gprop = gui.gprop;
    path = new BaseXTextField(mprop.dbpath().path(), this);

    final BaseXButton button = new BaseXButton(BROWSE_D, this);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final IOFile file = new BaseXFileChooser(CHOOSE_DIR, path.getText(),
            gui).select(Mode.DOPEN);
        if(file != null) path.setText(file.dir());
      }
    });

    p.add(path);
    p.add(button);
    pp.add(p);
    pp.add(new BaseXLabel(GUI_INTERACTIONS, true, true).border(12, 0, 6, 0));

    // checkbox for Java look and feel
    javalook = new BaseXCheckBox(JAVA_LF, gprop.is(GUIProp.JAVALOOK), this);
    pp.add(javalook);

    // checkbox for realtime mouse focus
    focus = new BaseXCheckBox(RT_FOCUS, gprop.is(GUIProp.MOUSEFOCUS), this);
    pp.add(focus);

    // checkbox for simple file dialog
    simpfd = new BaseXCheckBox(SIMPLE_FILE_CHOOSER,
        gprop.is(GUIProp.SIMPLEFD), this);
    pp.add(simpfd);

    // enable only if current document contains name attributes
    names = new BaseXCheckBox(SHOW_NAME_ATTS,
        gprop.is(GUIProp.SHOWNAME), 12, this);
    final Data data = gui.context.data();
    names.setEnabled(data != null && data.nameID != 0);
    pp.add(names);

    // checkbox for simple file dialog
    pp.add(new BaseXLabel(LANGUAGE_RESTART, true, true));

    p = new BaseXBack(new TableLayout(1, 2, 12, 0));

    lang = new BaseXCombo(this, LANGS[0]);
    lang.setSelectedItem(mprop.get(MainProp.LANG));
    p.add(lang);

    creds = new BaseXLabel(" ");
    creds.setText(TRANSLATION + COLS +
        creds(lang.getSelectedItem().toString()));
    p.add(creds);

    pp.add(p);

    set(pp, BorderLayout.CENTER);
    set(okCancel(), BorderLayout.SOUTH);
    finish(null);
  }

  @Override
  public void action(final Object cmp) {
    creds.setText(TRANSLATION + COLS +
        creds(lang.getSelectedItem().toString()));
    gui.notify.layout();
  }

  @Override
  public void close() {
    final MainProp mprop = gui.context.mprop;
    mprop.set(MainProp.LANG, lang.getSelectedItem().toString());
    // new database path: close existing database
    final String dbpath = path.getText();
    if(!mprop.get(MainProp.DBPATH).equals(dbpath)) gui.execute(new Close());
    mprop.set(MainProp.DBPATH, dbpath);
    mprop.write();
    final GUIProp gprop = gui.gprop;
    gprop.set(GUIProp.MOUSEFOCUS, focus.isSelected());
    gprop.set(GUIProp.SHOWNAME, names.isSelected());
    gprop.set(GUIProp.SIMPLEFD, simpfd.isSelected());
    gprop.set(GUIProp.JAVALOOK, javalook.isSelected());
    gprop.write();
    dispose();
  }

  /**
   * Returns the translation credits for the specified language.
   * @param lng language
   * @return credits
   */
  static String creds(final String lng) {
    for(int i = 0; i < LANGS[0].length; ++i) {
      if(LANGS[0][i].equals(lng)) return LANGS[1][i];
    }
    return "";
  }
}
