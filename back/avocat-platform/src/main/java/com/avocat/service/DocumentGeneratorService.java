package com.avocat.service;

import com.itextpdf.html2pdf.HtmlConverter;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import java.io.ByteArrayOutputStream;
import java.util.Map;

@Service
public class DocumentGeneratorService {

    // On utilise 'final' pour s'assurer que l'injection est obligatoire
    private final AuditService auditService;
    private final TemplateEngine templateEngine;

    // CORRECTION : Le constructeur DOIT prendre AuditService en paramètre pour que Spring l'injecte
    public DocumentGeneratorService(AuditService auditService) {
        this.auditService = auditService;
        this.templateEngine = new TemplateEngine();

        // Configuration du resolver
        StringTemplateResolver resolver = new StringTemplateResolver();
        this.templateEngine.setTemplateResolver(resolver);
    }

    public byte[] generatePdf(String templateId, Map<String, Object> data) throws Exception {
        String htmlTemplate = switch (templateId) {
            case "mise-en-demeure" -> getMiseEnDemeureTemplate();
            case "contrat-travail" -> getContratTravailTemplate();
            case "procuration" -> getProcurationTemplate();
            default -> "<html><body>Modèle inconnu</body></html>";
        };

        // On enregistre le Log d'audit (L'erreur arrivait ici car auditService était null)
        auditService.enregistrerLog(
                "GENERATION_DOCUMENT_JURIDIQUE",
                (String) data.get("nomClient"),
                "Type: " + templateId + " | Dossier N°: " + data.get("numeroDossier")
        );

        Context context = new Context();
        context.setVariables(data);
        String finalHtml = templateEngine.process(htmlTemplate, context);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            HtmlConverter.convertToPdf(finalHtml, out);
            return out.toByteArray();
        }
    }

    private String getMiseEnDemeureTemplate() {
        return """
            <html>
            <head><style>
                body { font-family: 'DejaVu Sans', sans-serif; font-size: 11pt; line-height: 1.5; margin: 40px; }
                .header { display: flex; justify-content: space-between; margin-bottom: 50px; }
                .logo { width: 120px; }
                .title { text-align: center; font-size: 16pt; font-weight: bold; text-decoration: underline; margin: 30px 0; }
                .bold { font-weight: bold; }
                .footer { margin-top: 50px; text-align: right; }
                .sig-box { width: 180px; margin-top: 10px; }
            </style></head>
            <body>
                <div class="header">
                    <div>
                        <img src="src/main/resources/static/logoo.png" class="logo" /><br>
                        Cabinet d'Avocat <span th:text="${nomAvocat}"></span>
                    </div>
                    <div style="text-align: right;">
                        <p>Fait à <span th:text="${ville}"></span>, le <span th:text="${date}"></span></p>
                    </div>
                </div>

                <div style="margin-left: 50%;">
                    <p class="bold">À l'attention de :</p>
                    <p><span th:text="${nomClient}"></span><br><span th:text="${adresseClient}"></span></p>
                </div>

                <p class="bold">OBJET : MISE EN DEMEURE - VALANT SOMMATION DE PAYER</p>
                <p>Dossier N° : <span th:text="${numeroDossier}"></span></p>

                <p>Madame, Monsieur,</p>
                <p>En ma qualité de conseil de votre créancier, je vous informe par la présente que malgré plusieurs relances, le règlement de la somme de <b th:text="${montant}"></b> DT relative à l'objet cité en référence n'a toujours pas été perçu.</p>
                
                <p>Par la présente, je vous mets officiellement en demeure de procéder au règlement de cette somme sous un délai de huit (8) jours à compter de la réception de cette lettre.</p>
                
                <p>À défaut de paiement ou d'accord de règlement dans le délai imparti, je me verrai contraint d'engager toutes les poursuites judiciaires nécessaires devant les tribunaux compétents.</p>

                <p>Sous toutes réserves,</p>
                <div class="footer">
                    <p>Maître <b th:text="${nomAvocat}"></b></p>
                    <img src="src/main/resources/static/avct.png" class="sig-box" />
                </div>
            </body></html>""";
    }

    private String getContratTravailTemplate() {
        return """
            <html>
            <head><style>
                body { font-family: 'DejaVu Sans', sans-serif; font-size: 10pt; line-height: 1.4; margin: 40px; }
                .logo { width: 100px; position: absolute; top: 0; left: 0; }
                .title { text-align: center; font-size: 14pt; font-weight: bold; margin-bottom: 30px; }
                .article { font-weight: bold; margin-top: 15px; }
                .sig-box { width: 150px; margin-top: 10px; }
            </style></head>
            <body>
                <img src="src/main/resources/static/logoo.png" class="logo" />
                <div class="title">CONTRAT DE TRAVAIL À DURÉE INDÉTERMINÉE</div>
                
                <p><b>ENTRE LES SOUSSIGNÉS :</b></p>
                <p>Le Cabinet d'Avocat Maître <span th:text="${nomAvocat}"></span>, ci-après désigné "l'Employeur",</p>
                <p><b>ET :</b></p>
                <p>M./Mme <span th:text="${nomClient}"></span>, demeurant à <span th:text="${adresseClient}"></span>, ci-après désigné(e) "le Salarié".</p>

                <p class="article">ARTICLE 1 : ENGAGEMENT ET FONCTIONS</p>
                <p>Le Salarié est engagé à compter du <span th:text="${date}"></span> en qualité de <b th:text="${poste}"></b>.</p>

                <p class="article">ARTICLE 2 : RÉMUNÉRATION</p>
                <p>En contrepartie de son activité, le Salarié percevra une rémunération mensuelle brute de <b th:text="${salaire}"></b> DT.</p>

                <p class="article">ARTICLE 3 : CONFIDENTIALITÉ</p>
                <p>Le Salarié s'engage à respecter une discrétion absolue sur l'ensemble des informations concernant les clients du cabinet.</p>

                <br><br>
                <div style="display: flex; justify-content: space-between;">
                    <div>
                        <p>Signature de l'Employeur :</p>
                        <img src="src/main/resources/static/avct.png" class="sig-box" />
                    </div>
                    <div style="text-align: right;">
                        <p>Signature du Salarié :</p>
                        <br><br><p>(Lu et approuvé)</p>
                    </div>
                </div>
            </body></html>""";
    }

    private String getProcurationTemplate() {
        return """
            <html>
            <head><style>
                body { font-family: 'DejaVu Sans', sans-serif; margin: 50px; line-height: 1.6; }
                .logo { width: 120px; float: left; }
                .title { text-align: center; font-size: 18pt; font-weight: bold; margin: 40px 0; }
                .sig-box { width: 160px; display: block; margin-left: auto; margin-right: 0; }
            </style></head>
            <body>
                <img src="src/main/resources/static/logoo.png" class="logo" />
                <div style="clear: both;"></div>
                <div class="title">PROCURATION / POUVOIR</div>

                <p>Je soussigné(e), <b th:text="${nomClient}"></b>, demeurant à <span th:text="${adresseClient}"></span>,</p>
                
                <p>Donne par les présentes pouvoir spécial à :</p>
                <p><b>Maître <span th:text="${nomAvocat}"></span></b>, Avocat au Barreau, pour me représenter dans le cadre du dossier <span th:text="${numeroDossier}"></span>.</p>

                <p>Fait pour valoir ce que de droit.</p>
                <p>Fait à <span th:text="${ville}"></span>, le <span th:text="${date}"></span>.</p>
                <br><br>
                <p style="text-align: right;">Signature de l'Avocat mandataire :</p>
                <img src="src/main/resources/static/avct.png" class="sig-box" />
            </body></html>""";
    }
}