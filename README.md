# Catálogo de Libros

Aplicación de escritorio para manejar el inventario de una librería. Permite
registrar, ver, editar y eliminar libros, guardando todo en una base de datos
MariaDB.

## Cómo está armado

El proyecto tiene dos partes separadas:

- **libreria-core**: aquí vive el modelo (`Libro`) y el DAO que habla con la
  base de datos. No tiene nada de interfaz gráfica.
- **libreria-ui**: la ventana en Swing. Usa `libreria-core` como dependencia,
  no le copia el código.

Los separé así porque la idea es que la parte que guarda datos no tenga que
saber que existe una ventana, y la ventana no tenga que saber que existe SQL
por debajo. Si algo cambia en cómo guardo los datos, no tengo que tocar la
interfaz, y viceversa.

## La base de datos

Tabla `libro` con: id, título, autor, categoría, precio, existencias y año de
publicación. El script está en `libreria-ui/sql/schema.sql`.

Usé `DECIMAL` para el precio en vez de un número decimal normal porque con
dinero conviene evitar errores de redondeo. Puse `CHECK` para que el precio
no pueda ser negativo y las existencias tampoco.

## La ventana

Tiene tres pestañas:

1. **Catálogo**: la tabla con todos los libros, con un buscador. Doble clic en
   un libro abre una ventana para editarlo o eliminarlo.
2. **Registrar**: formulario para meter libros nuevos.
3. **Resumen**: un vistazo rápido de cuántos libros hay, cuántos ejemplares en
   total, cuánto vale el inventario y cuántos están agotados.

Antes de guardar cualquier cosa, valida que el título y el autor no estén
vacíos, que el precio sea mayor a cero y que el año no sea del futuro. Si algo
falla al conectar con la base de datos, no se cae la aplicación, solo muestra
un mensaje de error.

## Cómo correrlo

1. Ejecutar `schema.sql` en MariaDB para crear la base y la tabla.
2. Poner el usuario y contraseña correctos en `LibroDAO.java`.
3. Instalar el módulo core: `mvn install` dentro de `libreria-core`.
4. Correr `MainUI.java`.
