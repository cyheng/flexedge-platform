

package cn.doraro.flexedge.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.Vector;

public class ServerCtrlUI extends Panel {
    static int DEFAULT_CTRL_PORT;

    static {
        ServerCtrlUI.DEFAULT_CTRL_PORT = 55311;
    }

    int maxLineNum;
    Vector contBuffer;
    BorderLayout borderLayout1;
    JTextArea contTextArea;
    JScrollPane scrollPane1;
    Panel cmdPanel;
    BorderLayout borderLayout2;
    Button btnSendCmd;
    TextField tfCmdTxt;
    Thread recvTh;
    Socket socket;
    InputStream inputs;
    OutputStream outputs;
    PrintStream outPS;
    Runnable runner;

    public ServerCtrlUI() {
        this.maxLineNum = 100;
        this.contBuffer = new Vector();
        this.borderLayout1 = new BorderLayout();
        this.contTextArea = new JTextArea();
        this.scrollPane1 = new JScrollPane(this.contTextArea);
        this.cmdPanel = new Panel();
        this.borderLayout2 = new BorderLayout();
        this.btnSendCmd = new Button("Enter");
        this.tfCmdTxt = new TextField();
        this.recvTh = null;
        this.socket = null;
        this.inputs = null;
        this.outputs = null;
        this.outPS = null;
        this.runner = new Runnable() {
            @Override
            public void run() {
                try {
                    final InputStreamReader isr = new InputStreamReader(ServerCtrlUI.this.inputs, "UTF-8");
                    final char[] buf = new char[1024];
                    while (ServerCtrlUI.this.recvTh != null) {
                        final int i = isr.read(buf);
                        if (i < 0) {
                            break;
                        }
                        final String tmps = new String(buf, 0, i);
                        ServerCtrlUI.this.onRecvStr(tmps);
                    }
                } catch (final Exception ee) {
                    ServerCtrlUI.this.onRecvStr(">>**>>read server error:" + ee.getMessage());
                    ServerCtrlUI.this.onRecvStr(">>**>>will disconnect from server!!");
                    return;
                } finally {
                    ServerCtrlUI.this.disconnFromServer();
                }
                ServerCtrlUI.this.disconnFromServer();
            }
        };
        try {
            this.jbInit();
            this.myInit();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(final String[] args) throws Throwable {
        final Frame frame = new Frame();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                System.exit(0);
            }
        });
        final ServerCtrlUI applet = new ServerCtrlUI();
        final BorderLayout borderLayout1 = new BorderLayout();
        final Panel panel1 = new Panel();
        final Panel panel2 = new Panel();
        final BorderLayout borderLayout2 = new BorderLayout();
        frame.setLayout(borderLayout1);
        panel1.setLayout(borderLayout2);
        frame.add(panel1, "Center");
        frame.add(panel2, "South");
        panel1.add(applet, "Center");
        frame.setTitle("Tomato Server Console");
        frame.setLayout(new BorderLayout());
        frame.add(applet, "Center");
        frame.setSize(700, 620);
        final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((d.width - frame.getSize().width) / 2, (d.height - frame.getSize().height) / 2);
        frame.setVisible(true);
    }

    private void jbInit() throws Exception {
        this.setLayout(this.borderLayout1);
        this.contTextArea.setBackground(Color.black);
        this.contTextArea.setForeground(Color.white);
        this.add(this.scrollPane1, "Center");
        this.cmdPanel.setLayout(this.borderLayout2);
        this.cmdPanel.add(this.tfCmdTxt, "Center");
        this.cmdPanel.add(this.btnSendCmd, "East");
        this.add(this.cmdPanel, "South");
    }

    private void myInit() throws Exception {
        this.btnSendCmd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                ServerCtrlUI.this.doSendCmd();
            }
        });
        this.tfCmdTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent e) {
                if (e.getKeyChar() == '\r' || e.getKeyChar() == '\n') {
                    ServerCtrlUI.this.doSendCmd();
                } else {
                    super.keyPressed(e);
                }
            }
        });
        this.tfCmdTxt.requestFocus();
    }

    private void doSendCmd() {
        try {
            this.doSendCmdDo();
        } catch (final Exception ee) {
            this.onRecvStr("XXX cmd error:" + ee.getMessage() + "\n");
        }
    }

    private void doSendCmdDo() throws Exception {
        String cmd = this.tfCmdTxt.getText();
        if (cmd == null) {
            return;
        }
        this.tfCmdTxt.setText("");
        this.tfCmdTxt.requestFocus();
        cmd = cmd.trim();
        if ("".equals(cmd)) {
            return;
        }
        if ("?".equals(cmd)) {
            this.setDisLine("************Tomato Server Control Console\n");
            this.setDisLine("conn [server] [port] to connect to server!\n");
            return;
        }
        if ("cls".equalsIgnoreCase(cmd)) {
            this.clear();
            return;
        }
        if (!"conn".equals(cmd) && !cmd.startsWith("conn ")) {
            this.setDisStr(cmd);
            final PrintStream ps = this.outPS;
            if (ps != null) {
                ps.println(cmd);
            }
            return;
        }
        if (this.recvTh != null) {
            this.setDisLine("Already Connected\n");
            return;
        }
        final StringTokenizer st = new StringTokenizer(cmd, " :");
        final int ct = st.countTokens();
        st.nextToken();
        if (ct == 1) {
            this.connToServer("localhost", ServerCtrlUI.DEFAULT_CTRL_PORT);
        } else if (ct == 2) {
            this.connToServer(st.nextToken(), ServerCtrlUI.DEFAULT_CTRL_PORT);
        } else if (ct > 2) {
            this.connToServer(st.nextToken(), Integer.parseInt(st.nextToken()));
        }
    }

    private void connToServer(final String ip, final int port) throws Exception {
        this.socket = new Socket(ip, port);
        this.inputs = this.socket.getInputStream();
        this.outputs = this.socket.getOutputStream();
        this.outPS = new PrintStream(this.outputs, true, "UTF-8");
        (this.recvTh = new Thread(this.runner, "server_ctrl_ui_recv")).start();
    }

    private void disconnFromServer() {
        if (this.inputs != null) {
            try {
                this.inputs.close();
            } catch (final Exception ex) {
            }
            this.inputs = null;
        }
        if (this.outputs != null) {
            try {
                this.outputs.close();
            } catch (final Exception ex2) {
            }
            this.outputs = null;
        }
        if (this.socket != null) {
            try {
                this.outputs.close();
            } catch (final Exception ex3) {
            }
            this.socket = null;
        }
        this.recvTh = null;
        this.setDisLine(">>**>>disconnected from server,please reconnect!!");
    }

    public void onRecvStr(final String str) {
        try {
            final BufferedReader br = new BufferedReader(new StringReader(str));
            String line = null;
            line = br.readLine();
            if (line == null) {
                return;
            }
            if (this.contBuffer.size() > 0) {
                this.contBuffer.setElementAt(this.contBuffer.elementAt(this.contBuffer.size() - 1) + line, this.contBuffer.size() - 1);
            } else {
                this.contBuffer.addElement(line);
            }
            while ((line = br.readLine()) != null) {
                this.contBuffer.addElement(line);
            }
            while (this.contBuffer.size() > this.maxLineNum) {
                this.contBuffer.remove(0);
            }
            final int s = this.contBuffer.size();
            final StringBuffer sb = new StringBuffer(s * 100);
            for (int i = 0; i < s; ++i) {
                if (i < s - 1) {
                    sb.append(this.contBuffer.elementAt(i)).append('\n');
                } else {
                    sb.append(this.contBuffer.elementAt(i));
                }
            }
            this.contTextArea.setText(sb.toString());
            this.contTextArea.setCaretPosition(sb.length());
            this.contTextArea.invalidate();
            this.contTextArea.requestFocus();
            this.tfCmdTxt.requestFocus();
        } catch (final IOException ex) {
        }
    }

    public void setDisLine(final String str) {
        this.contBuffer.addElement(str);
        if (this.contBuffer.size() > this.maxLineNum) {
            this.contBuffer.remove(0);
        }
        final int s = this.contBuffer.size();
        final StringBuffer sb = new StringBuffer(s * 100);
        for (int i = 0; i < s; ++i) {
            sb.append(this.contBuffer.elementAt(i)).append('\n');
        }
        this.contTextArea.setText(sb.toString());
        this.contTextArea.setCaretPosition(sb.length());
        this.contTextArea.requestFocus();
        this.tfCmdTxt.requestFocus();
    }

    public void setDisStr(final String str) {
        this.contBuffer.addElement(str);
        if (this.contBuffer.size() > this.maxLineNum) {
            this.contBuffer.remove(0);
        }
        final int s = this.contBuffer.size();
        final StringBuffer sb = new StringBuffer(s * 100);
        for (int i = 0; i < s; ++i) {
            if (i < s - 1) {
                sb.append(this.contBuffer.elementAt(i)).append('\n');
            } else {
                sb.append(this.contBuffer.elementAt(i));
            }
        }
        this.contTextArea.setText(sb.toString());
        this.contTextArea.setCaretPosition(sb.length());
        this.contTextArea.requestFocus();
        this.tfCmdTxt.requestFocus();
    }

    public void clear() {
        this.contBuffer.clear();
        this.contTextArea.setText("");
    }

    public void setMaxLineNum(final int mdb) {
        if (mdb <= 0) {
            throw new IllegalArgumentException("Max line num cannot < 0 !");
        }
        this.maxLineNum = mdb;
    }
}
