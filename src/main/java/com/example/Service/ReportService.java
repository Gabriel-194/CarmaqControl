package com.example.Service;

import com.example.Models.*;
import com.example.Repository.ServiceExpenseRepository;
import com.example.Repository.ServicePartRepository;
import com.example.Repository.TimeTrackingRepository;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.element.Cell;   
import com.itextpdf.layout.element.Table;  
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
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
    private final ServiceOrderService       serviceOrderService;
    private final ResourceLoader           resourceLoader;

    // ─── Constantes visuais ─────────────────────────────────────────────────────
    private static final DeviceRgb GREEN_DARK   = new DeviceRgb(0,  176,  80);   // #00B050 – fundo banner
    private static final DeviceRgb GREEN_LIGHT  = new DeviceRgb(187, 251, 189);  // #BBFBBD – linhas alt.
    private static final DateTimeFormatter PTBR  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Cores Apache POI (XSSF)
    private static final byte[] XLS_GREEN_DARK  = new byte[]{(byte)0x00, (byte)0xB0, (byte)0x50};
    private static final byte[] XLS_GREEN_LIGHT = new byte[]{(byte)0xBB, (byte)0xFB, (byte)0xBD};

    // ═══════════════════════════════════════════════════════════════════════════
    // 1. PDF – MANUTENÇÃO
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Gera o PDF de Ordem de Serviço de Manutenção conforme o layout
     * do modelo manutenção.pdf (OS em Garantia / Manutenção – CARMAQ SERVICE).
     */
    public byte[] generateMaintenancePdf(ServiceOrder order, String userRole) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (PdfWriter writer = new PdfWriter(baos);
             PdfDocument pdf   = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A4)) {

            document.setMargins(28, 28, 28, 28);

            PdfFont bold   = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
            PdfFont normal = PdfFontFactory.createFont(StandardFonts.HELVETICA);

            // ── CABEÇALHO ──────────────────────────────────────────────────────
            Table header = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .useAllAvailableWidth();

            // Coluna esquerda – logo (texto alternativo se sem imagem)
            Cell logoCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setVerticalAlignment(VerticalAlignment.MIDDLE);
            try {
                Resource resource = resourceLoader.getResource("classpath:/static/logo-carmaq.png");
                if (resource.exists()) {
                    byte[] logoBytes = resource.getInputStream().readAllBytes();
                    Image logo = new Image(ImageDataFactory.create(logoBytes)).setWidth(120);
                    logoCell.add(logo);
                } else {
                    throw new RuntimeException("Logo não encontrado via ResourceLoader");
                }
            } catch (Exception e) {
                System.err.println("Erro ao carregar logo no PDF: " + e.getMessage());
                // fallback textual
                logoCell.add(new Paragraph("CARMAQ\nMÁQUINAS INDUSTRIAIS")
                        .setFont(bold).setFontSize(12)
                        .setFontColor(GREEN_DARK));
            }

            // Coluna direita – dados da empresa
            Cell infoCell = new Cell()
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER);
            infoCell.add(new Paragraph("CARMAQ SERVICE").setFont(bold).setFontSize(11));
            infoCell.add(new Paragraph("CNPJ: 60.526.327/0001-23").setFont(normal).setFontSize(9));
            infoCell.add(new Paragraph("Av. Das Araucárias, 4255 | 83707-065 | Araucária | Paraná").setFont(normal).setFontSize(9));
            infoCell.add(new Paragraph("Fone: 55 41 3346 1430   |   55 41 99663 1349").setFont(normal).setFontSize(9));
            infoCell.add(new Paragraph("vendas@carmaq.ind.br   |   service@carmaq.ind.br").setFont(normal).setFontSize(9));

            header.addCell(logoCell);
            header.addCell(infoCell);
            document.add(header);
            document.add(new Paragraph(" ").setFontSize(4));

            // ── BANNER: TIPO DA OS ─────────────────────────────────────────────
            String osNumber = "OS" + LocalDate.now().getYear()
                    + String.format("%02d", LocalDate.now().getMonthValue())
                    + String.format("%02d", LocalDate.now().getDayOfMonth())
                    + String.format("%02d", order.getId());

            boolean isGarantia = "VALENTIM".equalsIgnoreCase(order.getManutencaoOrigin());
            String bannerTitle = isGarantia
                    ? "ORDEM DE SERVIÇO EM GARANTIA"
                    : "ORDEM DE SERVIÇO DE MANUTENÇÃO";

            Table bannerTable = new Table(UnitValue.createPercentArray(new float[]{70, 30}))
                    .useAllAvailableWidth();
            Cell bannerLeft = new Cell()
                    .setBackgroundColor(GREEN_DARK)
                    .setBorder(Border.NO_BORDER)
                    .setPadding(5);
            bannerLeft.add(new Paragraph(bannerTitle).setFont(bold).setFontSize(13)
                    .setFontColor(ColorConstants.WHITE));
            Cell bannerRight = new Cell()
                    .setBackgroundColor(GREEN_DARK)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(5);
            bannerRight.add(new Paragraph(osNumber).setFont(bold).setFontSize(12)
                    .setFontColor(ColorConstants.WHITE));
            bannerTable.addCell(bannerLeft);
            bannerTable.addCell(bannerRight);
            document.add(bannerTable);

            // ── DADOS DO CLIENTE ───────────────────────────────────────────────
            Table clientTable = new Table(UnitValue.createPercentArray(new float[]{15, 45, 15, 25}))
                    .useAllAvailableWidth();

            addClientRow(clientTable, bold, normal, "Cliente:",
                    order.getClient().getCompanyName(), "Data:",
                    order.getServiceDate() != null ? order.getServiceDate().format(PTBR) : "");
            addClientRow(clientTable, bold, normal, "Endereço:",
                    safeStr(order.getClient().getAddress()), "Cidade:",
                    extractCity(order.getClient().getAddress()));
            addClientRow(clientTable, bold, normal, "CNPJ:",
                    safeStr(order.getClient().getCnpj()), "Estado:",
                    extractState(order.getClient().getAddress()));
            addClientRow(clientTable, bold, normal, "Contato:",
                    safeStr(order.getClient().getContactName()), "Fone:",
                    safeStr(order.getClient().getPhone()));
            document.add(clientTable);

            // ── BANNER: MANUTENÇÃO – MÁQUINA – CLIENTE ────────────────────────
            String sectionTitle = "MANUTENÇÃO - MÁQUINA - CLIENTE";
            Table sectionBanner = new Table(new float[]{1}).useAllAvailableWidth();
            Cell sectionCell = new Cell()
                    .setBackgroundColor(GREEN_DARK)
                    .setBorder(Border.NO_BORDER)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setPadding(5);
            sectionCell.add(new Paragraph(sectionTitle).setFont(bold).setFontSize(13)
                    .setFontColor(ColorConstants.WHITE));
            sectionBanner.addCell(sectionCell);
            document.add(sectionBanner);

            // ── TABELA DE ITENS ────────────────────────────────────────────────
            float[] colWidths = {5f, 7f, 7f, 9f, 38f, 17f, 17f};
            Table itemsTable = new Table(UnitValue.createPercentArray(colWidths))
                    .useAllAvailableWidth();

            // Cabeçalho
            String[] headers = {"Item", "Unid.", "Qtde.", "Código", "Descrição", "R$ Unitario", "R$ Total"};
            for (String h : headers) {
                Cell hc = new Cell().setBackgroundColor(GREEN_LIGHT)
                        .setBorder(new SolidBorder(0.5f))
                        .setTextAlignment(TextAlignment.CENTER)
                        .setPadding(3);
                hc.add(new Paragraph(h).setFont(bold).setFontSize(9));
                itemsTable.addHeaderCell(hc);
            }

            // Dados de tempo (horas trabalhadas)
            List<TimeTracking> times = timeTrackingRepository.findByServiceOrderId(order.getId());
            double totalWorkMinutes = times.stream()
                    .filter(t -> "TRABALHO".equals(t.getType()) && t.getStartTime() != null && t.getEndTime() != null)
                    .mapToLong(t -> java.time.Duration.between(t.getStartTime(), t.getEndTime()).toMinutes())
                    .sum();
            double totalTravelMinutes = times.stream()
                    .filter(t -> !"TRABALHO".equals(t.getType()) && t.getStartTime() != null && t.getEndTime() != null)
                    .mapToLong(t -> java.time.Duration.between(t.getStartTime(), t.getEndTime()).toMinutes())
                    .sum();

            double multiplier = "TECNICO".equals(userRole) ? 0.1 : 1.0;
            double hourlyRate = ("VALENTIM".equalsIgnoreCase(order.getManutencaoOrigin()) ? 185.0 : 250.0) * multiplier;
            double travelRate  = 85.0 * multiplier;
            double kmRate      = 2.20;

            double horasTrabalho     = totalWorkMinutes / 60.0;
            double horasDeslocamento = totalTravelMinutes / 60.0;
            double kmRodados         =  0.0;

            // Despesas
            List<ServiceExpense> expenses = serviceExpenseRepository.findByServiceOrderId(order.getId());
            double totalRefeicao = expenses.stream()
                    .filter(e -> e.getExpenseType() != null &&
                            e.getExpenseType().name().equals("ALIMENTACAO"))
                    .mapToDouble(ServiceExpense::getValue).sum();
            double totalPedagio = expenses.stream()
                    .filter(e -> e.getExpenseType() != null &&
                            e.getExpenseType().name().equals("PEDAGIO"))
                    .mapToDouble(ServiceExpense::getValue).sum();

            // Peças - Técnicos não devem ver o valor total
            List<ServicePart> parts = servicePartRepository.findByServiceOrderId(order.getId());
            double totalPecas = "TECNICO".equals(userRole) ? 0.0 : parts.stream().mapToDouble(p -> p.getUnitPrice() * p.getQuantity()).sum();

            // Linhas fixas do template
            Object[][] rows = {
                {1, "MO",   horasDeslocamento, "XXX", "Hora Deslocamento",                         travelRate,  horasDeslocamento * travelRate},
                {2, "MO",   horasTrabalho,     "XXX", "Hora Trabalhada",                           hourlyRate,  horasTrabalho * hourlyRate},
                {3, "KM",   kmRodados,         "XXX", "Quilometro rodado ida e volta",             kmRate,      kmRodados * kmRate},
                {4, "DESP", 1.0,               "XXXX","REFEIÇÃO 1 DIAS + 12,5 % imposto NF",       totalRefeicao, totalRefeicao > 0 ? totalRefeicao : null},
                {5, "DESP", totalPedagio > 0 ? 1.0 : 0.0, "XXX", "Pedágios",                      totalPedagio,  totalPedagio > 0 ? totalPedagio : null},
                {6, "PÇS",  totalPecas > 0 ? 1.0 : 0.0,  "XXX", "Peças Descrição: " + buildPartsDescription(parts), totalPecas, totalPecas > 0 ? totalPecas : null},
            };

            boolean alt = true;
            double runningTotal = 0;
            for (Object[] row : rows) {
                DeviceRgb rowBg = alt ? GREEN_LIGHT : null;
                alt = !alt;

                double qty  = row[2] instanceof Number ? ((Number) row[2]).doubleValue() : 0;
                double unit = row[5] instanceof Number ? ((Number) row[5]).doubleValue() : 0;
                Double total = (Double) row[6];

                addItemRow(itemsTable, bold, normal, rowBg,
                        String.valueOf(row[0]),
                        String.valueOf(row[1]),
                        qty == 0 ? "0" : String.format("%.2f", qty),
                        String.valueOf(row[3]),
                        String.valueOf(row[4]),
                        unit == 0 ? "-" : String.format("%.2f", unit),
                        total == null || total == 0 ? "-" : String.format("R$ %.2f", total));

                if (total != null) runningTotal += total;
            }

            document.add(itemsTable);

            // ── TOTAIS ─────────────────────────────────────────────────────────
            Table totalsTable = new Table(UnitValue.createPercentArray(new float[]{83, 17}))
                    .useAllAvailableWidth();

            addTotalRow(totalsTable, bold, normal, GREEN_LIGHT, "Total", runningTotal > 0 ? String.format("R$ %.2f", runningTotal) : "-");
            addTotalRow(totalsTable, bold, normal, null,        "Desconto", "-");

            // Total Geral – fundo verde escuro
            Cell tgLabel = new Cell().setBackgroundColor(GREEN_DARK).setBorder(new SolidBorder(0.5f))
                    .setPadding(4);
            tgLabel.add(new Paragraph("Total Geral do Pedido").setFont(bold).setFontSize(10)
                    .setFontColor(ColorConstants.WHITE));
            Cell tgValue = new Cell().setBackgroundColor(GREEN_DARK).setBorder(new SolidBorder(0.5f))
                    .setTextAlignment(TextAlignment.RIGHT).setPadding(4);
            tgValue.add(new Paragraph(runningTotal > 0 ? String.format("R$ %.2f", runningTotal) : "R$ 0,00")
                    .setFont(bold).setFontSize(10).setFontColor(ColorConstants.WHITE));
            totalsTable.addCell(tgLabel);
            totalsTable.addCell(tgValue);
            document.add(totalsTable);

            // ── RODAPÉ DE CONDIÇÕES ────────────────────────────────────────────
            document.add(new Paragraph(" ").setFontSize(6));
            document.add(new Paragraph("Prazo de entrega: Serviço efetuado").setFont(normal).setFontSize(10));
            document.add(new Paragraph("Efetuado por: " + safeStr(order.getTechnician().getNome())).setFont(normal).setFontSize(10));

            // Forma de pagamento – fundo verde
            Table pagTable = new Table(new float[]{1}).useAllAvailableWidth();
            Cell pagCell = new Cell().setBackgroundColor(GREEN_DARK).setBorder(Border.NO_BORDER).setPadding(4);
            pagCell.add(new Paragraph("Forma de pagamento:").setFont(bold).setFontSize(10)
                    .setFontColor(ColorConstants.WHITE));
            pagTable.addCell(pagCell);
            document.add(pagTable);

            document.add(new Paragraph("Impostos: Incluso").setFont(bold).setFontSize(10));

            // ── DADOS PARA PAGAMENTO ───────────────────────────────────────────
            document.add(new Paragraph(" ").setFontSize(8));
            document.add(new Paragraph("DADOS PARA PAGAMENTO").setFont(bold).setFontSize(10));
            String[] bankData = {
                    "CARMAQ SERVICE LTDA",
                    "CNPJ: 60.526.327/0001-23",
                    "ITAU",
                    "AG: 4685",
                    "CC: 98576-6",
                    "PIX: 60526327000123"
            };
            for (String line : bankData) {
                document.add(new Paragraph(line).setFont(normal).setFontSize(9));
            }

            document.add(new Paragraph(" ").setFontSize(4));
            document.add(new Paragraph("OBS:  " + safeStr(order.getObservations())).setFont(normal).setFontSize(9));

            // Rodapé – logo Valentin (cliente) se for garantia
            if (isGarantia) {
                document.add(new Paragraph(" ").setFontSize(10));
                try {
                    byte[] vLogo = getClass().getResourceAsStream("/static/logo-valentin.png").readAllBytes();
                    Image vImg = new Image(ImageDataFactory.create(vLogo)).setWidth(140)
                            .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
                    document.add(vImg);
                } catch (Exception ignored) {
                    document.add(new Paragraph("VALENTIN – SOLUÇÕES INDUSTRIAIS")
                            .setFont(bold).setFontSize(11).setFontColor(GREEN_DARK)
                            .setTextAlignment(TextAlignment.CENTER));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de manutenção: " + e.getMessage(), e);
        }

        return baos.toByteArray();
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
            ws.createRow(0).setHeightInPoints(13.5f);

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

            // ── Linhas 8-11 – dados do cliente ────────────────────────────────
            String[][] clientData = {
                    {"Cliente:",    order.getClient().getCompanyName(),     "Data:",   order.getServiceDate() != null ? order.getServiceDate().format(PTBR) : ""},
                    {" Endereço:",  safeStr(order.getClient().getAddress()), "Cidade:", extractCity(order.getClient().getAddress())},
                    {"    CNPJ:",   safeStr(order.getClient().getCnpj()),    "Estado:", extractState(order.getClient().getAddress())},
                    {"  Contato:",  safeStr(order.getClient().getContactName()), "Fone:", safeStr(order.getClient().getPhone())},
            };
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
                if (i == 2 && order.getClient().getIe() != null) {
                    org.apache.poi.ss.usermodel.Cell ie = rr.createCell(5);
                    ie.setCellValue("IE:" + order.getClient().getIe());
                    ie.setCellStyle(normalStyle);
                }
                // Linha contato tem email
                if (i == 3 && order.getClient().getEmail() != null) {
                    org.apache.poi.ss.usermodel.Cell email = rr.createCell(4);
                    email.setCellValue("Email: " + order.getClient().getEmail());
                    email.setCellStyle(normalStyle);
                }
                ws.addMergedRegion(new CellRangeAddress(clientRows[i], clientRows[i], 0, 1));
                ws.addMergedRegion(new CellRangeAddress(clientRows[i], clientRows[i], 2, 5));
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
            double kmRodados   = 0.0;
            List<ServiceExpense> expenses = serviceExpenseRepository.findByServiceOrderId(order.getId());
            double refeicao = expenses.stream()
                    .filter(e -> "ALIMENTACAO".equals(e.getExpenseType().name()))
                    .mapToDouble(ServiceExpense::getValue).sum();
            List<ServicePart> parts = servicePartRepository.findByServiceOrderId(order.getId());

            // Calcular horas trabalhadas da tabela de tempo
            List<TimeTracking> times = timeTrackingRepository.findByServiceOrderId(order.getId());
            double horasWork   = calcHorasByType(times, "TRABALHO");
            double horasTravel = calcHorasByType(times, null);  // todos exceto TRABALHO

            double multiplier = "TECNICO".equals(userRole) ? 0.1 : 1.0;
            double travelRate = 85.0 * multiplier;
            double hourlyRateExec = hourlyRate * multiplier;

            Object[][] itemRows = {
                    {1, "MO",   horasTravel,  "XXXX", "Hora Deslocamento",              travelRate,      null},
                    {2, "MO",   horasWork,    "XXXX", "Hora Trabalhada",                hourlyRateExec, null},
                    {3, "MO",   0.0,          "XXXX", null,                             0.0,       null},
                    {4, "MO",   0.0,          "XXXX", null,                             0.0,       null},
                    {5, "KM",   kmRodados,    "XXXX", "km ida e volta",                 2.2,       null},
                    {6, "DESP", refeicao > 0 ? 1.0 : 0.0, "XXXX", "REFEIÇÃO 1 DIAS", refeicao > 0 ? refeicao : 0.0, null},
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
            xlsNum(r22, 7, 0.0, normalStyle);

            // ── Linha 23 – Total Geral ────────────────────────────────────────
            Row r23 = ws.createRow(22);
            r23.setHeightInPoints(18.75f);
            
            if ("TECNICO".equals(userRole)) {
                // Técnico não vê o Total Geral do faturamento da empresa
                xlsCell(r23, 0, "Resumo de Repasse (Mão de Obra + Despesas)", totalGreenStyle);
                ws.addMergedRegion(new CellRangeAddress(22, 22, 0, 6)); 
            } else {
                xlsCell(r23, 0, "Total Geral do Pedido", totalGreenStyle);
            }
            
            org.apache.poi.ss.usermodel.Cell tgCell = r23.createCell(7);
            tgCell.setCellFormula("H21-H22");
            CellStyle tgCs = xlsBannerStyle(wb, XLS_GREEN_DARK, true, 12, HorizontalAlignment.RIGHT);
            tgCs.setDataFormat(fmt.getFormat("R$ #,##0.00"));
            tgCell.setCellStyle(tgCs);

            // ── Linhas 24 em diante – condições e dados de pagamento ──────────
            String techName = order.getTechnician() != null ? order.getTechnician().getNome() : "";
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
            int rowIdx = 23;
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

            // Linhas 1-5 – bloco vazio (header area para logo se necessário)
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
    // Helpers – PDF (iText)
    // ═══════════════════════════════════════════════════════════════════════════

    private void addClientRow(Table t, PdfFont bold, PdfFont normal,
                               String l1, String v1, String l2, String v2) throws Exception {
        Cell c1 = new Cell().setBorder(Border.NO_BORDER).setPaddingLeft(4);
        c1.add(new Paragraph(l1).setFont(bold).setFontSize(10));
        Cell c2 = new Cell().setBorder(Border.NO_BORDER);
        c2.add(new Paragraph(safeStr(v1)).setFont(normal).setFontSize(10));
        Cell c3 = new Cell().setBorder(Border.NO_BORDER);
        c3.add(new Paragraph(l2).setFont(bold).setFontSize(10));
        Cell c4 = new Cell().setBorder(Border.NO_BORDER);
        c4.add(new Paragraph(safeStr(v2)).setFont(normal).setFontSize(10));
        t.addCell(c1); t.addCell(c2); t.addCell(c3); t.addCell(c4);
    }

    private void addItemRow(Table t, PdfFont bold, PdfFont normal,
                             DeviceRgb bg, String... cols) {
        for (int i = 0; i < cols.length; i++) {
            Cell c = new Cell().setBorder(new SolidBorder(0.3f)).setPadding(2);
            if (bg != null) c.setBackgroundColor(bg);
            c.add(new Paragraph(cols[i]).setFont(normal).setFontSize(9));
            if (i == 4) c.setTextAlignment(TextAlignment.LEFT);
            else         c.setTextAlignment(TextAlignment.CENTER);
            t.addCell(c);
        }
    }

    private void addTotalRow(Table t, PdfFont bold, PdfFont normal,
                              DeviceRgb bg, String label, String value) {
        Cell lc = new Cell().setBorder(new SolidBorder(0.3f)).setPadding(3);
        if (bg != null) lc.setBackgroundColor(bg);
        lc.add(new Paragraph(label).setFont(normal).setFontSize(9));
        Cell vc = new Cell().setBorder(new SolidBorder(0.3f)).setTextAlignment(TextAlignment.RIGHT).setPadding(3);
        if (bg != null) vc.setBackgroundColor(bg);
        vc.add(new Paragraph(value).setFont(normal).setFontSize(9));
        t.addCell(lc); t.addCell(vc);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Helpers – XLSX (Apache POI)
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Insere o logo da Carmaq no canto superior esquerdo da planilha Excel.
     */
    private void addLogoToExcel(XSSFWorkbook wb, XSSFSheet sheet) {
        try {
            Resource resource = resourceLoader.getResource("classpath:/static/logo-carmaq.png");
            if (!resource.exists()) {
                System.err.println("addLogoToExcel: Arquivo não encontrado via ResourceLoader");
                return;
            }
            byte[] logoBytes = resource.getInputStream().readAllBytes();
            int pictureIdx = wb.addPicture(logoBytes, Workbook.PICTURE_TYPE_PNG);
            
            CreationHelper helper = wb.getCreationHelper();
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            ClientAnchor anchor = helper.createClientAnchor();
            
            // Posição: Coluna A-C, Linha 2-6 (indices 0-2, 1-5)
            anchor.setCol1(0);
            anchor.setRow1(1); 
            anchor.setCol2(3);
            anchor.setRow2(6);
            
            Picture pict = drawing.createPicture(anchor, pictureIdx);
            pict.resize(1.0); // Ajusta a imagem mantendo a proporção
        } catch (Exception e) {
            System.err.println("Erro ao inserir logo no Excel: " + e.getMessage());
            e.printStackTrace();
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
}