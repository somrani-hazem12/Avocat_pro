package com.avocat.service;

import com.avocat.entity.Facture;
import com.avocat.repository.FactureRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class FactureService {

    private final FactureRepository factureRepository;

    public byte[] genererPdfFacture(Long id) {
        Facture facture = factureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture non trouvée"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 1. En-tête
            document.add(new Paragraph("AVOCAT PRO")
                    .setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Plateforme de Gestion Juridique")
                    .setItalic().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            // 2. Infos Facture & Avocat
            document.add(new Paragraph("Facture N° : " + facture.getId())
                    .setBold());
            document.add(new Paragraph("Date : " + facture.getDate()));
            document.add(new Paragraph("Avocat : " + facture.getUtilisateur().getNom()));
            document.add(new Paragraph("\n"));

            // 3. Infos Client
            document.add(new Paragraph("Client concerné :")
                    .setUnderline().setBold());
            document.add(new Paragraph("Nom : " + facture.getClient().getNom()));
            document.add(new Paragraph("Email : " + facture.getClient().getEmail()));
            document.add(new Paragraph("\n"));

            // 4. Tableau des montants
            float[] columnWidths = {3, 1};
            Table table = new Table(UnitValue.createPointArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            table.addCell("Désignation");
            table.addCell("Montant");

            table.addCell("Honoraires juridiques (HT)");
            table.addCell(String.format("%.2f DT", facture.getMontantHT()));

            table.addCell("TVA (" + facture.getTva() + "%)");
            table.addCell(String.format("%.2f DT", (facture.getMontantTotal() - facture.getMontantHT())));

            table.addCell(new Paragraph("TOTAL TTC").setBold());
            table.addCell(new Paragraph(String.format("%.2f DT", facture.getMontantTotal())).setBold());

            document.add(table);
            document.add(new Paragraph("\nStatut de la facture : " + facture.getStatut()));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Erreur génération PDF : " + e.getMessage());
        }

        return baos.toByteArray();
    }
}