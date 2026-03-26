package com.example.Service;

import com.example.Models.ServiceOrder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceOrderCalculationTest {

    // Como os métodos de cálculo não usam repositórios, podemos testá-los instanciando o serviço com nulls
    private final ServiceOrderService serviceOrderService = new ServiceOrderService(null, null, null, null, null, null, null);

    @Test
    public void testTechnicianPaymentCalculationPhase6() {
        ServiceOrder os = new ServiceOrder();
        os.setServiceValue(1000.0);      // Mão de Obra (Comissionável)
        os.setTravelValue(200.0);       // Tempo de Viagem (Comissionável)
        os.setDisplacementValue(150.0); // Km (NÃO Comissionável)
        os.setPartsValue(500.0);        // Peças (NÃO Comissionável)
        os.setExpensesValue(300.0);     // Despesas Extras (NÃO Comissionável)
        os.setDiscountValue(100.0);      // Desconto no faturamento

        // Total Faturado = 1000 + 200 + 150 + 500 + 300 - 100 = 2050.0
        Double total = serviceOrderService.calculateTotal(os);
        assertEquals(2050.0, total, 0.01, "O valor total faturado deve ser 2050.0");

        // Repasse Técnico (10%) = (1000 + 200) * 0.10 = 120.0
        // Peças, Km e Despesas são ignorados na comissão
        Double technicianPayment = serviceOrderService.calculateTechnicianPayment(os);
        assertEquals(120.0, technicianPayment, 0.01, "O repasse técnico deve ser 120.0 (10% de MoO + Viagem)");
    }

    @Test
    public void testCalculationWithNullValues() {
        ServiceOrder os = new ServiceOrder();
        os.setServiceValue(100.0);
        os.setTravelValue(50.0);
        
        Double total = serviceOrderService.calculateTotal(os);
        assertEquals(150.0, total, 0.01);
        
        Double payment = serviceOrderService.calculateTechnicianPayment(os);
        assertEquals(15.0, payment, 0.01);
    }
}
