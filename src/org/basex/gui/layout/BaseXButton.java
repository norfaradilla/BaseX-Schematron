package org.basex.gui.layout;

import static org.basex.core.Text.*;
import static org.basex.gui.layout.BaseXKeys.*;

import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.border.EmptyBorder;

import org.basex.core.Prop;
import org.basex.gui.GUI;
import org.basex.gui.GUICommand;
import org.basex.gui.dialog.Dialog;
import org.basex.util.Token;

/**
 * Project specific button implementation.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public class BaseXButton extends JButton {
  /**
   * Constructor for text buttons.
   * @param l button title
   * @param win parent window
   */
  public BaseXButton(final String l, final Window win) {
    super(l);
    BaseXLayout.addInteraction(this, win);
    if(!(win instanceof Dialog)) return;

    final Dialog d = (Dialog) win;
    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        final String text = getText();
        if(text.equals(B_CANCEL)) d.cancel();
        else if(text.equals(B_OK)) d.close();
        else d.action(e.getSource());
      }
    });
    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(final KeyEvent e) {
        if(ESCAPE.is(e)) d.cancel();
      }
    });
    setMnemonic(this, d.mnem);
  }

  /**
   * Constructor for image buttons.
   * @param gui main window
   * @param img image reference
   * @param hlp help text
   */
  public BaseXButton(final Window gui, final String img, final byte[] hlp) {
    super(BaseXLayout.icon(img));
    BaseXLayout.addInteraction(this, gui);
    if(hlp != null) setToolTipText(Token.string(hlp));

    // trim horizontal button margins
    final Insets in = getMargin();
    in.left /= 4;
    in.right /= 4;
    if(in.top < in.left) setMargin(in);
  }

  /**
   * Sets the label borders.
   * @param t top distance
   * @param l left distance
   * @param b bottom distance
   * @param r right distance
   * @return self reference
   */
  public BaseXButton border(final int t, final int l, final int b,
      final int r) {
    setBorder(new EmptyBorder(t, l, b, r));
    return this;
  }

  /**
   * Creates a new image button for the specified command.
   * @param cmd command
   * @param gui reference to main window
   * @return button
   */
  public static BaseXButton command(final GUICommand cmd, final GUI gui) {
    final BaseXButton button = new BaseXButton(gui,
        cmd.toString().toLowerCase(Locale.ENGLISH), Token.token(cmd.help()));
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e) {
        cmd.execute(gui);
      }
    });
    return button;
  }

  /**
   * Sets a mnemomic for the specified button.
   * @param b button
   * @param mnem mnemonics that have already been assigned
   */
  public static void setMnemonic(final AbstractButton b,
      final StringBuilder mnem) {
    // do not set mnemonics for Mac! Alt+key used for special characters.
    if(Prop.MAC) return;

    // find and assign unused mnemomic
    final String label = b.getText();
    for(int l = 0; l < label.length(); l++) {
      final char ch = Character.toLowerCase(label.charAt(l));
      if(!Token.letter(ch) || mnem.indexOf(Character.toString(ch)) != -1)
        continue;
      b.setMnemonic(ch);
      mnem.append(ch);
      break;
    }
  }

  @Override
  public void setEnabled(final boolean flag) {
    if(flag != isEnabled()) super.setEnabled(flag);
  }
}
