# Referência de Arquivos e Funções — Quero Mais Doces Finos

> Projeto: Spring Boot 3.2 · Java 17 · Thymeleaf · H2 · Lombok  
> Última atualização: 2026-06-02

---

## Estrutura de Pacotes

```
com.confeitaria
├── config/          Configuração: segurança, rotas estáticas, dados iniciais
├── model/           Entidades JPA (tabelas do banco)
├── repository/      Interfaces Spring Data JPA (queries)
├── service/         Lógica de negócio
├── controller/      Endpoints HTTP (rotas)
├── web/             Contexto de marketing para as páginas públicas
└── util/            Utilitários estáticos
```

---

## config/

### `WebConfig.java`
Registra os handlers de recursos estáticos para imagens.

| Método | O que faz |
|--------|-----------|
| `addResourceHandlers(registry)` | Mapeia `/uploads/**` → `${app.upload.dir}` (padrão: `./uploads`) para imagens enviadas via formulário admin. Também mapeia `/imagens/**` → `${app.images.dir}` (padrão: `../Imagens`) para servir logo, hero e galeria da pasta `Imagens/` na raiz do projeto. |

---

### `SecurityConfig.java`
Define autenticação e regras de acesso.

| Método | O que faz |
|--------|-----------|
| `passwordEncoder()` | Bean `BCryptPasswordEncoder` usado para verificar a senha do admin. |
| `userDetailsService(encoder)` | Cria o único usuário do sistema (`admin` / `confeitaria123` por padrão) em memória, com role `ADMIN`. Credenciais configuráveis via `app.admin.username` e `app.admin.password`. |
| `filterChain(http)` | Libera `/admin/login`, `/admin/logout` e tudo público (`/`, `/galeria`, `/contato`, etc.). Protege `/admin/**` exigindo role ADMIN. Configura form login em `/admin/login`, redirect de sucesso para `/admin`, redirect de falha para `/admin/login?error=true`, logout com redirect para `/`. |

---

### `DataInitializer.java`
Semeia dados de exemplo no banco na primeira execução (guard `count() == 0`).

| Método | O que faz |
|--------|-----------|
| `run(args)` | Ponto de entrada; chama todos os `seed*()` se as tabelas estiverem vazias. |
| `seedGalleryItems()` | Insere 6 itens de galeria apontando para os arquivos `WhatsApp Image *.jpeg` da pasta `Imagens/`, servidos via `/imagens/**` (paths URL-encoded). |
| `seedSales()` | Insere 22 vendas de exemplo (setembro/2025 e março/2026) com categorias Vendas, Shopee, Indicação, Doces Finos. |
| `seedMonthlyExpenses()` | Insere 26 despesas para setembro/2025: fixas (Enel, aluguel, mão-de-obra, ovos) e eventuais (Uber, curso, reembolso Shopee). |
| `addGallery(title, desc, imagePath, order)` | Helper: cria e salva um `GalleryItem`. |
| `addSale(product, group, revenue, date, notes)` | Helper: cria e salva um `Sale` (cost = revenue × 0,37). |
| `addExpense(desc, month, amount, type)` | Helper: cria e salva uma `MonthlyExpense`. |
| `addFaq / addIngredient / addTestimonial` | Helpers análogos para suas entidades. |

---

## model/

### `Contact.java`
Formulário de pedido enviado por visitantes.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `name`, `email`, `phone` | String | Dados de contato |
| `message` | String (2000) | Mensagem do visitante |
| `source` | String | Origem declarada (instagram, google, indicação…) |
| `referralCode` | String | Código de indicação capturado da URL |
| `utmSource/Medium/Campaign/Term/Content` | String | Parâmetros UTM propagados da URL |
| `createdAt` | LocalDateTime | Preenchido automaticamente |

---

### `CostSettings.java`
Singleton (id = 1) com configuração global de custos.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `estimatedMonthlyProductionUnits` | BigDecimal | Número de unidades produzidas por mês — usado para ratear custos fixos mensais sobre cada receita |

---

