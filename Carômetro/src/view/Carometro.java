package view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import model.DAO;
import utils.Validador;

public class Carometro extends JFrame {

	DAO dao = new DAO();
	private Connection con;
	private PreparedStatement pst;
	private ResultSet rs;

	private FileInputStream fis;
	private int tamanho;

	private boolean alunoCadastrado = false;

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JLabel lblStatus;
	private JLabel lblData;
	private JLabel lblRA;
	private JTextField txtRA;
	private JLabel lblNome;
	private JTextField txtNome;
	private JLabel lblFoto;
	private JButton btnAdicionar;
	private JButton btnReset;
	private JButton btnBuscar;
	private JButton btnExcluir;
	private JButton btnEditar;
	private JButton btnCarregar;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Carometro frame = new Carometro();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public Carometro() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				status();
				setarData();
			}
		});
		setTitle("Carômetro");
		setResizable(false);
		setIconImage(Toolkit.getDefaultToolkit().getImage(Carometro.class.getResource("/img/insta.png")));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 640, 360);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));

		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBackground(new Color(0, 0, 0));
		panel.setBounds(0, 272, 624, 49);
		contentPane.add(panel);
		panel.setLayout(null);

		lblStatus = new JLabel("");
		lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dboff.png")));
		lblStatus.setBounds(566, 11, 32, 32);
		panel.add(lblStatus);

		lblData = new JLabel("");
		lblData.setForeground(SystemColor.text);
		lblData.setFont(new Font("Tahoma", Font.PLAIN, 14));
		lblData.setBounds(30, 11, 390, 27);
		panel.add(lblData);

		lblRA = new JLabel("RA");
		lblRA.setBounds(28, 23, 46, 14);
		contentPane.add(lblRA);

		txtRA = new JTextField();
		txtRA.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				String caracteres = "0123456789";
				if (!caracteres.contains(e.getKeyChar() + "")) {
					e.consume();
				}
			}
		});
		txtRA.setBounds(62, 20, 86, 20);
		contentPane.add(txtRA);
		txtRA.setColumns(10);

		txtRA.setDocument(new Validador(6));

		lblNome = new JLabel("Nome");
		lblNome.setBounds(15, 69, 46, 14);
		contentPane.add(lblNome);

		txtNome = new JTextField();
		txtNome.setBounds(62, 66, 264, 20);
		contentPane.add(txtNome);
		txtNome.setColumns(10);
		txtNome.setDocument(new Validador(30));

		lblFoto = new JLabel("");
		lblFoto.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		lblFoto.setIcon(new ImageIcon(Carometro.class.getResource("/img/camera.png")));
		lblFoto.setBounds(347, 11, 256, 256);
		contentPane.add(lblFoto);

		btnCarregar = new JButton("Carregar foto");
		btnCarregar.setEnabled(false);
		btnCarregar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnCarregar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				carregarFoto();
			}
		});
		btnCarregar.setForeground(new Color(0, 0, 0));
		btnCarregar.setBounds(195, 116, 119, 23);
		contentPane.add(btnCarregar);

		btnAdicionar = new JButton("");
		btnAdicionar.setEnabled(false);
		btnAdicionar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnAdicionar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				adicionar();
			}
		});
		btnAdicionar.setToolTipText("Adicionar");
		btnAdicionar.setIcon(new ImageIcon(Carometro.class.getResource("/img/add.png")));
		btnAdicionar.setBounds(30, 190, 64, 64);
		contentPane.add(btnAdicionar);

		btnReset = new JButton("");
		btnReset.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});
		btnReset.setIcon(new ImageIcon(Carometro.class.getResource("/img/eraser.png")));
		btnReset.setToolTipText("Limpar campos");
		btnReset.setBounds(252, 190, 64, 64);
		contentPane.add(btnReset);

		btnBuscar = new JButton("Pesquisar");
		btnBuscar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnBuscar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!txtRA.getText().isEmpty() && !txtNome.getText().isEmpty()) {
					JOptionPane.showMessageDialog(null, "Para pesquisar preencha apenas um dos campos!");
				} else if (!txtRA.getText().isEmpty()) {
					buscarRA();
				} else if (!txtNome.getText().isEmpty()) {
					buscarNome();
				} else {
					JOptionPane.showMessageDialog(null, "Informe o RA ou o Nome para realizar a busca.");
				}
			}
		});
		btnBuscar.setForeground(new Color(0, 0, 0));
		btnBuscar.setBounds(62, 116, 119, 23);
		contentPane.add(btnBuscar);

		btnExcluir = new JButton("");
		btnExcluir.setEnabled(false);
		btnExcluir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				excluir();
			}
		});
		btnExcluir.setIcon(new ImageIcon(Carometro.class.getResource("/img/delete.png")));
		btnExcluir.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnExcluir.setToolTipText("Excluir");
		btnExcluir.setBounds(178, 190, 64, 64);
		contentPane.add(btnExcluir);

		btnEditar = new JButton("");
		btnEditar.setEnabled(false);
		btnEditar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editar();
			}
		});
		btnEditar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btnEditar.setIcon(new ImageIcon(Carometro.class.getResource("/img/edit.png")));
		btnEditar.setToolTipText("Editar");
		btnEditar.setBounds(104, 190, 64, 64);
		contentPane.add(btnEditar);
	}

	private void status() {
		try {
			con = dao.conectar();
			if (con == null) {

				lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dboff.png")));
			} else {

				lblStatus.setIcon(new ImageIcon(Carometro.class.getResource("/img/dbon.png")));
			}
			con.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private void setarData() {
		Date data = new Date();
		DateFormat formatador = DateFormat.getDateInstance(DateFormat.FULL);
		lblData.setText(formatador.format(data));
	}

	private void carregarFoto() {
		JFileChooser jfc = new JFileChooser();
		jfc.setDialogTitle("Selecionar arquivo");
		jfc.setFileFilter(new FileNameExtensionFilter("Arquivo de imagens (*.PNG,*.JPG,*JPEG)", "png", "jpg", "jpeg"));
		int resultado = jfc.showOpenDialog(this);
		if (resultado == JFileChooser.APPROVE_OPTION) {
			try {
				fis = new FileInputStream(jfc.getSelectedFile());
				tamanho = (int) jfc.getSelectedFile().length();
				Image foto = ImageIO.read(jfc.getSelectedFile()).getScaledInstance(lblFoto.getWidth(),
						lblFoto.getHeight(), Image.SCALE_SMOOTH);
				lblFoto.setIcon(new ImageIcon(foto));
				lblFoto.updateUI();

				if (alunoCadastrado) {
					btnEditar.setEnabled(true);
					btnAdicionar.setEnabled(false);
				} else {
					btnAdicionar.setEnabled(true);
					btnEditar.setEnabled(false);
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void adicionar() {
		if (txtNome.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Preencha o nome do aluno!");
			txtNome.requestFocus();
		} else {
			String insert = "insert into alunos(nome,foto) values (?,?)";
			try {
				con = dao.conectar();
				pst = con.prepareStatement(insert);
				pst.setString(1, txtNome.getText());
				pst.setBlob(2, fis, tamanho);
				int confirma = pst.executeUpdate();
				if (confirma == 1) {
					JOptionPane.showMessageDialog(null, "Aluno adicionado com sucesso!");
					reset();
					txtRA.requestFocus();
				} else {
					JOptionPane.showMessageDialog(null, "Erro! aluno não cadastrado!");
				}
				con.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void buscarRA() {
		if (txtRA.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Digite o RA ou nome do aluno!");
			txtRA.requestFocus();
		} else {
			String readRA = "select * from alunos where ra = ?";
			try {
				con = dao.conectar();
				pst = con.prepareStatement(readRA);
				pst.setString(1, txtRA.getText());
				rs = pst.executeQuery();
				if (rs.next()) {
					txtNome.setText(rs.getString(2));
					Blob blob = (Blob) rs.getBlob(3);
					byte[] img = blob.getBytes(1, (int) blob.length());
					BufferedImage imagem = null;
					try {
						imagem = ImageIO.read(new ByteArrayInputStream(img));
					} catch (Exception e) {
						System.out.println(e);
					}
					ImageIcon icone = new ImageIcon(imagem);
					Icon foto = new ImageIcon(icone.getImage().getScaledInstance(lblFoto.getWidth(),
							lblFoto.getHeight(), Image.SCALE_SMOOTH));
					alunoCadastrado = true;
					lblFoto.setIcon(foto);
					txtRA.setEnabled(false);
					btnBuscar.setEnabled(false);
					btnCarregar.setEnabled(true);
					btnExcluir.setEnabled(true);
				} else {
					JOptionPane.showMessageDialog(null, "Aluno não cadastrado!");
					alunoCadastrado = false;
					txtRA.setEnabled(false);
					txtRA.setText(null);
					txtNome.requestFocus();
					btnBuscar.setEnabled(false);
					btnCarregar.setEnabled(true);
				}
				con.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void buscarNome() {
		try {
			con = dao.conectar();
			pst = con.prepareStatement("SELECT * FROM alunos WHERE nome LIKE ?");
			pst.setString(1, "%" + txtNome.getText() + "%");
			rs = pst.executeQuery();
			if (rs.next()) {
				txtRA.setText(String.valueOf(rs.getInt("ra")));
				txtNome.setText(rs.getString("nome"));
				Blob blob = rs.getBlob("foto");
				byte[] dados = blob.getBytes(1, (int) blob.length());
				BufferedImage img = ImageIO.read(new ByteArrayInputStream(dados));
				ImageIcon foto = new ImageIcon(
						img.getScaledInstance(lblFoto.getWidth(), lblFoto.getHeight(), Image.SCALE_SMOOTH));
				lblFoto.setIcon(foto);
				alunoCadastrado = true;
			} else {
				JOptionPane.showMessageDialog(null, "Aluno não encontrado.");
				reset();
			}

			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void reset() {
		txtRA.setText(null);
		txtNome.setText(null);
		lblFoto.setIcon(new ImageIcon(Carometro.class.getResource("/img/camera.png")));
		alunoCadastrado = false;
		btnCarregar.setEnabled(false);
		btnAdicionar.setEnabled(false);
		btnEditar.setEnabled(false);
		btnExcluir.setEnabled(false);
		btnBuscar.setEnabled(true);
		txtRA.setEnabled(true);
		txtRA.requestFocus();
	}

	private void editar() {
		if (txtRA.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Preencha o RA do aluno!");
			txtRA.requestFocus();
		} else if (txtNome.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Preencha o nome do aluno!");
			txtNome.requestFocus();
		} else {
			String update = "update alunos set nome = ?, foto = ? where ra = ?";
			try {
				con = dao.conectar();
				pst = con.prepareStatement(update);
				pst.setString(1, txtNome.getText());
				pst.setBlob(2, fis, tamanho);
				pst.setString(3, txtRA.getText());
				int confirma = pst.executeUpdate();
				if (confirma == 1) {
					JOptionPane.showMessageDialog(null, "Dados do aluno alterados com sucesso!");
					reset();
				} else {
					JOptionPane.showMessageDialog(null, "Erro, não foi possível alterar os dados do aluno!");
				}
				con.close();
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private void excluir() {
		if (txtRA.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Digite o RA do aluno!");
			txtRA.requestFocus();
		} else if (txtNome.getText().isEmpty()) {
			JOptionPane.showMessageDialog(null, "Digite o nome do aluno!");
			txtNome.requestFocus();
		} else {
			int confirma = JOptionPane.showConfirmDialog(null, "Confirma a exclusão deste aluno?", "Atenção",
					JOptionPane.YES_NO_OPTION);
			if (confirma == JOptionPane.YES_OPTION) {
				String sql = "delete from alunos where ra=?";
				try {
					con = dao.conectar();
					pst = con.prepareStatement(sql);
					pst.setString(1, txtRA.getText());
					int apagado = pst.executeUpdate();
					if (apagado > 0) {
						JOptionPane.showMessageDialog(null, "Aluno excluido com sucesso!");
						reset();
					} else {
						JOptionPane.showMessageDialog(null, "Erro, Aluno não excluido.");
					}
					con.close();
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, e);
				}
			}
		}
	}
}
