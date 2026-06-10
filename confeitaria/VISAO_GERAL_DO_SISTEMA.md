# Visão Geral do Sistema — Quero Mais Doces Finos

> Documento de referência completo. Cobre arquitetura, módulos, fluxos de dados, configuração e pontos de atenção.  
> Última atualização: 2026-06-09

---

## O que é o sistema

Sistema web completo para uma confeitaria artesanal. Tem dois lados:

- **Site público** — landing page com galeria de produtos, depoimentos, FAQ e formulário de pedido. O formulário captura dados do visitante no banco e redireciona para o WhatsApp da confeitaria.
- **Painel admin** — área protegida para gerenciar o conteúdo do site (galeria, depoimentos, FAQs) e o financeiro do negócio (ingredientes, receitas com cálculo de custo, gastos mensais e registro de vendas com gráficos).

---

## Stack e dependências

| Camada | Tecnologia |
|--------|-----------|
| Linguagem | Java 17 |
| Framework | Spring Boot 3.2 |
| Views | Thymeleaf 3 |
| Persistência | Spring Data JPA + H2 (arquivo) |
| Segurança | Spring Security |
| Build | Maven 3.8+ |
| Utilitários | Lombok (boilerplate), Jackson (JSON) |
| Frontend | HTML + CSS próprio + Chart.js (gráficos) |

---

## Estrutura de diretórios

```
confeitaria-projeto-master/           ← raiz do repositório
├── Imagens/                          ← imagens estáticas (logo, hero, galeria)
│   ├── Logo.png
│   ├── Principal.png
│   └── WhatsApp Image *.jpeg         (6 fotos de produtos)
└── confeitaria/                      ← projeto Maven (tudo aqui)
    ├── pom.xml
    ├── src/main/java/com/confeitaria/
    │   ├── config/          SecurityConfig · WebConfig · DataInitializer
    │   ├── controller/      PublicController · AdminController · ContentAdminController
    │   │                    RecipeController · SaleController
    │   │                    MonthlyExpenseController · FinancialDashboardController
    │   │                    UtmLinkAdminController
    │   ├── model/           Entidades JPA
    │   ├── repository/      Interfaces Spring Data
    │   ├── service/         RecipeService · MonthlyExpenseService
    │   │                    ImageUploadService · FinanceAnalyticsService
    │   ├── web/             PublicMarketingService · PublicMarketingContext
    │   └── util/            UtmLinkUrlBuilder
    └── src/main/resources/
        ├── templates/
        │   ├── public/      index · galeria · depoimentos · faq · contato
        │   └── admin/       dashboard · contatos · galeria · depoimentos · faqs
        │                    ingredientes · receitas · receita-detalhe
        │                    gastos-mensais · vendas · financeiro
        │                    utm-links · utm-link-form · utm-link-detalhe · login
        ├── static/css/      style.css (público) · admin.css (painel)
        └── application.properties
```

---

## Módulos do sistema

### 1. Site público

| Página | URL | O que exibe |
|--------|-----|-------------|
| Início | `/` | Hero com foto de fundo, botões "Fazer pedido" e "Ver galeria", banner de indicação se `?ref=CODIGO` |
| Galeria | `/galeria` | Grid de fotos dos produtos (somente `visible=true`) |
| Depoimentos | `/depoimentos` | Cards com avaliações dos clientes (somente `visible=true`) |
| FAQ | `/perguntas-frequentes` ou `/faq` | Acordeão de perguntas/respostas (somente `visible=true`) |
| Contato | `/contato` | Formulário de pedido: nome, e-mail, telefone, mensagem, origem |

**Fluxo do formulário de contato:**

```
Visitante preenche o form  →  POST /contato
→  dados salvos no banco (Contact com UTM + referral)
→  redirecionamento para https://w.app/68oeeg  (WhatsApp da confeitaria)
```

---

### 2. Rastreamento de marketing

**Links UTM:** o admin cria links com parâmetros `utm_source`, `utm_medium`, `utm_campaign` etc. Quando um visitante entra pelo link, os parâmetros são armazenados na sessão e gravados junto ao contato quando o formulário é enviado.

**Referral (indicação):** o admin cadastra códigos (ex: `INSTAGRAM`, `CLAUDIA`). O link `/?ref=INSTAGRAM` incrementa o contador de visitas. O envio do formulário incrementa o contador de conversões.

