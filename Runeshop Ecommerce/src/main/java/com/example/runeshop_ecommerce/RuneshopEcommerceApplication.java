package com.example.runeshop_ecommerce;

import com.example.runeshop_ecommerce.entities.*;
import com.example.runeshop_ecommerce.entities.enums.Marca;
import com.example.runeshop_ecommerce.entities.enums.Role;
import com.example.runeshop_ecommerce.entities.enums.TipoProducto;
import com.example.runeshop_ecommerce.repositories.*;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@EnableScheduling
@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class RuneshopEcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RuneshopEcommerceApplication.class, args);
        System.out.println("Servidor Iniciado");
    }
    @Bean
    @Transactional
    CommandLineRunner init(
            TalleRepository talleRepository,
            ImagenRepository imagenRepository,
            PrecioRepository precioRepository,
            DetalleRepository detalleRepository,
            UsuarioRepository usuarioRepository,
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            DescuentoRepository descuentoRepository,
            DireccionRepository direccionRepository,
            OrdenCompraRepository ordenCompraRepository,
            UsuarioDireccionRepository usuarioDireccionRepository

    ) {
        return args -> {
            try {
                // Usuario
                Usuario usuario = Usuario.builder()
                        .nombre("Juan")
                        .apellido("Perez")
                        .nombreUsuario("juan.perez")
                        .email("juanperez@example.com")
                        .contrasenia("password123")
                        .tipoUsuario(Role.USER)
                        .dni(12345678)
                        .build();
                usuarioRepository.save(usuario);

                // Direccion
                Direccion direccion = Direccion.builder()
                        .direccion("Pedro Molina 458")
                        .codigoPostal(5501)
                        .departamento("Palermo")
                        .provincia("Buenos Aires")
                        .pais("Argentina")
                        .build();
                direccionRepository.save(direccion);

                // UsuarioDireccion
                UsuarioDireccion usuarioDireccion = UsuarioDireccion.builder()
                        .usuario(usuario)
                        .direccion(direccion)
                        .build();
                usuarioDireccionRepository.save(usuarioDireccion);

                // Talle
                Talle talle = Talle.builder()
                        .numero(43)
                        .build();
                talleRepository.save(talle);

                Talle talle1 = Talle.builder()
                        .numero(52)
                        .build();
                talleRepository.save(talle1);

                // Categoria
                Categoria categoria = Categoria.builder()
                        .nombre("Urbano")
                        .build();
                categoriaRepository.save(categoria);

                Categoria categoria1 = Categoria.builder()
                        .nombre("Running")
                        .build();
                categoriaRepository.save(categoria1);

                // Imagen
                Imagen imagen = Imagen.builder()
                        .nombre("Jordan No Fake img")
                        .imagenUrl("http://res.cloudinary.com/dpyfse8qb/image/upload/v1748128244/eniss20uamjmqx6dnyjj.jpg")
                        .build();
                imagenRepository.save(imagen);

                Imagen imagen2 = Imagen.builder()
                        .nombre("Jordan No Fake img")
                        .imagenUrl("http://res.cloudinary.com/dpyfse8qb/image/upload/v1748129211/t0a6flafawooaxknq8sz.png")
                        .build();
                imagenRepository.save(imagen2);

                List<Imagen> imagenes = new ArrayList<>();
                imagenes.add(imagen);
                imagenes.add(imagen2);

                Imagen imagen3 = Imagen.builder()
                        .nombre("Remera No Fake img")
                        .imagenUrl("http://res.cloudinary.com/dpyfse8qb/image/upload/v1749212779/f4bqtlawyn00rhxcogkz.jpg")
                        .build();
                imagenRepository.save(imagen3);

                List<Imagen> imagenes1 = new ArrayList<>();
                imagenes1.add(imagen3);

                // Precio
                Precio precio = Precio.builder()
                        .precioCompra(23000.56)
                        .precioVenta(25000.00)
                        .build();
                precioRepository.save(precio);

                // Producto
                Producto producto = Producto.builder()
                        .modelo("Jordan No Fake")
                        .sexo("Hombre")
                        .tipoProducto(TipoProducto.ZAPATILLA)
                        .categoria(categoria)
                        .build();
                productoRepository.save(producto);

                Producto producto1 = Producto.builder()
                        .modelo("Remera No Fake")
                        .sexo("Hombre")
                        .tipoProducto(TipoProducto.REMERA)
                        .categoria(categoria1)
                        .build();
                productoRepository.save(producto1);

                // Detalle
                Detalle detalle = Detalle.builder()
                        .marca(Marca.ADIDAS)
                        .stock(100)
                        .color("Rojo")
                        .producto(producto)
                        .precio(precio)
                        .talle(talle)
                        .imagenes(imagenes)
                        .build();
                detalleRepository.save(detalle);
                List<Detalle> detalles = new ArrayList<>();
                detalles.add(detalle);
                talle.setDetalles(detalles);

                // Detalle
                Detalle detalle1 = Detalle.builder()
                        .marca(Marca.NIKE)
                        .stock(69)
                        .color("Azul")
                        .producto(producto1)
                        .precio(precio)
                        .talle(talle1)
                        .imagenes(imagenes1)
                        .build();
                detalleRepository.save(detalle1);
                List<Detalle> detalles1 = new ArrayList<>();
                detalles1.add(detalle1);
                talle.setDetalles(detalles1);

                // Descuento
                Descuento noDescuento = Descuento.builder()
                        .porcentaje("Sin descuento")
                        .valor(0.0)
                        .build();
                descuentoRepository.save(noDescuento);

                Descuento descuento10 = Descuento.builder()
                        .porcentaje("10")
                        .valor(0.10)
                        .build();
                descuentoRepository.save(descuento10);

                Descuento descuento20 = Descuento.builder()
                        .porcentaje("20")
                        .valor(0.20)
                        .build();
                descuentoRepository.save(descuento20);

                Descuento descuento30 = Descuento.builder()
                        .porcentaje("30")
                        .valor(0.30)
                        .build();
                descuentoRepository.save(descuento30);

                Descuento descuento40 = Descuento.builder()
                        .porcentaje("40")
                        .valor(0.40)
                        .build();
                descuentoRepository.save(descuento40);

                Descuento descuento50 = Descuento.builder()
                        .porcentaje("50")
                        .valor(0.50)
                        .build();
                descuentoRepository.save(descuento50);
            } catch (Exception e) {
                throw new Exception(e.getMessage());
            }
        };
    }

}