### `Faq.java`
Perguntas frequentes exibidas no site público.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `question`, `answer` | String | Pergunta e resposta |
| `visible` | boolean | Controla exibição no site |
| `displayOrder` | int | Ordem de exibição |

---

### `GalleryItem.java`
Item da galeria de produtos.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `title`, `description` | String | Texto exibido sob a foto |
| `imagePath` | String | Caminho relativo, ex: `/imagens/Logo.png` ou `/uploads/<uuid>_foto.jpg` |
| `visible` | boolean | Controla exibição no site |
| `displayOrder` | int | Ordem de exibição |
| `createdAt` | LocalDateTime | Data de cadastro |

---

### `Ingredient.java`
Ingrediente usado em receitas.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `name` | String | Nome do ingrediente |
| `pricePerKg` | BigDecimal | Preço por kg/L ou por unidade |
| `unit` | String | `"kg"` · `"L"` · `"un"` — define como o preço e a quantidade são interpretados |

---

### `Recipe.java`
Receita com seus ingredientes.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `name`, `category` | String | Nome e categoria (bolo, torta, brigadeiro…) |
| `description` | String | Anotações livres |
| `yieldGrams` | BigDecimal | Peso total da batelada em gramas |
| `yieldDescription` | String | Texto livre ("1 bolo 20 cm", "30 unidades") |
| `ingredients` | `List<RecipeIngredient>` | Ingredientes com quantidade e custo |

| Método | Retorno | Fórmula |
|--------|---------|---------|
| `getIngredientCostRaw()` | BigDecimal | Σ `RecipeIngredient.totalCost` |
| `getMarginalCost()` | BigDecimal | `ingredientCostRaw × 1,05` (+5% perdas) |
| `getCostTotal()` | BigDecimal | Alias de `getMarginalCost()` |
| `getRecommendedPrice()` | BigDecimal | `marginalCost × 3,0` |
| `getCostPerGram()` | BigDecimal | `marginalCost / yieldGrams` |

---

### `RecipeIngredient.java`
Linha de ingrediente dentro de uma receita.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `recipe` | Recipe | Receita pai |
| `ingredient` | Ingredient | Ingrediente referenciado |
| `quantityGrams` | BigDecimal | Quantidade (gramas, ml ou unidades conforme `ingredient.unit`) |

| Método | Retorno | Lógica |
|--------|---------|--------|
| `getTotalCost()` | BigDecimal | Se `unit = "un"`: `quantity × pricePerKg`; se `"kg"` ou `"L"`: `(quantity / 1000) × pricePerKg` |

---

### `Sale.java`
Registro de uma venda realizada.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `productName` | String | Nome do produto vendido |
| `productGroup` | String | Categoria para agrupamento em gráficos |
| `recipe` | Recipe (nullable) | Vínculo opcional com receita |
| `cost`, `revenue` | BigDecimal | Custo total e receita total da venda |
| `quantity` | BigDecimal | Quantidade vendida (padrão 1) |
| `saleDate` | LocalDate | Data da venda |
| `notes` | String | Observações livres |

| Método | Retorno | Fórmula |
|--------|---------|---------|
| `getProfit()` | BigDecimal | `revenue - cost` (retorna ZERO se nulos) |
| `getMargin()` | BigDecimal | `(profit / revenue) × 100` |

---

### `Testimonial.java`
Depoimento de cliente exibido no site.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `clientName`, `text` | String | Nome e depoimento |
| `rating` | int | 1–5 estrelas |
| `visible` | boolean | Controla exibição |
| `createdAt` | LocalDateTime | Data de cadastro |

---

### `UtmLink.java`
Link UTM gerado para rastreamento de campanhas.

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `name` | String | Nome interno da campanha |
| `baseUrl` | String | URL de destino (ex: `https://site.com/contato`) |
| `utmSource/Medium/Campaign/Term/Content` | String | Parâmetros UTM (Term e Content são opcionais) |
| `shortDescription` | String | Anotação interna |
| `createdAt`, `lastUpdated` | Instant | Datas de criação e última edição |

---

