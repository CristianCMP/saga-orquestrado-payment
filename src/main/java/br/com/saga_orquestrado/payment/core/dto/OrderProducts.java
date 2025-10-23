package br.com.saga_orquestrado.payment.core.dto;

import br.com.saga_orquestrado.product_validation.core.dto.Product;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderProducts {
    private Product product;
    private int quantity;
}
