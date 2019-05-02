import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.io.*;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Statement;

import java.util.ArrayList;
import java.util.Arrays;


public class ClientGui extends Thread{

  final JTextPane jtextFilDiscu = new JTextPane();
  final JTextPane jtextListUsers = new JTextPane();
  final JTextField jtextInputChat = new JTextField();
  private String oldMsg = "";
  private Thread read;
  private String serverName;
  private int PORT;
  private String name;
  BufferedReader input;
  PrintWriter output;
  Socket server;
  Server serv;

  public ClientGui() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
    this.serverName = "localhost";
    this.PORT = 12345;
    this.name = "nickname";
    this.serv= new Server(this.PORT);

    String fontfamily = "Arial, sans-serif";
    Font font = new Font(fontfamily, Font.PLAIN, 15);

    final JFrame jfr = new JFrame("Chat");
    jfr.getContentPane().setLayout(null);
    jfr.setSize(700, 500);
    jfr.setResizable(false);
    jfr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
    final JPanel FirstView = new JPanel();
	FirstView.setSize(700,500);
	FirstView.setBackground(Color.white);
	FirstView.setLayout(null);
	FirstView.setVisible(false);
	
	final JButton Back = new JButton("Back");
    Back.setFont(font);
    Back.setBounds(575, 410, 100, 35);
    //FirstView.add(Back);
    
    final JButton inSignUp = new JButton("Sign Up");
    inSignUp.setFont(font);
    inSignUp.setBounds(575, 370, 100, 35);
    //FirstView.add(Back);

    // Module du fil de discussion
    jtextFilDiscu.setBounds(25, 25, 490, 320);
    jtextFilDiscu.setFont(font);
    jtextFilDiscu.setMargin(new Insets(6, 6, 6, 6));
    jtextFilDiscu.setEditable(false);
    JScrollPane jtextFilDiscuSP = new JScrollPane(jtextFilDiscu);
    jtextFilDiscuSP.setBounds(25, 25, 490, 320);

    jtextFilDiscu.setContentType("text/html");
    jtextFilDiscu.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // Module de la liste des utilisateurs
    jtextListUsers.setBounds(520, 25, 156, 320);
    jtextListUsers.setEditable(true);
    jtextListUsers.setFont(font);
    jtextListUsers.setMargin(new Insets(6, 6, 6, 6));
    jtextListUsers.setEditable(false);
    JScrollPane jsplistuser = new JScrollPane(jtextListUsers);
    jsplistuser.setBounds(520, 25, 156, 320);

    jtextListUsers.setContentType("text/html");
    jtextListUsers.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // Field message user input
    jtextInputChat.setBounds(0, 350, 400, 50);
    jtextInputChat.setFont(font);
    jtextInputChat.setMargin(new Insets(6, 6, 6, 6));
    final JScrollPane jtextInputChatSP = new JScrollPane(jtextInputChat);
    jtextInputChatSP.setBounds(25, 350, 650, 50);

    // button send
    final JButton jsbtn = new JButton("Send");
    jsbtn.setFont(font);
    jsbtn.setBounds(575, 410, 100, 35);

    // button Disconnect
    final JButton jsbtndeco = new JButton("Disconnect");
    jsbtndeco.setFont(font);
    jsbtndeco.setBounds(25, 410, 130, 35);

