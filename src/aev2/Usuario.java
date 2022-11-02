package aev2;

/**
 * @author Miguel Clase para crear objetos Usuario, la cual nos servira para
 *         enlazar cada usuario con su contrasenya obtenidos de la base de
 *         datos\ y poder comparar con la introducida por el usuario
 */
public class Usuario {
	private String nombre, password;

	/**
	 * Constructor de clase
	 * 
	 * @param nombre
	 * @param password
	 */
	public Usuario(String nombre, String password) {
		super();
		this.nombre = nombre;
		this.password = password;
	}

	public String getNombre() {
		return nombre;
	}

	public String getPassword() {
		return password;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		return "Usuario [nombre=" + nombre + ", password=" + password + "]";
	}

}