**Propagação de parâmetros:** todos os links internos do site (botões "Fazer pedido", navbar CTA) usam `${marketing.url('/destino')}`, que preserva os parâmetros UTM e de referral ativos na sessão.

---

### 3. Painel administrativo

Acesso em `/admin/login`. Usuário e senha configurados em `application.properties`.

| Seção | URL | Funcionalidades |
|-------|-----|-----------------|
| Dashboard | `/admin` | Totais de contatos, vendas e receitas; últimos contatos; ranking por fonte/indicação |
| Contatos | `/admin/contatos` | Lista todos os pedidos com campos UTM e origem |
| Galeria | `/admin/galeria` | Upload de fotos, toggle visível/oculto, exclusão |
| Depoimentos | `/admin/depoimentos` | Adicionar/ocultar/excluir avaliações |
| FAQs | `/admin/faqs` | Adicionar/excluir perguntas |
| Ingredientes | `/admin/ingredientes` | Cadastro com nome, preço e unidade (kg · L · un) |
| Receitas | `/admin/receitas` | Criar receitas, adicionar ingredientes, ver custo calculado |
| Gastos mensais | `/admin/gastos-mensais` | Registrar custos fixos e eventuais por mês |
| Vendas | `/admin/vendas` | Registrar vendas, gráficos de linha e barras, totais do período |
| Análise financeira | `/admin/financeiro` | Dashboard avançado com filtros, rankings e gráfico interativo |
| Links UTM | `/admin/links-utm` | Gerenciar links de campanha e copiar URLs |

---

### 4. Módulo financeiro

Este é o núcleo de gestão do negócio. Composto por quatro camadas interligadas:

#### 4.1 Ingredientes

Cada ingrediente tem nome, preço e unidade de medida (`kg`, `L` ou `un`). A unidade determina como o custo é calculado quando o ingrediente é usado em uma receita.

#### 4.2 Receitas e fórmulas de custo

```
Custo bruto (ingredientes)  = Σ RecipeIngredient.getTotalCost()
Custo marginal              = custo bruto × 1,05    (+5% perdas/desperdício)
Rateio fixo por unidade     = Σ gastos FIXO do mês ÷ unidades estimadas/mês
Custo de produção completo  = custo marginal + rateio fixo
Preço sugerido de venda     = custo de produção × 3,0
Custo por grama             = custo de produção ÷ yieldGrams
```

O custo de cada ingrediente respeita a unidade:
- `kg` ou `L`: `(quantidadeGramas / 1000) × preçoPorKg`
- `un`: `quantidade × preçoPorKg`

#### 4.3 Gastos mensais

O admin registra dois tipos:

| Tipo | Descrição | Entra no rateio? |
|------|-----------|-----------------|
| `FIXO` | Aluguel, luz, água, salário | Sim — divide pelo nº de unidades estimadas |
| `EVENTUAL` | Reembolso, curso, perda de ingrediente | Não — registrado para controle mas não afeta o custo de produção |

O número de **unidades estimadas por mês** é configurável na própria tela de gastos. Padrão: 100 unidades.

#### 4.4 Registro de vendas

Ao criar uma venda:
1. Seleciona a receita base (opcional)
2. O JS faz `GET /admin/receitas/{id}/custo-json` e preenche automaticamente os campos **Custo** e **Preço de venda** com os valores recomendados
3. Os valores são editáveis antes de salvar

O modelo `Sale` calcula:
- **Lucro** = receita − custo
- **Margem** = lucro / receita × 100

#### 4.5 Análise financeira

Dashboard avançado com filtros por produto, grupo, período. Gera rankings de produtos mais e menos lucrativos, melhor e pior margem, mais vendidos. Gráfico interativo com agrupamentos por dia, produto ou produto+mês.

---

### 5. Imagens

O sistema serve imagens de dois diretórios independentes:

| Diretório | Propriedade | URL | Finalidade |
|-----------|-------------|-----|------------|
| `../Imagens/` | `app.images.dir` | `/imagens/**` | Logo, hero e galeria (estáticos no repositório) |
| `./uploads/` | `app.upload.dir` | `/uploads/**` | Imagens enviadas via formulário admin |

A separação permite que as imagens da identidade visual fiquem versionadas no Git enquanto os uploads do admin ficam em disco local.

---

## Fluxo de dados por caso de uso

### Visitante faz um pedido

