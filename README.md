# Sistema de Catálogo de Libros — CRUD Swing + Maven Multi-módulo + MariaDB

Proyecto individual desarrollado para el curso de Programación 2. Implementa un
CRUD completo (Crear, Leer, Actualizar, Eliminar) sobre el catálogo de una
librería, usando una interfaz gráfica de escritorio (Swing) sobre una base de
datos relacional (MariaDB), organizado en un proyecto Maven multi-módulo que
separa la lógica de acceso a datos de la interfaz gráfica.

**Variante asignada:** B — Catálogo de una librería (venta de libros).

---

## 1. Descripción del dominio

Una librería pequeña necesita digitalizar el catálogo de libros que tiene a la
venta. Cada libro registrado tiene:

- Identificador único, asignado automáticamente.
- Título.
- Autor (una sola persona por libro).
- Categoría o género (texto libre: Novela, Técnico, Infantil, Historia, etc.).
- Precio de venta (siempre positivo).
- Existencias disponibles (nunca negativas; puede ser cero, es decir, "agotado").
- Año de publicación (no puede ser mayor al año actual).

---

## 2. Arquitectura del proyecto

El proyecto está dividido en dos módulos Maven independientes bajo un proyecto
padre, para forzar la separación entre la capa de datos y la capa visual:

```
libreria-crud/                        (padre, packaging=pom)
├── pom.xml
├── libreria-core/                    (módulo librería: modelo + DAO)
│   ├── pom.xml                       (packaging=jar)
│   └── src/main/java/edu/umg/programacion2/proyecto/
│       ├── modelo/Libro.java
│       └── dao/LibroDAO.java
└── libreria-ui/                      (módulo aplicación: interfaz Swing)
    ├── pom.xml                       (packaging=jar, depende de libreria-core)
    ├── sql/schema.sql
    └── src/main/java/edu/umg/programacion2/proyecto/
        ├── MainUI.java
        └── ui/VentanaPrincipal.java
```

### ¿Por qué dos módulos separados?

Es la misma idea de separación de responsabilidades que se formaliza más
adelante con los principios SOLID: la lógica de acceso a datos (`libreria-core`)
no debería saber que existe Swing, y la interfaz gráfica (`libreria-ui`) no
debería saber que existe JDBC directamente.

Al declarar `libreria-core` como una dependencia Maven (`<dependency>`) del
módulo `libreria-ui` — en vez de copiar y pegar las clases del DAO — el
compilador obliga a respetar esa separación: `libreria-ui` ni siquiera tiene el
driver de MariaDB como dependencia directa (le llega de forma transitiva a
través de `libreria-core`), así que sería imposible usar `Connection`,
`PreparedStatement` o `ResultSet` por error dentro de la interfaz gráfica.

La única excepción es `java.sql.SQLException`, que la interfaz sí debe conocer
para poder atrapar los errores que el DAO propaga hacia arriba y mostrarlos al
usuario sin que la aplicación se caiga. Esto es distinto a usar JDBC
directamente: la UI nunca abre conexiones ni ejecuta sentencias SQL, solo
recibe la excepción ya empaquetada.

---

## 3. Diseño del esquema (`sql/schema.sql`)

```sql
CREATE DATABASE IF NOT EXISTS libreria_db;
USE libreria_db;

CREATE TABLE IF NOT EXISTS libro (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    titulo            VARCHAR(150) NOT NULL,
    autor             VARCHAR(100) NOT NULL,
    categoria         VARCHAR(50),
    precio            DECIMAL(10,2) NOT NULL,
    existencias       INT NOT NULL DEFAULT 0,
    anio_publicacion  INT NOT NULL,
    CONSTRAINT chk_precio_positivo CHECK (precio > 0),
    CONSTRAINT chk_existencias_no_negativas CHECK (existencias >= 0)
);
```

### Justificación de los tipos de dato

| Columna             | Tipo            | Justificación |
|---------------------|-----------------|----------------|
| `id`                 | `INT AUTO_INCREMENT PRIMARY KEY` | Identificador único generado por el motor, no por la aplicación. |
| `titulo`             | `VARCHAR(150)`  | Suficiente para títulos largos sin desperdiciar espacio como lo haría un `TEXT`. |
| `autor`              | `VARCHAR(100)`  | Nombre completo de una sola persona; no se modela coautoría según el enunciado. |
| `categoria`          | `VARCHAR(50)`   | Texto libre y corto; no existe un catálogo cerrado de géneros. |
| `precio`             | `DECIMAL(10,2)` | Nunca se usa `FLOAT`/`DOUBLE` para dinero, ya que introducen errores de redondeo binario. `DECIMAL` garantiza precisión exacta en dos decimales (centavos). |
| `existencias`        | `INT`           | Cantidades enteras de ejemplares; no existen medios ejemplares. |
| `anio_publicacion`   | `INT`           | Un año no necesita el tipo `DATE` completo. |

Las reglas de negocio (precio > 0, existencias ≥ 0, año no futuro) se validan
en dos niveles:

1. **En la base de datos**, con restricciones `CHECK`, como última línea de
   defensa contra datos inconsistentes.
