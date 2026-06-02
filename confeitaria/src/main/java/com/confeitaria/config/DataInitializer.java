package com.confeitaria.config;

import com.confeitaria.model.*;
import com.confeitaria.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FaqRepository faqRepo;
    private final ReferralLinkRepository referralRepo;
    private final IngredientRepository ingredientRepo;
    private final TestimonialRepository testimonialRepo;
    private final CostSettingsRepository costSettingsRepo;
    private final UtmLinkRepository utmLinkRepo;
    private final SaleRepository saleRepo;
    private final MonthlyExpenseRepository monthlyExpenseRepo;
    private final GalleryItemRepository galleryItemRepo;

    public DataInitializer(FaqRepository faqRepo, ReferralLinkRepository referralRepo,
                           IngredientRepository ingredientRepo, TestimonialRepository testimonialRepo,
                           CostSettingsRepository costSettingsRepo, UtmLinkRepository utmLinkRepo,
                           SaleRepository saleRepo, MonthlyExpenseRepository monthlyExpenseRepo,
                           GalleryItemRepository galleryItemRepo) {
        this.faqRepo = faqRepo;
        this.referralRepo = referralRepo;
        this.ingredientRepo = ingredientRepo;
        this.testimonialRepo = testimonialRepo;
        this.costSettingsRepo = costSettingsRepo;
        this.utmLinkRepo = utmLinkRepo;
        this.saleRepo = saleRepo;
        this.monthlyExpenseRepo = monthlyExpenseRepo;
        this.galleryItemRepo = galleryItemRepo;
    }

    @Override
    public void run(String... args) {
        if (!costSettingsRepo.existsById(CostSettings.SINGLETON_ID)) {
            var cs = new CostSettings();
            cs.setId(CostSettings.SINGLETON_ID);
            cs.setEstimatedMonthlyProductionUnits(new BigDecimal("100"));
            costSettingsRepo.save(cs);
        }

        if (utmLinkRepo.count() == 0) {
            var utm = new UtmLink();
            utm.setName("Exemplo: Instagram → Contato");
            utm.setBaseUrl("http://localhost:8080/contato");
            utm.setUtmSource("instagram");
            utm.setUtmMedium("social");
            utm.setUtmCampaign("doces_2026");
            utm.setShortDescription("Ajuste a URL base ao publicar em produção.");
            utmLinkRepo.save(utm);
        }

        // Sample FAQs
        if (faqRepo.count() == 0) {
            addFaq("Como faço um pedido?", "Entre em contato pelo formulário ou WhatsApp. Respondemos em até 24h para alinhar os detalhes do seu pedido.", 1);
            addFaq("Qual o prazo mínimo para encomendar?", "Recomendamos pelo menos 7 dias de antecedência para bolos decorados e 3 dias para doces finos.", 2);
            addFaq("Vocês entregam?", "Sim! Entregamos na cidade e região. O valor do frete é calculado conforme o endereço.", 3);
            addFaq("Trabalham com restrições alimentares?", "Sim, trabalhamos com opções sem glúten, sem lactose e veganas. Informe ao fazer o pedido.", 4);
            addFaq("Como é feito o pagamento?", "Pedimos 50% de entrada no momento da confirmação e o restante na entrega. Aceitamos Pix, cartão e dinheiro.", 5);
        }

        // Sample referral links
        if (referralRepo.count() == 0) {
            var ref = new ReferralLink();
            ref.setCode("INSTAGRAM");
            ref.setReferrerName("Instagram Oficial");
            referralRepo.save(ref);

            var ref2 = new ReferralLink();
            ref2.setCode("INDICA");
            ref2.setReferrerName("Indicação Geral");
            referralRepo.save(ref2);
        }

        // Sample ingredients
        if (ingredientRepo.count() == 0) {
            addIngredient("Farinha de Trigo", new BigDecimal("4.50"));
            addIngredient("Açúcar Refinado", new BigDecimal("5.00"));
            addIngredient("Manteiga", new BigDecimal("38.00"));
            addIngredient("Ovos", new BigDecimal("18.00"));
            addIngredient("Chocolate em Pó 50%", new BigDecimal("35.00"));
            addIngredient("Leite Condensado", new BigDecimal("22.00"));
            addIngredient("Creme de Leite", new BigDecimal("16.00"));
            addIngredient("Fermento em Pó", new BigDecimal("25.00"));
            addIngredient("Leite Integral", new BigDecimal("5.00"));
            addIngredient("Chocolate Nobre 70%", new BigDecimal("80.00"));
        }

        // Sample testimonials
        if (testimonialRepo.count() == 0) {
            addTestimonial("Mariana S.", "O bolo de aniversário ficou lindo e delicioso! Todos adoraram. Com certeza vou encomendar novamente!", 5);
            addTestimonial("Carlos R.", "Brigadeiros incríveis, derretem na boca. O atendimento foi super atencioso e o prazo foi cumprido direitinho.", 5);
            addTestimonial("Juliana M.", "Fiz a encomenda para o chá de bebê e superou todas as expectativas. As fotos não fazem jus ao sabor!", 5);
        }

        if (galleryItemRepo.count() == 0) {
            seedGalleryItems();
        }

        if (saleRepo.count() == 0) {
            seedSales();
        }

        if (monthlyExpenseRepo.count() == 0) {
            seedMonthlyExpenses();
        }
    }

    private void seedGalleryItems() {
        addGallery("Bolo de Chocolate com Morangos",
                "Bolo de chocolate recheado com mousse, coberto com ganache e decorado com morangos frescos e brigadeiros dourados.",
                "/uploads/produto1.jpeg", 1);
        addGallery("Bolo Chá Revelação",
                "Bolo especial para chá revelação com decoração azul e rosa, pézinhos de bebê e laços delicados.",
                "/uploads/produto2.jpeg", 2);
        addGallery("Trufas Brancas com Nozes Douradas",
                "Trufas de chocolate branco com pó pérola, decoradas com nozes caramelizadas douradas.",
                "/uploads/produto3.jpeg", 3);
        addGallery("Brigadeiros Recheados com Maracujá",
                "Brigadeiros de chocolate recheados com creme de maracujá — combinação irresistível de sabores.",
                "/uploads/produto4.jpeg", 4);
        addGallery("Caixinha de Trufas com Nozes",
                "Caixinha presenteável com trufas de chocolate branco cobertas com nozes douradas — perfeita para presentes.",
                "/uploads/produto5.jpeg", 5);
        addGallery("Mesa de Doces Artesanais",
                "Mesa completa com trufas brancas, nozes caramelizadas e brigadeiros decorados com flores marsala.",
                "/uploads/produto6.jpeg", 6);
    }

    private void addGallery(String title, String description, String imagePath, int order) {
        var g = new GalleryItem();
        g.setTitle(title);
        g.setDescription(description);
        g.setImagePath(imagePath);
        g.setDisplayOrder(order);
        g.setVisible(true);
        galleryItemRepo.save(g);
    }

    private void seedSales() {
        // Recebimentos — setembro 2025
        addSale("Recebimento", "Vendas", new BigDecimal("59.48"),  LocalDate.of(2025, 9,  1), null);
        addSale("Recebimento", "Vendas", new BigDecimal("19.63"),  LocalDate.of(2025, 9,  1), null);
        addSale("Recebimento", "Vendas", new BigDecimal("75.00"),  LocalDate.of(2025, 9,  3), null);
        addSale("Recebimento", "Vendas", new BigDecimal("28.62"),  LocalDate.of(2025, 9,  3), null);
        addSale("Recebimento", "Vendas", new BigDecimal("60.00"),  LocalDate.of(2025, 9,  3), null);
        addSale("Recebimento", "Vendas", new BigDecimal("75.00"),  LocalDate.of(2025, 9,  4), null);
        addSale("Recebimento", "Vendas", new BigDecimal("50.00"),  LocalDate.of(2025, 9,  4), null);
        addSale("Recebimento", "Vendas", new BigDecimal("8.00"),   LocalDate.of(2025, 9,  4), null);
        addSale("Recebimento", "Vendas", new BigDecimal("63.55"),  LocalDate.of(2025, 9,  5), null);
        addSale("Recebimento", "Vendas", new BigDecimal("50.00"),  LocalDate.of(2025, 9,  5), null);
        addSale("Recebimento", "Vendas", new BigDecimal("34.42"),  LocalDate.of(2025, 9,  5), null);
        addSale("Recebimento", "Vendas", new BigDecimal("22.00"),  LocalDate.of(2025, 9,  6), null);
        addSale("Recebimento", "Vendas", new BigDecimal("50.00"),  LocalDate.of(2025, 9,  6), null);
        addSale("Recebimento", "Vendas", new BigDecimal("100.00"), LocalDate.of(2025, 9,  6), null);
        addSale("Recebimento", "Vendas", new BigDecimal("51.40"),  LocalDate.of(2025, 9,  6), null);
        addSale("Recebimento", "Vendas", new BigDecimal("24.00"),  LocalDate.of(2025, 9,  8), null);
        addSale("Recebimento", "Vendas", new BigDecimal("425.00"), LocalDate.of(2025, 9,  8), null);
        addSale("Shopee",      "Shopee", new BigDecimal("24.89"),  LocalDate.of(2025, 9,  9), null);
        addSale("Renata ex Shopee", "Indicação", new BigDecimal("100.00"), LocalDate.of(2025, 9, 9), null);
        addSale("Recebimento", "Vendas", new BigDecimal("60.00"),  LocalDate.of(2025, 9, 10), null);
        addSale("Recebimento", "Vendas", new BigDecimal("43.00"),  LocalDate.of(2025, 9, 11), null);
        addSale("Reembolso Uber", "Reembolso", new BigDecimal("9.78"), LocalDate.of(2025, 9, 12), null);

        // Pedido Claudia / Ana TK — 25/03/2026
        addSale("50 Trufas de Limão Siciliano + 50 Brigadeiros Nutella", "Doces Finos",
                new BigDecimal("333.00"), LocalDate.of(2026, 3, 25),
                "Cliente: Claudia / Ana TK. Valor bruto R$350,00, desconto 5% = -R$17,00. " +
                "Decoração: trufa dourada + flor marsala; brigadeiro recheado com Nutella + flor marsala.");
    }

    private void addSale(String name, String group, BigDecimal revenue, LocalDate date, String notes) {
        var s = new Sale();
        s.setProductName(name);
        s.setProductGroup(group);
        s.setRevenue(revenue);
        s.setCost(BigDecimal.ZERO);
        s.setSaleDate(date);
        s.setNotes(notes);
        saleRepo.save(s);
    }

    private void seedMonthlyExpenses() {
        String ym = "2025-09";
        // FIXO — custos recorrentes
        addExpense(ym, "Enel",        new BigDecimal("116.42"), ExpenseType.FIXO);
        addExpense(ym, "Enel",        new BigDecimal("16.48"),  ExpenseType.FIXO);
        addExpense(ym, "Jolana",      new BigDecimal("74.96"),  ExpenseType.FIXO);
        addExpense(ym, "Ovos",        new BigDecimal("50.00"),  ExpenseType.FIXO);
        addExpense(ym, "Ingredientes",new BigDecimal("18.91"),  ExpenseType.FIXO);
        addExpense(ym, "Ingredientes",new BigDecimal("10.88"),  ExpenseType.FIXO);
        addExpense(ym, "Ingredientes",new BigDecimal("30.02"),  ExpenseType.FIXO);
        addExpense(ym, "Ingredientes",new BigDecimal("63.96"),  ExpenseType.FIXO);
        addExpense(ym, "Ingredientes",new BigDecimal("93.98"),  ExpenseType.FIXO);
        addExpense(ym, "Ingredientes",new BigDecimal("25.72"),  ExpenseType.FIXO);
        addExpense(ym, "Ingredientes",new BigDecimal("132.08"), ExpenseType.FIXO);
        addExpense(ym, "Ingredientes",new BigDecimal("43.98"),  ExpenseType.FIXO);
        addExpense(ym, "Ingredientes",new BigDecimal("90.00"),  ExpenseType.FIXO);
        addExpense(ym, "Embalagens",  new BigDecimal("35.44"),  ExpenseType.FIXO);
        addExpense(ym, "Embalagens",  new BigDecimal("110.40"), ExpenseType.FIXO);
        addExpense(ym, "Val Caixas",  new BigDecimal("94.63"),  ExpenseType.FIXO);
        addExpense(ym, "Papel Arroz", new BigDecimal("15.00"),  ExpenseType.FIXO);
        // EVENTUAL — custos pontuais
        addExpense(ym, "Uber",        new BigDecimal("9.98"),   ExpenseType.EVENTUAL);
        addExpense(ym, "Uber",        new BigDecimal("34.03"),  ExpenseType.EVENTUAL);
        addExpense(ym, "Uber",        new BigDecimal("4.87"),   ExpenseType.EVENTUAL);
        addExpense(ym, "Uber",        new BigDecimal("9.50"),   ExpenseType.EVENTUAL);
        addExpense(ym, "Uber",        new BigDecimal("43.00"),  ExpenseType.EVENTUAL);
        addExpense(ym, "Almoço",      new BigDecimal("40.28"),  ExpenseType.EVENTUAL);
        addExpense(ym, "Shopee",      new BigDecimal("24.89"),  ExpenseType.EVENTUAL);
        addExpense(ym, "Caixa Shopee",new BigDecimal("105.00"), ExpenseType.EVENTUAL);
        addExpense(ym, "Curso",       new BigDecimal("47.00"),  ExpenseType.EVENTUAL);
    }

    private void addExpense(String yearMonth, String description, BigDecimal amount, ExpenseType type) {
        var e = new MonthlyExpense();
        e.setYearMonth(yearMonth);
        e.setDescription(description);
        e.setAmount(amount);
        e.setType(type);
        monthlyExpenseRepo.save(e);
    }

    private void addFaq(String q, String a, int order) {
        var faq = new Faq();
        faq.setQuestion(q);
        faq.setAnswer(a);
        faq.setDisplayOrder(order);
        faqRepo.save(faq);
    }

    private void addIngredient(String name, BigDecimal price) {
        var ing = new Ingredient();
        ing.setName(name);
        ing.setPricePerKg(price);
        ingredientRepo.save(ing);
    }

    private void addTestimonial(String name, String text, int rating) {
        var t = new Testimonial();
        t.setClientName(name);
        t.setText(text);
        t.setRating(rating);
        testimonialRepo.save(t);
    }
}
