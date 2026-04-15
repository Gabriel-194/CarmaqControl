package com.example.Service;

import com.example.Models.*;
import com.example.Repository.ServiceExpenseRepository;
import com.example.Repository.ServicePartRepository;
import com.example.Repository.TimeTrackingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.Resource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Serviço responsável pela geração de relatórios em PDF e Excel.
 *
 * Documentos gerados:
 * 1. PDF  – Ordem de Serviço de Manutenção  (manutenção.pdf)
 * 2. XLSX – Ordem de Serviço de Instalação  (instalação.xlsx)
 * 3. XLSX – Relatório de Despesas           (despesas.xlsx)
 *
 * Os layouts replicam fielmente os modelos originais fornecidos pela CARMAQ SERVICE.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ServicePartRepository     servicePartRepository;
    private final ServiceExpenseRepository  serviceExpenseRepository;
    private final TimeTrackingRepository    timeTrackingRepository;
    private final ResourceLoader           resourceLoader;

    // ─── Constantes visuais ─────────────────────────────────────────────────────
    // Cores Apache POI (XSSF)
    private static final byte[] XLS_GREEN_DARK  = new byte[]{(byte)0x00, (byte)0xB0, (byte)0x50};
    private static final byte[] XLS_GREEN_LIGHT = new byte[]{(byte)0xBB, (byte)0xFB, (byte)0xBD};
    private static final DateTimeFormatter PTBR  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. PDF – MANUTENÇÃO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Gera o PDF de Ordem de Serviço de Manutenção conforme o layout
     * do modelo manutenção.pdf (OS em Garantia / Manutenção – CARMAQ SERVICE).
     */
    public byte[] generateMaintenanceXlsx(ServiceOrder order, String userRole) {
        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            XSSFSheet ws = wb.createSheet("Manutenção");

            // ── Inserir Logo ──────────────────────────────────────────────────
            addLogoToExcel(wb, ws);

            // ── Larguras das colunas (A-H) ─────────────────────────────────────
            double[] colWidths = {4.88, 6.13, 6.88, 11.0, 18.0, 34.38, 14.38, 23.13};
            for (int i = 0; i < colWidths.length; i++) {
                ws.setColumnWidth(i, (int)(colWidths[i] * 256));
            }

            // ── Estilos ────────────────────────────────────────────────────────
            CellStyle greenDarkBold   = xlsBannerStyle(wb, XLS_GREEN_DARK,  true,  16, HorizontalAlignment.CENTER);
            CellStyle greenDarkRight  = xlsBannerStyle(wb, XLS_GREEN_DARK,  true,  14, HorizontalAlignment.RIGHT);
            CellStyle headerStyle     = xlsBodyStyle(wb,   null,            false, 12, HorizontalAlignment.CENTER);
            CellStyle normalStyle     = xlsBodyStyle(wb,   null,            false, 12, HorizontalAlignment.LEFT);
            CellStyle normalCenter    = xlsBodyStyle(wb,   null,            false, 12, HorizontalAlignment.CENTER);
            CellStyle normalRight     = xlsBodyStyle(wb,   null,            false, 12, HorizontalAlignment.RIGHT);
            CellStyle totalGreenStyle = xlsBannerStyle(wb, XLS_GREEN_DARK,  true,  12, HorizontalAlignment.LEFT);

            CellStyle currencyStyle = xlsBodyStyle(wb, null, false, 11, HorizontalAlignment.CENTER);
            DataFormat fmt = wb.createDataFormat();
            currencyStyle.setDataFormat(fmt.getFormat("R$ #,##0.00"));

            CellStyle currencyGreenStyle = xlsBodyStyle(wb, XLS_GREEN_LIGHT, false, 11, HorizontalAlignment.CENTER);
            currencyGreenStyle.setDataFormat(fmt.getFormat("R$ #,##0.00"));

            // ── Linhas 2-6 – cabeçalho da empresa (centrado) ────
            String[] companyLines = {
                    "CARMAQ SERVICE",
                    "CNPJ: 60.526.327/0001-23",
                    "Av. Das Araucárias, 4255 | 83707-065 | Araucária | Paraná",
                    "Fone: 55 41 3346 1430     |      55 41 99663 1349",
                    "vendas@carmaq.ind.br     |     service@carmaq.ind.br"
            };
            float[] companyFontSizes = {14f, 11f, 11f, 11f, 11f};
            float[] companyHeights   = {20.25f, 20.25f, 20.25f, 21.75f, 21.75f};
            for (int i = 0; i < companyLines.length; i++) {
                Row r = ws.createRow(i + 1);
                r.setHeightInPoints(companyHeights[i]);
                org.apache.poi.ss.usermodel.Cell c = r.createCell(3);
                c.setCellValue(companyLines[i]);
                CellStyle s = xlsBodyStyle(wb, null, i == 0, companyFontSizes[i], HorizontalAlignment.CENTER);
                c.setCellStyle(s);
                ws.addMergedRegion(new CellRangeAddress(i + 1, i + 1, 3, 7));
            }

            // ── Linha 7 – ORDEM DE SERVIÇO DE MANUTENÇÃO + OS Num ───────────
            Row r7 = ws.createRow(6);
            r7.setHeightInPoints(27.75f);
            
            boolean isGarantia = "VALENTIM".equalsIgnoreCase(order.getManutencaoOrigin());
            String bannerTitle = isGarantia ? "ORDEM DE SERVIÇO EM GARANTIA" : "ORDEM DE SERVIÇO DE MANUTENÇÃO";
            xlsMergedCell(ws, wb, r7, 1, 6, 6, bannerTitle, greenDarkBold);
            
            String osNum = "OS" + LocalDate.now().getYear()
                    + String.format("%02d%02d", LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth())
                    + String.format("%02d", order.getId());
            xlsCell(r7, 7, osNum, greenDarkRight);

            // ── Linhas 8-11 – dados do cabeçalho (cliente ou Valentim) ────────
            // Para garantia (Valentim) o cabeçalho é sempre da Valentim; para Carmarq é do cliente
            String[][] clientData;
            if (isGarantia) {
                clientData = buildValentimClientData(order);
            } else {
                clientData = new String[][]{
                    {"Cliente:",    order.getClient().getCompanyName(),          "Data:",   order.getServiceDate() != null ? order.getServiceDate().format(PTBR) : ""},
                    {" Endereço:",  safeStr(order.getClient().getAddress()),      "Cidade:", extractCity(order.getClient().getAddress())},
                    {"    CNPJ:",   safeStr(order.getClient().getCnpj()),         "Estado:", extractState(order.getClient().getAddress())},
                    {"  Contato:",  safeStr(order.getClient().getContactName()), "Fone:",   safeStr(order.getClient().getPhone())},
                };
            }

            // IE e email a exibir dependem da origem
            String ieAExibir       = isGarantia ? "257.368.515" : order.getClient().getIe();
            String emailAExibir    = isGarantia ? "wagner@valentin.tec.br" : order.getClient().getEmail();
            float[] clientHeights  = {24f, 24f, 24f, 24f};
            int[] clientRows = {7, 8, 9, 10};
            for (int i = 0; i < clientData.length; i++) {
                Row rr = ws.createRow(clientRows[i]);
                rr.setHeightInPoints(clientHeights[i]);
                org.apache.poi.ss.usermodel.Cell lbl   = rr.createCell(0); lbl.setCellValue(clientData[i][0]); lbl.setCellStyle(normalCenter);
                org.apache.poi.ss.usermodel.Cell val   = rr.createCell(2); val.setCellValue(clientData[i][1]); val.setCellStyle(normalStyle);
                org.apache.poi.ss.usermodel.Cell lbl2  = rr.createCell(6); lbl2.setCellValue(clientData[i][2]); lbl2.setCellStyle(normalRight);
                org.apache.poi.ss.usermodel.Cell val2  = rr.createCell(7); val2.setCellValue(clientData[i][3]); val2.setCellStyle(normalCenter);
                if (i == 2 && ieAExibir != null) {
                    org.apache.poi.ss.usermodel.Cell ie = rr.createCell(5);
                    ie.setCellValue("IE:" + ieAExibir);
                    ie.setCellStyle(normalStyle);
                }
                ws.addMergedRegion(new CellRangeAddress(clientRows[i], clientRows[i], 0, 1));
                // Linha de contato (i==3): quebra a mescla 2-5 para exibir email separadamente
                if (i == 3 && emailAExibir != null) {
                    org.apache.poi.ss.usermodel.Cell email = rr.createCell(4);
                    email.setCellValue("Email: " + emailAExibir);
                    email.setCellStyle(normalStyle);
                    ws.addMergedRegion(new CellRangeAddress(clientRows[i], clientRows[i], 2, 3)); // nome
                    ws.addMergedRegion(new CellRangeAddress(clientRows[i], clientRows[i], 4, 5)); // email
                } else {
                    ws.addMergedRegion(new CellRangeAddress(clientRows[i], clientRows[i], 2, 5));
                }
            }
            
            // ── Linhas Extras - DADOS DA MÁQUINA
            Row rMachine = ws.createRow(11);
            rMachine.setHeightInPoints(24f);
            org.apache.poi.ss.usermodel.Cell mLbl = rMachine.createCell(0); mLbl.setCellValue("Máquina:"); mLbl.setCellStyle(normalCenter);
            String machineTypeEnum = order.getMachine().getMachineType() != null ? order.getMachine().getMachineType().name() : "";
            org.apache.poi.ss.usermodel.Cell mVal = rMachine.createCell(2); mVal.setCellValue(safeStr(order.getMachine().getModel()) + " (" + machineTypeEnum + ")"); mVal.setCellStyle(normalStyle);
            ws.addMergedRegion(new CellRangeAddress(11, 11, 0, 1));
            ws.addMergedRegion(new CellRangeAddress(11, 11, 2, 7));

            // ── RELATÓRIO DE SERVIÇOS E PEÇAS banner
            Row rBannerSrv = ws.createRow(12);
            rBannerSrv.setHeightInPoints(27.75f);
            xlsMergedCell(ws, wb, rBannerSrv, 0, 7, 7, "RELATÓRIO DE SERVIÇOS E PEÇAS", greenDarkBold);

            // ── Cabeçalho da tabela 
            Row r13 = ws.createRow(13);
            r13.setHeightInPoints(21.75f);
            String[] tableHeaders = {"Item", "Unid.", "Qtde.", "Código", "Descrição do Serviço / Peça", null, "R$ Unitario", "R$ Total"};
            for (int i = 0; i < tableHeaders.length; i++) {
                if (tableHeaders[i] != null) {
                    org.apache.poi.ss.usermodel.Cell hc = r13.createCell(i);
                    hc.setCellValue(tableHeaders[i]);
                    hc.setCellStyle(headerStyle);
                }
            }
            ws.addMergedRegion(new CellRangeAddress(13, 13, 4, 5));

            int currentRow = 14; 
                    
            double multiplier = "TECNICO".equals(userRole) ? 0.1 : 1.0;
            List<TimeTracking> times = timeTrackingRepository.findByServiceOrderId(order.getId());
            double totalWorkHours = calcHorasByType(times, "TRABALHO");
            double totalTravelHours = calcHorasByType(times, null);

            double valMaoObra = (order.getServiceValue() != null ? order.getServiceValue() : 0.0);
            double valTravel = (order.getTravelValue() != null ? order.getTravelValue() : 0.0);
            double valKm = (order.getDisplacementValue() != null ? order.getDisplacementValue() : 0.0);
            
            int itemIdx = 1;
            
            // Helper local modificado para escrever a linha inteira diretamente
            CellStyle rowBgNorm = xlsBodyStyle(wb, null, false, 11, HorizontalAlignment.CENTER);
            CellStyle rowBgAlt = xlsBodyStyle(wb, XLS_GREEN_LIGHT, false, 11, HorizontalAlignment.CENTER);
            CellStyle rowBgLNorm = xlsBodyStyle(wb, null, false, 11, HorizontalAlignment.LEFT);
            CellStyle rowBgLAlt = xlsBodyStyle(wb, XLS_GREEN_LIGHT, false, 11, HorizontalAlignment.LEFT);

            // Add TRV Row
            currentRow = writeItemRow(ws, wb, itemIdx++, currentRow, "H", totalTravelHours, "TRV", "HORAS DESLOCAMENTO TÉCNICO", 
                totalTravelHours > 0 ? (valTravel / totalTravelHours) : valTravel, valTravel,
                rowBgNorm, rowBgAlt, rowBgLNorm, rowBgLAlt, currencyStyle, currencyGreenStyle);

            // Add SRV Row
            currentRow = writeItemRow(ws, wb, itemIdx++, currentRow, "H", totalWorkHours, "SRV", "HORAS TRABALHADAS (MÃO DE OBRA)", 
                totalWorkHours > 0 ? (valMaoObra / totalWorkHours) : valMaoObra, valMaoObra,
                rowBgNorm, rowBgAlt, rowBgLNorm, rowBgLAlt, currencyStyle, currencyGreenStyle);

            // Add KM Row if applicable
            if (valKm > 0) {
                currentRow = writeItemRow(ws, wb, itemIdx++, currentRow, "KM", 1.0, "KM", "DESLOCAMENTO (KM)", valKm, valKm,
                    rowBgNorm, rowBgAlt, rowBgLNorm, rowBgLAlt, currencyStyle, currencyGreenStyle);
            }

            // Expeneses
            List<ServiceExpense> expenses = serviceExpenseRepository.findByServiceOrderId(order.getId());
            for (ServiceExpense exp : expenses) {
                currentRow = writeItemRow(ws, wb, itemIdx++, currentRow, "V", 1.0, "EXP", exp.getExpenseType().name() + ": " + safeStr(exp.getDescription()), exp.getValue(), exp.getValue(),
                    rowBgNorm, rowBgAlt, rowBgLNorm, rowBgLAlt, currencyStyle, currencyGreenStyle);
            }

            // Parts
            List<ServicePart> parts = servicePartRepository.findByServiceOrderId(order.getId());
            double partsTotal = 0.0;
            if (!parts.isEmpty()) {
                partsTotal = "TECNICO".equals(userRole) ? 0.0 : parts.stream().mapToDouble(p -> p.getUnitPrice() * p.getQuantity()).sum();
                currentRow = writeItemRow(ws, wb, itemIdx++, currentRow, "CJ", 1.0, "PART", "CONJUNTO DE PEÇAS: " + buildPartsDescription(parts), partsTotal, partsTotal,
                    rowBgNorm, rowBgAlt, rowBgLNorm, rowBgLAlt, currencyStyle, currencyGreenStyle);
            }
            
            // Espaçador
            ws.createRow(currentRow++).setHeightInPoints(16.5f);

            // TOTAIS
            double grossTotal = (order.getServiceValue() != null ? order.getServiceValue() : 0.0) +
                                (order.getTravelValue() != null ? order.getTravelValue() : 0.0) +
                                (order.getDisplacementValue() != null ? order.getDisplacementValue() : 0.0) +
                                (order.getPartsValue() != null ? order.getPartsValue() : 0.0) +
                                (expenses.stream().mapToDouble(ServiceExpense::getValue).sum());
            
            double discount = order.getDiscountValue() != null ? order.getDiscountValue() : 0.0;
            double totalBilled = grossTotal - discount;
            
            if ("TECNICO".equals(userRole)) {
                // Nova Regra: Repasse de 10% sobre a Base Líquida (Pós Impostos 12% e Taxa 3,50)
                double imposto = totalBilled * 0.12;
                double taxaBoleto = 3.50;
                double baseLiquida = Math.max(0, totalBilled - imposto - taxaBoleto);
                double totalComissao = baseLiquida * 0.10;
                double totalReembolso = (order.getDisplacementValue() != null ? order.getDisplacementValue() : 0.0) +
                                       (order.getPartsValue() != null ? order.getPartsValue() : 0.0) +
                                       expenses.stream().mapToDouble(ServiceExpense::getValue).sum();

                // Detalhamento de Cálculo (Transparência para o Técnico)
                Row ri = ws.createRow(currentRow++);
                xlsCell(ri, 0, "Base de Faturamento (Bruto - Desc)", normalStyle);
                ws.addMergedRegion(new CellRangeAddress(ri.getRowNum(), ri.getRowNum(), 0, 6));
                xlsNum(ri, 7, totalBilled, currencyStyle);

                Row rt = ws.createRow(currentRow++);
                xlsCell(rt, 0, "Dedução Impostos (12%) + Taxas (3,50)", normalStyle);
                ws.addMergedRegion(new CellRangeAddress(rt.getRowNum(), rt.getRowNum(), 0, 6));
                xlsNum(rt, 7, -(imposto + taxaBoleto), currencyStyle);

                // Comissão
                Row rc = ws.createRow(currentRow++);
                xlsCell(rc, 0, "Comissão Repasse (10% da Base Líquida)", normalStyle);
                ws.addMergedRegion(new CellRangeAddress(rc.getRowNum(), rc.getRowNum(), 0, 6));
                xlsNum(rc, 7, totalComissao, currencyStyle);

                // Reembolso 
                Row rr = ws.createRow(currentRow++);
                xlsCell(rr, 0, "Reembolso Integral (KM + Peças + Despesas)", normalStyle);
                ws.addMergedRegion(new CellRangeAddress(rr.getRowNum(), rr.getRowNum(), 0, 6));
                xlsNum(rr, 7, totalReembolso, currencyStyle);

                // Total a receber
                Row rFin = ws.createRow(currentRow++);
                xlsCell(rFin, 0, "TOTAL A RECEBER", totalGreenStyle);
                ws.addMergedRegion(new CellRangeAddress(rFin.getRowNum(), rFin.getRowNum(), 0, 6));
                xlsNum(rFin, 7, totalComissao + totalReembolso, totalGreenStyle);
            } else {
                Row rs = ws.createRow(currentRow++);
                xlsCell(rs, 0, "Subtotal", normalStyle);
                ws.addMergedRegion(new CellRangeAddress(rs.getRowNum(), rs.getRowNum(), 0, 6));
                xlsNum(rs, 7, grossTotal, currencyStyle);

                Row rd = ws.createRow(currentRow++);
                xlsCell(rd, 0, "Desconto", normalStyle);
                ws.addMergedRegion(new CellRangeAddress(rd.getRowNum(), rd.getRowNum(), 0, 6));
                xlsNum(rd, 7, discount, currencyStyle);

                Row rTotal = ws.createRow(currentRow++);
                xlsCell(rTotal, 0, "TOTAL GERAL", totalGreenStyle);
                ws.addMergedRegion(new CellRangeAddress(rTotal.getRowNum(), rTotal.getRowNum(), 0, 6));
                xlsNum(rTotal, 7, totalBilled, totalGreenStyle);
            }

            // Espaçador
            ws.createRow(currentRow++).setHeightInPoints(16.5f);

            // DADOS Bancários E Assinaturas
            String techName = order.getTechnician() != null ? order.getTechnician().getNome() : "N/A";
            String[][] footerRows = {
                    {"OBSERVAÇÕES: " + safeStr(order.getObservations())},
                    {null},
                    {"DADOS PARA PAGAMENTO:"},
                    {"CARMAQ SERVICE LTDA | CNPJ: 60.526.327/0001-23"},
                    {"ITAU AG: 4685 CC: 98576-6 | PIX: 60526327000123"},
                    {null},
                    {null},
                    {null},
                    {"________________________________________________"}
            };

            for (String[] fr : footerRows) {
                Row rr = ws.createRow(currentRow++);
                rr.setHeightInPoints(18.75f);
                if (fr[0] != null) {
                    xlsCell(rr, 0, fr[0], normalStyle);
                    ws.addMergedRegion(new CellRangeAddress(rr.getRowNum(), rr.getRowNum(), 0, 7));
                }
            }

            // Assinaturas (split at row before last)
            Row rSigName = ws.createRow(currentRow++);
            xlsCell(rSigName, 0, "Assinatura do Técnico", normalCenter);
            xlsCell(rSigName, 4, "Assinatura do Cliente (Carimbo/Nome)", normalCenter);
            ws.addMergedRegion(new CellRangeAddress(rSigName.getRowNum(), rSigName.getRowNum(), 0, 3));
            ws.addMergedRegion(new CellRangeAddress(rSigName.getRowNum(), rSigName.getRowNum(), 4, 7));
            
            Row rSigTech = ws.createRow(currentRow++);
            xlsCell(rSigTech, 0, techName, normalCenter);
            xlsCell(rSigTech, 4, "Data: ___/___/___", normalCenter);
            ws.addMergedRegion(new CellRangeAddress(rSigTech.getRowNum(), rSigTech.getRowNum(), 0, 3));
            ws.addMergedRegion(new CellRangeAddress(rSigTech.getRowNum(), rSigTech.getRowNum(), 4, 7));


            wb.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar XLSX de manutenção: " + e.getMessage(), e);
        }
    }

    private int writeItemRow(XSSFSheet ws, XSSFWorkbook wb, int itemIdx, int currentRow, 
                             String unit, double qty, String code, String desc, double vUnit, double vTotal,
                             CellStyle rowBgNorm, CellStyle rowBgAlt, CellStyle rowBgLNorm, CellStyle rowBgLAlt,
                             CellStyle curNorm, CellStyle curAlt) {
        Row rr = ws.createRow(currentRow);
        rr.setHeightInPoints(16.5f);
        boolean isAlt = (itemIdx % 2 == 0);
        CellStyle rowBg = isAlt ? rowBgAlt : rowBgNorm;
        CellStyle rowBgL = isAlt ? rowBgLAlt : rowBgLNorm;
        CellStyle curBg = isAlt ? curAlt : curNorm;

        xlsNum(rr, 0, itemIdx, rowBg);
        xlsCell(rr, 1, unit, rowBg);
        xlsNum(rr, 2, qty, rowBg);
        xlsCell(rr, 3, code, rowBg);
        xlsCell(rr, 4, desc, rowBgL);
        ws.addMergedRegion(new CellRangeAddress(currentRow, currentRow, 4, 5));
        
        org.apache.poi.ss.usermodel.Cell c6 = rr.createCell(6); c6.setCellValue(vUnit); c6.setCellStyle(curBg);
        org.apache.poi.ss.usermodel.Cell c7 = rr.createCell(7); c7.setCellValue(vTotal); c7.setCellStyle(curBg);
        return currentRow + 1;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 2. XLSX – INSTALAÇÃO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Gera o Excel de Ordem de Serviço de Instalação conforme o layout
     * do modelo instalação.xlsx (ENTREGA TECNICA – MAQUINA – CLIENTE).
     */
    public byte[] generateInstallationXlsx(ServiceOrder order, String userRole) {

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            XSSFSheet ws = wb.createSheet("Plan1");

            // ── Inserir Logo ──────────────────────────────────────────────────
            addLogoToExcel(wb, ws);

            // ── Larguras das colunas (A-H) ─────────────────────────────────────
            double[] colWidths = {4.88, 6.13, 6.88, 11.0, 18.0, 34.38, 14.38, 23.13};
            for (int i = 0; i < colWidths.length; i++) {
                ws.setColumnWidth(i, (int)(colWidths[i] * 256));
            }

            // ── Estilos ────────────────────────────────────────────────────────
            CellStyle greenDarkBold   = xlsBannerStyle(wb, XLS_GREEN_DARK,  true,  16, HorizontalAlignment.CENTER);
            CellStyle greenDarkRight  = xlsBannerStyle(wb, XLS_GREEN_DARK,  true,  14, HorizontalAlignment.RIGHT);
            CellStyle headerStyle     = xlsBodyStyle(wb,   null,            false, 12, HorizontalAlignment.CENTER);
            CellStyle normalStyle     = xlsBodyStyle(wb,   null,            false, 12, HorizontalAlignment.LEFT);
            CellStyle normalCenter    = xlsBodyStyle(wb,   null,            false, 12, HorizontalAlignment.CENTER);
            CellStyle normalRight     = xlsBodyStyle(wb,   null,            false, 12, HorizontalAlignment.RIGHT);
            CellStyle totalGreenStyle = xlsBannerStyle(wb, XLS_GREEN_DARK,  true,  12, HorizontalAlignment.LEFT);
            CellStyle altRowStyle     = xlsBodyStyle(wb,   XLS_GREEN_LIGHT, false, 11, HorizontalAlignment.CENTER);

            CellStyle currencyStyle = xlsBodyStyle(wb, null, false, 11, HorizontalAlignment.CENTER);
            DataFormat fmt = wb.createDataFormat();
            currencyStyle.setDataFormat(fmt.getFormat("R$ #,##0.00"));

            CellStyle currencyGreenStyle = xlsBodyStyle(wb, XLS_GREEN_LIGHT, false, 11, HorizontalAlignment.CENTER);
            currencyGreenStyle.setDataFormat(fmt.getFormat("R$ #,##0.00"));

            // ── Linha 1 – em branco ────────────────────────────────────────────
            // Lógica para desenhar imagem no Excel
            try {
                org.springframework.core.io.ClassPathResource imgFile = new org.springframework.core.io.ClassPathResource("static/logo-carmaq.png");
                byte[] imgBytes = imgFile.getInputStream().readAllBytes();
                int pictureIdx = wb.addPicture(imgBytes, Workbook.PICTURE_TYPE_PNG);
                
                CreationHelper helper = wb.getCreationHelper();
                Drawing<?> drawing = ws.createDrawingPatriarch();
                ClientAnchor anchor = helper.createClientAnchor();
                
                // Define a posição da imagem (Ex: Coluna 0, Linha 0)
                anchor.setCol1(0); 
                anchor.setRow1(0);
                anchor.setCol2(2); // Vai até a coluna 2
                anchor.setRow2(4); // Vai até a linha 4
                
                drawing.createPicture(anchor, pictureIdx);
            } catch (Exception e) {
                System.out.println("Erro ao carregar imagem para o Excel: " + e.getMessage());
            }

            // ── Linhas 2-6 – cabeçalho da empresa (centrado, mesclado A-H) ────
            String[] companyLines = {
                    "CARMAQ SERVICE",
                    "CNPJ: 60.526.327/0001-23",
                    "Av. Das Araucárias, 4255 | 83707-065 | Araucária | Paraná",
                    "Fone: 55 41 3346 1430     |      55 41 99663 1349",
                    "vendas@carmaq.ind.br     |     service@carmaq.ind.br"
            };
            float[] companyFontSizes = {14f, 11f, 11f, 11f, 11f};
            float[] companyHeights   = {20.25f, 20.25f, 20.25f, 21.75f, 21.75f};
            for (int i = 0; i < companyLines.length; i++) {
                Row r = ws.createRow(i + 1);
                r.setHeightInPoints(companyHeights[i]);
                org.apache.poi.ss.usermodel.Cell c = r.createCell(3); // Start from column D (index 3) to leave space for logo
                c.setCellValue(companyLines[i]);
                CellStyle s = xlsBodyStyle(wb, null, i == 0, companyFontSizes[i], HorizontalAlignment.CENTER);
                c.setCellStyle(s);
                ws.addMergedRegion(new CellRangeAddress(i + 1, i + 1, 3, 7));
            }

            // ── Linha 7 – ORDEM DE SERVIÇO + número ───────────────────────────
            Row r7 = ws.createRow(6);
            r7.setHeightInPoints(27.75f);
            xlsMergedCell(ws, wb, r7, 1, 6, 6, "ORDEM DE SERVIÇO", greenDarkBold);
            String osNum = "OS" + LocalDate.now().getYear()
                    + String.format("%02d%02d", LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth())
                    + String.format("%02d", order.getId());
            xlsCell(r7, 7, osNum, greenDarkRight);

            // ── Linhas 8-11 – dados do cabeçalho: Instalação sempre usa dados da Valentim ────
            // Instalação é sempre em nome da Valentim (origem do equipamento)
            String[][] clientData = buildValentimClientData(order);
            String ieAExibir = "257.368.515";
            String emailAExibir = "wagner@valentin.tec.br";
            float[] clientHeights = {24f, 24f, 24f, 24f};
            int[] clientRows = {7, 8, 9, 10};
            for (int i = 0; i < clientData.length; i++) {
                Row rr = ws.createRow(clientRows[i]);
                rr.setHeightInPoints(clientHeights[i]);
                org.apache.poi.ss.usermodel.Cell lbl   = rr.createCell(0); lbl.setCellValue(clientData[i][0]); lbl.setCellStyle(normalCenter);
                org.apache.poi.ss.usermodel.Cell val   = rr.createCell(2); val.setCellValue(clientData[i][1]); val.setCellStyle(normalStyle);
                org.apache.poi.ss.usermodel.Cell lbl2  = rr.createCell(6); lbl2.setCellValue(clientData[i][2]); lbl2.setCellStyle(normalRight);
                org.apache.poi.ss.usermodel.Cell val2  = rr.createCell(7); val2.setCellValue(clientData[i][3]); val2.setCellStyle(normalCenter);
                // Linha CNPJ tem IE extra
                if (i == 2 && ieAExibir != null) {
                    org.apache.poi.ss.usermodel.Cell ie = rr.createCell(5);
                    ie.setCellValue("IE:" + ieAExibir);
                    ie.setCellStyle(normalStyle);
                }
                ws.addMergedRegion(new CellRangeAddress(clientRows[i], clientRows[i], 0, 1));
                // Linha contato (i==3): quebra a mescla 2-5 para exibir email separadamente
                if (i == 3 && emailAExibir != null) {
                    org.apache.poi.ss.usermodel.Cell email = rr.createCell(4);
                    email.setCellValue("Email: " + emailAExibir);
                    email.setCellStyle(normalStyle);
                    ws.addMergedRegion(new CellRangeAddress(clientRows[i], clientRows[i], 2, 3)); // nome do contato
                    ws.addMergedRegion(new CellRangeAddress(clientRows[i], clientRows[i], 4, 5)); // email
                } else {
                    ws.addMergedRegion(new CellRangeAddress(clientRows[i], clientRows[i], 2, 5));
                }
            }

            // ── Linha 12 – banner ENTREGA TECNICA ────────────────────────────
            Row r12 = ws.createRow(11);
            r12.setHeightInPoints(27.75f);
            xlsMergedCell(ws, wb, r12, 0, 7, 7, "ENTREGA TECNICA - MAQUINA - CLIENTE", greenDarkBold);

            // ── Linha 13 – cabeçalho da tabela ────────────────────────────────
            Row r13 = ws.createRow(12);
            r13.setHeightInPoints(21.75f);
            String[] tableHeaders = {"Item", "Unid.", "Qtde.", "Código", "Descrição", null, "R$ Unitario", "R$ Total"};
            for (int i = 0; i < tableHeaders.length; i++) {
                if (tableHeaders[i] != null) {
                    org.apache.poi.ss.usermodel.Cell hc = r13.createCell(i);
                    hc.setCellValue(tableHeaders[i]);
                    hc.setCellStyle(headerStyle);
                }
            }
            ws.addMergedRegion(new CellRangeAddress(12, 12, 4, 5));

            // ── Linhas 14-19 – itens da OS ────────────────────────────────────
            double hourlyRate  = "VALENTIM".equalsIgnoreCase(order.getManutencaoOrigin()) ? 185.0 : 250.0;
            double kmRodados   = order.getDisplacementValue() != null ? order.getDisplacementValue() / 2.2 : 0.0; // Estimativa reversa do KM baseado no valor cobrado
            double kmRate      = "TECNICO".equalsIgnoreCase(userRole) ? 0.0 : 2.2; // Técnico não recebe KM como faturamento, apenas reembolso (que já está em Expenses)
            
            List<ServiceExpense> expenses = serviceExpenseRepository.findByServiceOrderId(order.getId());
            double refeicao = expenses.stream()
                    .filter(e -> "ALIMENTACAO".equals(e.getExpenseType().name()))
                    .mapToDouble(ServiceExpense::getValue).sum();
            
            // Calcular horas trabalhadas da tabela de tempo
            List<TimeTracking> times = timeTrackingRepository.findByServiceOrderId(order.getId());
            double horasWork   = calcHorasByType(times, "TRABALHO");
            double horasTravel = calcHorasByType(times, null);  // todos exceto TRABALHO

            boolean isInstalacao = "INSTALACAO".equalsIgnoreCase(order.getServiceType());
            double multiplier = "TECNICO".equalsIgnoreCase(userRole) ? 0.1 : 1.0;
            double travelRate = isInstalacao ? 0.0 : (85.0 * multiplier);
            double hourlyRateExec = isInstalacao ? 0.0 : (hourlyRate * multiplier);
            
            // Para instalação, o valor de repasse é fixo (geralmente o serviceValue já foi setado com o valor da máquina ou repasse)
            double fixedInstallValue = 0.0;
            if (isInstalacao) {
                double basePrice = (order.getMachine().getInstallationPrice() != null ? order.getMachine().getInstallationPrice() : order.getServiceValue());
                // Aplica 10% se for técnico, ou 100% se for admin/financeiro
                fixedInstallValue = basePrice * multiplier;
            }

            String techName = order.getTechnician() != null ? order.getTechnician().getNome() : "N/A";

            Object[][] itemRows = {
                    {1, "MO",   horasTravel,  "TRV",  "Hora Deslocamento",              travelRate,      null},
                    {2, "MO",   horasWork,    "SRV",  "Hora Trabalhada",                hourlyRateExec, null},
                    {3, "UN",   isInstalacao ? 1.0 : 0.0, "INST", "INSTALAÇÃO TÉCNICA: " + order.getMachine().getModel(), fixedInstallValue, null},
                    {4, "UN",   1.0,          "TEC", "TÉCNICO: " + techName,            0.0,       null},
                    {5, "KM",   kmRodados,    "KM",  "km ida e volta",                 kmRate,    null},
                    {6, "DESP", refeicao > 0 ? 1.0 : 0.0, "ALIM", "ALIMENTAÇÃO", refeicao > 0 ? refeicao : 0.0, null},
            };

            int[] itemRowNums = {13, 14, 15, 16, 17, 18};
            for (int i = 0; i < itemRows.length; i++) {
                Object[] ir  = itemRows[i];
                Row rr       = ws.createRow(itemRowNums[i]);
                rr.setHeightInPoints(16.5f);
                boolean isAlt = (i % 2 == 0);
                CellStyle rowBg = isAlt ? altRowStyle : xlsBodyStyle(wb, null, false, 11, HorizontalAlignment.CENTER);
                CellStyle rowBgL = isAlt ? xlsBodyStyle(wb, XLS_GREEN_LIGHT, false, 11, HorizontalAlignment.LEFT)
                                         : xlsBodyStyle(wb, null, false, 11, HorizontalAlignment.LEFT);

                xlsNum(rr, 0, ((Number) ir[0]).intValue(), rowBg);
                xlsCell(rr, 1, (String) ir[1], rowBg);
                xlsNum(rr, 2, ((Number) ir[2]).doubleValue(), rowBg);
                xlsCell(rr, 3, (String) ir[3], rowBg);
                if (ir[4] != null) xlsCell(rr, 4, (String) ir[4], rowBgL);
                xlsNum(rr, 6, ((Number) ir[5]).doubleValue(), rowBg);

                int excelRow = itemRowNums[i] + 1; // 1-indexed
                org.apache.poi.ss.usermodel.Cell totalCell = rr.createCell(7);
                totalCell.setCellFormula("IF(C" + excelRow + "<>0,(C" + excelRow + "*G" + excelRow + "),\"\")");
                CellStyle cs = isAlt ? currencyGreenStyle : currencyStyle;
                totalCell.setCellStyle(cs);
            }

            // ── Linha 20 – em branco ──────────────────────────────────────────
            ws.createRow(19).setHeightInPoints(16.5f);

            // ── Linha 21 – Total ──────────────────────────────────────────────
            Row r21 = ws.createRow(20);
            r21.setHeightInPoints(16.5f);
            xlsCell(r21, 0, "Total ", altRowStyle);
            org.apache.poi.ss.usermodel.Cell totalFormCell = r21.createCell(7);
            totalFormCell.setCellFormula("SUM(H14:H19)");
            CellStyle tcs = xlsBodyStyle(wb, XLS_GREEN_LIGHT, false, 12, HorizontalAlignment.LEFT);
            tcs.setDataFormat(fmt.getFormat("R$ #,##0.00"));
            totalFormCell.setCellStyle(tcs);

            // ── Linha 22 – Desconto ───────────────────────────────────────────
            Row r22 = ws.createRow(21);
            r22.setHeightInPoints(16.5f);
            xlsCell(r22, 0, "Desconto", normalStyle);
            double discountVal = order.getDiscountValue() != null ? order.getDiscountValue() : 0.0;
            xlsNum(r22, 7, discountVal, normalStyle);

            // ── Linha 23 – Total Geral ────────────────────────────────────────
            // Cálculo de Totais para Instalação
            double grossTotal = (order.getServiceValue() != null ? order.getServiceValue() : 0.0) +
                                (order.getTravelValue() != null ? order.getTravelValue() : 0.0) +
                                (order.getDisplacementValue() != null ? order.getDisplacementValue() : 0.0) +
                                (expenses.stream().mapToDouble(ServiceExpense::getValue).sum());
            
            double discount = order.getDiscountValue() != null ? order.getDiscountValue() : 0.0;
            double totalBilled = grossTotal - discount;

            Row r23 = ws.createRow(22);
            r23.setHeightInPoints(18.75f);
            
            int rowIdx = 23;
            if ("TECNICO".equals(userRole)) {
                // Nova Regra para Instalação: 10% da Base Líquida
                double imposto = totalBilled * 0.12;
                double taxaBoleto = 3.50;
                double baseLiquida = Math.max(0, totalBilled - imposto - taxaBoleto);
                double totalComissao = baseLiquida * 0.10;
                double totalReembolso = (order.getDisplacementValue() != null ? order.getDisplacementValue() : 0.0) +
                                       (order.getPartsValue() != null ? order.getPartsValue() : 0.0) +
                                       expenses.stream().mapToDouble(ServiceExpense::getValue).sum();

                xlsCell(r23, 0, "Repasse (10% Base Líquida) + Reembolsos", totalGreenStyle);
                ws.addMergedRegion(new CellRangeAddress(22, 22, 0, 6));
                xlsNum(r23, 7, totalComissao + totalReembolso, totalGreenStyle);
                
                // Adicionar linhas extras de detalhamento se sobrar espaço ou no rodapé
                Row rD1 = ws.createRow(rowIdx++);
                xlsCell(rD1, 0, "Base Líquida após Impostos/Taxas: R$ " + String.format("%.2f", baseLiquida), normalStyle);
                ws.addMergedRegion(new CellRangeAddress(23, 23, 0, 7));
            } else {
                xlsCell(r23, 0, "Total Geral do Pedido", totalGreenStyle);
                org.apache.poi.ss.usermodel.Cell tgCell = r23.createCell(7);
                tgCell.setCellValue(totalBilled);
                CellStyle tgCs = xlsBannerStyle(wb, XLS_GREEN_DARK, true, 12, HorizontalAlignment.RIGHT);
                tgCs.setDataFormat(fmt.getFormat("R$ #,##0.00"));
                tgCell.setCellStyle(tgCs);
            }

            // ── Linhas 24 em diante – condições e dados de pagamento ──────────
            String[][] footerRows = {
                    {"Prazo de entrega: Serviço efetuado"},
                    {"Efetuado por: " + techName},
                    {"Forma de pagamento: "},
                    {"Impostos: Incluso"},
                    {"Frete: FOB"},
                    {null}, // spacer
                    {"DADOS PARA PAGAMENTO"},
                    {"CARMAQ SERVICE LTDA"},
                    {"CNPJ: 60.526.327/0001-23"},
                    {"ITAU"},
                    {"AG: 4685"},
                    {"CC: 98576-6"},
                    {"PIX: 60526327000123"},
                    {"OBS: "},
            };
            boolean[] footerGreen = {false, false, true, false, false, false, false, false, false, false, false, false, false, false};
            // rowIdx já foi declarado acima e pode ter sido incrementado
            for (int i = 0; i < footerRows.length; i++) {
                Row rr = ws.createRow(rowIdx++);
                rr.setHeightInPoints(18.75f);
                if (footerRows[i][0] != null) {
                    CellStyle s = footerGreen[i]
                            ? xlsBannerStyle(wb, XLS_GREEN_DARK, false, 14, HorizontalAlignment.LEFT)
                            : xlsBodyStyle(wb, null, false, 14, HorizontalAlignment.LEFT);
                    xlsCell(rr, 0, footerRows[i][0], s);
                    ws.addMergedRegion(new CellRangeAddress(rr.getRowNum(), rr.getRowNum(), 0, 7));
                }
            }

            wb.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar XLSX de instalação: " + e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // 3. XLSX – DESPESAS
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Gera o Excel de Relatório de Despesas conforme o modelo despesas.xlsx.
     */
    public byte[] generateExpensesXlsx(ServiceOrder order) {

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            XSSFSheet ws = wb.createSheet("Modelo Relatório de despesas");

            // ── Inserir Logo ──────────────────────────────────────────────────
            addLogoToExcel(wb, ws);

            // Larguras das colunas (A=10.38, I=12.63 – restante padrão)
            ws.setColumnWidth(0, (int)(10.38 * 256));
            ws.setColumnWidth(8, (int)(12.63 * 256));
            for (int c = 1; c <= 7; c++) ws.setColumnWidth(c, (int)(8.0 * 256));

            // Estilos
            CellStyle greenDark  = xlsBannerStyle(wb, XLS_GREEN_DARK,  true,  12, HorizontalAlignment.CENTER);
            CellStyle boldLeft   = xlsBodyStyle(wb, null, true,  11, HorizontalAlignment.LEFT);
            CellStyle normalLeft = xlsBodyStyle(wb, null, false, 11, HorizontalAlignment.LEFT);
            CellStyle normalSm   = xlsBodyStyle(wb, null, false, 10, HorizontalAlignment.LEFT);
            CellStyle altRowStyle = xlsBodyStyle(wb, XLS_GREEN_LIGHT, false, 10, HorizontalAlignment.LEFT);
            CellStyle valueStyle = xlsBodyStyle(wb, null, false, 10, HorizontalAlignment.LEFT);
            DataFormat fmt = wb.createDataFormat();
            valueStyle.setDataFormat(fmt.getFormat("R$ #,##0.00"));
            CellStyle totalStyle = xlsBodyStyle(wb, null, false, 11, HorizontalAlignment.LEFT);
            totalStyle.setDataFormat(fmt.getFormat("R$ #,##0.00"));
            CellStyle headerGreen = xlsBodyStyle(wb, XLS_GREEN_LIGHT, true, 11, HorizontalAlignment.LEFT);

            // ── Inserir Logo ──────────────────────────────────────────────────
            addLogoToExcel(wb, ws);
            for (int i = 0; i < 5; i++) ws.createRow(i).setHeightInPoints(14f);

            // Linha 6 – RELATÓRIO DE DESPESAS (banner verde)
            Row r6 = ws.createRow(5);
            r6.setHeightInPoints(22f);
            xlsMergedCell(ws, wb, r6, 0, 8, 8, "RELATÓRIO DE DESPESAS", greenDark);

            // Linha 7 – Cliente + número da OS
            Row r7 = ws.createRow(6);
            r7.setHeightInPoints(18f);
            ws.addMergedRegion(new CellRangeAddress(6, 6, 0, 1));
            ws.addMergedRegion(new CellRangeAddress(6, 6, 2, 6));
            xlsCell(r7, 0, "Cliente: ", boldLeft);
            xlsCell(r7, 2, safeStr(order.getClient().getCompanyName()), normalLeft);
            String osNum = "OS" + LocalDate.now().getYear()
                    + String.format("%02d%02d", LocalDate.now().getMonthValue(), LocalDate.now().getDayOfMonth())
                    + String.format("%02d", order.getId());
            xlsCell(r7, 7, osNum, boldLeft);

            // Linha 8 – Efetuado por + DATA
            Row r8 = ws.createRow(7);
            r8.setHeightInPoints(18f);
            ws.addMergedRegion(new CellRangeAddress(7, 7, 0, 1));
            ws.addMergedRegion(new CellRangeAddress(7, 7, 2, 6));
            xlsCell(r8, 0, "Efetuado por:", normalLeft);
            String techName = order.getTechnician() != null ? order.getTechnician().getNome() : "";
            xlsCell(r8, 2, techName, normalLeft);
            xlsCell(r8, 7, "DATA: " + (order.getServiceDate() != null ? order.getServiceDate().format(PTBR) : ""), normalLeft);

            // Linha 9 – Veículo / Placa
            Row r9 = ws.createRow(8);
            r9.setHeightInPoints(16f);
            xlsCell(r9, 0, "Veículo:", normalSm);
            xlsCell(r9, 2, "Placa:", normalSm);

            // Linha 10 – Cidade
            Row r10 = ws.createRow(9);
            r10.setHeightInPoints(16f);
            xlsCell(r10, 0, "Cidade:", normalSm);
            xlsCell(r10, 2, extractCity(order.getClient().getAddress()), normalSm);

            // Linha 11 – Banner DESPESAS
            Row r11 = ws.createRow(10);
            r11.setHeightInPoints(20f);
            xlsMergedCell(ws, wb, r11, 0, 8, 8, "DESPESAS", greenDark);

            // Linha 12 – Cabeçalho da tabela
            Row r12 = ws.createRow(11);
            r12.setHeightInPoints(18f);
            ws.addMergedRegion(new CellRangeAddress(11, 11, 0, 4));
            ws.addMergedRegion(new CellRangeAddress(11, 11, 5, 6));
            ws.addMergedRegion(new CellRangeAddress(11, 11, 7, 8));
            xlsCell(r12, 0, "Descrição", headerGreen);
            xlsCell(r12, 5, "Qtde.", headerGreen);
            xlsCell(r12, 7, "Valor", headerGreen);

            // Busca despesas reais da OS
            List<ServiceExpense> expenses = serviceExpenseRepository.findByServiceOrderId(order.getId());

            // Categorias de despesas
            String[][] expenseCategories = {
                    {"Refeição",          "ALIMENTACAO"},
                    {"Hotel",             "HOSPEDAGEM"},
                    {"Passagem Aérea",    "OUTRO"},
                    {"Taxi",              "OUTRO"},
                    {"Pedágio",           "PEDAGIO"},
                    {"Combustível",       "OUTRO"},
                    {"Estacionamento",    "OUTRO"},
                    {"Aluguel Carro",     "OUTRO"},
                    {"Quilometragem",     "DESLOCAMENTO_KM"},
                    {"Desp.com Material", "OUTRO"},
                    {"Outros*",           "OUTRO"},
            };

            int expRowIdx = 12;
            double grandTotal = 0;

            for (String[] cat : expenseCategories) {
                double catValue = expenses.stream()
                        .filter(e -> e.getExpenseType() != null && cat[1].equals(e.getExpenseType().name()))
                        .filter(e -> matchDescription(e, cat[0]))
                        .mapToDouble(ServiceExpense::getValue).sum();

                Row rr = ws.createRow(expRowIdx++);
                rr.setHeightInPoints(17f);
                ws.addMergedRegion(new CellRangeAddress(rr.getRowNum(), rr.getRowNum(), 0, 4));
                ws.addMergedRegion(new CellRangeAddress(rr.getRowNum(), rr.getRowNum(), 5, 6));
                ws.addMergedRegion(new CellRangeAddress(rr.getRowNum(), rr.getRowNum(), 7, 8));
                
                boolean isAlt = (expRowIdx % 2 == 0);
                CellStyle s = isAlt ? altRowStyle : normalLeft;
                xlsCell(rr, 0, cat[0], s);
                
                org.apache.poi.ss.usermodel.Cell vc = rr.createCell(7);
                vc.setCellValue(catValue);
                vc.setCellStyle(isAlt ? altRowStyle : valueStyle);
                grandTotal += catValue;
            }

            // Linha TOTAL Despesas
            Row rTotal = ws.createRow(expRowIdx++);
            rTotal.setHeightInPoints(18f);
            ws.addMergedRegion(new CellRangeAddress(rTotal.getRowNum(), rTotal.getRowNum(), 0, 4));
            ws.addMergedRegion(new CellRangeAddress(rTotal.getRowNum(), rTotal.getRowNum(), 5, 6));
            ws.addMergedRegion(new CellRangeAddress(rTotal.getRowNum(), rTotal.getRowNum(), 7, 8));
            CellStyle totalLabelStyle = xlsBodyStyle(wb, null, true, 11, HorizontalAlignment.LEFT);
            xlsCell(rTotal, 0, "TOTAL Despesas ", totalLabelStyle);
            org.apache.poi.ss.usermodel.Cell totalCell = rTotal.createCell(7);
            totalCell.setCellValue(grandTotal);
            totalCell.setCellStyle(totalStyle);

            // Linha Valor à Creditar
            Row rCredit = ws.createRow(expRowIdx++);
            rCredit.setHeightInPoints(18f);
            ws.addMergedRegion(new CellRangeAddress(rCredit.getRowNum(), rCredit.getRowNum(), 0, 8));
            xlsCell(rCredit, 0, "Valor à Creditar", normalLeft);

            // Linha Outras Informações
            Row rInfo = ws.createRow(expRowIdx++);
            rInfo.setHeightInPoints(18f);
            ws.addMergedRegion(new CellRangeAddress(rInfo.getRowNum(), rInfo.getRowNum(), 0, 8));
            xlsCell(rInfo, 0, "Outras Informações", normalLeft);

            // Espaço
            ws.createRow(expRowIdx++).setHeightInPoints(14f);

            // Assinaturas
            Row rSig1 = ws.createRow(expRowIdx++);
            rSig1.setHeightInPoints(18f);
            ws.addMergedRegion(new CellRangeAddress(rSig1.getRowNum(), rSig1.getRowNum(), 0, 3));
            ws.addMergedRegion(new CellRangeAddress(rSig1.getRowNum(), rSig1.getRowNum(), 5, 8));
            xlsCell(rSig1, 0, "Assinatura técnico", normalLeft);
            xlsCell(rSig1, 5, "Assinatura do responsável", normalLeft);

            Row rSig2 = ws.createRow(expRowIdx);
            rSig2.setHeightInPoints(18f);
            ws.addMergedRegion(new CellRangeAddress(rSig2.getRowNum(), rSig2.getRowNum(), 0, 3));
            ws.addMergedRegion(new CellRangeAddress(rSig2.getRowNum(), rSig2.getRowNum(), 5, 8));
            xlsCell(rSig2, 0, "Data", normalLeft);
            xlsCell(rSig2, 5, "Data", normalLeft);

            wb.write(baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar XLSX de despesas: " + e.getMessage(), e);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helpers – XLSX (Apache POI)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Insere o logotipo oficial da Carmarq no Excel no canto superior esquerdo */
    private void addLogoToExcel(XSSFWorkbook wb, XSSFSheet sheet) {
        try {
            Resource resource = resourceLoader.getResource("classpath:static/logo-carmaq.png");
            if (!resource.exists()) {
                System.err.println("addLogoToExcel: Arquivo logo-carmaq.png não encontrado em static/");
                return;
            }
            byte[] logoBytes = resource.getInputStream().readAllBytes();
            int pictureIdx = wb.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG);
            
            CreationHelper helper = wb.getCreationHelper();
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();
            
            // Posição: Canto Superior Esquerdo (A1 até C5 aproximadamente)
            anchor.setCol1(0);
            anchor.setRow1(0); 
            anchor.setCol2(3);
            anchor.setRow2(5);
            
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize(0.8); // Reduz levemente para não encostar nas bordas
        } catch (Exception e) {
            System.err.println("Erro ao inserir logo no Excel: " + e.getMessage());
        }
    }

    private CellStyle xlsBannerStyle(XSSFWorkbook wb, byte[] bgRgb, boolean bold,
                                      double fontSize, HorizontalAlignment halign) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f  = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) fontSize);
        if (bgRgb != null) f.setColor(new XSSFColor(new byte[]{(byte)0xFF,(byte)0xFF,(byte)0xFF}, null));
        s.setFont(f);
        if (bgRgb != null) {
            XSSFCellStyle xs = (XSSFCellStyle) s;
            xs.setFillForegroundColor(new XSSFColor(bgRgb, null));
            xs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        s.setAlignment(halign);
        s.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        return s;
    }

    private CellStyle xlsBodyStyle(XSSFWorkbook wb, byte[] bgRgb, boolean bold,
                                    double fontSize, HorizontalAlignment halign) {
        CellStyle s = wb.createCellStyle();
        XSSFFont f  = wb.createFont();
        f.setBold(bold);
        f.setFontHeightInPoints((short) fontSize);
        s.setFont(f);
        if (bgRgb != null) {
            XSSFCellStyle xs = (XSSFCellStyle) s;
            xs.setFillForegroundColor(new XSSFColor(bgRgb, null));
            xs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        s.setAlignment(halign);
        s.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
        return s;
    }

    private void xlsMergedCell(XSSFSheet ws, XSSFWorkbook wb, Row row,
                                int startCol, int endCol, int cellCol,
                                String value, CellStyle style) {
        ws.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), startCol, endCol));
        org.apache.poi.ss.usermodel.Cell c = row.createCell(cellCol);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void xlsCell(Row row, int col, String value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    private void xlsNum(Row row, int col, double value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell c = row.createCell(col);
        c.setCellValue(value);
        c.setCellStyle(style);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Utilitários
    // ═══════════════════════════════════════════════════════════════════════════

    private String safeStr(String s) { return s != null ? s : ""; }

    /** Extrai cidade do endereço no padrão "Logradouro, Bairro, Cidade - UF" */
    private String extractCity(String address) {
        if (address == null) return "";
        String[] parts = address.split(",");
        if (parts.length >= 3) {
            String last = parts[parts.length - 1].trim();
            if (last.contains("-")) return last.split("-")[0].trim();
        }
        return "";
    }

    /** Extrai estado (UF) do endereço */
    private String extractState(String address) {
        if (address == null) return "";
        int dash = address.lastIndexOf('-');
        if (dash >= 0 && dash + 1 < address.length()) {
            return address.substring(dash + 1).trim().toUpperCase();
        }
        return "";
    }

    /** Calcula horas de um tipo de atividade nos registros de tempo */
    private double calcHorasByType(List<TimeTracking> times, String type) {
        return times.stream()
                .filter(t -> type == null
                        ? !"TRABALHO".equals(t.getType())
                        : type.equals(t.getType()))
                .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
                .mapToLong(t -> java.time.Duration.between(t.getStartTime(), t.getEndTime()).toMinutes())
                .sum() / 60.0;
    }

    /** Monta descrição resumida das peças */
    private String buildPartsDescription(List<ServicePart> parts) {
        if (parts == null || parts.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (ServicePart p : parts) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(p.getPartName()).append(" (").append(p.getQuantity()).append("x)");
        }
        return sb.toString();
    }

    /** Verifica se uma despesa corresponde à categoria pela descrição ou tipo */
    private boolean matchDescription(ServiceExpense e, String category) {
        // Para OUTRO, aceita qualquer (já que não há sub-tipo)
        return true;
    }

    /**
     * Retorna os dados fixos da Valentim Rep. e Comércio Ltda para uso no cabeçalho do Excel.
     * Utilizado em: Instalação (sempre) e Manutenção em Garantia (VALENTIM).
     * Formato: { label, valor, label2, valor2 }
     */
    private String[][] buildValentimClientData(ServiceOrder order) {
        return new String[][] {
            {"Cliente:",    "VALENTIN REP.E COMERCIO LTDA",        "Data:",   order.getServiceDate() != null ? order.getServiceDate().format(PTBR) : ""},
            {" Endereço:",  "Rua José Carlos Librelato,99,Vila São José", "Cidade:", "Içara"},
            {"    CNPJ:",   "20.356.154/0001-28",                  "Estado:", "SC"},
            {"  Contato:",  "Sr. Wagner",                          "Fone:",   "48 99187 4156"},
        };
    }
}