2. **En la interfaz Swing**, antes de siquiera llamar al DAO, para dar
   retroalimentación inmediata al usuario sin necesidad de un viaje a la base
   de datos.

---

## 4. Contrato del DAO

```java
public class LibroDAO {
    public Libro crear(Libro libro) throws SQLException { ... }
    public List<Libro> listarTodos() throws SQLException { ... }
    public Optional<Libro> buscarPorId(int id) throws SQLException { ... }
    public boolean actualizar(Libro libro) throws SQLException { ... }
    public boolean eliminar(int id) throws SQLException { ... }
}
```

Toda sentencia SQL se ejecuta a través de `PreparedStatement`, tanto para
prevenir inyección SQL como por buena práctica en el manejo de tipos (por
ejemplo, `setBigDecimal` para el precio). Ninguna consulta concatena valores
directamente en el texto del SQL.

---

## 5. Interfaz gráfica (Swing)

La ventana principal (`VentanaPrincipal.java`) está organizada en tres
pestañas:

- **Catálogo**: tabla (`JTable`) con todos los libros, buscador en vivo que
  filtra por título, autor o categoría, y resalta en rojo los libros con
  existencias en cero ("Agotado"). Hacer doble clic sobre una fila abre una
  ventana emergente (`JDialog`) para editar o eliminar ese libro puntual.
- **Registrar**: formulario dedicado exclusivamente a dar de alta libros
  nuevos, con spinners numéricos para existencias y año (evitan que el
  usuario ingrese texto no numérico por error).
- **Resumen**: panel tipo dashboard con estadísticas del inventario (total de
  títulos, ejemplares en existencia, valor total del inventario y cantidad de
  títulos agotados), recalculado cada vez que se visita la pestaña.

### Validación antes de tocar la base de datos

Antes de llamar a cualquier método del DAO, la interfaz valida:

- Título y autor no vacíos.
- Precio numérico y mayor a cero.
- Existencias no negativas (garantizado por el `JSpinner`, que no permite
  valores fuera de su rango configurado).
- Año de publicación no mayor al año actual.

### Manejo de errores

Ningún `catch` queda vacío. Toda `SQLException` se captura, se registra en
consola con `System.err` (para depuración del desarrollador) y se muestra al
usuario final en un `JOptionPane` con un mensaje entendible, sin exponer el
stacktrace completo.

---

## 6. Cómo ejecutar el proyecto

### Requisitos previos

- JDK 11 o superior.
- Maven.
- MariaDB corriendo localmente, con un usuario que tenga permisos para crear
  bases de datos.

### Pasos

1. **Crear la base de datos**: ejecutar el contenido de `libreria-ui/sql/schema.sql`
   en MySQL Workbench, DBeaver, o por línea de comandos:
   ```bash
   mysql -u root -p < libreria-ui/sql/schema.sql
   ```

2. **Configurar la conexión**: en `libreria-core/src/main/java/edu/umg/programacion2/proyecto/dao/LibroDAO.java`,
   ajustar usuario y contraseña según el entorno local:
   ```java
   private static final String URL = "jdbc:mariadb://localhost:3306/libreria_db";
   private static final String USUARIO = "root";
   private static final String CLAVE = "TU_PASSWORD_AQUI";
   ```

3. **Instalar el módulo `libreria-core`** en el repositorio Maven local (necesario
   cada vez que se modifique el modelo o el DAO):
   ```bash
   cd libreria-core
   mvn install
   ```

4. **Ejecutar la aplicación**: correr la clase `MainUI.java`, ubicada en
   `libreria-ui/src/main/java/edu/umg/programacion2/proyecto/MainUI.java`.

---

## 7. Datos de prueba

```sql
USE libreria_db;

INSERT INTO libro (titulo, autor, categoria, precio, existencias, anio_publicacion)
VALUES
    ('Cien años de soledad', 'Gabriel García Márquez', 'Novela', 145.00, 12, 1967),
    ('Clean Code', 'Robert C. Martin', 'Tecnico', 220.50, 5, 2008),
    ('El principito', 'Antoine de Saint-Exupéry', 'Infantil', 85.00, 0, 1943);
```

---

## 8. Tecnologías utilizadas

- Java 11
- Swing (interfaz gráfica de escritorio)
- Maven (gestión de dependencias y estructura multi-módulo)
- MariaDB / JDBC (persistencia)
- Patrón DAO (Data Access Object) para desacoplar el modelo de datos de su
  origen (la base de datos)

---

## 9. Uso de inteligencia artificial en este proyecto

Conforme a lo permitido por el enunciado del proyecto, se utilizó IA como
apoyo para resolver dudas puntuales de sintaxis de Maven (declaración de un
`pom.xml` padre con módulos) y de la API de Swing (eventos de `JTable`,
componentes de `JSpinner`, estructura de un `JDialog`), así como para
depurar errores de conexión JDBC. El diseño del esquema de base de datos, la
lógica de negocio, las decisiones de arquitectura (separación en módulos) y
la validación de reglas de negocio fueron desarrolladas y comprendidas por el
autor del proyecto.
