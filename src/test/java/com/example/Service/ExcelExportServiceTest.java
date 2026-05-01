package com.example.Service;

import com.example.Models.ServiceOrder;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExcelExportServiceTest {

    private final ExcelExportService excelExportService = new ExcelExportService();

    @Test
    public void testExportServiceOrdersToExcelWithSXSSF() throws IOException {
        List<ServiceOrder> orders = new ArrayList<>();
        
        // Mocking a few orders
        ServiceOrder order1 = new ServiceOrder();
        order1.setId(1L);
        order1.setOsCode("OS2024010101");
        
        ServiceOrder order2 = new ServiceOrder();
        order2.setId(2L);
        order2.setOsCode("OS2024010102");
        
        orders.add(order1);
        orders.add(order2);
        
        byte[] result = excelExportService.exportServiceOrdersToExcel(orders);
        
        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}
