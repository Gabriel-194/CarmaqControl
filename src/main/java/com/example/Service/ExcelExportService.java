package com.example.Service;

import com.example.Models.ServiceOrder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExcelExportService {

    private static final java.time.format.DateTimeFormatter DATE_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Exporta ordens de serviço utilizando SXSSF (Streaming API) para garantir complexidade de memória O(1).
     * Ideal para grandes volumes de dados industriais.
     */
    public byte[] exportServiceOrdersToExcel(List<ServiceOrder> orders) throws IOException {
        // SXSSF mantém apenas 100 linhas em memória e faz o swap para o disco
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Ordens de Serviço");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Código", "Data", "Status", "Cliente", "CNPJ", "Máquina", 
                "Tipo Serviço", "Técnico Responsável", "Pgto Técnico", 
                "Base (Mão de Obra)", "Viagem", "Deslocamento (Km)", "Peças", "Despesas", "Desconto Aplicado", 
                "Total Bruto", "Valor Faturado", "Taxa Boleto", "Impostos (12%)", "Lucro Líquido", "Motivo de Rejeição Repasse"
            };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (ServiceOrder order : orders) {
                Row row = sheet.createRow(rowIdx++);
                // ... rest of row population ...

                row.createCell(0).setCellValue(order.getOsCode() != null ? order.getOsCode() : order.getId().toString());
                row.createCell(1).setCellValue(order.getOpenedAt() != null ? order.getOpenedAt().format(DATE_FORMATTER) : "N/A");
                row.createCell(2).setCellValue(order.getStatus());
                row.createCell(3).setCellValue(order.getClient() != null ? order.getClient().getCompanyName() : "N/A");
                row.createCell(4).setCellValue(order.getClient() != null && order.getClient().getCnpj() != null ? order.getClient().getCnpj() : "N/A");
                row.createCell(5).setCellValue(order.getMachine() != null ? order.getMachine().getModel() : "N/A");
                row.createCell(6).setCellValue(order.getServiceType() != null ? order.getServiceType() : "N/A");
                row.createCell(7).setCellValue(order.getTechnician() != null ? order.getTechnician().getNome() : "N/A");
                row.createCell(8).setCellValue(order.getTechnicianPaymentStatus());
                
                double mo = order.getServiceValue() != null ? order.getServiceValue() : 0.0;
                double viagem = order.getTravelValue() != null ? order.getTravelValue() : 0.0;
                double km = order.getDisplacementValue() != null ? order.getDisplacementValue() : 0.0;
                double pecas = order.getPartsValue() != null ? order.getPartsValue() : 0.0;
                double despesas = order.getExpensesValue() != null ? order.getExpensesValue() : 0.0;
                double desconto = order.getDiscountValue() != null ? order.getDiscountValue() : 0.0;

                double bruto = mo + viagem + km + pecas + despesas;
                double faturado = bruto - desconto;
                
                double boleto = 3.50;
                double imposto = faturado * 0.12;
                
                // Repasse Técnico (10% sobre o Faturado descontando Impostos e Boleto)
                double netBase = faturado - imposto - boleto;
                if (netBase < 0) netBase = 0.0;
                double repasse = netBase * 0.10;
                
                double lucro = faturado - repasse - boleto - imposto;

                row.createCell(9).setCellValue(mo);
                row.createCell(10).setCellValue(viagem);
                row.createCell(11).setCellValue(km);
                row.createCell(12).setCellValue(pecas);
                row.createCell(13).setCellValue(despesas);
                row.createCell(14).setCellValue(desconto);
                row.createCell(15).setCellValue(bruto);
                row.createCell(16).setCellValue(faturado);
                row.createCell(17).setCellValue(boleto);
                row.createCell(18).setCellValue(imposto);
                row.createCell(19).setCellValue(lucro);
                row.createCell(20).setCellValue(order.getRejectionReason() != null ? order.getRejectionReason() : "");
            }

            // Nota: autoSizeColumn é desativado no modo Streaming (SXSSF) para manter a performance O(1).
            // sheet.autoSizeColumn(i); 

            workbook.write(out);
            workbook.dispose(); // Limpa arquivos temporários do disco
            return out.toByteArray();
        }
    }

    public byte[] generateInstalacaoExcel(ServiceOrder order) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Entrega Técnica");
            
            // Header Styles
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true); titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont(); boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);

            // Cabeçalho Oficial
            Row r0 = sheet.createRow(0);
            r0.createCell(0).setCellValue("ORDEM DE SERVIÇO: " + (order.getOsCode() != null ? order.getOsCode() : order.getId()));
            r0.getCell(0).setCellStyle(titleStyle);
            
            Row r1 = sheet.createRow(1);
            r1.createCell(0).setCellValue("Data: " + (order.getOpenedAt() != null ? order.getOpenedAt().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : ""));
            
            Row r2 = sheet.createRow(2);
            com.example.Models.Client c = order.getClient();
            r2.createCell(0).setCellValue("Cliente: " + c.getCompanyName());
            r2.createCell(3).setCellValue("CNPJ: " + c.getCnpj());
            r2.createCell(5).setCellValue("IE: " + (c.getIe() != null ? c.getIe() : ""));
            
            Row r3 = sheet.createRow(3);
            r3.createCell(0).setCellValue("Endereço: " + (c.getAddress() != null ? c.getAddress() : ""));
            r3.createCell(3).setCellValue("Contato: " + c.getContactName());
            r3.createCell(5).setCellValue("Email: " + (c.getEmail() != null ? c.getEmail() : ""));
            
            Row r4 = sheet.createRow(4);
            r4.createCell(0).setCellValue("Tipo: ENTREGA TECNICA - MAQUINA - CLIENTE");
            r4.getCell(0).setCellStyle(boldStyle);

            // Tabela 7 Colunas
            Row headerRow = sheet.createRow(6);
            String[] headers = {"Item", "Unid.", "Qtde.", "Código", "Descrição", "R$ Unitario", "R$ Total"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Aqui poderíamos injetar as peças de instalação se desejado (a definir), por ora deixamos 1 linha vazia
            Row dataRow = sheet.createRow(7);
            dataRow.createCell(0).setCellValue("Instalação");
            dataRow.createCell(1).setCellValue("MO");
            dataRow.createCell(2).setCellValue(1);
            dataRow.createCell(3).setCellValue("-");
            dataRow.createCell(4).setCellValue(order.getServiceDescription() != null ? order.getServiceDescription() : "Entrega Técnica Padrão");
            double val = order.getServiceValue() != null ? order.getServiceValue() : 0.0;
            dataRow.createCell(5).setCellValue(val);
            dataRow.createCell(6).setCellValue(val);

            for (int i = 0; i < headers.length; i++) { sheet.autoSizeColumn(i); }
            workbook.write(out);
            return out.toByteArray();
        }
    }

    public byte[] generateDespesasExcel(ServiceOrder order, List<com.example.Models.ServiceExpense> expenses) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Relatório Despesas");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true); titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            
            CellStyle boldStyle = workbook.createCellStyle();
            Font boldFont = workbook.createFont(); boldFont.setBold(true);
            boldStyle.setFont(boldFont);
            
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(boldFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Cabeçalho
            Row r0 = sheet.createRow(0);
            r0.createCell(0).setCellValue("RELATÓRIO DE DESPESAS");
            r0.getCell(0).setCellStyle(titleStyle);

            sheet.createRow(2).createCell(0).setCellValue("Cliente: " + order.getClient().getCompanyName());
            sheet.createRow(3).createCell(0).setCellValue("OS: " + (order.getOsCode() != null ? order.getOsCode() : order.getId()));
            sheet.createRow(4).createCell(0).setCellValue("Efetuado por: " + order.getTechnician().getNome());
            sheet.createRow(5).createCell(0).setCellValue("DATA: " + java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            sheet.createRow(6).createCell(0).setCellValue("Veículo: ________  Placa: ________  Cidade: ________");

            // Tabela Despesas
            Row headerRow = sheet.createRow(8);
            String[] headers = {"Descrição", "Qtde.", "Valor"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            String[] fixedRows = {"Refeição", "Hotel", "Passagem Aérea", "Taxi", "Pedágio", "Combustível", "Estacionamento", "Aluguel Carro", "Quilometragem", "Desp. com Material", "Outros"};
            
            int rowIndex = 9;
            double total = 0.0;
            
            for (String fr : fixedRows) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(fr);
                
                // Procurar se tem essa despesa no banco
                double val = 0.0;
                double qtd = 1.0;
                
                for (com.example.Models.ServiceExpense e : expenses) {
                    // Mapeamento simples
                    String typeStr = e.getExpenseType().name();
                    boolean match = false;
                    if (fr.equals("Refeição") && typeStr.equals("ALIMENTACAO")) match = true;
                    if (fr.equals("Hotel") && typeStr.equals("HOSPEDAGEM")) match = true;
                    if (fr.equals("Passagem Aérea") && typeStr.equals("PASSAGEM")) match = true;
                    if (fr.equals("Pedágio") && typeStr.equals("PEDAGIO")) match = true;
                    if (fr.equals("Quilometragem") && typeStr.equals("DESLOCAMENTO_KM")) match = true;
                    if (fr.equals("Desp. com Material") && typeStr.equals("MATERIAL")) match = true;
                    if (fr.equals("Outros") && (typeStr.equals("OUTROS") || typeStr.equals("OUTRO"))) match = true;
                    
                    if (match) {
                        val += e.getValue();
                        if (e.getQuantity() != null) {
                            // Se for a primeira vez ou se for um tipo que acumula quantidade (como dias ou km)
                            if (qtd == 1.0) qtd = e.getQuantity();
                            else qtd += e.getQuantity();
                        }
                    }
                }
                
                row.createCell(1).setCellValue(qtd);
                row.createCell(2).setCellValue(val);
                total += val;
            }

            Row totalRow = sheet.createRow(rowIndex + 1);
            totalRow.createCell(0).setCellValue("TOTAL Despesas");
            totalRow.getCell(0).setCellStyle(boldStyle);
            totalRow.createCell(2).setCellValue(total);
            totalRow.getCell(2).setCellStyle(boldStyle);

            Row creditarRow = sheet.createRow(rowIndex + 2);
            creditarRow.createCell(0).setCellValue("Valor à Creditar");
            creditarRow.getCell(0).setCellStyle(boldStyle);
            creditarRow.createCell(2).setCellValue(total); // Geralmente o mesmo do total
            
            Row infoRow = sheet.createRow(rowIndex + 4);
            infoRow.createCell(0).setCellValue("Outras Informações: ___________________________________________________________");
            
            Row signatureRow1 = sheet.createRow(rowIndex + 7);
            signatureRow1.createCell(0).setCellValue("Assinatura do Técnico: ___________________________  Data: ____/____/20___");
            
            Row signatureRow2 = sheet.createRow(rowIndex + 9);
            signatureRow2.createCell(0).setCellValue("Assinatura do Responsável: _______________________  Data: ____/____/20___");

            for (int i = 0; i < headers.length; i++) { sheet.autoSizeColumn(i); }
            workbook.write(out);
            return out.toByteArray();
        }
    }
}
