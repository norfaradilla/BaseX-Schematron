package org.basex;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import static org.basex.core.Text.*;
import org.basex.core.cmd.Check;
import org.basex.gui.GUI;
import org.basex.gui.GUIConstants;
import org.basex.gui.GUIMacOSX;
import org.basex.gui.GUIProp;
import org.basex.io.IO;
import org.basex.io.IOFile;
import org.basex.util.Args;
import org.basex.util.Util;
import org.basex.util.list.StringList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import java.io.*;
import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This is the starter class for the graphical frontend.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
public final class BaseXGUI extends JPanel implements ActionListener  {
  /** Database context. */
  final Context context = new Context();
  /** Files, specified as arguments. */
  final StringList files = new StringList();
  /** Mac OS X GUI optimizations. */
  GUIMacOSX osxGUI;
  //Class dummy;
  
  //my code
  //public String filepath ="";
  static private final String newline = "\n";

  static JFrame frame;
  JButton openButton, cancelButton, nextButton;

  JTextArea log;
  JPanel buttonPanel2;

  JFileChooser fc;
  
   
            
  public static volatile String inputpath; //belum berjaya lagi
  
  public volatile String schpath ="initial"; 
  public static volatile boolean running = true;
  public static volatile JComponent newContentPane;
  
  public String xml; //belum berjaya lagi
  public String sch; 
  
    public void setSchPath(String p){
      schpath = p;
      //System.err.println(p);
  }
          
   public String getSchPath(){
       schpath = "Schematron/XIND.sch"; 
       
      return schpath ; 
      //return this.schpath;
  }   
   //public String getSch(){
   //     return sch;
   // }  
  public BaseXGUI(){
      
    this.xml = inputpath;
    this.sch = schpath;
    //this.sch = "Schematron/fitness.sch";
    
   
}


  
  public BaseXGUI(int dummy) {
    super(new BorderLayout());

    //Create the log first, because the action listeners
    //need to refer to it.
    log = new JTextArea(5, 50);
    log.setMargin(new Insets(5, 5, 5, 5));
    log.setEditable(false);
    JScrollPane logScrollPane = new JScrollPane(log);
    
    nextButton = new JButton("Next");
    nextButton.addActionListener(this);
    //nextButton.setHorizontalAlignment(SwingConstants.RIGHT);
    nextButton.setEnabled(false);

    //Create a file chooser
    fc = new JFileChooser();

    //Uncomment one of the following lines to try a different
    //file selection mode. The first allows just directories
    //to be selected (and, at least in the Java look and feel,
    //shown). The second allows both files and directories
    //to be selected. If you leave these lines commented out,
    //then the default mode (FILES_ONLY) will be used.
    //
    //fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    //fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

    //Create the open button. We use the image from the JLF
    //Graphics Repository (but we extracted it from the jar).
    openButton = new JButton("Browse...");
    openButton.addActionListener(this);

    //Create the save button. We use the image from the JLF
    //Graphics Repository (but we extracted it from the jar).
    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);

    //For layout purposes, put the buttons in a separate panel
    JPanel buttonPanel = new JPanel(); //use FlowLayout
    buttonPanel.add(openButton);
    buttonPanel.add(cancelButton);
    
    buttonPanel2 = new JPanel(); //use FlowLayout
    buttonPanel2.setPreferredSize(new java.awt.Dimension(50, 50));
    buttonPanel2.add(nextButton);
    //buttonPanel.add(nextButton);


    //Add the buttons and the log to this panel.
    add(buttonPanel, BorderLayout.PAGE_START);
    add(logScrollPane, BorderLayout.CENTER);
    add(buttonPanel2, BorderLayout.SOUTH);
          
