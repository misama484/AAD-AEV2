package aev2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * @author Miguel Clase que se encarga de gestionar las funcionalidades de la
 *         interfaz grafica
 */
public class Controlador {

	private Modelo modelo;
	private Vista vista;

	private ActionListener ALCargar, ALConectar, ALDesconectar, ALConsulta, ALCampos, ALTablas;

	public Controlador(Modelo modelo, Vista vista) {
		this.modelo = modelo;
		this.vista = vista;
		control();
	}

	private void control() {

		// MOSTRAR XML
		// mostramos un FileChooser para elegir el fichero xml
		ALCargar = new ActionListener() {
			/**
			 * Carga y procesa el fichero xml con los datos de la base de datos, muestra una
			 * ventana donde seleccionamos el fichero, y llamamos al metodo de la clase
			 * Modelo que gestionara el mismo.
			 * 
			 * @param e
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser("D:\\DAMFlorida\\AAD\\");
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.showOpenDialog(fc);
				modelo.setFicheroXML(fc.getSelectedFile().getAbsolutePath());
				vista.getTextFieldRuatXml().setText(fc.getSelectedFile().getAbsolutePath());

				try {
					vista.getTextAreaPrincipal().setText(modelo.formatearXML(modelo.getFicheroXML()));
				} catch (IOException | ParserConfigurationException | SAXException e1) {
					e1.printStackTrace();
				}
			}

		};
		vista.getBtnCargaXml().addActionListener(ALCargar);

		// CONECTAR BD
		ALConectar = new ActionListener() {
			/**
			 * Asocia el boton conectar con el metodo conectarBD, que conectara con la base
			 * de datos
			 * 
			 * @param e
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// vista.getTextAreaPrincipal().setText(conectarBD());
					vista.getLblEstado().setText(conectarBD());
				} catch (ClassNotFoundException | SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		};
		vista.getBtnConectar().addActionListener(ALConectar);

		// DESCONECTAR BD
		ALDesconectar = new ActionListener() {
			/**
			 * Asocia el boton Desconectar con el metodo DesconectarBD, que desconecta de la
			 * base de datos.
			 * 
			 * @param e
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					vista.getTextAreaPrincipal().setText(desconectaBD());
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		};
		vista.getBtnDesconectar().addActionListener(ALDesconectar);

		// CONSULTA SQL
		ALConsulta = new ActionListener() {
			/**
			 * Asocia el boton de enviar consulta con alguno de los dos metodos de
			 * consukltas, dependiendo de la sentencia indicada (SELCT,INSERT, UPDATE,
			 * DELETE)
			 */
			@Override
			public void actionPerformed(ActionEvent e) {

				String consulta = vista.getTextFieldSql().getText();
				if (consulta.contains("select")) {
					consultaSelect(consulta);
					vista.getLblEstado().setText("Ejecutando: " + consulta);
				} else {
					consultaResto(consulta);
					vista.getLblEstado().setText("Ejecutando: " + consulta);
				}

			}

		};
		vista.getBtnSql().addActionListener(ALConsulta);

		// CONSULTA CAMPOS TABLA
		ALCampos = new ActionListener() {
			/**
			 * Asocia el boton MOSTRAR CAMPOS con el metodo de la clase Modelo que muestra
			 * los campos de una tabla.
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				vista.getTextAreaTablas().setText("");
				vista.getTextAreaPrincipal().setText("");
				String tabla = vista.getTextFieldTabla().getText();
				vista.getLblEstado().setText("Mostrando campos de tabla " + tabla);
				try {
					ArrayList<String> campos = modelo.mostrarCampos(tabla);
					for (String campo : campos) {
						vista.getTextAreaTablas().append(campo + "\n");
						vista.getTextAreaPrincipal().append(campo + "\n");
					}
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		};
		vista.getBtnMostrarCampos().addActionListener(ALCampos);

		// CONSULTA TABLAS
		ALTablas = new ActionListener() {
			/**
			 * Asocia el boton MOSTRAR TABLAS con el metodo de la clase Modelo que muestra
			 * las tablas de la BD.
			 * 
			 * @param e
			 */
			@Override
			public void actionPerformed(ActionEvent e) {
				vista.getTextAreaTablas().setText("");
				vista.getLblEstado().setText("Mostrando tablas en BD");
				try {
					ArrayList<String> tablas = modelo.mostrarTablas();
					for (String tabla : tablas) {
						vista.getTextAreaTablas().append(tabla);
					}
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

		};
		vista.getBtnMostrarTablas().addActionListener(ALTablas);

	}

	/**
	 * Metodo para enlazar el metodo de conectar con la BD de la clase modelo con la
	 * interfaz grafica.
	 * 
	 * @return la llamada al metodo de la clase Modelo.
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	private String conectarBD() throws ClassNotFoundException, SQLException {
		return modelo.conectarBD();
	}

	/**
	 * Metodo para enlazar el metodo Deconectar con la bs de la clase modelo con la
	 * interfaz grafica.
	 * 
	 * @return la llamada al metodo de la clase Modelo.
	 * @throws SQLException
	 */
	private String desconectaBD() throws SQLException {
		return modelo.desconectarBD();
	}

	/**
	 * Metodo para enlazar el metodo consulta de la clase modelo con la interfaz
	 * grafica.
	 * 
	 * @return la llamada al metodo de la clase Modelo.
	 * @param consulta
	 */
	private void consultaSelect(String consulta) {
		vista.getTextAreaPrincipal().setText("");

		try {
			ArrayList<String> response = modelo.consultaSelect(consulta);
			for (String elemento : response) {
				vista.getTextAreaPrincipal().append(elemento + "\n");
			}
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	/**
	 * Metodo para enlazar el metodo de consultaResto de la clase modelo con la
	 * interfaz grafica.
	 * 
	 * @return la llamada al metodo de la clase Modelo.
	 * @param consulta
	 */
	private void consultaResto(String consulta) {
		modelo.consultaResto(consulta);
	}

}