### `MonthlyExpense.java`
Gasto mensal (fixo ou eventual).

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `yearMonth` | String (7 chars) | Mês de referência, formato `yyyy-MM` |
| `description` | String | Descrição do gasto |
| `amount` | BigDecimal | Valor em R$ |
| `type` | ExpenseType | `FIXO` (recorrente) · `EVENTUAL` (pontual) |

---

### `ExpenseType.java` (enum)
`FIXO` — custo recorrente (aluguel, luz, salário).  
`EVENTUAL` — custo pontual (reembolso, perda, curso).

---

## repository/

Todas as interfaces estendem `JpaRepository<Entidade, Long>` e herdam automaticamente `save`, `findById`, `findAll`, `delete`, etc.

| Interface | Métodos customizados |
|-----------|---------------------|
| `ContactRepository` | `findAllByOrderByCreatedAtDesc()` · `countBySource()` (source + count) · `countByReferralCode()` (code + count) |
| `CostSettingsRepository` | — (só CRUD padrão) |
| `FaqRepository` | `findByVisibleTrueOrderByDisplayOrderAsc()` |
| `GalleryItemRepository` | `findByVisibleTrueOrderByDisplayOrderAsc()` |
| `IngredientRepository` | `findAllByOrderByNameAsc()` |
| `RecipeRepository` | `findAllByOrderByNameAsc()` · `findByCategoryOrderByNameAsc(category)` |
| `RecipeIngredientRepository` | — (só CRUD padrão) |
| `ReferralLinkRepository` | `findByCode(code)` → `Optional<ReferralLink>` |
| `SaleRepository` | `findAllByOrderBySaleDateDesc()` · `findBySaleDateBetweenOrderBySaleDateAsc(start, end)` · `groupSummaryByPeriod(start, end)` · `productSummaryByPeriod(start, end)` · `dailySummaryByPeriod(start, end)` |
| `TestimonialRepository` | `findByVisibleTrueOrderByCreatedAtDesc()` |
| `UtmLinkRepository` | `findAllByOrderByCreatedAtDesc()` |
| `MonthlyExpenseRepository` | `findByYearMonthOrderByIdDesc(ym)` · `sumAmountByYearMonth(ym)` → BigDecimal |

---

## service/

### `RecipeService.java`
Lógica financeira de custos de receita.

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `computeFixedAllocationPerUnit(yearMonth)` | BigDecimal | Busca soma dos gastos FIXO do mês e divide pelas unidades estimadas (`CostSettings`). Retorna ZERO se não houver dados. Usado para calcular quanto de custo fixo cabe em cada receita. |
| `parseYearMonth(mes)` | YearMonth | Converte string `"yyyy-MM"` para `YearMonth`. Retorna `YearMonth.now()` se inválido (e loga aviso). |
| `getMonthlyFixed(yearMonth)` | BigDecimal | Soma todas as `MonthlyExpense` do mês (qualquer tipo). |
| `getEstimatedUnits()` | BigDecimal | Lê `CostSettings.estimatedMonthlyProductionUnits`. |

---

### `MonthlyExpenseService.java`
Helpers para o controller de gastos mensais.

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `parseMonthOrNow(mes)` | YearMonth | Delega para `RecipeService.parseYearMonth`. |
| `blankExpense(ymStr)` | MonthlyExpense | Cria `MonthlyExpense` em branco com `yearMonth` preenchido e tipo `FIXO`. |
| `defaultCostSettings()` | CostSettings | Cria `CostSettings` com id=1 (singleton) e 100 unidades padrão. |

---

### `ImageUploadService.java`
Salvamento de imagens enviadas pelo admin.

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `save(file)` | String | Cria o diretório `uploadDir` se não existir. Gera nome único com `UUID`. Copia o arquivo. Retorna o caminho relativo `/uploads/<uuid>_<nome-original>`. Loga sucesso ou erro. |

---

### `FinanceAnalyticsService.java`
Geração de relatório financeiro para o dashboard.

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `buildReport(inicio, fim, produto, grupo)` | `FinanceReport` | Filtra vendas por período, nome de produto (contains) e grupo. Calcula totais (receita, custo, lucro, margem, quantidade). Agrupa por produto, por dia e por produto+mês. Retorna rankings: top/bottom lucro, melhor/pior margem, mais populares. |

