package main;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.JFrame;

public class ClienteTCPBasico extends JFrame implements Runnable {

	private Socket socket;

	private javax.swing.JButton btnEnviar;
	private javax.swing.JTextField entrada;
	private javax.swing.JTextField usuarioLogado;
	private javax.swing.JScrollPane jScrollPane1;
	private javax.swing.JTextArea saida;

	private DataInputStream in;
	private DataOutputStream out;
	private Thread listener;

	// trocar o nome do host caso queira testar
	private static String host = "Paulo";

	private static final long serialVersionUID = 7807451284291881701L;

	public static void main(String[] args) throws UnknownHostException, IOException {

		// tenta se conectar com o server
		Socket socket = null;
		try {
			socket = new Socket("127.0.0.1", 12345);
			new ClienteTCPBasico(socket);
		} catch (UnknownHostException ex) {
			ex.printStackTrace();
			System.out.println("Não encontrou o host servidor.");
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Não conseguiu abrir conexão com o host.");
		}

	}

	public ClienteTCPBasico(Socket socket) throws IOException {

		// inicia o front e as threads
		initComponents();

		this.socket = socket;

		// lê msgs do teclado e manda pro servidor
		this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
		this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

		out.writeUTF(this.host);
		out.flush();

		this.listener = new Thread(this);
		this.listener.start();

		this.setVisible(true);

	}

	@SuppressWarnings("unchecked")
	private void initComponents() {

		// define os componentes do front
		usuarioLogado = new javax.swing.JTextField();
		btnEnviar = new javax.swing.JButton();
		entrada = new javax.swing.JTextField();
		jScrollPane1 = new javax.swing.JScrollPane();
		saida = new javax.swing.JTextArea();

		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		setTitle("Chat Socket");
		addWindowListener(new java.awt.event.WindowAdapter() {
			public void windowClosing(java.awt.event.WindowEvent evt) {
				formWindowClosing(evt);
			}
		});

		btnEnviar.setText("Enviar");
		btnEnviar.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				btnEnviarActionPerformed(evt);
			}
		});

		jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		jScrollPane1.setAutoscrolls(true);

		saida.setColumns(20);
		saida.setLineWrap(true);
		saida.setRows(5);
		jScrollPane1.setViewportView(saida);

		usuarioLogado.setText(host);
		usuarioLogado.setEnabled(false);
		usuarioLogado.setDisabledTextColor(Color.BLACK);

		// define as posições dos componentes
		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
						.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE)
						.addGroup(layout.createSequentialGroup().addComponent(usuarioLogado,
								javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE))
						.addGroup(layout.createSequentialGroup()
								.addComponent(entrada, javax.swing.GroupLayout.DEFAULT_SIZE, 311, Short.MAX_VALUE)
								.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
								.addComponent(btnEnviar)))
				.addContainerGap()));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout
				.createSequentialGroup().addContainerGap()
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(
						usuarioLogado, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE,
						javax.swing.GroupLayout.PREFERRED_SIZE))
				.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(btnEnviar)
						.addComponent(entrada, javax.swing.GroupLayout.PREFERRED_SIZE,
								javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
						.addComponent(usuarioLogado))
				.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
				.addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
				.addContainerGap()));

		pack();
	}

	private void btnEnviarActionPerformed(java.awt.event.ActionEvent evt) {

		// lógica do envio de mensagens para o server
		try {
			if (this.entrada.getText().trim().length() > 0) {
				out.writeUTF(this.entrada.getText());
				out.flush();
				this.entrada.setText(null);
			}

			this.entrada.requestFocus();
		} catch (Exception ex) {
			ex.printStackTrace();
			sair();
		}
	}

	private void formWindowClosing(java.awt.event.WindowEvent evt) {
		sair();
	}

	public void run() {

		// mantém a thread do cliente ativa
		try {
			while (true) {
				String msg = this.in.readUTF();
				this.saida.append(msg);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private void sair() {

		// lógica para finalizar a conexão
		try {
			out.writeUTF("SAIR");
			out.flush();
			if (!this.socket.isClosed()) {
				this.socket.close();
			}

			if (listener != null) {
				listener.interrupt();
				listener = null;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
			System.out.println("Não conseguiu fechar o socket.");
		}
	}
}