    jtextInputChat.addKeyListener(new KeyAdapter() {
      // send message on Enter
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }

        // Get last message typed
        if (e.getKeyCode() == KeyEvent.VK_UP) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          String currentMessage = jtextInputChat.getText().trim();
          jtextInputChat.setText(oldMsg);
          oldMsg = currentMessage;
        }
      }
    });

    // Click on send button
    jsbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        sendMessage();
      }
    });

    // Connection view
    final JTextField jtfName = new JTextField(this.name);
    final JTextField jtfport = new JTextField(Integer.toString(this.PORT));
    final JTextField jtfAddr = new JTextField(this.serverName);
    final JTextField signInPassword = new JTextField("password");
    final JButton jcbtn = new JButton("Connect");
    final JButton signUp = new JButton("SignUp");
    final JTextField signUpName = new JTextField("username");
    final JTextField signUpPassword = new JTextField("password");
    signUpName.setBounds(100, 400, 135, 40);
    signUpPassword.setBounds(240, 400, 135, 40);
    signInPassword.setBounds(375,420, 135, 40);

    // check if those field are not empty
    jtfName.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfport.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));
    jtfAddr.getDocument().addDocumentListener(new TextListener(jtfName, jtfport, jtfAddr, jcbtn));

    // position des Modules
    jcbtn.setFont(font);
    signUp.setFont(font);
    jtfAddr.setBounds(25, 380, 135, 40);
    jtfName.setBounds(375, 380, 135, 40);
    jtfport.setBounds(200, 380, 135, 40);
    jcbtn.setBounds(575, 380, 100, 40);
    signUp.setBounds(575, 420, 100, 40);

    // couleur par defaut des Modules fil de discussion et liste des utilisateurs
    jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
    jtextListUsers.setBackground(Color.LIGHT_GRAY);

    // ajout des éléments
    jfr.add(jcbtn);
    jfr.add(signUp);
    jfr.add(jtextFilDiscuSP);
    jfr.add(jsplistuser);
    jfr.add(jtfName);
    jfr.add(jtfport);
    jfr.add(jtfAddr);
    jfr.add(signInPassword);
    jfr.setVisible(true);


    // info sur le Chat
    appendToPane(jtextFilDiscu, "<h4>Chatting commands:</h4>"
        +"<ul>"
        +"<li><b>@nickname</b> to send a Private Message to the user 'nickname''</li>"
        +"<li><b>#d3961b</b> to change the color of his nickname to hexadecimal indicate</li>"
        +"</ul><br/>");

    
    inSignUp.addActionListener(new ActionListener(){
    	public void actionPerformed(ActionEvent as){
    		String username = signUpName.getText().trim();
    		String password = signUpPassword.getText().trim();
    		Connection con;
			try {
				con = getConnection();
				PreparedStatement insert = con.prepareStatement("INSERT INTO Users_Password (user_name, password1) VALUES ('"+username+"', '"+password+"')");
		     	insert.executeUpdate();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jfr.add(jtfName);
            jfr.add(jtfport);
            jfr.add(jtfAddr);
            jfr.add(jcbtn);
            jfr.add(signUp);
            jfr.add(jtextFilDiscuSP);
            jfr.add(jsplistuser);
            jfr.add(signInPassword);
            jfr.remove(Back);
            jfr.remove(inSignUp);
            jfr.remove(signUpName);
            jfr.remove(signUpPassword);
            jfr.revalidate();
            jfr.repaint();
           
    	}
    });
    
    Back.addActionListener(new ActionListener(){
    	public void actionPerformed(ActionEvent as){
    		jfr.add(jtfName);
            jfr.add(jtfport);
            jfr.add(jtfAddr);
            jfr.add(jcbtn);
            jfr.add(signUp);
            jfr.add(jtextFilDiscuSP);
            jfr.add(jsplistuser);
            jfr.add(signInPassword);
            jfr.remove(Back);
            jfr.remove(inSignUp);
            jfr.remove(signUpName);
            jfr.remove(signUpPassword);
            jfr.revalidate();
            jfr.repaint();
    	}
    });
    
    signUp.addActionListener(new ActionListener(){
    	public void actionPerformed(ActionEvent as){
    		jfr.remove(jtfName);
            jfr.remove(jtfport);
            jfr.remove(jtfAddr);
            jfr.remove(jcbtn);
            jfr.remove(signUp);
            jfr.remove(jtextFilDiscuSP);
            jfr.remove(jsplistuser);
            jfr.remove(signInPassword);
            jfr.add(Back);
            jfr.add(inSignUp);
            jfr.add(signUpName);
            jfr.add(signUpPassword);
            jfr.revalidate();
            jfr.repaint();
    	}
    });
    
    // On connect
    jcbtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          name = jtfName.getText();
          String password = signInPassword.getText().trim();
          String port = jtfport.getText();
          serverName = jtfAddr.getText();
          PORT = Integer.parseInt(port);
          boolean emptyPasswordBool = true;
          
          if(signInPassword.getText().isEmpty()){
        	  try {
  				Connection con2 = getConnection();
  				PreparedStatement usernameResult = con2.prepareStatement("SELECT user_name FROM Tokens");
  				ResultSet UserNameresult = usernameResult.executeQuery();
  				while(UserNameresult.next()){
  					String username = UserNameresult.getString("user_name");
  					if(jtfName.getText().trim().contains(username) && username.contains(jtfName.getText().trim())){
  						emptyPasswordBool = false;
  						appendToPane(jtextFilDiscu, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
  		                server = new Socket(serverName, PORT);

  		                appendToPane(jtextFilDiscu, "<span>Connected to " +
  		                    server.getRemoteSocketAddress()+"</span>");

  		                input = new BufferedReader(new InputStreamReader(server.getInputStream()));
  		                output = new PrintWriter(server.getOutputStream(), true);

  		                // send nickname to server
  		                output.println(name);

  		                // create new Read Thread
  		                read = new Read();
  		                read.start();
  		                jfr.remove(jtfName);
  		                jfr.remove(jtfport);
  		                jfr.remove(jtfAddr);
  		                jfr.remove(jcbtn);
  		                jfr.remove(signUp);
  		                jfr.remove(signInPassword);
  		                jfr.add(jsbtn);
  		                jfr.add(jtextInputChatSP);
  		                jfr.add(jsbtndeco);
  		                jfr.revalidate();
  		                jfr.repaint();
  		                jtextFilDiscu.setBackground(Color.WHITE);
  		                jtextListUsers.setBackground(Color.WHITE);
  		              try {
  	    				Connection con3 = getConnection();
  	    				PreparedStatement delete = con3.prepareStatement("delete from Tokens where user_name = '"+jtfName.getText().trim()+"'");
  	    	            delete.execute();
  	    			} catch (Exception e) {
  	    				// TODO Auto-generated catch block
  	    				e.printStackTrace();
  	    			}
  					}
  				}
  			} catch (Exception e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
        	  if(emptyPasswordBool == true){
        	  JOptionPane.showMessageDialog(new JFrame(), "Token Expired", "Dialog",
    			        JOptionPane.ERROR_MESSAGE);
        	  }
          }
          else {
          try{
          	Connection con = getConnection();
      		PreparedStatement usernameResult = con.prepareStatement("SELECT user_name FROM Users_Password");
      		ResultSet UserNameresult = usernameResult.executeQuery();
      		PreparedStatement passwordResult = con.prepareStatement("SELECT password1 FROM Users_Password");
      		ResultSet Passwordresult = passwordResult.executeQuery();
      		boolean flag = false;
      		
      		while(UserNameresult.next() && Passwordresult.next()){
      		 String userName = UserNameresult.getString("user_name");
      		 String Password1 = Passwordresult.getString("password1");
      		 System.out.println(userName);
      		 System.out.println(jtfName.getText().trim());
      		 System.out.println(Password1);
      		 System.out.println(signInPassword.getText().trim());
      		 if(jtfName.getText().trim().contains(userName) && signInPassword.getText().trim().contains(Password1) && Password1.contains(signInPassword.getText().trim()) && userName.contains(jtfName.getText().trim())){
      			//System.out.println("myass");
      			 flag = true;
      			 appendToPane(jtextFilDiscu, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
                server = new Socket(serverName, PORT);

                appendToPane(jtextFilDiscu, "<span>Connected to " +
                    server.getRemoteSocketAddress()+"</span>");

                input = new BufferedReader(new InputStreamReader(server.getInputStream()));
                output = new PrintWriter(server.getOutputStream(), true);

                // send nickname to server
                output.println(name);

                // create new Read Thread
                read = new Read();
                read.start();
                jfr.remove(jtfName);
                jfr.remove(jtfport);
                jfr.remove(jtfAddr);
                jfr.remove(jcbtn);
                jfr.remove(signUp);
                jfr.remove(signInPassword);
                jfr.add(jsbtn);
                jfr.add(jtextInputChatSP);
                jfr.add(jsbtndeco);
                jfr.revalidate();
                jfr.repaint();
                jtextFilDiscu.setBackground(Color.WHITE);
                jtextListUsers.setBackground(Color.WHITE);
                try {
    				Connection con1 = getConnection();
    				PreparedStatement insert = con1.prepareStatement("INSERT INTO Tokens (user_name, password1) VALUES ('"+jtfName.getText().trim()+"', '"+signInPassword.getText().trim()+"')");
    		     	insert.executeUpdate();
    			} catch (Exception e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
                break;
      		 }
      		 
      		}
      		if(flag == false) {
      			JOptionPane.showMessageDialog(new JFrame(), "Wrong Passsword or UserName", "Dialog",
      			        JOptionPane.ERROR_MESSAGE);
      			
      		 }
          } catch(Exception e){System.out.println(e);}
        }

          /*appendToPane(jtextFilDiscu, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
          server = new Socket(serverName, PORT);

          appendToPane(jtextFilDiscu, "<span>Connected to " +
              server.getRemoteSocketAddress()+"</span>");

          input = new BufferedReader(new InputStreamReader(server.getInputStream()));
          output = new PrintWriter(server.getOutputStream(), true);

          // send nickname to server
          output.println(name);

          // create new Read Thread
          read = new Read();
          read.start();
          jfr.remove(jtfName);
          jfr.remove(jtfport);
          jfr.remove(jtfAddr);
          jfr.remove(jcbtn);
          jfr.remove(signUp);
          jfr.remove(signInPassword);
          jfr.add(jsbtn);
          jfr.add(jtextInputChatSP);
          jfr.add(jsbtndeco);
          jfr.revalidate();
          jfr.repaint();
          jtextFilDiscu.setBackground(Color.WHITE);
          jtextListUsers.setBackground(Color.WHITE);*/
        } catch (Exception ex) {
          appendToPane(jtextFilDiscu, "<span>Could not connect to Server</span>");
          JOptionPane.showMessageDialog(jfr, ex.getMessage());
        }
      }

    });

    // on deco
    jsbtndeco.addActionListener(new ActionListener()  {
      public void actionPerformed(ActionEvent ae) {
        jfr.add(jtfName);
        jfr.add(jtfport);
        jfr.add(jtfAddr);
        jfr.add(jcbtn);
        jfr.add(signUp);
        jfr.add(signInPassword);
        jfr.remove(jsbtn);
        jfr.remove(jtextInputChatSP);
        jfr.remove(jsbtndeco);
        jfr.revalidate();
        jfr.repaint();
        read.interrupt();
        jtextListUsers.setText(null);
        jtextFilDiscu.setBackground(Color.LIGHT_GRAY);
        jtextListUsers.setBackground(Color.LIGHT_GRAY);
        appendToPane(jtextFilDiscu, "<span>Connection closed.</span>");
        output.close();
      }
    });

  } 

  // check if if all field are not empty
  public class TextListener implements DocumentListener{
    JTextField jtf1;
    JTextField jtf2;
    JTextField jtf3;
    JButton jcbtn;

    public TextListener(JTextField jtf1, JTextField jtf2, JTextField jtf3, JButton jcbtn){
      this.jtf1 = jtf1;
      this.jtf2 = jtf2;
      this.jtf3 = jtf3;
      this.jcbtn = jcbtn;
    }

    public void changedUpdate(DocumentEvent e) {}

    public void removeUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }
    public void insertUpdate(DocumentEvent e) {
      if(jtf1.getText().trim().equals("") ||
          jtf2.getText().trim().equals("") ||
          jtf3.getText().trim().equals("")
          ){
        jcbtn.setEnabled(false);
      }else{
        jcbtn.setEnabled(true);
      }
    }

  }

  // envoi des messages
  public void sendMessage() {
    try {
      String message = jtextInputChat.getText().trim();
      Cipher desCipher=serv.desCipher;
      String m=message;
      byte[] text = m.getBytes();
      byte[] textEncrypted = desCipher.doFinal(text);
      System.out.println(textEncrypted);
      if (message.equals("")) {
        return;
      }
      this.oldMsg = message;
      output.println(message);
      jtextInputChat.requestFocus();
      jtextInputChat.setText(null);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(null, ex.getMessage());
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
    ClientGui client = new ClientGui();
  }

  // read new incoming messages
  class Read extends Thread {
    public void run() {
      String message;
      while(!Thread.currentThread().isInterrupted()){
        try {
          message = input.readLine();
          if(message != null){
            if (message.charAt(0) == '[') {
              message = message.substring(1, message.length()-1);
              ArrayList<String> ListUser = new ArrayList<String>(
                  Arrays.asList(message.split(", "))
                  );
              jtextListUsers.setText(null);
              for (String user : ListUser) {
                appendToPane(jtextListUsers, "@" + user);
              }
            }else{
              appendToPane(jtextFilDiscu, message);
            }
          }
        }
        catch (IOException ex) {
          System.err.println("Failed to parse incoming message");
        }
      }
    }
  }

  // send html to pane
  private void appendToPane(JTextPane tp, String msg){
    HTMLDocument doc = (HTMLDocument)tp.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit)tp.getEditorKit();
    try {
      editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
      tp.setCaretPosition(doc.getLength());
    } catch(Exception e){
      e.printStackTrace();
    }
  }
  

  public static Connection getConnection() throws Exception{
  	try{
  	String driver = "com.mysql.jdbc.Driver";
  	String url = "jdbc:mysql://localhost:3306/TrainingSchema?autoReconnect=true&useSSL=false";
  	String username = "root";
  	String password = "server123";
  	Class.forName(driver);
  	Connection conn = (Connection) DriverManager.getConnection(url, username, password);
  	System.out.println("connected");
  	return conn;
  	} catch(Exception e){System.out.println(e);}
  	return null;
  }

  /*void DataBaseCon () throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
	  
	  Class.forName("com.mysql.jdbc.Driver").newInstance();
	  Connection conn = (Connection) DriverManager.getConnection
	     ("jdbc:mysql://localhost:3306/foo", "root", "password");

	  Statement stmt = (Statement) conn.createStatement();
//	  stmt.execute("SELECT * FROM `FOO.BAR`");
	  stmt.close();
	  conn.close();
  }*/
}
