package com.example.demo.service;

import com.example.demo.dto.CrearPedidoDTO;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.ItemPedido;
import com.example.demo.entity.Pedido;
import com.example.demo.entity.Producto;
import com.example.demo.entity.Usuario;
import com.example.demo.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioService usuarioService;
    private final ProductoService productoService;

    @Transactional
    public Pedido crearPedido(CrearPedidoDTO dto) {
        Usuario usuario = usuarioService.obtenerUsuario(dto.getUsuarioId());

        // BUG SUTIL: No valida que el usuario esté activo
        // Permite crear pedidos para usuarios desactivados

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);

        // Procesar items
        for (CrearPedidoDTO.ItemPedidoDTO itemDTO : dto.getItems()) {
            Producto producto = productoService.obtenerProducto(itemDTO.getProductoId());

            // BUG SUTIL: No valida que la cantidad sea mayor que 0
            // Permite agregar items con cantidad 0 o negativa
            if (!productoService.hayStockDisponible(itemDTO.getProductoId(), itemDTO.getCantidad())) {
                throw new InvalidOperationException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            ItemPedido item = new ItemPedido();
            item.setProducto(producto);
            item.setCantidad(itemDTO.getCantidad());
            item.setPrecioUnitario(producto.getPrecio());

            pedido.agregarItem(item);

            // Reducir stock
            productoService.actualizarStock(producto.getId(), -itemDTO.getCantidad());
        }

        // Calcular total
        BigDecimal total = calcularTotal(pedido);

        // Aplicar descuento si existe
        if (dto.getDescuento() != null) {
            // BUG SUTIL: No valida que el descuento no sea mayor que el total
            // Puede resultar en totales negativos
            total = total.subtract(dto.getDescuento());
            pedido.setDescuento(dto.getDescuento());
        }

        pedido.setTotal(total);
        pedido.setEstado(Pedido.EstadoPedido.PENDIENTE);

        return pedidoRepository.save(pedido);
    }

    private BigDecimal calcularTotal(Pedido pedido) {
        // BUG SUTIL: No valida que los items tengan cantidad válida
        return pedido.getItems().stream()
                .map(ItemPedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Pedido obtenerPedido(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado"));
    }

    public List<Pedido> obtenerPedidosUsuario(Long usuarioId) {
        Usuario usuario = usuarioService.obtenerUsuario(usuarioId);
        return pedidoRepository.findByUsuario(usuario);
    }

    @Transactional
    public Pedido confirmarPedido(Long id) {
        Pedido pedido = obtenerPedido(id);

        // BUG SUTIL: No valida el estado actual del pedido
        // Permite confirmar un pedido ya confirmado, enviado, entregado o cancelado
        pedido.setEstado(Pedido.EstadoPedido.CONFIRMADO);

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cancelarPedido(Long id) {
        Pedido pedido = obtenerPedido(id);

        // BUG SUTIL: No valida que solo se puedan cancelar pedidos en estado PENDIENTE
        // Permite cancelar pedidos ya enviados o entregados
        pedido.setEstado(Pedido.EstadoPedido.CANCELADO);

        // Restaurar stock
        for (ItemPedido item : pedido.getItems()) {
            productoService.actualizarStock(item.getProducto().getId(), item.getCantidad());
        }

        return pedidoRepository.save(pedido);
    }

    public BigDecimal calcularTotalConDescuento(BigDecimal subtotal, BigDecimal descuento) {
        // BUG SUTIL: No valida que el descuento sea positivo ni que no exceda el subtotal
        if (descuento == null) {
            return subtotal;
        }
        return subtotal.subtract(descuento);
    }
}