    running = false;
    //System.out.println(schpath);
  }
  


  public void actionPerformed(ActionEvent e) {

    //Handle open button action.
    if (e.getSource() == openButton) {
      int returnVal = fc.showOpenDialog(BaseXGUI.this);

      if (returnVal == JFileChooser.APPROVE_OPTION) {
        File file = fc.getSelectedFile();
        //This is where a real application would open the file.
        log.append("Opening: " + file.getName() + "..." + newline);
        schpath = file.getAbsolutePath();
        setSchPath(schpath);
        //schpath = schpath.replaceAll("\\", "/");
        //System.out.println(schpath);
      } else {
        log.append("Open command cancelled by user..." + newline +"Use default file..." + newline);
        
      }      
      //buttonPanel2.add(nextButton);
      nextButton.setEnabled(true);
      log.setCaretPosition(log.getDocument().getLength());

      //Handle save button action.
    } else if (e.getSource() == cancelButton) {
      
        log.append("Use default file..." + newline);
        //buttonPanel2.add(nextButton);
        nextButton.setEnabled(true);
        log.setCaretPosition(log.getDocument().getLength());
      
    } else if(e.getSource() == nextButton) {
        frame.dispose();
    }

    
  }


  /**
   * Create the GUI and show it. For thread safety, this method should be
   * invoked from the event-dispatching thread.
   */
  public static void createAndShowSch() {
           try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (Exception e) {
      e.printStackTrace();
    }
     
    //JDialog.setDefaultLookAndFeelDecorated(true);

    //Create and set up the window.
    frame = new JFrame("Opt:Schematron File");
    frame.setBounds(630, 150, 500, 200);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    
    
    frame.setIconImage(Toolkit.getDefaultToolkit().getImage("img/icon.png"));
    //Create and set up the content pane.
    newContentPane = new BaseXGUI(0);
    newContentPane.setOpaque(true); //content panes must be opaque
    frame.setContentPane(newContentPane);

    //Display the window.
    frame.pack();
    frame.setVisible(true);
    frame.setAlwaysOnTop(true);
    
    //System.out.println("wek "+new BaseXGUI(0).getSchPath());
    //System.err.println(getSchPath());

    
    
  }
  
  //----------------------------------------
  /**
   * Main method.
   * @param args command-line arguments.
   * An XML document or query file can be specified as argument
   */
  public static void main(final String... args) {
      BaseXGUI obj = null;
    Toolkit toolkit;
    Timer timer=null;
   
      
    File filename = new File("pre_DYN.txt");
    File filename2 =  new File("pre_ins.txt");
    File filename3 =  new File("pre_del.txt");
    //File filename4 =  new File("pre_rep.txt");

    filename.delete();
    filename2.delete();
    filename3.delete();
    //filename4.delete();

    
    File filenamenew = new File("pre_DYN.txt");
    File filenamenew2 = new File("pre_ins.txt");
    File filenamenew3 = new File("pre_del.txt");
    //File filenamenew4 = new File("pre_rep.txt");

    
    try {
        filenamenew.createNewFile();
        filenamenew2.createNewFile();
        filenamenew3.createNewFile();
        //filenamenew4.createNewFile();

    } catch (IOException ex) {
        Logger.getLogger(BaseXGUI.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    try { 
        
        obj = new BaseXGUI(args); 
        //mycode
        try {
            Thread.sleep(3000);
        } catch(InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        createAndShowSch();
        
        //end mycode
        
       
        
        //System.out.println("wek "+new BaseXGUI(0).getSchPath());
      
    } catch(final BaseXException ex) {
      Util.errln(ex);
      System.exit(1);
    }

    /*
    if(running == false){
    System.out.println("last "+newContentPane.);
    }
     */ 
    
  }

  /**
   * Constructor.
   * @param args command-line arguments
   * @throws BaseXException database exception
   */
  private BaseXGUI(final String... args) throws BaseXException {
    parseArguments(args);
    //BaseXGUI ob = new BaseXGUI();
    
   // Class obj = Class.forName(BaseXGUI);

    // set mac specific properties
    if(Prop.MAC) {
      try {
        osxGUI = new GUIMacOSX();
      } catch(final Exception ex) {
        throw new BaseXException(
            "Failed to initialize native Mac OS X interface", ex);
      }
    }

    // read properties
    context.prop.set(Prop.CACHEQUERY, true);
    final GUIProp gprop = new GUIProp();
    
    GUIConstants.init(gprop);

    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
          
 
        // initialize look and feel
        init(gprop);
        // open main window
        final GUI gui = new GUI(context, gprop);
        if(osxGUI != null) osxGUI.init(gui);

        // open specified document or database
        boolean xml = false;
        for(final String file : files) {
          final String input = file.replace('\\', '/');
            
          
          //inputpath = input;
          //System.err.println("XML input"+inputpath);
          
          final IOFile io = new IOFile(input);
          inputpath = io.path();
          boolean xq = false;
          for(final String suf : IO.XQSUFFIXES) xq |= input.endsWith(suf);
          if(xq) {
            gui.editor.open(io);
          } else if(!xml) {
            // only parse first xml file
            gui.execute(new Check(input));
            gprop.set(GUIProp.CREATEPATH, io.path());
            gprop.set(GUIProp.CREATENAME, io.dbname());
            xml = true;
                      

          }
 
        }
        

      }
    });
           
  }
    

  /**
   * Initializes the GUI.
   * @param prop gui properties
   */
  static void init(final GUIProp prop) {
    try {
      // added to handle possible JDK 1.6 bug (thanks to Makoto Yui)
      UIManager.getInstalledLookAndFeels();
      // refresh views when windows are resized
      Toolkit.getDefaultToolkit().setDynamicLayout(true);
      // set specified look & feel
      if(prop.is(GUIProp.JAVALOOK)) {
        // use non-bold fonts in Java's look & feel
        final UIDefaults def = UIManager.getDefaults();
        final Enumeration<?> en = def.keys();
        while(en.hasMoreElements()) {
          final Object k = en.nextElement();
          final Object v = def.get(k);
          if(v instanceof Font) def.put(k, ((Font) v).deriveFont(Font.PLAIN));
        }
      } else {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
    } catch(final Exception ex) {
      Util.stack(ex);
    }
  }

  /**
   * Parses the command-line arguments, specified by the user.
   * @param args command-line arguments
   * @throws BaseXException database exception
   */
  private void parseArguments(final String[] args) throws BaseXException {
    final Args arg = new Args(args, this, GUIINFO, Util.info(CONSOLE, GUIMODE));
    while(arg.more()) {
      if(arg.dash()) arg.usage();
      files.add(arg.string());
    }
  }

    //@Override
    //public void actionPerformed(ActionEvent ae) {
    //    throw new UnsupportedOperationException("Not supported yet.");
   // }
}
