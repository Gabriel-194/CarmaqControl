package com.example.Service;

import com.example.Models.*;
import com.example.Repository.ServiceExpenseRepository;
import com.example.Repository.ServicePartRepository;
import com.example.Repository.TimeTrackingRepository;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ServicePartRepository servicePartRepository;
    private final ServiceExpenseRepository serviceExpenseRepository;
    private final TimeTrackingRepository timeTrackingRepository;
    private final ServiceOrderService serviceOrderService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final DeviceRgb CARMAQ_GREEN = new DeviceRgb(16, 185, 129); 

    public byte[] generateServiceOrderReport(ServiceOrder order, String userRole) {
        if ("INSTALACAO".equalsIgnoreCase(order.getServiceType())) {
            return generateInstalacaoReport(order, userRole);
        } else {
            return generateManutencaoReport(order, userRole);
        }
    }

    public byte[] generateExpenseReport(ServiceOrder order, String userRole) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("RELATÓRIO DE DESPESAS DE VIAGEM E ATENDIMENTO")
                    .setFontSize(16)
                    .setBold()
                    .setFontColor(CARMAQ_GREEN)
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("OS #" + order.getId() + " - " + (order.getNumeroChamado() != null ? "Chamado: " + order.getNumeroChamado() : ""))
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

            // Info Basica
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            infoTable.addCell(new Cell().add(new Paragraph("Técnico:").setBold()).add(new Paragraph(order.getTechnician() != null ? order.getTechnician().getNome() : "N/A")));
            infoTable.addCell(new Cell().add(new Paragraph("Cliente/Empresa:").setBold()).add(new Paragraph(order.getClient() != null ? order.getClient().getCompanyName() : "N/A")));
            document.add(infoTable.setMarginBottom(15));

            document.add(new Paragraph("DETALHAMENTO DE CUSTOS").setBold().setUnderline());

            Table expTable = new Table(UnitValue.createPercentArray(new float[]{3, 5, 2})).useAllAvailableWidth();
            expTable.addHeaderCell("Tipo");
            expTable.addHeaderCell("Descrição");
            expTable.addHeaderCell("Valor (R$)");

            List<ServiceExpense> expenses = serviceExpenseRepository.findByServiceOrderId(order.getId());
            double totalExp = 0.0;

            for (ServiceExpense exp : expenses) {
                expTable.addCell(exp.getExpenseType().name());
                String desc = exp.getDescription();
                if (exp.getExpenseType().name().equals("DESLOCAMENTO_KM") && exp.getQuantityKm() != null) {
                    desc = "Deslocamento de " + exp.getQuantityKm() + " Km" + (desc != null ? " (" + desc + ")" : "");
                }
                expTable.addCell(desc != null ? desc : "N/A");
                expTable.addCell(String.format("R$ %.2f", exp.getValue()));
                totalExp += exp.getValue();
            }

            document.add(expTable.setMarginBottom(10));



            document.add(new Paragraph("TOTAL DE DESPESAS: R$ " + String.format("%.2f", totalExp))
                    .setBold().setFontSize(14).setFontColor(CARMAQ_GREEN).setMarginTop(10));

            if ("TECNICO".equals(userRole)) {
                 document.add(new Paragraph("\nObs: Comprovantes ou notas fiscais devem acompanhar as despesas inseridas.").setFontSize(10).setItalic());
            }

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage());
        }
        return baos.toByteArray();
    }

    private byte[] generateManutencaoReport(ServiceOrder order, String userRole) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Cabeçalho da Empresa
            document.add(new Paragraph("CARMAQ MÁQUINAS INDUSTRIAIS / CARMAQ SERVICE")
                    .setFontSize(14).setBold().setFontColor(CARMAQ_GREEN).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("CNPJ: 60.526.327/0001-23 | Av. Das Araucárias, 4255 | 83707-065 | Araucária | Paraná")
                    .setFontSize(9).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Fone: 55 41 3346 1430 | 55 41 99663 1349")
                    .setFontSize(9).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Email: vendas@carmaq.ind.br | service@carmaq.ind.br")
                    .setFontSize(9).setTextAlignment(TextAlignment.CENTER).setMarginBottom(15));
            
            // Dados Gerais OS
            String title = "VALENTIM".equals(order.getManutencaoOrigin()) ? "ORDEM DE SERVIÇO EM GARANTIA" : "ORDEM DE SERVIÇO DE MANUTENÇÃO";
            document.add(new Paragraph(title).setFontSize(12).setBold().setUnderline().setTextAlignment(TextAlignment.CENTER).setMarginBottom(10));

            Table topTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1})).useAllAvailableWidth();
            topTable.addCell(new Cell().add(new Paragraph("OS nº: " + order.getId())));
            topTable.addCell(new Cell().add(new Paragraph("Chamado: " + (order.getNumeroChamado() != null ? order.getNumeroChamado() : "-"))));
            topTable.addCell(new Cell().add(new Paragraph("Data: " + (order.getOpenedAt() != null ? order.getOpenedAt().format(DATE_FORMATTER) : "-"))));
            document.add(topTable.setMarginBottom(10));

            // Dados do Cliente
            Table clientTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
            com.example.Models.Client c = order.getClient();
            clientTable.addCell("Cliente: " + c.getCompanyName());
            clientTable.addCell("Endereço: " + (c.getAddress() != null ? c.getAddress() : ""));
            clientTable.addCell("CNPJ: " + (c.getCnpj() != null ? c.getCnpj() : ""));
            clientTable.addCell("IE: " + (c.getIe() != null ? c.getIe() : ""));
            clientTable.addCell("Contato: " + c.getContactName());
            clientTable.addCell("Email: " + (c.getEmail() != null ? c.getEmail() : ""));
            document.add(clientTable.setMarginBottom(10));

            // Dados Serviço
            String tipoServico = "MANUTENÇÃO - " + (order.getMachine() != null ? order.getMachine().getModel() : "") + " - " + c.getCompanyName();
            document.add(new Paragraph("Tipo: " + tipoServico).setBold().setMarginBottom(10));
            document.add(new Paragraph("Defeito: " + (order.getProblemDescription() != null ? order.getProblemDescription() : "-")).setMarginBottom(5));
            document.add(new Paragraph("Serviço Executado: " + (order.getServiceDescription() != null ? order.getServiceDescription() : "-")).setMarginBottom(15));

            // Tabela de Serviços (Official 7 Columns)
            Table svcTable = new Table(UnitValue.createPercentArray(new float[]{3, 1.5f, 1, 1.5f, 4, 1.5f, 1.5f})).useAllAvailableWidth();
            svcTable.addHeaderCell(new Cell().add(new Paragraph("1. Item").setBold().setFontSize(8)));
            svcTable.addHeaderCell(new Cell().add(new Paragraph("2. Unid. (MO, KM, DESP)").setBold().setFontSize(8)));
            svcTable.addHeaderCell(new Cell().add(new Paragraph("3. Qtde.").setBold().setFontSize(8)));
            svcTable.addHeaderCell(new Cell().add(new Paragraph("4. Código").setBold().setFontSize(8)));
            svcTable.addHeaderCell(new Cell().add(new Paragraph("5. Descrição").setBold().setFontSize(8)));
            svcTable.addHeaderCell(new Cell().add(new Paragraph("6. R$ Unitário").setBold().setFontSize(8)));
            svcTable.addHeaderCell(new Cell().add(new Paragraph("7. R$ Total").setBold().setFontSize(8)));

            double totalGeral = 0.0;
            double rateTrabalho = "VALENTIM".equals(order.getManutencaoOrigin()) ? 185.0 : 250.0;
            double rateDeslocamento = 85.0;

            // Injetar Mão de Obra e Deslocamento (TimeTracking)
            List<TimeTracking> times = timeTrackingRepository.findByServiceOrderId(order.getId());
            long minsTrabalho = 0, minsDesloc = 0;
            for (TimeTracking tt : times) {
                if (tt.getStartTime() != null && tt.getEndTime() != null) {
                    long m = java.time.Duration.between(tt.getStartTime(), tt.getEndTime()).toMinutes();
                    if ("TRABALHO".equals(tt.getType())) minsTrabalho += m;
                    else minsDesloc += m;
                }
            }
            if (minsTrabalho > 0) {
                double horas = minsTrabalho / 60.0;
                double total = horas * rateTrabalho;
                totalGeral += total;
                addServiceRow(svcTable, "Mão de Obra", "MO", horas, "-", "Hora Trabalhada", rateTrabalho, total);
            }
            if (minsDesloc > 0) {
                double horas = minsDesloc / 60.0;
                double total = horas * rateDeslocamento;
                totalGeral += total;
                addServiceRow(svcTable, "Deslocamento", "MO", horas, "-", "Hora Deslocamento", rateDeslocamento, total);
            }

            // Injetar Custo por Km e Despesas Extras
            List<ServiceExpense> expenses = serviceExpenseRepository.findByServiceOrderId(order.getId());
            for (ServiceExpense exp : expenses) {
                totalGeral += exp.getValue();
                if ("DESLOCAMENTO_KM".equals(exp.getExpenseType().name()) && exp.getQuantityKm() != null) {
                    double rateKm = 2.20;
                    addServiceRow(svcTable, "Quilometragem", "KM", exp.getQuantityKm(), "-", "Quilometro rodado ida e volta", rateKm, exp.getValue());
                } else {
                    addServiceRow(svcTable, "Despesa Extra", "DESP", 1.0, "-", exp.getExpenseType().name() + " / " + (exp.getDescription()!=null?exp.getDescription():""), exp.getValue(), exp.getValue());
                }
            }

            // Injetar Peças (ServicePart)
            List<ServicePart> parts = servicePartRepository.findByServiceOrderId(order.getId());
            for (ServicePart pt : parts) {
                totalGeral += pt.getTotalPrice();
                addServiceRow(svcTable, "Peça", "PC", (double)pt.getQuantity(), "-", pt.getPartName(), pt.getUnitPrice(), pt.getTotalPrice());
            }

            document.add(svcTable.setMarginBottom(10));

            // Total
            if (!"TECNICO".equals(userRole)) {
                if (order.getDiscountValue() != null && order.getDiscountValue() > 0) {
                    totalGeral -= order.getDiscountValue();
                    document.add(new Paragraph("Desconto Aplicado: - R$ " + String.format("%.2f", order.getDiscountValue())).setTextAlignment(TextAlignment.RIGHT));
                }
                document.add(new Paragraph("TOTAL GERAL: R$ " + String.format("%.2f", totalGeral))
                    .setBold().setFontSize(14).setFontColor(CARMAQ_GREEN).setTextAlignment(TextAlignment.RIGHT).setMarginBottom(20));
            }

            document.add(new Paragraph("\n___________________________________________________")
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(30));
            document.add(new Paragraph("Assinatura do Cliente / Responsável").setTextAlignment(TextAlignment.CENTER));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage());
        }
        return baos.toByteArray();
    }

    private void addServiceRow(Table table, String col1, String col2, double qtde, String col4, String col5, double unit, double total) {
        table.addCell(new Cell().add(new Paragraph(col1).setFontSize(8)));
        table.addCell(new Cell().add(new Paragraph(col2).setFontSize(8)));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f", qtde)).setFontSize(8)));
        table.addCell(new Cell().add(new Paragraph(col4).setFontSize(8)));
        table.addCell(new Cell().add(new Paragraph(col5).setFontSize(8)));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f", unit)).setFontSize(8)));
        table.addCell(new Cell().add(new Paragraph(String.format("%.2f", total)).setFontSize(8)));
    }


    private byte[] generateInstalacaoReport(ServiceOrder order, String userRole) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("RELATÓRIO DE INSTALAÇÃO E ENTREGA TÉCNICA")
                    .setFontSize(18).setBold().setFontColor(CARMAQ_GREEN).setTextAlignment(TextAlignment.CENTER));
            
            Table topTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1})).useAllAvailableWidth();
            topTable.addCell("Instalação nº: " + order.getId());
            topTable.addCell("Chamado: " + (order.getNumeroChamado() != null ? order.getNumeroChamado() : ""));
            topTable.addCell("Data: " + (order.getOpenedAt() != null ? order.getOpenedAt().format(DATE_FORMATTER) : ""));
            document.add(topTable.setMarginBottom(10));

            addClientBox(document, order);
            addMachineBox(document, order);

            document.add(new Paragraph("CHECKLIST E ATIVIDADES").setBold().setUnderline());
            document.add(new Paragraph(order.getServiceDescription() != null ? order.getServiceDescription() : "Nenhuma atividade preenchida"));
            document.add(new Paragraph("\n"));

            addTimeBox(document, order);
            addFinanceBox(document, order, userRole);

            document.add(new Paragraph("\nDeclaramos que a máquina foi instalada e testada operando em conformidade.")
                .setItalic().setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("\n___________________________________________________")
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(30));
            document.add(new Paragraph("Assinatura do Cliente / Recebedor").setTextAlignment(TextAlignment.CENTER));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage());
        }
        return baos.toByteArray();
    }

    private void addClientBox(Document doc, ServiceOrder order) {
        doc.add(new Paragraph("1. DADOS DO CLIENTE / DISTRIBUIDOR").setBold().setBackgroundColor(new DeviceRgb(240, 240, 240)));
        Table clientTable = new Table(UnitValue.createPercentArray(new float[]{2, 2})).useAllAvailableWidth();
        clientTable.addCell("Razão Social: " + order.getClient().getCompanyName());
        clientTable.addCell("Contato: " + order.getClient().getContactName());
        clientTable.addCell("Endereço: " + (order.getClient().getAddress() != null ? order.getClient().getAddress() : ""));
        clientTable.addCell("CNPJ/IE: " + (order.getClient().getCnpj() != null ? order.getClient().getCnpj() : "") + " / " + (order.getClient().getIe() != null ? order.getClient().getIe() : ""));
        doc.add(clientTable.setMarginBottom(10));
    }

    private void addMachineBox(Document doc, ServiceOrder order) {
        doc.add(new Paragraph("2. DADOS DA MÁQUINA").setBold().setBackgroundColor(new DeviceRgb(240, 240, 240)));
        Table machineTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1})).useAllAvailableWidth();
        machineTable.addCell("Modelo: " + order.getMachine().getModel());
        machineTable.addCell("Nº Série: " + (order.getMachine().getName() != null ? order.getMachine().getName() : ""));
        machineTable.addCell("Tipo: " + (order.getMachine().getMachineType() != null ? order.getMachine().getMachineType().name() : ""));
        doc.add(machineTable.setMarginBottom(10));
    }

    private void addServiceDetailsBox(Document doc, ServiceOrder order) {
        doc.add(new Paragraph("3. DESCRIÇÃO DOS SERVIÇOS").setBold().setBackgroundColor(new DeviceRgb(240, 240, 240)));
        doc.add(new Paragraph("Defeito Reclamado:").setBold());
        doc.add(new Paragraph(order.getProblemDescription() != null ? order.getProblemDescription() : "N/A").setMarginBottom(5));
        doc.add(new Paragraph("Serviços Executados:").setBold());
        doc.add(new Paragraph(order.getServiceDescription() != null ? order.getServiceDescription() : "N/A").setMarginBottom(10));
    }

    private void addTimeBox(Document doc, ServiceOrder order) {
        doc.add(new Paragraph("4. APONTAMENTO DE HORAS").setBold().setBackgroundColor(new DeviceRgb(240, 240, 240)));
        List<TimeTracking> times = timeTrackingRepository.findByServiceOrderId(order.getId());
        if (times.isEmpty()) {
            doc.add(new Paragraph("Nenhum apontamento de tempo."));
            return;
        }

        Table timeTable = new Table(UnitValue.createPercentArray(new float[]{2, 2, 2, 2, 2})).useAllAvailableWidth();
        timeTable.addHeaderCell("Tipo");
        timeTable.addHeaderCell("Data");
        timeTable.addHeaderCell("Início");
        timeTable.addHeaderCell("Fim");
        timeTable.addHeaderCell("Horas Úteis");

        for (TimeTracking t : times) {
            timeTable.addCell(t.getType());
            timeTable.addCell(t.getRegisteredDate().format(DATE_FORMATTER));
            timeTable.addCell(t.getStartTime().format(TIME_FORMATTER));
            timeTable.addCell(t.getEndTime() != null ? t.getEndTime().format(TIME_FORMATTER) : "Em andamento");
            
            if (t.getEndTime() != null) {
                Duration dur = Duration.between(t.getStartTime(), t.getEndTime());
                long hs = dur.toHours();
                long ms = dur.toMinutesPart();
                timeTable.addCell(String.format("%02d:%02d", hs, ms));
            } else {
                timeTable.addCell("-");
            }
        }
        doc.add(timeTable.setMarginBottom(10));
    }

    private void addPartsBox(Document doc, ServiceOrder order) {
        List<ServicePart> parts = servicePartRepository.findByServiceOrderId(order.getId());
        if (parts.isEmpty()) return;

        doc.add(new Paragraph("5. PEÇAS UTILIZADAS E SUBSTITUÍDAS").setBold().setBackgroundColor(new DeviceRgb(240, 240, 240)));
        Table partsTable = new Table(UnitValue.createPercentArray(new float[]{4, 1, 2})).useAllAvailableWidth();
        partsTable.addHeaderCell("Descrição / Peça");
        partsTable.addHeaderCell("Qtd");
        partsTable.addHeaderCell("Valor Total");

        for (ServicePart part : parts) {
            partsTable.addCell(part.getPartName());
            partsTable.addCell(String.valueOf(part.getQuantity()));
            partsTable.addCell(String.format("R$ %.2f", part.getTotalPrice()));
        }
        doc.add(partsTable.setMarginBottom(10));
    }

    private void addFinanceBox(Document doc, ServiceOrder order, String userRole) {
        if ("TECNICO".equals(userRole)) {
            doc.add(new Paragraph("Resumo financeiro omitido para a visualização técnica.").setFontSize(8).setItalic());
            return;
        }

        doc.add(new Paragraph("RESUMO DE VALORES COBRADOS").setBold().setBackgroundColor(new DeviceRgb(240, 240, 240)));
        Table financeTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        financeTable.addCell("Mão de Obra (Horas):"); financeTable.addCell(String.format("R$ %.2f", order.getServiceValue()));
        financeTable.addCell("Peças / Insumos:"); financeTable.addCell(String.format("R$ %.2f", order.getPartsValue()));
        financeTable.addCell("Despesas / Rodagem:"); financeTable.addCell(String.format("R$ %.2f", order.getExpensesValue()));
        
        if (order.getDiscountValue() != null && order.getDiscountValue() > 0) {
            financeTable.addCell("Desconto:"); financeTable.addCell(String.format("- R$ %.2f", order.getDiscountValue()));
        }

        Double total = serviceOrderService.calculateTotal(order);
        Cell totalCell = new Cell().add(new Paragraph("TOTAL GERAL A FATURAR:").setBold());
        Cell totalValCell = new Cell().add(new Paragraph(String.format("R$ %.2f", total)).setBold().setFontColor(CARMAQ_GREEN));
        financeTable.addCell(totalCell);
        financeTable.addCell(totalValCell);

        doc.add(financeTable);
    }
}