**Classes internas (resultado do `buildReport`):**

| Classe | Campos principais |
|--------|------------------|
| `FinanceReport` | `totalRevenue`, `totalCost`, `totalProfit`, `marginPercent`, `saleCount`, `productMetrics`, `dailyPoints`, `productMonthPoints`, `topByProfit`, `leastByProfit`, `bestMargin`, `worstMargin`, `mostPopular` |
| `ProductMetric` | `productName`, `productGroup`, `revenue`, `cost`, `profit`, `quantity`, `marginPercent` |
| `DailyPoint` | `date` (LocalDate), `revenue`, `cost`, `profit` |
| `ProductMonthPoint` | `label` ("yyyy-MM \| Produto"), `revenue`, `cost`, `profit`, `quantity` |

---

## controller/

### `PublicController.java`
Rotas do site público.

| Rota | Método | O que faz |
|------|--------|-----------|
| `GET /` | `index()` | Enriquece model com `PublicMarketingContext`. Se `?ref=CODE`, incrementa `referralLink.visits`. Renderiza `public/index`. |
| `GET /galeria` | `galeria()` | Passa gallery items visíveis. Renderiza `public/galeria`. |
| `GET /depoimentos` | `depoimentos()` | Passa testimonials visíveis. Renderiza `public/depoimentos`. |
| `GET /perguntas-frequentes` | `faq()` | Passa FAQs visíveis ordenadas. Renderiza `public/faq`. |
| `GET /contato` | `contato()` | Passa `Contact` vazio para o form. Renderiza `public/contato`. |
| `POST /contato` | `submitContato()` | Salva `Contact` com UTM/ref da sessão. Incrementa `referralLink.conversions`. Redireciona para `https://w.app/68oeeg` (WhatsApp da confeitaria). |

---

### `AdminController.java`
Login, dashboard e contatos.

| Rota | Método | O que faz |
|------|--------|-----------|
| `GET /admin/login` | `login()` | Renderiza `admin/login`. |
| `GET /admin` | `dashboard()` | Passa contagens, últimos 5 contatos, contagens por fonte e por código de indicação. Renderiza `admin/dashboard`. |
| `GET /admin/contatos` | `contatos()` | Lista todos os contatos ordenados por data desc. Renderiza `admin/contatos`. |
| `POST /admin/contatos/delete/{id}` | `deleteContato()` | Delete e redireciona. |
| `POST /admin/referral/add` | `addReferral()` | Cria `ReferralLink` com code em maiúsculas. |
| `POST /admin/referral/delete/{id}` | `deleteReferral()` | Delete e redireciona. |

---

### `ContentAdminController.java`
Gerencia conteúdo do site: galeria, depoimentos e FAQs.

| Rota | Método | O que faz |
|------|--------|-----------|
| `GET /admin/galeria` | `galeria()` | Lista todos os GalleryItems. |
| `POST /admin/galeria/add` | `addGallery()` | Recebe form + arquivo (`MultipartFile`). Salva imagem via `ImageUploadService`. Cria `GalleryItem`. |
| `POST /admin/galeria/delete/{id}` | `deleteGallery()` | Delete. |
| `POST /admin/galeria/toggle/{id}` | `toggleGallery()` | Inverte `visible`. |
| `GET /admin/depoimentos` | `depoimentos()` | Lista todos os Testimonials. |
| `POST /admin/depoimentos/add` | `addTestimonial()` | Cria Testimonial. |
| `POST /admin/depoimentos/delete/{id}` | `deleteTestimonial()` | Delete. |
| `POST /admin/depoimentos/toggle/{id}` | `toggleTestimonial()` | Inverte `visible`. |
| `GET /admin/faqs` | `faqs()` | Lista todos os FAQs. |
| `POST /admin/faqs/add` | `addFaq()` | Cria FAQ. |
| `POST /admin/faqs/delete/{id}` | `deleteFaq()` | Delete. |

---

### `RecipeController.java`
Gerencia ingredientes e receitas.

