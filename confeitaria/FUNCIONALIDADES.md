# Funcionalidades do Site — Quero Mais Doces Finos

> Mapeamento de cada funcionalidade com os arquivos envolvidos  
> Última atualização: 2026-06-02

---

## Índice

1. [Site Público](#1-site-público)
   - 1.1 Página inicial
   - 1.2 Galeria de produtos
   - 1.3 Depoimentos
   - 1.4 Perguntas frequentes
   - 1.5 Formulário de pedido / contato
2. [Rastreamento de Marketing](#2-rastreamento-de-marketing)
   - 2.1 Links UTM
   - 2.2 Códigos de indicação (Referral)
3. [Painel Administrativo](#3-painel-administrativo)
   - 3.1 Login e autenticação
   - 3.2 Dashboard
   - 3.3 Gerenciar galeria
   - 3.4 Gerenciar depoimentos
   - 3.5 Gerenciar FAQs
   - 3.6 Gerenciar contatos recebidos
4. [Módulo Financeiro](#4-módulo-financeiro)
   - 4.1 Ingredientes
   - 4.2 Receitas e custos
   - 4.3 Gastos mensais e rateio
   - 4.4 Registro de vendas
   - 4.5 Análise financeira
5. [Upload de Imagens](#5-upload-de-imagens)
6. [Dados Iniciais de Exemplo](#6-dados-iniciais-de-exemplo)

---

## 1. Site Público

### 1.1 Página inicial

**O que faz:** Apresenta o negócio, propaga parâmetros de marketing e direciona para Galeria, Depoimentos e Contato.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/PublicController.java` | `@GetMapping("/")` → método `index()` |
| Enriquecimento de marketing | `web/PublicMarketingService.java` | `enrichPublicModel()` — sincroniza ref e UTM da URL para sessão e cria `PublicMarketingContext` |
| Contexto de marketing | `web/PublicMarketingContext.java` | `url(path)` — gera links com parâmetros UTM preservados |
| Template | `templates/public/index.html` | Seção `.hero` com background `Principal.png`, botões "Fazer pedido" e "Ver galeria", banner de referral |
| Navbar | `templates/public/fragments.html` | Fragmento `publicNav` — logo, links, botão CTA |
| CSS hero | `static/css/style.css` | Classe `.hero` (background CSS cover via `style` inline no template) |
| CSS navbar | `static/css/style.css` | `.navbar-public` (fundo `#f6e8de`) |
| Imagem do hero | `uploads/principal.png` | Servida por `WebConfig.addResourceHandlers()` |
| Logo | `uploads/logo.png` | Referenciado em `fragments.html`, altura 64px |

---

### 1.2 Galeria de produtos

**O que faz:** Exibe fotos e descrições dos produtos da confeitaria.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/PublicController.java` | `@GetMapping("/galeria")` → método `galeria()` |
| Query | `repository/GalleryItemRepository.java` | `findByVisibleTrueOrderByDisplayOrderAsc()` — só itens marcados como visíveis |
| Template | `templates/public/galeria.html` | Grid `gallery-grid`, `<img>` com `th:src="${item.imagePath}"` |
| CSS grid | `static/css/style.css` | `.gallery-grid` (auto-fill, minmax 280px) · `.gallery-item img` (height 220px, cover) |
| Imagens | `uploads/produto1.jpeg`…`produto6.jpeg` | Servidas por `WebConfig` |

---

### 1.3 Depoimentos

**O que faz:** Exibe avaliações de clientes com estrelas.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/PublicController.java` | `@GetMapping("/depoimentos")` → método `depoimentos()` |
| Query | `repository/TestimonialRepository.java` | `findByVisibleTrueOrderByCreatedAtDesc()` |
| Entidade | `model/Testimonial.java` | Campos: `clientName`, `text`, `rating` (1–5) |
| Template | `templates/public/depoimentos.html` | Cards com `.testimonial-card`, estrelas via `${t.rating}` |
| CSS | `static/css/style.css` | `.testimonial-card` (borda pink-300, citação decorativa) |

---

### 1.4 Perguntas frequentes

**O que faz:** Lista perguntas expansíveis (acordeão) sobre pedidos, prazos e pagamento.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/PublicController.java` | `@GetMapping({"/perguntas-frequentes","/faq"})` → método `faq()` |
| Query | `repository/FaqRepository.java` | `findByVisibleTrueOrderByDisplayOrderAsc()` |
| Entidade | `model/Faq.java` | Campos: `question`, `answer`, `visible`, `displayOrder` |
| Template | `templates/public/faq.html` | `.faq-item` com toggle JS `open/close` |
| CSS | `static/css/style.css` | `.faq-question`, `.faq-answer` (max-height animado), `.faq-arrow` |

---

### 1.5 Formulário de pedido / contato

**O que faz:** Recebe nome, e-mail, telefone, origem e mensagem. Salva no banco com rastreamento UTM e referral.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota GET | `controller/PublicController.java` | `@GetMapping("/contato")` — passa `Contact` vazio |
| Rota POST | `controller/PublicController.java` | `@PostMapping("/contato")` → método `submitContato()` |
| Salvar contato | `repository/ContactRepository.java` | `save(contact)` |
| Aplicar marketing | `web/PublicMarketingService.java` | `applyStoredMarketingToContact()` — preenche campos UTM do Contact com dados da sessão |
| Incrementar indicação | `repository/ReferralLinkRepository.java` | `findByCode(ref)` + `link.conversions++` + `save()` |
| Entidade | `model/Contact.java` | Todos os campos de contato + 5 campos UTM + `referralCode` + `source` |
| Template | `templates/public/contato.html` | Form com `th:action`, campos + `<select>` source + hidden ref |

---

## 2. Rastreamento de Marketing

### 2.1 Links UTM

**O que faz:** Admin cria links com parâmetros UTM pré-definidos para distribuir em campanhas. O site captura os parâmetros e os salva junto com cada contato.

**Fluxo completo:**

```
Admin cria link UTM  →  URL gerada com ?utm_source=...  →
Visitante acessa o site via link  →
PublicMarketingService captura params e armazena na sessão  →
Visitante preenche o form  →
Params UTM salvos no Contact
```

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| CRUD admin | `controller/UtmLinkAdminController.java` | Rotas: `GET/POST /admin/links-utm/novo`, `editar/{id}`, `excluir/{id}`, `visualizar/{id}` |
| Geração da URL completa | `util/UtmLinkUrlBuilder.java` | `generateFullUrl(utmLink)` — monta `baseUrl?utm_source=x&utm_medium=y&...` |
| Entidade | `model/UtmLink.java` | `baseUrl`, `utmSource/Medium/Campaign/Term/Content`, `name`, `shortDescription` |
| Captura na sessão | `web/PublicMarketingService.java` | `syncQueryParamsToSession()` — chamado em cada request público |
| Propagação em links | `web/PublicMarketingContext.java` | `url(path)` — todos os links do site carregam os params ativos |
| Gravação no contato | `web/PublicMarketingService.java` | `applyStoredMarketingToContact()` |
| Templates admin | `templates/admin/utm-links.html`, `utm-link-form.html`, `utm-link-detalhe.html` | Listagem, formulário e visualização do link gerado |

---

### 2.2 Códigos de indicação (Referral)

**O que faz:** Admin cria códigos para rastrear quem indicou o site (ex: `INSTAGRAM`, `CLAUDIA`). Visitas e conversões são contadas separadamente.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Criar / excluir código | `controller/AdminController.java` | `POST /admin/referral/add` e `POST /admin/referral/delete/{id}` |
| Incrementar visita | `controller/PublicController.java` | `index()` — `findByCode(ref)` + `link.visits++` + `save()` |
| Incrementar conversão | `controller/PublicController.java` | `submitContato()` — `link.conversions++` |
| Entidade | `model/ReferralLink.java` | `code`, `referrerName`, `visits`, `conversions` |
| Exibição no admin | `templates/admin/dashboard.html` | Tabela de contagens por código de indicação |

---

## 3. Painel Administrativo

### 3.1 Login e autenticação

**O que faz:** Protege todas as rotas `/admin/**` com usuário e senha.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Configuração | `config/SecurityConfig.java` | `filterChain()` — libera rotas públicas, exige ROLE_ADMIN para `/admin/**` |
| Usuário | `config/SecurityConfig.java` | `userDetailsService()` — usuário `admin` com senha BCrypt; credenciais em `application.properties` |
| Login page | `controller/AdminController.java` | `GET /admin/login` |
| Template | `templates/admin/login.html` | Form com `action="/admin/login"` method POST |

---

### 3.2 Dashboard

**O que faz:** Visão geral do negócio com totais, últimos contatos e rankings de origem.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/AdminController.java` | `GET /admin` → `dashboard()` |
| Contatos recentes | `repository/ContactRepository.java` | `findAllByOrderByCreatedAtDesc()` (primeiros 5) |
| Contagem por fonte | `repository/ContactRepository.java` | `countBySource()` → `List<Object[]>` [source, count] |
| Contagem por referral | `repository/ContactRepository.java` | `countByReferralCode()` → `List<Object[]>` [code, count] |
| Template | `templates/admin/dashboard.html` | Stat cards + tabelas de resumo |

---

### 3.3 Gerenciar galeria

**O que faz:** Admin adiciona, remove e oculta fotos de produtos. O upload é feito via formulário.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/ContentAdminController.java` | `GET /admin/galeria`, `POST /admin/galeria/add`, `/delete/{id}`, `/toggle/{id}` |
| Upload de imagem | `service/ImageUploadService.java` | `save(MultipartFile)` — UUID + cópia para `./uploads/` |
| Servir imagem | `config/WebConfig.java` | `addResourceHandlers()` — mapeia `/uploads/**` para o disco |
| Entidade | `model/GalleryItem.java` | `title`, `description`, `imagePath`, `visible`, `displayOrder` |
| Template | `templates/admin/galeria.html` | Form multipart + grid com toggle visible |

---

### 3.4 Gerenciar depoimentos

**O que faz:** Admin adiciona, remove e oculta avaliações de clientes.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/ContentAdminController.java` | `GET /admin/depoimentos`, `POST /admin/depoimentos/add`, `/delete/{id}`, `/toggle/{id}` |
| Entidade | `model/Testimonial.java` | `clientName`, `text`, `rating`, `visible` |
| Template | `templates/admin/depoimentos.html` | Tabela com toggle e form de adição |

---

### 3.5 Gerenciar FAQs

**O que faz:** Admin adiciona, remove e reordena perguntas exibidas no site.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/ContentAdminController.java` | `GET /admin/faqs`, `POST /admin/faqs/add`, `/delete/{id}` |
| Ordenação | `model/Faq.java` + `repository/FaqRepository.java` | Campo `displayOrder` + query `findByVisibleTrueOrderByDisplayOrderAsc()` |
| Template | `templates/admin/faqs.html` | Tabela de FAQs + form de adição |

---

### 3.6 Gerenciar contatos recebidos

**O que faz:** Admin visualiza todos os pedidos recebidos com origem e dados UTM.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/AdminController.java` | `GET /admin/contatos` |
| Query | `repository/ContactRepository.java` | `findAllByOrderByCreatedAtDesc()` |
| Entidade | `model/Contact.java` | `name`, `email`, `phone`, `source`, `message`, `utmSource/...`, `referralCode` |
| Template | `templates/admin/contatos.html` | Tabela paginável com todos os campos + contagem por fonte |

---

## 4. Módulo Financeiro

### 4.1 Ingredientes

**O que faz:** Admin cadastra ingredientes com nome, preço e unidade de medida. A unidade define como o custo é calculado nas receitas.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/RecipeController.java` | `GET /admin/ingredientes`, `POST /admin/ingredientes/add`, `/delete/{id}`, `/edit/{id}` |
| Entidade | `model/Ingredient.java` | `name`, `pricePerKg`, `unit` (`"kg"` · `"L"` · `"un"`) |
| Cálculo de custo | `model/RecipeIngredient.java` | `getTotalCost()` — usa `unit` para dividir ou multiplicar diretamente |
| Template | `templates/admin/ingredientes.html` | Tabela + form de adição + label dinâmico (JS: "Preço/kg" ou "Preço/un") |

---

### 4.2 Receitas e custos

**O que faz:** Admin cria receitas, adiciona ingredientes e visualiza o custo completo de produção e o preço sugerido de venda.

**Fórmulas de custo:**

```
Custo bruto          = Σ RecipeIngredient.totalCost
Custo marginal       = custo bruto × 1,05   (+5% perdas)
Custo de produção    = custo marginal + fixedAllocationPerUnit
Preço sugerido       = custo de produção × 3,0
Custo por grama      = custo de produção / yieldGrams
```

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Lista de receitas | `controller/RecipeController.java` | `GET /admin/receitas` → passa `fixedAllocationPerUnit` do mês atual |
| Detalhe / custo | `controller/RecipeController.java` | `GET /admin/receitas/{id}` → calcula `fullProductionCost`, `recommendedSalePrice`, `costPerGramProduction` |
| Rateio fixo | `service/RecipeService.java` | `computeFixedAllocationPerUnit(yearMonth)` — soma gastos FIXO do mês ÷ unidades estimadas |
| Custo bruto | `model/Recipe.java` | `getIngredientCostRaw()` → `getMarginalCost()` |
| Custo por ingrediente | `model/RecipeIngredient.java` | `getTotalCost()` — respeita unidade do ingrediente |
| Template lista | `templates/admin/receitas.html` | Tabela com custo e preço sugerido calculados em `th:with` |
| Template detalhe | `templates/admin/receita-detalhe.html` | Tabela de ingredientes + painel "Análise de Custo" + navegação por mês |
| Navegação por mês | `controller/RecipeController.java` | Parâmetro `?mes=yyyy-MM`; `prevMonth`/`nextMonth` passados ao model |

---

### 4.3 Gastos mensais e rateio

**O que faz:** Admin cadastra custos fixos e eventuais de cada mês. O total fixo é dividido pelas unidades mensais estimadas para chegar ao rateio por receita.

**Fórmula de rateio:**

```
fixedPerProductionUnit = Σ MonthlyExpense(FIXO, mês) / CostSettings.estimatedMonthlyProductionUnits
```

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/MonthlyExpenseController.java` | `GET /admin/gastos-mensais?mes=yyyy-MM` |
| Totais | `controller/MonthlyExpenseController.java` | `list()` — calcula `fixedTotal`, `eventualTotal`, `monthTotal`, `fixedPerProductionUnit` |
| Query soma | `repository/MonthlyExpenseRepository.java` | `sumAmountByYearMonth(ym)` |
| Unidades estimadas | `model/CostSettings.java` + `repository/CostSettingsRepository.java` | Singleton com `estimatedMonthlyProductionUnits` |
| Atualizar unidades | `controller/MonthlyExpenseController.java` | `POST /admin/gastos-mensais/settings` |
| Entidade | `model/MonthlyExpense.java` | `yearMonth`, `description`, `amount`, `type` (FIXO/EVENTUAL) |
| Template | `templates/admin/gastos-mensais.html` | Tabela de despesas + resumo de totais + campo para editar unidades estimadas |

---

### 4.4 Registro de vendas

**O que faz:** Admin registra vendas com produto, grupo, custo, receita e data. Gera gráficos de acompanhamento do período.

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota lista | `controller/SaleController.java` | `GET /admin/vendas?inicio=...&fim=...` |
| Filtro de datas | `controller/SaleController.java` | `vendas()` — padrão: 1º dia do mês até hoje |
| Query filtrada | `repository/SaleRepository.java` | `findBySaleDateBetweenOrderBySaleDateAsc(start, end)` |
| Gráficos | `controller/SaleController.java` | Monta arrays `chartLabels`, `chartCosts`, `chartRevenues`, `chartProfits` |
| Chart.js | `templates/admin/vendas.html` | `th:inline="javascript"` + `new Chart(dailyChart, ...)` (linha) e `productChart` (barras) |
| Lucro e margem | `model/Sale.java` | `getProfit()` = revenue − cost · `getMargin()` = profit/revenue × 100 |
| Adicionar venda | `controller/SaleController.java` | `POST /admin/vendas/add` — associa receita se `recipeId` fornecido |
| Template | `templates/admin/vendas.html` | Stat cards + gráficos + tabela + form de registro |

---

### 4.5 Análise financeira

**O que faz:** Dashboard avançado com filtros por produto e grupo, rankings e gráfico interativo (por dia, por produto, produto+mês).

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Rota | `controller/FinancialDashboardController.java` | `GET /admin/financeiro?inicio&fim&produto&grupo` |
| Relatório | `service/FinanceAnalyticsService.java` | `buildReport()` — filtra, agrega, calcula totais e rankings |
| Rankings | `service/FinanceAnalyticsService.java` | `topByProfit` / `leastByProfit` / `bestMargin` / `worstMargin` / `mostPopular` |
| JSON para gráfico | `controller/FinancialDashboardController.java` | `dailyJson`, `productJson`, `productMonthJson` → serializado via Jackson |
| Gráfico interativo | `templates/admin/financeiro.html` | `<script type="application/json" th:utext>` + Chart.js com seletores de agrupamento/métrica/tipo |
| Agrupamentos | `templates/admin/financeiro.html` | JS `buildDataset()` — lê `finGroupBy` (dia / produto / produto_mes) e `finMetric` (lucro/receita/custo/qtd) |
| Dropdown de grupos | `controller/FinancialDashboardController.java` | `productGroups` — `Set<String>` de grupos únicos para o `<select>` |
| Template | `templates/admin/financeiro.html` | Filtros + stat cards + gráfico principal + tabelas de ranking |

---

## 5. Upload de Imagens

**O que faz:** Admin faz upload de imagens pelo painel. As imagens são salvas em disco e servidas como arquivos estáticos.

**Fluxo:**

```
Form multipart no admin  →  ContentAdminController  →
ImageUploadService.save()  →  arquivo em ./uploads/<uuid>_<nome>  →
GalleryItem.imagePath = "/uploads/<uuid>_<nome>"  →
Template público: <img th:src="${item.imagePath}">  →
WebConfig serve /uploads/** → disco
```

| Camada | Arquivo | Trecho responsável |
|--------|---------|--------------------|
| Receber upload | `controller/ContentAdminController.java` | `POST /admin/galeria/add` — `@RequestParam MultipartFile imageFile` |
| Salvar arquivo | `service/ImageUploadService.java` | `save(file)` — gera UUID, cria dir, copia, retorna path |
| Servir arquivo | `config/WebConfig.java` | `addResourceHandlers()` — `/uploads/**` → `file:./uploads/` |
| Config tamanho | `src/main/resources/application.properties` | `spring.servlet.multipart.max-file-size=10MB` |
| Exibição | `templates/public/galeria.html` | `<img th:src="${item.imagePath}">` |

---

## 6. Dados Iniciais de Exemplo

**O que faz:** Na primeira inicialização (tabelas vazias), o sistema insere dados de demonstração para que o site não apareça em branco.

| Dado semeado | Quantidade | Arquivo | Método |
|-------------|------------|---------|--------|
| FAQs | 5 | `config/DataInitializer.java` | `addFaq()` |
| Ingredientes | 10 | `config/DataInitializer.java` | `addIngredient()` |
| Depoimentos | 3 | `config/DataInitializer.java` | `addTestimonial()` |
| Códigos de indicação | 2 (INSTAGRAM, INDICA) | `config/DataInitializer.java` | `run()` |
| Itens de galeria | 6 (produto1–6.jpeg) | `config/DataInitializer.java` | `seedGalleryItems()` |
| Vendas de exemplo | 22 (set/2025 e mar/2026) | `config/DataInitializer.java` | `seedSales()` |
| Gastos mensais | 26 (set/2025) | `config/DataInitializer.java` | `seedMonthlyExpenses()` |
| CostSettings | 1 (100 unidades/mês) | `config/DataInitializer.java` | `run()` |
| Links UTM | 2 (instagram, whatsapp) | `config/DataInitializer.java` | `run()` |

**Guard de idempotência:** cada bloco só executa se `repository.count() == 0`. Reiniciar o servidor não duplica os dados.

---

## Mapa de Endpoints

```
PUBLIC
  GET  /                                → index (home)
  GET  /galeria                         → galeria pública
  GET  /depoimentos                     → depoimentos
  GET  /perguntas-frequentes            → FAQ
  GET  /contato                         → form de pedido
  POST /contato                         → submeter pedido

ADMIN (requer login)
  GET  /admin                           → dashboard
  GET  /admin/login                     → página de login
  GET  /admin/contatos                  → contatos recebidos
  POST /admin/contatos/delete/{id}      → excluir contato
  POST /admin/referral/add              → criar código indicação
  POST /admin/referral/delete/{id}      → excluir código

  GET  /admin/galeria                   → lista galeria
  POST /admin/galeria/add               → upload + criar item
  POST /admin/galeria/delete/{id}       → excluir item
  POST /admin/galeria/toggle/{id}       → ocultar/exibir item

  GET  /admin/depoimentos               → lista depoimentos
  POST /admin/depoimentos/add           → criar depoimento
  POST /admin/depoimentos/delete/{id}   → excluir
  POST /admin/depoimentos/toggle/{id}   → ocultar/exibir

  GET  /admin/faqs                      → lista FAQs
  POST /admin/faqs/add                  → criar FAQ
  POST /admin/faqs/delete/{id}          → excluir

  GET  /admin/ingredientes              → lista ingredientes
  POST /admin/ingredientes/add          → criar ingrediente
  POST /admin/ingredientes/edit/{id}    → editar preço/unidade
  POST /admin/ingredientes/delete/{id}  → excluir

  GET  /admin/receitas                  → lista receitas com custos
  GET  /admin/receitas/{id}             → detalhe + calculadora
  POST /admin/receitas/add              → criar receita
  POST /admin/receitas/{id}/addIngredient         → add ingrediente
  POST /admin/receitas/{id}/removeIngredient/{riId} → remover
  POST /admin/receitas/delete/{id}      → excluir receita

  GET  /admin/gastos-mensais            → gastos do mês
  POST /admin/gastos-mensais/add        → adicionar gasto
  POST /admin/gastos-mensais/delete/{id}→ excluir gasto
  POST /admin/gastos-mensais/settings   → atualizar unidades estimadas

  GET  /admin/vendas                    → vendas com gráficos
  POST /admin/vendas/add                → registrar venda
  POST /admin/vendas/delete/{id}        → excluir venda

  GET  /admin/financeiro                → análise financeira
  GET  /admin/links-utm                 → lista links UTM
  GET  /admin/links-utm/novo            → form novo link
  POST /admin/links-utm/novo            → criar link
  GET  /admin/links-utm/editar/{id}     → form editar
  POST /admin/links-utm/editar/{id}     → atualizar link
  POST /admin/links-utm/excluir/{id}    → excluir link
  GET  /admin/links-utm/visualizar/{id} → ver URL gerada
```
