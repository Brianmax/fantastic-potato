package com.example.demo.service;

import com.example.demo.dto.ProductoDTO;
import com.example.demo.exception.InvalidOperationException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Producto;
import com.example.demo.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Transactional
    public Producto crearProducto(ProductoDTO dto) {
        // BUG SUTIL: No valida que el precio sea mayor que cero
        // Permite crear productos con precio 0 o negativo
        if (dto.getPrecio() == null) {
            throw new InvalidOperationException("El precio es requerido");
        }

        Producto producto = new Producto();
        producto.setNombre(dto.getNombre());
        producto.setDescripcion(dto.getDescripcion());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock() != null ? dto.getStock() : 0);
        producto.setDisponible(true);

        return productoRepository.save(producto);
    }

    public Producto obtenerProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerDisponibles() {
        return productoRepository.findByDisponibleTrue();
    }

    @Transactional
    public Producto actualizarStock(Long id, Integer cantidad) {
        Producto producto = obtenerProducto(id);

        // BUG SUTIL: Permite actualizar el stock a valores negativos
        // La validación debería ser: nuevoStock >= 0
        int nuevoStock = producto.getStock() + cantidad;
        producto.setStock(nuevoStock);

        // BUG SUTIL: Marca como no disponible solo si stock es exactamente 0
        // Si el stock es negativo, el producto sigue disponible
        if (nuevoStock == 0) {
            producto.setDisponible(false);
        } else {
            producto.setDisponible(true);
        }

        return productoRepository.save(producto);
    }

    public boolean hayStockDisponible(Long id, Integer cantidad) {
        Producto producto = obtenerProducto(id);

        // BUG SUTIL: Usa >= en lugar de >
        // Si el stock es exactamente igual a la cantidad, retorna true
        // pero después de la compra el stock quedaría en 0
        return producto.getStock() >= cantidad && producto.getDisponible();
    }
}