| Rota | Método | O que faz |
|------|--------|-----------|
| `GET /admin/ingredientes` | `ingredientes()` | Lista ingredientes ordenados por nome. |
| `POST /admin/ingredientes/add` | `addIngredient()` | Cria `Ingredient`. |
| `POST /admin/ingredientes/delete/{id}` | `deleteIngredient()` | Delete. |
| `POST /admin/ingredientes/edit/{id}` | `editIngredient()` | Atualiza `name`, `pricePerKg` e `unit`. |
| `GET /admin/receitas` | `receitas()` | Lista receitas com custo de produção e preço sugerido por unidade. Chama `RecipeService.computeFixedAllocationPerUnit` para o mês atual. |
| `GET /admin/receitas/{id}` | `recipeDetail()` | Carrega receita completa. Calcula: `fullProductionCost`, `recommendedSalePrice`, `costPerGramProduction`. Suporta navegação por mês (`?mes=yyyy-MM`). |
| `GET /admin/receitas/{id}/custo-json` | `recipeCustoJson()` | Retorna JSON `{name, cost, recommendedPrice}`. `cost` = custo marginal + rateio fixo do mês atual; `recommendedPrice` = `cost × 3`. Endpoint usado pelo formulário de vendas para auto-preencher campos. |
| `POST /admin/receitas/add` | `addRecipe()` | Cria receita e redireciona para detalhe. |
| `POST /admin/receitas/{id}/addIngredient` | `addRecipeIngredient()` | Cria `RecipeIngredient` com o ingrediente e quantidade informados. |
| `POST /admin/receitas/{id}/removeIngredient/{riId}` | `removeRecipeIngredient()` | Delete o RecipeIngredient. |
| `POST /admin/receitas/delete/{id}` | `deleteRecipe()` | Delete receita e ingredientes (cascade). |

---

### `SaleController.java`
Registro e visualização de vendas.

| Rota | Método | O que faz |
|------|--------|-----------|
| `GET /admin/vendas` | `vendas()` | Filtra vendas por período (`inicio`/`fim`, default: mês atual). Calcula totais, gera arrays para gráficos de linha (diário) e barra (por produto/grupo). Passa todas as vendas e a lista de receitas para o formulário. |
| `POST /admin/vendas/add` | `addSale()` | Salva `Sale`. Se `recipeId` fornecido, associa receita. Se sem data, usa hoje. |
| `POST /admin/vendas/delete/{id}` | `deleteSale()` | Delete. |

**Comportamento do formulário:** o select "Vincular à receita" aparece antes dos campos Custo e Preço. Ao selecionar uma receita, o JS faz `fetch('/admin/receitas/{id}/custo-json')` e preenche os dois campos automaticamente. Os campos ficam editáveis e exibem o texto "Sugerido pela receita" enquanto houver receita selecionada.

---

### `FinancialDashboardController.java`
Dashboard de análise financeira avançada.

| Rota | Método | O que faz |
|------|--------|-----------|
| `GET /admin/financeiro` | `financeiro()` | Aceita `inicio`, `fim`, `produto`, `grupo` como query params. Chama `FinanceAnalyticsService.buildReport()`. Serializa listas de dados para JSON (`dailyJson`, `productJson`, `productMonthJson`) para os gráficos Chart.js. Passa grupos únicos para o dropdown de filtro. |

---

### `MonthlyExpenseController.java`
Gerencia gastos mensais.

| Rota | Método | O que faz |
|------|--------|-----------|
| `GET /admin/gastos-mensais` | `list()` | Busca despesas do mês (`?mes=yyyy-MM`, default: mês atual). Calcula `fixedTotal`, `eventualTotal`, `monthTotal`, `fixedPerProductionUnit`. Passa `CostSettings` para edição. |
| `POST /admin/gastos-mensais/add` | `add()` | Cria `MonthlyExpense`. Define `yearMonth` a partir do param ou do form. |
| `POST /admin/gastos-mensais/delete/{id}` | `delete()` | Delete. Mantém param `?mes` na URL. |
| `POST /admin/gastos-mensais/settings` | `saveSettings()` | Atualiza `estimatedMonthlyProductionUnits` (valida > 0). |