```
1. Acessa /?ref=INSTAGRAM
   → PublicController.index() incrementa referralLink.visits
   → PublicMarketingService armazena ref + UTM na sessão

2. Navega pelo site (galeria, depoimentos, FAQ)
   → Todos os links incluem ?ref= e UTM via marketing.url()

3. Abre /contato
   → PublicController.contato() renderiza o form com Contact vazio

4. Preenche e submete o form
   → PublicController.submitContato()
   → PublicMarketingService.applyStoredMarketingToContact()  (aplica UTM da sessão)
   → contactRepo.save(contact)  (salva no banco com todos os metadados)
   → referralLink.conversions++
   → redirect https://w.app/68oeeg  (WhatsApp da confeitaria)
```

### Admin registra uma venda

```
1. Abre /admin/vendas
   → SaleController carrega vendas do mês atual, totais e lista de receitas

2. Seleciona a receita no form
   → JS: fetch('/admin/receitas/{id}/custo-json')
   → RecipeController.recipeCustoJson() retorna {cost, recommendedPrice, name}
   → Campos Custo e Preço preenchidos automaticamente

3. Ajusta valores se necessário e salva
   → SaleController.addSale() persiste a Sale com recipeId associado
```

### Admin calcula custo de uma receita

```
1. Abre /admin/receitas/{id}?mes=2026-05

2. RecipeController.recipeDetail()
   → RecipeService.computeFixedAllocationPerUnit("2026-05")
      → MonthlyExpenseRepository.sumAmountByYearMonth("2026-05")  (somente FIXO)
      → ÷ CostSettings.estimatedMonthlyProductionUnits
   → fullProductionCost = recipe.getMarginalCost() + fixedAllocationPerUnit
   → recommendedSalePrice = fullProductionCost × 3

3. Template exibe: ingredientes, custo por grama, custo completo, preço sugerido
```

---

## Configuração (application.properties)

```properties
# Banco H2 em arquivo (persistente entre reinicializações)
spring.datasource.url=jdbc:h2:file:./data/confeitaria;DB_CLOSE_ON_EXIT=FALSE
spring.jpa.hibernate.ddl-auto=update

# Diretório de uploads (imagens enviadas pelo admin)
app.upload.dir=./uploads

# Pasta de imagens estáticas (raiz do repositório)
app.images.dir=../Imagens

# Credenciais do admin
app.admin.username=admin
app.admin.password=confeitaria123

# Tamanho máximo de upload
spring.servlet.multipart.max-file-size=10MB

# Thymeleaf sem cache (dev)
spring.thymeleaf.cache=false
```

### Variáveis de ambiente equivalentes (para produção)

Cada propriedade pode ser sobrescrita via variável de ambiente sem mexer no arquivo:

| Propriedade | Variável de ambiente |
|-------------|---------------------|
| `app.admin.username` | `APP_ADMIN_USERNAME` |
| `app.admin.password` | `APP_ADMIN_PASSWORD` |
| `app.upload.dir` | `APP_UPLOAD_DIR` |
| `app.images.dir` | `APP_IMAGES_DIR` |

---

## Segurança

- Painel admin protegido por Spring Security (usuário em memória com BCrypt)
- CSRF habilitado em todas as rotas exceto `/h2-console/**`
- Rota `/h2-console/**` só acessível com role ADMIN
- Uploads sem validação forte de tipo/extensão — risco em produção (ver DOCUMENTACAO_DO_PROGRAMA.md)
- Credenciais padrão `admin/confeitaria123` devem ser trocadas antes de expor em produção

---

## Dados iniciais (DataInitializer)

Na primeira execução, o sistema insere dados de demonstração (guard `count() == 0` — idempotente):

| Dado | Qtd | Destaque |
|------|-----|---------|
| FAQs | 5 | Prazos, pagamentos, personalizações |
| Ingredientes | 10 | Chocolate, manteiga, farinha, ovos... |
| Depoimentos | 3 | Avaliações 5 estrelas |
| Indicações (referral) | 2 | INSTAGRAM, INDICA |
| Galeria | 6 | WhatsApp Images da pasta Imagens/ |
| Vendas de exemplo | 22 | Set/2025 e Mar/2026 |
| Gastos mensais | 26 | Set/2025 — fixos e eventuais |
| CostSettings | 1 | 100 unidades/mês |
| Links UTM | 2 | Instagram e WhatsApp |

