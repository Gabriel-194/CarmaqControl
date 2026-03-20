package com.example.Service;

import com.example.Models.*;
import com.example.Repository.ServicePartRepository;
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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ServicePartRepository servicePartRepository;
    private final ServiceOrderService serviceOrderService;

    public byte[] generateServiceOrderReport(ServiceOrder order, String userRole) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Estilo
            DeviceRgb carmaqGreen = new DeviceRgb(16, 185, 129); // #10b981
            
            // Cabeçalho
            document.add(new Paragraph("RELATÓRIO DE SERVIÇO TÉCNICO")
                    .setFontSize(20)
                    .setBold()
                    .setFontColor(carmaqGreen)
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("Ordem de Serviço #" + order.getId())
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // Informações do Cliente
            document.add(new Paragraph("INFORMAÇÕES DO CLIENTE").setBold().setUnderline());
            Table clientTable = new Table(UnitValue.createPercentArray(new float[]{1, 3})).useAllAvailableWidth();
            clientTable.addCell("Empresa:"); clientTable.addCell(order.getClient().getCompanyName());
            clientTable.addCell("Contato:"); clientTable.addCell(order.getClient().getContactName());
            clientTable.addCell("Endereço:"); clientTable.addCell(order.getClient().getAddress() != null ? order.getClient().getAddress() : "N/A");
            document.add(clientTable.setMarginBottom(15));

            // Informações da Máquina
            document.add(new Paragraph("DADOS DA MÁQUINA").setBold().setUnderline());
            Table machineTable = new Table(UnitValue.createPercentArray(new float[]{1, 3})).useAllAvailableWidth();
            machineTable.addCell("Tipo:"); machineTable.addCell(order.getMachine().getMachineType() != null ? order.getMachine().getMachineType().name() : "N/A");
            machineTable.addCell("Modelo:"); machineTable.addCell(order.getMachine().getModel());
            machineTable.addCell("Nome/Marca:"); machineTable.addCell(order.getMachine().getName() != null ? order.getMachine().getName() : "N/A");
            document.add(machineTable.setMarginBottom(15));

            // Detalhes do Serviço
            document.add(new Paragraph("DETALHES DO SERVIÇO").setBold().setUnderline());
            document.add(new Paragraph("Problema Relatado:").setBold());
            document.add(new Paragraph(order.getProblemDescription() != null ? order.getProblemDescription() : "Não informado"));
            document.add(new Paragraph("Serviço Realizado:").setBold());
            document.add(new Paragraph(order.getServiceDescription() != null ? order.getServiceDescription() : "Em andamento"));
            document.add(new Paragraph("Observações:").setBold());
            document.add(new Paragraph(order.getObservations() != null ? order.getObservations() : "Nenhuma"));
            
            // Peças Utilizadas
            List<ServicePart> parts = servicePartRepository.findByServiceOrderId(order.getId());
            if (!parts.isEmpty()) {
                document.add(new Paragraph("\nPEÇAS UTILIZADAS").setBold().setUnderline());
                Table partsTable = new Table(UnitValue.createPercentArray(new float[]{3, 1, 1, 1})).useAllAvailableWidth();
                partsTable.addHeaderCell("Peça");
                partsTable.addHeaderCell("Qtd");
                partsTable.addHeaderCell("V. Unit");
                partsTable.addHeaderCell("V. Total");
                
                for (ServicePart part : parts) {
                    partsTable.addCell(part.getPartName());
                    partsTable.addCell(String.valueOf(part.getQuantity()));
                    partsTable.addCell(String.format("R$ %.2f", part.getUnitPrice()));
                    partsTable.addCell(String.format("R$ %.2f", part.getTotalPrice()));
                }
                document.add(partsTable.setMarginBottom(15));
            }

            // Resumo Financeiro (Filtrado por Role)
            if (!"TECNICO".equals(userRole)) {
                document.add(new Paragraph("\nRESUMO FINANCEIRO").setBold().setUnderline());
                Table financeTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
                financeTable.addCell("Mão de Obra:"); financeTable.addCell(String.format("R$ %.2f", order.getServiceValue()));
                financeTable.addCell("Despesas:"); financeTable.addCell(String.format("R$ %.2f", order.getExpensesValue()));
                financeTable.addCell("Total Peças:"); financeTable.addCell(String.format("R$ %.2f", order.getPartsValue()));
                
                Double total = serviceOrderService.calculateTotal(order);
                Cell totalCell = new Cell().add(new Paragraph("TOTAL GERAL:").setBold());
                Cell totalValCell = new Cell().add(new Paragraph(String.format("R$ %.2f", total)).setBold().setFontColor(carmaqGreen));
                financeTable.addCell(totalCell);
                financeTable.addCell(totalValCell);
                
                document.add(financeTable);
            } else {
                document.add(new Paragraph("\nEste relatório não inclui valores financeiros por restrições de acesso.").setFontSize(8).setItalic());
            }

            // Rodapé
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            document.add(new Paragraph("\n\nDocumento gerado em: " + java.time.LocalDateTime.now().format(formatter))
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.RIGHT));
            
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage());
        }
        
        return baos.toByteArray();
    }
}