---

### `UtmLinkAdminController.java`
Gerencia links UTM para rastreamento de campanhas.

| Rota | Método | O que faz |
|------|--------|-----------|
| `GET /admin/links-utm` | `list()` | Lista todos os links ordenados por data de criação desc. |
| `GET /admin/links-utm/novo` | `novo()` | Exibe form vazio. |
| `POST /admin/links-utm/novo` | `criar()` | Salva novo `UtmLink` com `createdAt = now`. |
| `GET /admin/links-utm/editar/{id}` | `editar()` | Exibe form preenchido. |
| `POST /admin/links-utm/editar/{id}` | `atualizar()` | Atualiza campos e `lastUpdated = now`. |
| `POST /admin/links-utm/excluir/{id}` | `excluir()` | Delete. |
| `GET /admin/links-utm/visualizar/{id}` | `visualizar()` | Gera `fullUrl` via `UtmLinkUrlBuilder` e exibe. |

---

## web/

### `PublicMarketingService.java`
Propaga parâmetros UTM e código de referral através das páginas públicas via sessão HTTP.

| Método | O que faz |
|--------|-----------|
| `enrichPublicModel(model, request, session, ref)` | Sincroniza parâmetros UTM da URL atual para a sessão. Armazena `ref` em sessão (uppercase). Cria `PublicMarketingContext` e injeta no model como `"marketing"`. |
| `applyStoredMarketingToContact(contact, session)` | Preenche os campos UTM e `referralCode` do `Contact` com os valores armazenados na sessão. Chamado ao salvar o formulário de contato. |
| `redirectWithMarketing(path, session, extraParams)` | Constrói URL de redirect com parâmetros de marketing da sessão + parâmetros extras. |

**Chaves de sessão usadas:** `marketing.ref`, `marketing.utm_source`, `marketing.utm_medium`, `marketing.utm_campaign`, `marketing.utm_term`, `marketing.utm_content`.

---

### `PublicMarketingContext.java`
Snapshot imutável dos parâmetros de marketing da sessão, disponível nos templates como `${marketing}`.

| Método | O que faz |
|--------|-----------|
| `fromSession(session)` | Factory: cria instância a partir da sessão atual. |
| `url(path)` | Retorna o caminho com query params de marketing anexados. Ex: `/contato?ref=INSTAGRAM&utm_source=ig`. Usado nos templates: `${marketing.url('/contato')}`. |
| `appendQueryParams(builder)` | Adiciona os params ao `UriComponentsBuilder`. Uso interno. |
| `hasAnyUtm()` | Retorna `true` se pelo menos um parâmetro UTM estiver ativo. |

---

## util/

### `UtmLinkUrlBuilder.java`
Utilitário estático para gerar a URL completa de um `UtmLink`.

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `generateFullUrl(utmLink)` | String | Concatena `baseUrl` com os query params `?utm_source=...&utm_medium=...&utm_campaign=...`. Inclui `utm_term` e `utm_content` apenas se não estiverem em branco. |

---

## Templates HTML

### Públicos (`templates/public/`)

| Arquivo | Fragmentos / Seções | Dados do model |
|---------|---------------------|----------------|
| `fragments.html` | `publicHead(pageTitle)` · `publicNav` · `publicFooter` · `publicScripts` | `marketing` (PublicMarketingContext) · `navActive` |
| `index.html` | Hero com background CSS, botões Fazer Pedido / Ver Galeria, banner de referral, teaser de seções | `marketing` · `ref` |
| `contato.html` | Formulário de pedido com nome/email/telefone/origem/mensagem, alerta de sucesso, banner ref | `contact` · `ref` |
| `galeria.html` | Grid de fotos com título e descrição, CTA de pedido | `gallery` (List\<GalleryItem\>) · `ref` |
| `depoimentos.html` | Cards com nome, texto e estrelas | `testimonials` (List\<Testimonial\>) · `ref` |
| `faq.html` | Acordeão expansível de perguntas | `faqs` (List\<Faq\>) · `ref` |

---

### Admin (`templates/admin/`)

