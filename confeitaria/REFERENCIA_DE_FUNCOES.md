# Referência de Funções — Confeitaria

Documentação técnica de todas as funções principais do sistema. Organizada por camada: controllers, services, modelos e utilitários.

---

## Índice

1. [Controllers](#1-controllers)
   - [PublicController](#11-publiccontroller)
   - [AdminController](#12-admincontroller)
   - [ContentAdminController](#13-contentadmincontroller)
   - [RecipeController](#14-recipecontroller)
   - [SaleController](#15-salecontroller)
   - [FinancialDashboardController](#16-financialdashboardcontroller)
   - [MonthlyExpenseController](#17-monthlyexpensecontroller)
   - [UtmLinkAdminController](#18-utmlinkedmincontroller)
2. [Services](#2-services)
   - [RecipeService](#21-recipeservice)
   - [MonthlyExpenseService](#22-monthlyexpenseservice)
   - [ImageUploadService](#23-imageuploadservice)
   - [FinanceAnalyticsService](#24-financeanalyticsservice)
   - [PublicMarketingService](#25-publicmarketingservice)
3. [Modelos (Entidades JPA)](#3-modelos-entidades-jpa)
   - [Recipe](#31-recipe)
   - [RecipeIngredient](#32-recipeingredient)
   - [Ingredient](#33-ingredient)
   - [Sale](#34-sale)
   - [Contact](#35-contact)
   - [MonthlyExpense](#36-monthlyexpense)
   - [CostSettings](#37-costsettings)
   - [GalleryItem](#38-galleryitem)
   - [Testimonial](#39-testimonial)
   - [Faq](#310-faq)
   - [ReferralLink](#311-referrallink)
   - [UtmLink](#312-utmlink)
4. [Utilitários](#4-utilitários)
   - [UtmLinkUrlBuilder](#41-utmlinkulrbuilder)
   - [PublicMarketingContext](#42-publicmarketingcontext)

---

## 1. Controllers

### 1.1 PublicController

**Pacote:** `com.confeitaria.controller`  
**Prefixo de rotas:** `/` (raiz pública, sem autenticação)  
**Dependências:** `GalleryItemRepository`, `TestimonialRepository`, `FaqRepository`, `ContactRepository`, `ReferralLinkRepository`, `PublicMarketingService`

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/` | Exibe a página inicial. Se o parâmetro `?ref=` estiver presente, busca o `ReferralLink` correspondente e incrementa o contador de visitas. Chama `enrichPublicModel` para sincronizar parâmetros UTM na sessão. |
| `GET` | `/galeria` | Lista itens de galeria com `visible = true`, ordenados por `displayOrder`. Enriquece o model com contexto de marketing. |
| `GET` | `/depoimentos` | Lista depoimentos com `visible = true`, ordenados por data de criação decrescente. |
| `GET` | `/perguntas-frequentes` ou `/faq` | Lista FAQs com `visible = true`, ordenadas por `displayOrder`. |
| `GET` | `/contato` | Exibe o formulário de contato com um objeto `Contact` vazio no model. |
| `POST` | `/contato` | Salva o `Contact` no banco. Aplica parâmetros UTM armazenados na sessão. Se houver código `ref` (via parâmetro ou sessão), associa ao contato e incrementa `conversions` no `ReferralLink`. Redireciona para `/contato?sucesso=true` preservando parâmetros de marketing. |

---

### 1.2 AdminController

**Pacote:** `com.confeitaria.controller`  
**Prefixo de rotas:** `/admin`  
**Autenticação:** obrigatória (Spring Security)  
**Dependências:** `ContactRepository`, `ReferralLinkRepository`, `SaleRepository`, `RecipeRepository`

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/admin/login` | Retorna a view de login (`admin/login`). |
| `GET` | `/admin` | Dashboard principal. Conta totais de contatos, vendas e receitas. Lista os 5 contatos mais recentes, distribuição de origens, contagem por código referral e todos os links de referência. |
| `GET` | `/admin/contatos` | Lista todos os contatos em ordem decrescente de criação, com totalizações por origem e por código referral. |
| `POST` | `/admin/contatos/delete/{id}` | Exclui o contato pelo ID e redireciona para a lista. |
| `POST` | `/admin/referral/add` | Cria um novo `ReferralLink` com o `code` convertido para maiúsculas. Redireciona para o dashboard. |
| `POST` | `/admin/referral/delete/{id}` | Exclui o link de referência pelo ID e redireciona para o dashboard. |

---

### 1.3 ContentAdminController

**Pacote:** `com.confeitaria.controller`  
**Prefixo de rotas:** `/admin`  
**Autenticação:** obrigatória  
**Dependências:** `GalleryItemRepository`, `TestimonialRepository`, `FaqRepository`, `ImageUploadService`

Responsável pelo conteúdo editorial exibido no site público.

#### Galeria

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/admin/galeria` | Lista todos os itens da galeria (inclusive os invisíveis) e um objeto `GalleryItem` vazio para o formulário de criação. |
| `POST` | `/admin/galeria/add` | Salva um novo `GalleryItem`. Se um arquivo de imagem for enviado, chama `ImageUploadService.save` e armazena o caminho em `imagePath`. |
| `POST` | `/admin/galeria/delete/{id}` | Exclui o item de galeria pelo ID. |
| `POST` | `/admin/galeria/toggle/{id}` | Inverte o campo `visible` do item (ativa/desativa a exibição no site público). |

#### Depoimentos

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/admin/depoimentos` | Lista todos os depoimentos e um objeto `Testimonial` vazio. |
| `POST` | `/admin/depoimentos/add` | Salva um novo `Testimonial`. |
| `POST` | `/admin/depoimentos/delete/{id}` | Exclui o depoimento pelo ID. |
| `POST` | `/admin/depoimentos/toggle/{id}` | Inverte o campo `visible` do depoimento. |

#### FAQs

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/admin/faqs` | Lista todas as FAQs e um objeto `Faq` vazio. |
| `POST` | `/admin/faqs/add` | Salva uma nova `Faq`. |
| `POST` | `/admin/faqs/delete/{id}` | Exclui a FAQ pelo ID. |

---

### 1.4 RecipeController

**Pacote:** `com.confeitaria.controller`  
**Prefixo de rotas:** `/admin`  
**Dependências:** `RecipeRepository`, `IngredientRepository`, `RecipeIngredientRepository`, `RecipeService`

#### Ingredientes

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/admin/ingredientes` | Lista todos os ingredientes em ordem alfabética com um objeto `Ingredient` vazio. |
| `POST` | `/admin/ingredientes/add` | Salva um novo `Ingredient`. |
| `POST` | `/admin/ingredientes/delete/{id}` | Exclui o ingrediente pelo ID. |
| `POST` | `/admin/ingredientes/edit/{id}` | Atualiza o campo `pricePerKg` de um ingrediente existente. |

#### Receitas

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/admin/receitas` | Lista todas as receitas em ordem alfabética. Calcula `fixedAllocationPerUnit` para o mês atual via `RecipeService`. |
| `GET` | `/admin/receitas/{id}` | Detalhe de uma receita. Aceita `?mes=yyyy-MM` para escolher o mês de referência dos gastos fixos. Calcula `fullProductionCost` (custo marginal + rateio fixo), `recommendedSalePrice` e `costPerGramProduction`. |
| `POST` | `/admin/receitas/add` | Cria uma nova `Recipe` com lista de ingredientes vazia e redireciona para seu detalhe. |
| `POST` | `/admin/receitas/{id}/addIngredient` | Adiciona um `RecipeIngredient` (ingrediente + quantidade em gramas) a uma receita existente. |
| `POST` | `/admin/receitas/{id}/removeIngredient/{riId}` | Remove um `RecipeIngredient` pelo ID da associação. |
| `POST` | `/admin/receitas/delete/{id}` | Exclui a receita e todos os seus `RecipeIngredient` (cascade). |

---

### 1.5 SaleController

**Pacote:** `com.confeitaria.controller`  
**Prefixo de rotas:** `/admin`  
**Dependências:** `SaleRepository`, `RecipeRepository`

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/admin/vendas` | Lista vendas com filtro por período (`?inicio=` e `?fim=`, padrão: mês atual). Calcula totais (custo, receita, lucro), agrega por dia para gráfico, e gera resumos por produto e por grupo de produtos. |
| `POST` | `/admin/vendas/add` | Registra uma nova venda. Se `recipeId` for fornecido, vincula a receita. Se `saleDate` estiver vazio, usa a data atual. |
| `POST` | `/admin/vendas/delete/{id}` | Exclui a venda pelo ID. |

---

### 1.6 FinancialDashboardController

**Pacote:** `com.confeitaria.controller`  
**Prefixo de rotas:** `/admin`  
**Dependências:** `FinanceAnalyticsService`, `SaleRepository`, `ObjectMapper`

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/admin/financeiro` | Dashboard financeiro avançado. Aceita `?inicio=`, `?fim=`, `?produto=` e `?grupo=`. Delega a análise ao `FinanceAnalyticsService` e serializa os dados em JSON para os gráficos: `dailyJson`, `productJson`, `productMonthJson`. Expõe também listas de grupos disponíveis e o objeto `report` completo. |

---

### 1.7 MonthlyExpenseController

**Pacote:** `com.confeitaria.controller`  
**Prefixo de rotas:** `/admin/gastos-mensais`  
**Dependências:** `MonthlyExpenseRepository`, `CostSettingsRepository`, `MonthlyExpenseService`

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/admin/gastos-mensais` | Lista gastos do mês indicado por `?mes=yyyy-MM` (padrão: mês atual). Calcula `fixedTotal` (só `FIXO`), `eventualTotal` (só `EVENTUAL`) e `monthTotal` (soma dos dois). O rateio por unidade usa apenas `fixedTotal`. |
| `POST` | `/admin/gastos-mensais/add` | Salva um novo `MonthlyExpense` com o `type` vindo do formulário (`FIXO` ou `EVENTUAL`). Se `yearMonth` estiver vazio no form, usa o mês do parâmetro `?mes=`. |
| `POST` | `/admin/gastos-mensais/delete/{id}` | Exclui o gasto pelo ID e redireciona para o mesmo mês. |
| `POST` | `/admin/gastos-mensais/settings` | Atualiza o campo `estimatedMonthlyProductionUnits` de `CostSettings` (singleton ID = 1). Ignora se o valor for nulo ou ≤ 0. |

**Atributos passados ao model:**

| Atributo | Tipo | Descrição |
|----------|------|-----------|
| `fixedTotal` | `BigDecimal` | Soma dos gastos do tipo `FIXO` no mês |
| `eventualTotal` | `BigDecimal` | Soma dos gastos do tipo `EVENTUAL` no mês |
| `monthTotal` | `BigDecimal` | Total geral (`fixedTotal + eventualTotal`) |
| `fixedPerProductionUnit` | `BigDecimal` | `fixedTotal ÷ estimatedMonthlyProductionUnits` (rateio) |
| `expenseTypes` | `ExpenseType[]` | Array `[FIXO, EVENTUAL]` para popular o select do formulário |

---

### 1.8 UtmLinkAdminController

**Pacote:** `com.confeitaria.controller`  
**Prefixo de rotas:** `/admin/links-utm`  
**Dependências:** `UtmLinkRepository`

| Método | Rota | O que faz |
|--------|------|-----------|
| `GET` | `/admin/links-utm` | Lista todos os links UTM ordenados por data de criação decrescente. |
| `GET` | `/admin/links-utm/novo` | Exibe formulário de criação com um `UtmLink` vazio. |
| `POST` | `/admin/links-utm/novo` | Salva um novo `UtmLink` com `createdAt = Instant.now()`. |
| `GET` | `/admin/links-utm/editar/{id}` | Exibe formulário de edição com os dados do link existente. |
| `POST` | `/admin/links-utm/editar/{id}` | Atualiza todos os campos do link e define `lastUpdated = Instant.now()`. |
| `POST` | `/admin/links-utm/excluir/{id}` | Exclui o link UTM pelo ID. |
| `GET` | `/admin/links-utm/visualizar/{id}` | Exibe a URL completa gerada pelo `UtmLinkUrlBuilder.generateFullUrl`. |

---

## 2. Services

### 2.1 RecipeService

**Pacote:** `com.confeitaria.service`  
**Dependências:** `MonthlyExpenseRepository`, `CostSettingsRepository`

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `computeFixedAllocationPerUnit(String yearMonth)` | `BigDecimal` | Soma todos os gastos do mês (`sumAmountByYearMonth`) e divide pela quantidade de unidades estimadas em `CostSettings`. Retorna `BigDecimal.ZERO` se não houver configuração ou se `units ≤ 0`. Escala: 4 casas decimais, arredondamento HALF_UP. |
| `parseYearMonth(String mes)` | `YearMonth` | Converte `"yyyy-MM"` em `YearMonth`. Retorna `YearMonth.now()` se o parâmetro for nulo, vazio ou inválido (log de warn). |
| `getMonthlyFixed(String yearMonth)` | `BigDecimal` | Retorna a soma total dos gastos mensais do mês indicado, diretamente do repositório. |
| `getEstimatedUnits()` | `BigDecimal` | Retorna `estimatedMonthlyProductionUnits` de `CostSettings` (singleton ID = 1). Retorna `BigDecimal.ZERO` se não configurado. |

---

### 2.2 MonthlyExpenseService

**Pacote:** `com.confeitaria.service`  
**Dependências:** `RecipeService`

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `parseMonthOrNow(String mes)` | `YearMonth` | Delega para `RecipeService.parseYearMonth`. Centraliza o parsing de mês em uma única implementação. |
| `blankExpense(String ymStr)` | `MonthlyExpense` | Cria um `MonthlyExpense` em branco com `yearMonth` preenchido, `amount = 0` e `type = FIXO`. Usado para pré-popular o formulário de adição. |
| `defaultCostSettings()` | `CostSettings` | Cria um `CostSettings` padrão com `id = SINGLETON_ID` e sem unidades configuradas. Usado como fallback no `orElseGet`. |

---

### 2.3 ImageUploadService

**Pacote:** `com.confeitaria.service`  
**Configuração:** `app.upload.dir` (default: `./uploads`)

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `save(MultipartFile file)` | `String` | Cria o diretório de uploads se não existir. Gera um nome único com `UUID + "_" + originalFilename`. Copia o arquivo para disco com `REPLACE_EXISTING`. Retorna o caminho público (`/uploads/<filename>`). Lança `IOException` em falha. |

---

### 2.4 FinanceAnalyticsService

**Pacote:** `com.confeitaria.service`  
**Dependências:** `SaleRepository`

#### Método principal

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `buildReport(LocalDate inicio, LocalDate fim, String produtoFiltro, String grupoFiltro)` | `FinanceReport` | Consulta vendas no período, aplica filtros por nome de produto (contém, case-insensitive) e grupo exato. Calcula totais, agrupa por produto, por dia e por produto-mês. Gera rankings. |

#### Classes de resultado (públicas, imutáveis com `@Getter`)

**`FinanceReport`** — relatório completo retornado por `buildReport`:

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `inicio`, `fim` | `LocalDate` | Período filtrado |
| `produtoFiltro`, `grupoFiltro` | `String` | Filtros aplicados |
| `saleCount` | `int` | Total de vendas no período filtrado |
| `totalCost`, `totalRevenue`, `totalProfit`, `totalQuantity` | `BigDecimal` | Totais agregados |
| `marginPercent` | `BigDecimal` | Margem total em % = `(lucro / receita) × 100` |
| `productMetrics` | `List<ProductMetric>` | Métricas por produto, ordenadas por lucro decrescente |
| `dailyPoints` | `List<DailyPoint>` | Dados agregados por dia |
| `productMonthPoints` | `List<ProductMonthPoint>` | Dados por combinação produto+mês |
| `topByProfit` | `List<ProductMetric>` | Top 10 por lucro (maior → menor) |
| `leastByProfit` | `List<ProductMetric>` | 10 piores por lucro |
| `bestMargin` | `List<ProductMetric>` | Top 10 melhores margens |
| `worstMargin` | `List<ProductMetric>` | 10 piores margens |
| `mostPopular` | `List<ProductMetric>` | Top 10 por quantidade vendida |

**`ProductMetric`** — métricas de um produto individual:

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `productName`, `productGroup` | `String` | Identificação do produto |
| `revenue`, `cost`, `profit`, `quantity` | `BigDecimal` | Totais do produto |
| `marginPercent` | `BigDecimal` | Margem em % |

**`DailyPoint`** — agregação diária:

| Campo | Tipo |
|-------|------|
| `date` | `LocalDate` |
| `revenue`, `cost`, `profit` | `BigDecimal` |

**`ProductMonthPoint`** — combinação produto + mês:

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `label` | `String` | Formato `"yyyy-MM | nome_produto"` |
| `revenue`, `cost`, `profit`, `quantity` | `BigDecimal` | Totais |

---

### 2.5 PublicMarketingService

**Pacote:** `com.confeitaria.web`  
**Constantes de sessão:** `SK_REF`, `SK_UTM_SOURCE`, `SK_UTM_MEDIUM`, `SK_UTM_CAMPAIGN`, `SK_UTM_TERM`, `SK_UTM_CONTENT`

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `enrichPublicModel(Model, HttpServletRequest, HttpSession, String refParam)` | `void` | Sincroniza parâmetros UTM da requisição para a sessão. Se `refParam` não for nulo/vazio, armazena na sessão. Cria um `PublicMarketingContext` a partir da sessão e adiciona ao model como `"marketing"` e `"ref"`. |
| `applyStoredMarketingToContact(Contact, HttpSession)` | `void` | Lê todos os atributos UTM da sessão e os aplica ao `Contact` antes de salvar. |
| `redirectWithMarketing(String path, HttpSession, Map<String,String> extraQueryParams)` | `String` | Constrói uma string de redirect (`"redirect:/path?..."`) adicionando os parâmetros extras e todos os parâmetros de marketing armazenados na sessão. |

---

## 3. Modelos (Entidades JPA)

### 3.1 Recipe

**Tabela:** `recipes`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Chave primária, gerada automaticamente |
| `name` | `String` | Nome da receita |
| `category` | `String` | Categoria (bolo, torta, doce, etc.) |
| `description` | `String` | Descrição longa (até 2000 chars) |
| `yieldGrams` | `BigDecimal` | Quantidade em gramas que a receita produz |
| `yieldDescription` | `String` | Descrição do rendimento (ex: "1 bolo 20cm", "30 brigadeiros") |
| `ingredients` | `List<RecipeIngredient>` | Ingredientes vinculados (cascade ALL, orphanRemoval) |

**Métodos de negócio:**

| Método | Fórmula | Descrição |
|--------|---------|-----------|
| `getIngredientCostRaw()` | `Σ RecipeIngredient.getTotalCost()` | Custo bruto de ingredientes, sem margem |
| `getMarginalCost()` | `getIngredientCostRaw() × 1,05` | Custo marginal com 5% de perdas/desperdício |
| `getCostTotal()` | alias de `getMarginalCost()` | Mantido para compatibilidade com templates |
| `getRecommendedPrice()` | `getMarginalCost() × 3,0` | Preço sugerido (markup 3×) só com ingredientes |
| `getCostPerGram()` | `getMarginalCost() ÷ yieldGrams` (4 casas, HALF_UP) | Custo por grama produzida; retorna `0` se `yieldGrams` for nulo ou zero |

> **Nota:** O preço recomendado completo (incluindo rateio de gastos fixos) é calculado no `RecipeController.recipeDetail` e está disponível como `recommendedSalePrice` no model da view de detalhe.

---

### 3.2 RecipeIngredient

**Tabela:** `recipe_ingredients`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Chave primária |
| `recipe` | `Recipe` | FK para a receita (ManyToOne) |
| `ingredient` | `Ingredient` | FK para o ingrediente (ManyToOne) |
| `quantityGrams` | `BigDecimal` | Quantidade do ingrediente em gramas |

**Método de negócio:**

| Método | Fórmula | Descrição |
|--------|---------|-----------|
| `getTotalCost()` | `(quantityGrams ÷ 1000) × ingredient.pricePerKg` (4 casas, HALF_UP) | Custo deste ingrediente na receita; retorna `0` se algum campo for nulo |

---

### 3.3 Ingredient

**Tabela:** `ingredients`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Chave primária |
| `name` | `String` | Nome do ingrediente |
| `pricePerKg` | `BigDecimal` | Preço por quilograma em R$ |
| `unit` | `String` | Unidade de medida (padrão: "kg") |

---

### 3.4 Sale

**Tabela:** `sales`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Chave primária |
| `productName` | `String` | Nome do produto vendido |
| `productGroup` | `String` | Grupo/categoria para agrupamento em relatórios |
| `recipe` | `Recipe` | Receita vinculada (opcional, ManyToOne) |
| `cost` | `BigDecimal` | Custo total da venda |
| `revenue` | `BigDecimal` | Receita da venda (preço cobrado) |
| `quantity` | `BigDecimal` | Quantidade vendida (padrão: 1) |
| `saleDate` | `LocalDate` | Data da venda |
| `notes` | `String` | Observações |

**Métodos de negócio:**

| Método | Fórmula | Descrição |
|--------|---------|-----------|
| `getProfit()` | `revenue - cost` | Lucro da venda; retorna `0` se algum campo for nulo |
| `getMargin()` | `(getProfit() ÷ revenue) × 100` (4 casas, HALF_UP) | Margem percentual; retorna `0` se `revenue` for nulo ou zero |

---

### 3.5 Contact

**Tabela:** `contacts`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Chave primária |
| `name` | `String` | Nome do contato |
| `email` | `String` | Email |
| `phone` | `String` | Telefone |
| `message` | `String` | Mensagem enviada (até 2000 chars) |
| `referralCode` | `String` | Código de referência usado ao chegar ao site |
| `source` | `String` | Origem declarada (instagram, referral, google, site, etc.) |
| `utmSource`, `utmMedium`, `utmCampaign`, `utmTerm`, `utmContent` | `String` | Parâmetros UTM capturados da sessão ao enviar o formulário |
| `createdAt` | `LocalDateTime` | Data/hora de criação (padrão: `LocalDateTime.now()`) |

---

### 3.6 MonthlyExpense

**Tabela:** `monthly_expenses`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Chave primária |
| `yearMonth` | `String` | Mês de referência no formato `"yyyy-MM"` |
| `description` | `String` | Descrição do gasto (aluguel, energia, etc.) |
| `amount` | `BigDecimal` | Valor do gasto em R$ |
| `type` | `ExpenseType` | Classificação do gasto: `FIXO` (recorrente) ou `EVENTUAL` (pontual). Padrão: `FIXO`. Persistido como `VARCHAR(10)`. |

**Enum `ExpenseType`** (`com.confeitaria.model`):

| Valor | Significado | Efeito no cálculo |
|-------|-------------|-------------------|
| `FIXO` | Custo recorrente mensal (aluguel, luz, água, internet) | Entra no rateio por batelada (`fixedPerProductionUnit`) |
| `EVENTUAL` | Custo pontual/inesperado (perda de ingrediente, roubo, reparo) | Contabilizado no total do mês mas **não** afeta o custo por receita |

---

### 3.7 CostSettings

**Tabela:** `cost_settings`  
**Singleton:** sempre existe apenas o registro com `id = 1` (`SINGLETON_ID`)

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Sempre `1` (singleton) |
| `estimatedMonthlyProductionUnits` | `BigDecimal` | Quantidade média de unidades produzidas por mês; usada para ratear gastos fixos por receita |

---

### 3.8 GalleryItem

**Tabela:** `gallery_items`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Chave primária |
| `title` | `String` | Título do item |
| `description` | `String` | Descrição |
| `imagePath` | `String` | Caminho público da imagem (ex: `/uploads/uuid_foto.jpg`) |
| `visible` | `boolean` | Se `true`, aparece no site público |
| `displayOrder` | `int` | Ordem de exibição (menor = primeiro) |
| `createdAt` | `LocalDateTime` | Data de criação |

---

### 3.9 Testimonial

**Tabela:** `testimonials`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Chave primária |
| `clientName` | `String` | Nome do cliente |
| `text` | `String` | Texto do depoimento |
| `rating` | `int` | Avaliação de 1 a 5 |
| `visible` | `boolean` | Se `true`, aparece no site público |
| `createdAt` | `LocalDateTime` | Data de criação |

---

### 3.10 Faq

**Tabela:** `faqs`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Chave primária |
| `question` | `String` | Pergunta |
| `answer` | `String` | Resposta |
| `visible` | `boolean` | Se `true`, aparece no site público |
| `displayOrder` | `int` | Ordem de exibição |

---

### 3.11 ReferralLink

**Tabela:** `referral_links`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Chave primária |
| `code` | `String` | Código único em maiúsculas (ex: `INSTAGRAM`, `AMIGA_CARLA`) |
| `referrerName` | `String` | Nome de quem indica |
| `visits` | `int` | Contador de acessos via `/?ref=CODE` |
| `conversions` | `int` | Contador de contatos enviados com este código |
| `createdAt` | `LocalDateTime` | Data de criação |

---

### 3.12 UtmLink

**Tabela:** `utm_links`

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `id` | `Long` | Chave primária |
| `name` | `String` | Nome descritivo do link (ex: "Campanha Instagram Stories") |
| `baseUrl` | `String` | URL de destino base (ex: `http://localhost:8080/contato`) |
| `utmSource` | `String` | Origem da campanha (ex: instagram) |
| `utmMedium` | `String` | Meio da campanha (ex: social) |
| `utmCampaign` | `String` | Nome da campanha |
| `utmTerm` | `String` | Termo (opcional, ex: palavra-chave de busca) |
| `utmContent` | `String` | Conteúdo (opcional, ex: identificador do criativo) |
| `shortDescription` | `String` | Resumo para exibição interna |
| `createdAt` | `Instant` | Data de criação |
| `lastUpdated` | `Instant` | Data da última edição |

---

## 4. Utilitários

### 4.1 UtmLinkUrlBuilder

**Pacote:** `com.confeitaria.util`  
**Tipo:** classe utilitária estática (construtor privado, não instanciável)

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `generateFullUrl(UtmLink link)` | `String` | Constrói a URL completa adicionando os parâmetros UTM à `baseUrl`. `utm_source`, `utm_medium` e `utm_campaign` são sempre incluídos. `utm_term` e `utm_content` só são adicionados se não forem nulos/vazios. Usa `UriComponentsBuilder` e codifica a URL (encode). |

**Exemplo de saída:**
```
https://meusite.com/contato?utm_source=instagram&utm_medium=social&utm_campaign=verao2026
```

---

### 4.2 PublicMarketingContext

**Pacote:** `com.confeitaria.web`  
**Tipo:** objeto imutável com `@Getter` (criado por factory method)  
**Disponível nos templates como:** `${marketing}`

**Campos:**

| Campo | Tipo | Descrição |
|-------|------|-----------|
| `ref` | `String` | Código de referência armazenado na sessão |
| `utmSource`, `utmMedium`, `utmCampaign`, `utmTerm`, `utmContent` | `String` | Parâmetros UTM da sessão |

**Métodos:**

| Método | Retorno | O que faz |
|--------|---------|-----------|
| `fromSession(HttpSession)` | `PublicMarketingContext` | (static) Lê todos os atributos de marketing da sessão e cria uma instância. Campos ausentes ficam `null`. |
| `url(String path)` | `String` | Constrói uma URL relativa com os parâmetros de marketing como query string. Útil nos templates para preservar rastreamento entre páginas. |
| `appendQueryParams(UriComponentsBuilder)` | `void` | Adiciona os parâmetros não nulos ao builder fornecido. Usado internamente e por `PublicMarketingService.redirectWithMarketing`. |
| `hasAnyUtm()` | `boolean` | Retorna `true` se pelo menos um parâmetro UTM estiver presente na sessão. |
