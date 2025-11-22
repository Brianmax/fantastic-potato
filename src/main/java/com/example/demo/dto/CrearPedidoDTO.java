package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPedidoDTO {
    private Long usuarioId;
    private List<ItemPedidoDTO> items;
    private BigDecimal descuento;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemPedidoDTO {
        private Long productoId;
        private Integer cantidad;
    }
}