| Arquivo | O que exibe | Dados principais do model |
|---------|-------------|--------------------------|
| `dashboard.html` | Totais gerais + últimos contatos + contagens por fonte e indicação | `totalContacts/Sales/Recipes` · `recentContacts` · `sourceCounts` · `referralLinks` |
| `contatos.html` | Tabela completa de contatos com UTM e origem | `contacts` · `sourceCounts` |
| `galeria.html` | Grid de itens com toggle visível/oculto + form upload | `galleryItems` |
| `depoimentos.html` | Tabela de depoimentos + toggle visível | `testimonials` |
| `faqs.html` | Tabela de FAQs + toggle visível | `faqs` |
| `ingredientes.html` | Tabela de ingredientes com edição inline de preço e unidade | `ingredients` |
| `receitas.html` | Form criar receita + tabela com custo de produção e preço sugerido | `recipes` · `fixedAllocationPerUnit` |
| `receita-detalhe.html` | Ingredientes da receita + calculadora de custo completa + navegação por mês | `recipe` · `referenceMonth` · `fixedAllocationPerUnit` · `fullProductionCost` · `recommendedSalePrice` |
| `gastos-mensais.html` | Despesas do mês + totais + rateio por unidade + form unidades | `expenses` · `fixedTotal` · `eventualTotal` · `fixedPerProductionUnit` · `costSettings` |
| `vendas.html` | Totais do período + gráficos (linha diária, barras produto/grupo) + tabela vendas + form nova venda (com select de receita antes de custo/valor; JS auto-preenche ao selecionar receita) | `filteredSales` · `totalRevenue/Cost/Profit` · `chartLabels/Costs/Revenues/Profits` · `recipes` |
| `financeiro.html` | Filtros avançados + stat cards + gráfico principal interativo Chart.js + rankings de produtos | `report` (FinanceReport) · `dailyJson` · `productJson` · `productMonthJson` |
| `utm-links.html` | Tabela de links UTM + ações | `utmLinks` |
| `utm-link-form.html` | Formulário criar/editar link UTM | `utmLink` · `editMode` |
| `utm-link-detalhe.html` | URL completa gerada com botão copiar | `utmLink` · `fullUrl` |

---

## CSS

### `static/css/style.css` — Site público
- **Tema:** Pastel Pink & Baby Blue com tipografia Playfair Display + Nunito
- **Variáveis:** `--pink-100` a `--pink-500`, `--blue-100` a `--blue-500`, `--cream`, `--text-dark/mid/light`
- **Principais classes:** `.navbar-public` · `.hero` · `.btn-primary/secondary/blue` · `.gallery-grid` · `.testimonial-card` · `.faq-item` · `.footer-public` · `.bg-calazans-strip` (banner de seção)

### `static/css/admin.css` — Painel admin
- **Layout:** `body { display: flex }` + sidebar fixed (240px) + `.main-content { margin-left: 240px }`
- **Sidebar:** gradiente `#f6e8de` → `#E8F7FF`, borda direita `#e8d5cc`
- **Principais classes:** `.sidebar` · `.sidebar-logo` · `.sidebar-nav` · `.topbar` · `.stats-grid` · `.stat-card` · `.table-card` · `.admin-form` · `.chart-card` · `.cost-highlight` · `.period-filter` · `.badge-pink/blue/green/gray`

---

## application.properties

```properties
# Banco de dados H2 persistido em arquivo
spring.datasource.url=jdbc:h2:file:./data/confeitaria

# Porta padrão: 8080
# Console H2: http://localhost:8080/h2-console

# Diretório de uploads (novos arquivos enviados via admin)
app.upload.dir=./uploads

# Pasta de imagens estáticas (logo, hero, galeria) — relativo ao diretório de trabalho (confeitaria/)
# Na VM, altere para o caminho absoluto ou relativo correto
app.images.dir=../Imagens

# Credenciais admin (mudar em produção)
app.admin.username=admin
app.admin.password=confeitaria123

# Thymeleaf sem cache (templates recarregados a cada request)
spring.thymeleaf.cache=false

# Tamanho máximo de arquivo enviado
spring.servlet.multipart.max-file-size=10MB
```
