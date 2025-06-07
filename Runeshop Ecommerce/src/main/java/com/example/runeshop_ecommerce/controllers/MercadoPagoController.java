package com.example.runeshop_ecommerce.controllers;

import com.example.runeshop_ecommerce.entities.Detalle;
import com.example.runeshop_ecommerce.entities.OrdenCompra;
import com.example.runeshop_ecommerce.exception.NotStockException;
import com.example.runeshop_ecommerce.repositories.OrdenCompraRepository;
import com.example.runeshop_ecommerce.services.OrdenCompraService;
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.mercadopago.resources.preference.PreferenceBackUrls;
import io.github.cdimascio.dotenv.Dotenv;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jdk.jfr.ContentType;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/mercado")
@Tag(name = "MercadoPago", description = "Controlador que se ejecuta cuando se va a realizar un pago")
public class MercadoPagoController {

    protected String mercadoPagoKey = System.getenv("MERCADOPAGO_API_KEY_SANDBOX");


    private final OrdenCompraService ordenCompraService;

    public MercadoPagoController(OrdenCompraService ordenCompraService) {
        this.ordenCompraService = ordenCompraService;
    }

    @GetMapping("/pago")
    @Operation(
            summary = "Controlador de MercadoPago",
            description = "Dicho metodo genera una orden de compra y luego redireccion a la pagina de pago de MercadoPago",
            tags = {"GetMapping"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "No hay descripcion, deberia redirigirte a la pagina de pago",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(
                                                    implementation = String.class
                                            )
                                    )
                            }
                    )
            }
    )
    public String mercado(
            @Parameter(description = "ID del detalle (Productos que se van a comprar)", required = true)
            @RequestParam("detallesId") List<Long> detallesId,

            @Parameter(description = "ID del usuario direccion (Donde y quien realiza la compra)", required = true)
            @RequestParam("usuarioDireccionId") Long usuarioDireccionId
    ) {
        try {
            MercadoPagoConfig.setAccessToken(mercadoPagoKey);

            OrdenCompra ordenCompra = ordenCompraService.generarOrdenCompra(detallesId, usuarioDireccionId);
            List<PreferenceItemRequest> items = new ArrayList<>();

            for (Detalle detalle : ordenCompra.getDetalles()) {

                ordenCompraService.reducirStock(detalle.getId());

                Double precioFinal = (detalle.getDescuentos() != null)
                        ? detalle.getPrecioDescuento()
                        : detalle.getPrecio().getPrecioVenta();

                System.out.println("==== PREFERENCIA DE ITEM ====");
                System.out.println("ID: " + detalle.getId());
                System.out.println("Título: " + detalle.getProducto().getModelo());
                System.out.println("Imagen URL: " + detalle.getImagenes().get(0).getImagenUrl());
                System.out.println("Precio Final: " + precioFinal);
                System.out.println("=================================");

                PreferenceItemRequest item = PreferenceItemRequest.builder()
                        .id(detalle.getId().toString())
                        .title(detalle.getProducto().getModelo())
                        .pictureUrl(detalle.getImagenes().get(0).getImagenUrl())
                        .categoryId(detalle.getProducto().getCategoria().getNombre())
                        .quantity(1)
                        .currencyId("ARS")
                        .unitPrice(BigDecimal.valueOf(precioFinal))
                        .build();

                items.add(item);
            }

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                            .success("https://99e2-2803-9800-9842-7276-90db-2977-c9c5-cd22.ngrok-free.app/exito")
                            .pending("https://99e2-2803-9800-9842-7276-90db-2977-c9c5-cd22.ngrok-free.app/pendiente")
                            .failure("https://99e2-2803-9800-9842-7276-90db-2977-c9c5-cd22.ngrok-free.app/fallo")
                            .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    .autoReturn("approved")
                    .externalReference(ordenCompra.getId().toString())
                    .build();

            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);


            return preference.getInitPoint();

        } catch (Exception e) {
            throw new RuntimeException("Error al crear la preferencia: " + e.getMessage());
        }
    }

    @GetMapping("/exito")
    @Operation(
            summary = "Ruta de MercadoPago cuando se realiza el pago sin exito",
            description = "BackUrl de pago exitoso",
            tags = {"GetMapping"},
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Redireccion hacia la pagina principal (landing) o pagina personalizada de confirmacion de pago",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(
                                                    implementation = String.class //Redirect View
                                            )
                                    )
                            }
                    )
            }
    )
    public String pagoExitoso(
            @Parameter(description = "ID del pago generado por Mercado pago", required = true)
            @RequestParam(required = false) String payment_id
    ) {
        return "Pago exitoso. ID del pago " + payment_id;
    }

    @GetMapping("/fallo")
    @Operation(
            summary = "Ruta de MercadoPago cuando el pago falla",
            description = "BackUrl de pago fallido",
            tags = {"GetMapping"},
            responses = {
                    @ApiResponse(
                            responseCode = "409",
                            description = "Redireccion hacia la pagina personalizada de pago fallido",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(
                                                    implementation = String.class //Redirect View
                                            )
                                    )
                            }
                    )
            }
    )
    public String pagoFallido(
            @Parameter(description = "Referencia externa generada por Mercado Pago (donde esta la orden de compra)")
            @RequestParam(required = false) String external_reference
    ) {
        Long ordenCompraId = Long.valueOf(external_reference);
        ordenCompraService.deleteOrdenCompraFallida(ordenCompraId);

        return "El pago fallo.";
    }

    @GetMapping("/pendiente")
    @Operation(
            summary = "Ruta de MercadoPago cuando el pago esta pendiente",
            description = "BackUrl de pago pendiente",
            tags = {"GetMapping"},
            responses = {
                    @ApiResponse(
                            responseCode = "500",
                            description = "Redireccion hacia la pagina personalizada de pago pendiente",
                            content = {
                                    @Content(
                                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                                            schema = @Schema(
                                                    implementation = String.class //Redirect View
                                            )
                                    )
                            }
                    )
            }
    )
    public String pagoPendiente() {
        return "El pago esta pendiente.";
    }
}
