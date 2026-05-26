package com.confeitaria.config;

import com.confeitaria.model.*;
import com.confeitaria.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private final FaqRepository faqRepo;
    private final ReferralLinkRepository referralRepo;
    private final IngredientRepository ingredientRepo;
    private final TestimonialRepository testimonialRepo;
    private final CostSettingsRepository costSettingsRepo;
    private final UtmLinkRepository utmLinkRepo;

    public DataInitializer(FaqRepository faqRepo, ReferralLinkRepository referralRepo,
                           IngredientRepository ingredientRepo, TestimonialRepository testimonialRepo,
                           CostSettingsRepository costSettingsRepo, UtmLinkRepository utmLinkRepo) {
        this.faqRepo = faqRepo;
        this.referralRepo = referralRepo;
        this.ingredientRepo = ingredientRepo;
        this.testimonialRepo = testimonialRepo;
        this.costSettingsRepo = costSettingsRepo;
        this.utmLinkRepo = utmLinkRepo;
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
