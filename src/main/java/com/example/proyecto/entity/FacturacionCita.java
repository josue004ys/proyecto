package com.example.proyecto.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FacturacionCita {

    private BigDecimal costo = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private EstadoPago estadoPago = EstadoPago.PENDIENTE;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    private String numeroTransaccion;

    private LocalDateTime fechaPago;

    private String observacionesPago;

    public enum EstadoPago {
        PENDIENTE("Pendiente"),
        PAGADO("Pagado"),
        PARCIAL("Pago Parcial"),
        CANCELADO("Cancelado"),
        REEMBOLSADO("Reembolsado");

        private final String descripcion;

        EstadoPago(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public enum MetodoPago {
        EFECTIVO("Efectivo"),
        TARJETA_CREDITO("Tarjeta de Crédito"),
        TARJETA_DEBITO("Tarjeta de Débito"),
        TRANSFERENCIA("Transferencia Bancaria"),
        SEGURO_MEDICO("Seguro Médico");

        private final String descripcion;

        MetodoPago(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }
}
