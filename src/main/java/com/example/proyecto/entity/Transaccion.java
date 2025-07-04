package com.example.proyecto.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cita_id")
    private Cita cita;

    @ManyToOne
    @JoinColumn(name = "paciente_id")
    private Paciente paciente;

    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private TipoTransaccion tipo;

    @Enumerated(EnumType.STRING)
    private EstadoTransaccion estado = EstadoTransaccion.PROCESANDO;

    @Enumerated(EnumType.STRING)
    private MetodoPago metodoPago;

    private String numeroTransaccion;

    private String numeroAutorizacion;

    private LocalDateTime fechaTransaccion = LocalDateTime.now();

    private String descripcion;

    private String observaciones;

    public enum TipoTransaccion {
        PAGO("Pago"),
        REEMBOLSO("Reembolso"),
        CARGO_ADICIONAL("Cargo Adicional"),
        DESCUENTO("Descuento");

        private final String descripcion;

        TipoTransaccion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDescripcion() {
            return descripcion;
        }
    }

    public enum EstadoTransaccion {
        PROCESANDO("Procesando"),
        EXITOSA("Exitosa"),
        FALLIDA("Fallida"),
        CANCELADA("Cancelada"),
        PENDIENTE_REVISION("Pendiente de Revisión");

        private final String descripcion;

        EstadoTransaccion(String descripcion) {
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