> **Atenção:** Se o banco já existir com dados antigos, o DataInitializer não roda. Para forçar uma nova semeadura, apague `data/confeitaria.mv.db`.

---

## Endpoints — mapa completo

```
PÚBLICO
  GET  /                                    Página inicial
  GET  /galeria                             Galeria de produtos
  GET  /depoimentos                         Depoimentos
  GET  /perguntas-frequentes  (alias /faq)  FAQ
  GET  /contato                             Formulário de pedido
  POST /contato                             Salva contato → redireciona para WhatsApp

ADMIN (exige autenticação)
  GET  /admin                               Dashboard
  GET  /admin/login                         Login
  GET  /admin/contatos                      Lista contatos
  POST /admin/contatos/delete/{id}          Excluir contato
  POST /admin/referral/add                  Criar código de indicação
  POST /admin/referral/delete/{id}          Excluir código

  GET  /admin/galeria                       Listar galeria
  POST /admin/galeria/add                   Upload + criar item
  POST /admin/galeria/delete/{id}           Excluir item
  POST /admin/galeria/toggle/{id}           Ocultar/exibir

  GET  /admin/depoimentos                   Listar depoimentos
  POST /admin/depoimentos/add               Criar depoimento
  POST /admin/depoimentos/delete/{id}       Excluir
  POST /admin/depoimentos/toggle/{id}       Ocultar/exibir

  GET  /admin/faqs                          Listar FAQs
  POST /admin/faqs/add                      Criar FAQ
  POST /admin/faqs/delete/{id}              Excluir

  GET  /admin/ingredientes                  Listar ingredientes
  POST /admin/ingredientes/add              Criar
  POST /admin/ingredientes/edit/{id}        Editar preço/unidade
  POST /admin/ingredientes/delete/{id}      Excluir

  GET  /admin/receitas                      Listar receitas com custos
  GET  /admin/receitas/{id}                 Detalhe + calculadora de custo
  GET  /admin/receitas/{id}/custo-json      JSON {cost, recommendedPrice, name} (usado pelo form de vendas)
  POST /admin/receitas/add                  Criar receita
  POST /admin/receitas/{id}/addIngredient           Adicionar ingrediente
  POST /admin/receitas/{id}/removeIngredient/{riId} Remover ingrediente
  POST /admin/receitas/delete/{id}          Excluir receita

  GET  /admin/gastos-mensais                Gastos do mês
  POST /admin/gastos-mensais/add            Adicionar gasto
  POST /admin/gastos-mensais/delete/{id}    Excluir gasto
  POST /admin/gastos-mensais/settings       Atualizar unidades estimadas/mês

  GET  /admin/vendas                        Vendas + gráficos
  POST /admin/vendas/add                    Registrar venda
  POST /admin/vendas/delete/{id}            Excluir venda

  GET  /admin/financeiro                    Análise financeira avançada

  GET  /admin/links-utm                     Listar links UTM
  GET  /admin/links-utm/novo                Form novo link
  POST /admin/links-utm/novo                Criar link
  GET  /admin/links-utm/editar/{id}         Form editar
  POST /admin/links-utm/editar/{id}         Atualizar link
  POST /admin/links-utm/excluir/{id}        Excluir link
  GET  /admin/links-utm/visualizar/{id}     Ver URL completa gerada

  GET  /h2-console                          Console do banco (requer admin)
```

---

## Diagnóstico rápido

| Sintoma | O que verificar |
|---------|-----------------|
| Login admin não funciona | `app.admin.username` e `app.admin.password` em `application.properties` |
| Imagens (logo, galeria) não aparecem | `app.images.dir` aponta para o caminho correto? A pasta `Imagens/` existe lá? |
| Uploads não aparecem | `app.upload.dir` e se o diretório `uploads/` existe com permissão de escrita |
| Auto-preenchimento de venda não funciona | Testar `GET /admin/receitas/{id}/custo-json` no browser com um ID real |
| Dados iniciais não aparecem | Banco já existe com dados; apagar `data/confeitaria.mv.db` e reiniciar |
| Erro de imagens no Linux/VM | Verificar maiúsculas/minúsculas: `Logo.png` e `Principal.png` (P e L maiúsculos) |
| Formulário de contato não redireciona para WhatsApp | Verificar se o POST `/contato` salva sem erro e retorna `redirect:https://w.app/68oeeg` |
