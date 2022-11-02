package aev2;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Miguel Clase Modelo, gestiona la logica de la aplicacion
 */
public class Modelo {

	private String ficheroXML;

	// datos conexon BD
	private String driver = "com.mysql.cj.jdbc.Driver";
	private String url;
	private String usuario;
	private String password;
	private Connection con;
	private ArrayList<Usuario> usuarios = new ArrayList<Usuario>();
	private String usuarioLog;

	// CONSTRUCTOR
	/**
	 * CONTRUCTOR DE CLASE
	 */
	public Modelo() {

	}

	// GETTERS & SETTERS
	public String getFicheroXML() {
		return ficheroXML;
	}

	public void setFicheroXML(String ficheroXML) {
		this.ficheroXML = ficheroXML;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	// METODOS

	// Formatear fichero xml
	/**
	 * Metodo que recibe un fichero XML con los datos de conexion a la base de
	 * datos, lo procesa y asigna a las variables los campos adecuados de url,
	 * nombre de usuario y contrasenya para la conexion con la base de datos.
	 * 
	 * @param FicheroXML
	 * @return String con los datos almacenados en el fichero
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 */
	public String formatearXML(String FicheroXML) throws ParserConfigurationException, IOException, SAXException {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document document = dBuilder.parse(new File(ficheroXML));
		Element raiz = document.getDocumentElement();
		NodeList nodeList = document.getElementsByTagName("Datos");
		for (int i = 0; i < nodeList.getLength(); i++) {
			Element datos = (Element) nodeList.item(i);
			this.url = datos.getElementsByTagName("url").item(0).getTextContent();
			this.usuario = datos.getElementsByTagName("nombre").item(0).getTextContent();
			this.password = datos.getElementsByTagName("password").item(0).getTextContent();
		}
		String response = "Url: " + this.url + "\nUsuario: " + this.usuario + "\nPassword: " + this.password;
		System.out.println(response);
		return response;
	}

	// CONECTAR A LA BD
	/**
	 * Metodo que conectara con la base de datos, no recibe parametros de entrada,
	 * obtiene los datos de las variables almacenadas en la clase a traves de
	 * getters. Hace uso del metodo validaUsuario() para validar el usuario
	 * introducido por teclado
	 * 
	 * @return Retorna un String con un mensaje del resultado de la conexion
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public String conectarBD() throws ClassNotFoundException, SQLException {
		String result = "";
		String url = getUrl();
		String usuario = getUsuario();
		String password = getPassword();

		Class.forName(driver);
		con = DriverManager.getConnection(url, usuario, password);

		if (validaUsuario()) {
			if (con.isValid(0)) {
				JOptionPane.showMessageDialog(null, "Conexion realizada con exito.");
				result = "Conectado con: " + url + " -- Usuario: " + usuarioLog;
			}
		} else {
			JOptionPane.showMessageDialog(null, "Credenciales erroneas", "Alerta", JOptionPane.WARNING_MESSAGE);
			con.close();
		}

		return result;
	}

	// VALIDA USUARIO
	/**
	 * Metodo boolenano que valida al usuario con alguno de los usuarios almacenados
	 * en la base de datos Recibe los usuario y contrasenyas encriptadas desde la
	 * base de datos, crea objetos usuario para poder asociar cada contrasenya a su
	 * usuario y compara con la introducida por teclado mediante un popup,
	 * encriptandola mediante MD5 previamente
	 * 
	 * @return True en caso de usuario y contrasenya validas, en caso contrario,
	 *         False.
	 * @throws SQLException
	 */
	public boolean validaUsuario() throws SQLException {

		String usuario = JOptionPane.showInputDialog("Introduzca nombre de usuario:");
		String password = JOptionPane.showInputDialog("Introduzca contrasenya:");

		String passEncrypt = encriptarContrasenya(password);
		Usuario user = null;

		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM users");
		int columnas = rs.getMetaData().getColumnCount();

		while (rs.next()) {
			for (int i = 2; i <= columnas; i++) {
				user = new Usuario(rs.getString(2), rs.getString(3));
			}
			usuarios.add(user);
		}

		boolean userVal = false;
		boolean userPass = false;

		for (Usuario usuarioList : usuarios) {
			if (usuarioList.getNombre().equalsIgnoreCase(usuario)) {
				userVal = true;
				usuarioLog = usuario;
				if (usuarioList.getPassword().equals(passEncrypt)) {
					userPass = true;
				}
			}
		}

		if (userVal && userPass) {
			return true;
		} else {
			return false;
		}
	}

	// DESCONECTAR BD
	/**
	 * Metodo que cierra la conexion con la base de datos.
	 * 
	 * @return String con el resultado de la operacion.
	 * @throws SQLException
	 */
	public String desconectarBD() throws SQLException {
		String result = "";

		con.close();
		if (con.isValid(0)) {
			result = "Conexion no cerrada";
		} else {
			result = "Conexion cerrada correctamente";
		}
		return result;
	}

	// ENCRIPTAR CONTRASENYA MD5
	/**
	 * Metodo que encripta las contrasenyas mediante MD5
	 * 
	 * @param String con la contrasenya
	 * @return String con la contrasenya encriptada
	 */
	public String encriptarContrasenya(String contrasenya) {
		String passwordToHash = contrasenya;
		String generatedPassword = null;

		try {
			// Create MessageDigest instance for MD5
			MessageDigest md = MessageDigest.getInstance("MD5");

			// Add password bytes to digest
			md.update(passwordToHash.getBytes());

			// Get the hash's bytes
			byte[] bytes = md.digest();

			// This bytes[] has bytes in decimal format. Convert it to hexadecimal format
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}

			// Get complete hashed password in hex format
			generatedPassword = sb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return generatedPassword;
	}

	// ENVIAR SENTENCIAS SQL
	// SELECT
	/**
	 * Metodo que realiza consultas de tipo SELECT a la base de datos, necesario
	 * tener la conexion abierta con el metodo ConectarBD().
	 * 
	 * @param String con la consulta SQL, estraida del campo de texto de la interfaz
	 *               grafica
	 * @return ArrayList<String> con los resultados de la consulta, que se mostraran
	 *         el el textArea de la interfaz grafica.
	 * @throws SQLException
	 */
	public ArrayList<String> consultaSelect(String consulta) throws SQLException {
		ArrayList<String> response = new ArrayList<String>();
		String respuesta = "ERROR";
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(consulta);
		int columnas = rs.getMetaData().getColumnCount();

		while (rs.next()) {
			for (int i = 1; i <= columnas; i++) {
				response.add(rs.getMetaData().getColumnName(i) + ": " + rs.getString(i));

			}
			response.add("");
		}
		return response;
	}

	// RESTO DE CONSULTAS
	/**
	 * Metodo que se encarga de realizar las consultas tipo INSERT, UPDATE, DELETE,
	 * solicitando al usuario confirmacion de las mismas mediante un popup.
	 * 
	 * @param String con la consulta recogida de un textField de la interfaz
	 *               grafica.
	 * 
	 */
	public void consultaResto(String consulta) {
		int input = JOptionPane.showConfirmDialog(null, "¿Desear lanzar la consulta?", "Atención",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (input == 0) {
			PreparedStatement ps;
			try {
				ps = con.prepareStatement(consulta);
				int resultadoActualizar = ps.executeUpdate();
				if (resultadoActualizar == 1) {
					JOptionPane.showInternalMessageDialog(null, "Consulta ejecutada correcamente", "Query Status",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showInternalMessageDialog(null, "Ha ocurrido un error al ejecutar la consulta",
							"Status", JOptionPane.INFORMATION_MESSAGE);
				}
				ps.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}

	}

	// MOSTRAR CAMPOS DE TABLA
	/**
	 * Metodo que muestra la informacion de una tabla pasada por parametro
	 * 
	 * @param String con el nombre de la tabla
	 * @return ArrayList<String> con los datos obtenido de la consulta, que se
	 *         mostraran por la interfaz grafica
	 * @throws SQLException
	 */
	public ArrayList<String> mostrarCampos(String tabla) throws SQLException {
		ArrayList<String> campos = new ArrayList<String>();
		Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + tabla);
		int columnas = rs.getMetaData().getColumnCount();
		int count = 0;

		campos.add("Cantidad de columnas en tabla: " + columnas);
		campos.add("");
		while (rs.next()) {
			count++;
		}
		campos.add("Numero de registros: " + count + "\n");
		campos.add("COLUMNAS: ");
		for (int i = 1; i <= columnas; i++) {
			campos.add("Nombre: " + rs.getMetaData().getColumnName(i));
			campos.add("Tipo: " + rs.getMetaData().getColumnTypeName(i));
			campos.add("");

		}

		return campos;
	}

	// MOSTRAR TABLAS BD
	/**
	 * Metodo que muestra las tablas que hay en la base de datos
	 * 
	 * @return ArrayList<String> con el listado de tablas, que se mostrara en la
	 *         interfaz grafica
	 * @throws SQLException
	 */
	public ArrayList<String> mostrarTablas() throws SQLException {
		ArrayList<String> resultado = new ArrayList<String>();
		Statement stmt = con.createStatement();
		resultado.add("TABLAS EN BD: \n");
		ResultSet rs = stmt.executeQuery("SELECT * FROM titles");
		resultado.add(rs.getMetaData().getTableName(1) + "\n");
		rs = stmt.executeQuery("SELECT * FROM authors");
		resultado.add(rs.getMetaData().getTableName(1) + "\n");
		rs = stmt.executeQuery("SELECT * FROM editorials");
		resultado.add(rs.getMetaData().getTableName(1) + "\n");
		rs = stmt.executeQuery("SELECT * FROM users");
		resultado.add(rs.getMetaData().getTableName(1) + "\n");

		return resultado;
	}
}
