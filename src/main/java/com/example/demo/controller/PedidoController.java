package com.example.demo.controller;

import com.example.demo.dto.CrearPedidoDTO;
import com.example.demo.entity.Pedido;
import com.example.demo.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
@Tag(name = "Controlador para pedidos", description = "Operaciones relacionadas con pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    @Operation(
            summary = "Crear nuevo pedido",
            description = """
                    Crea un nuevo pedido para un usuario con los productos especificados.
                    
                    El sistema:
                    - Valida que el usuario exista.
                    - Verfica el stock desponible
                    - Etc
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido creado exitosamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Pedido.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos invalidos (stock insuficiente, usuario no existe, etc",
                    content = @Content(mediaType = "application/json")
            )
        }
    )
    @PostMapping
    public ResponseEntity<Pedido> crearPedido(@RequestBody CrearPedidoDTO dto) {
        Pedido pedido = pedidoService.crearPedido(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtenerPedido(@PathVariable Long id) {
        Pedido pedido = pedidoService.obtenerPedido(id);
        return ResponseEntity.ok(pedido);
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Pedido>> obtenerPedidosUsuario(@PathVariable Long usuarioId) {
        List<Pedido> pedidos = pedidoService.obtenerPedidosUsuario(usuarioId);
        return ResponseEntity.ok(pedidos);
    }

    @PutMapping("/{id}/confirmar")
    public ResponseEntity<Pedido> confirmarPedido(@PathVariable Long id) {
        Pedido pedido = pedidoService.confirmarPedido(id);
        return ResponseEntity.ok(pedido);
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<Pedido> cancelarPedido(@PathVariable Long id) {
        Pedido pedido = pedidoService.cancelarPedido(id);
        return ResponseEntity.ok(pedido);
    }
}
