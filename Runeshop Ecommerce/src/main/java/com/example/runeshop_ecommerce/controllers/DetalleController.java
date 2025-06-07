package com.example.runeshop_ecommerce.controllers;

import com.example.runeshop_ecommerce.entities.Detalle;
import com.example.runeshop_ecommerce.services.DetalleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.parser.Entity;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/detalle")
@Tag(name = "Detalle", description = "Detalles de los productos")
public class DetalleController extends BaseController<Detalle, Long> {

    private final DetalleService detalleService;

    public DetalleController(DetalleService detalleService, DetalleService detalleService1) {
        super(detalleService);
        this.detalleService = detalleService1;
    }

    @GetMapping("/paginado")
    @Operation(
            summary = "Obtener todos los detalles con su paginacion",
            description = "Metodo personalizado para la paginacion",
            tags = {"GetMapping"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Detalles traidos correctamente",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    array = @ArraySchema(schema = @Schema(implementation = Detalle.class))
                            )
                    )
            }
    )
    public ResponseEntity<Page<Detalle>> getDetallesPaginados(
            @Parameter(description = "Pagina incial (valor por default = 0)")
            @RequestParam(defaultValue = "0", required = false) int page,

            @Parameter(description = "Cantidad de paginas (valor por default = 10)")
            @RequestParam(defaultValue = "10", required = false) int size
    ) throws Exception {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<Detalle> detallePage = detalleService.getDetallesPaginados(pageable);

            if (detallePage.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok().body(detallePage);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @PutMapping("/actualizarImagenDetalle")
    @Operation(
            summary = "Actualizar la imagen de un detalle",
            description = "Metodo personalizado que recibe el id de la imagen, el id del detalle y la imagen para actualizarse",
            tags = {"PutMapping"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Imagen actualizada correctamente",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = Detalle.class
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Detalle> actualizarDetalle(
            @Parameter(description = "ID del detalle", required = true)
            @RequestPart("detalleId") Long detalleId,

            @Parameter(description = "ID de la imagen", required = true)
            @RequestPart("imagenId") Long imagenId,

            @Parameter(description = "Imagen nueva", required = true)
            @RequestPart("imagen") MultipartFile file
    ) throws Exception {
        try {
            Detalle actualizarDetalle = detalleService.updateDetalle(detalleId, imagenId, file);
            return ResponseEntity.ok(actualizarDetalle);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }

    @PutMapping("/agregarDescuento")
    @Operation(
            summary = "Agregar un descuento al detalle",
            description = "Metodo personalizado que recibe el id del detalle, el id de descuento y la fecha de fin del descuento",
            tags = {"PutMapping"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Descuento agregado correctamente",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = Detalle.class
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Detalle> agregarDescuento(
            @Parameter(description = "ID del detalle", required = true)
            @RequestParam("detalleId") Long detalleId,

            @Parameter(description = "ID del descuento", required = true)
            @RequestParam("descuentoId") Long descuentoId,

            @Parameter(description = "Fecha de expiracion del descuento", required = true)
            @RequestParam("finDescuento") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fecha
            ) throws Exception {
        try {
            Detalle descuento = detalleService.aplicarDescuento(detalleId, descuentoId, fecha);
            return ResponseEntity.status(200).body(descuento);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
