package com.example.runeshop_ecommerce.controllers;

import com.example.runeshop_ecommerce.entities.Imagen;
import com.example.runeshop_ecommerce.services.BaseService;
import com.example.runeshop_ecommerce.services.ImagenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;

@RestController
@RequestMapping("/imagen")
@Tag(name = "Imagen", description = "Imagenes de los productos")
public class ImagenController extends BaseController<Imagen, Long> {
    
    private final ImagenService imagenService;
    
    public ImagenController(ImagenService imagenService, ImagenService imagenService1) {
        super(imagenService);
        this.imagenService = imagenService1;
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar imagen",
            description = "Metodo HTTP para eliminar imagen en caso de que se suba una repetida",
            tags = {"DeleteMapping"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "La imagen se borro con exito",
                            content = @Content(
                                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(
                                            implementation = String.class
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<String> deleteString(
            @Parameter(description = "ID de imagen a eliminar", required = true)
            @PathVariable Long id
            ) throws Exception {
        try {
            imagenService.borrarImagen(id);
            return ResponseEntity.status(200).body("La imagen se borró con éxito");
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }
    }
}
