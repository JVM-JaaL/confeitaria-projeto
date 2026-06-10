# Documentação do programa (Confeitaria)

Sistema web para confeitaria artesanal feito em **Java 17 + Spring Boot 3.2 + Thymeleaf + Spring Security + Spring Data JPA + H2**.

## Acesso ao painel admin

| Campo | Valor padrão |
|---|---|
| URL de login | `http://localhost:8080/admin/login` |
| Usuário | `admin` |
| Senha | `confeitaria123` |

> Para alterar as credenciais sem mexer no código, edite as propriedades em `src/main/resources/application.properties`:
> ```properties
> app.admin.username=admin
> app.admin.password=confeitaria123
> ```

## Visão geral do que o programa faz

- **Site público**: mostra uma landing page com **galeria** e **depoimentos**, além de páginas de **FAQ** e **contato**.
- **Painel admin**: área protegida por login para gerenciar conteúdos (contatos, galeria, depoimentos, FAQs), cadastrar **ingredientes**, criar **receitas** com cálculo de custo e registrar/analisar **vendas** com gráficos.
- **Links de indicação (referral)**: aceita `?ref=CODIGO` no site e registra **visitas** e **conversões** (envio do formulário de contato) por código.
- **Uploads**: permite subir imagens (usadas na galeria) e serve esses arquivos via `/uploads/**`.
- **Banco de dados**: usa H2 persistido em arquivo (`./data/confeitaria.mv.db`) e expõe o console em `/h2-console` (restrito a admin).

## Estrutura principal do código

### Dependências e build

- **Maven**: `pom.xml`
- Principais starters:
  - `spring-boot-starter-web` (MVC)
  - `spring-boot-starter-thymeleaf` (views)
  - `spring-boot-starter-data-jpa` (persistência)
  - `spring-boot-starter-security` (login/área protegida)
  - `com.h2database:h2` (banco)
  - `lombok` (gera getters/setters etc via `@Data`)

### Inicialização

- **Entrada do app**: `com.confeitaria.ConfeitariaApplication`
- **Carga de dados iniciais**: `com.confeitaria.config.DataInitializer`
  - Insere FAQs, ingredientes, depoimentos e links de indicação se as tabelas estiverem vazias.

### Configurações

- **Segurança**: `com.confeitaria.config.SecurityConfig`
  - Usuário em memória lido de `application.properties`:
    - usuário: `${app.admin.username}` (padrão `admin`)
    - senha: `${app.admin.password}` (padrão `confeitaria123`)
    - role: `ADMIN`
  - Regras:
    - `/admin/**` e `/h2-console/**` exigem `ADMIN`
    - Demais rotas são públicas
  - Login:
    - Página: `/admin/login`
    - Processamento: `/admin/login`
    - Sucesso: `/admin`
    - Logout: `/admin/logout` → redireciona para `/`
  - CSRF:
    - Ignorado somente para `/h2-console/**` (necessário para o console funcionar).
  - Frames:
    - `sameOrigin()` para permitir o H2 console em iframe.

- **Uploads / conteúdo estático**: `com.confeitaria.config.WebConfig`
  - Mapeia `/uploads/**` para a pasta configurada por `app.upload.dir` (padrão `./uploads`) — destino de novos uploads feitos pelo admin.
  - Mapeia `/imagens/**` para a pasta configurada por `app.images.dir` (padrão `../Imagens`) — logo, hero e fotos de galeria servidas diretamente do repositório.

### Camada web (controllers)

- **Público**: `com.confeitaria.controller.PublicController`
  - `GET /`:
    - Carrega galeria visível e depoimentos visíveis.
    - Se vier `?ref=...`, incrementa `visits` do `ReferralLink`.
    - Renderiza `templates/public/index.html`.
  - `GET /faq`: lista FAQs visíveis → `templates/public/faq.html`.
  - `GET /contato`: abre formulário → `templates/public/contato.html`.
  - `POST /contato`:
    - Salva `Contact` com data/hora, `source` e `referralCode` (se houver).
    - Se houver `ref`, incrementa `conversions` do `ReferralLink`.
    - Redireciona para `https://w.app/68oeeg` (WhatsApp da confeitaria).

- **Admin (conteúdo + dashboard + upload)**: `com.confeitaria.controller.AdminController`
  - `GET /admin`: dashboard com totais, últimos contatos e contagens por origem/referral.
  - CRUDs:
    - Contatos: listar e deletar
    - Galeria: listar, adicionar (com upload), deletar e alternar visibilidade
    - Depoimentos: listar, adicionar, deletar e alternar visibilidade
    - FAQs: listar, adicionar e deletar
    - Referral links: adicionar e deletar
  - Upload:
    - Salva arquivo com nome `UUID_originalFilename` dentro de `app.upload.dir`
    - Persiste o caminho web como `"/uploads/<nome>"` no banco

- **Admin (ingredientes e receitas)**: `com.confeitaria.controller.RecipeController`
  - Ingredientes:
    - `GET /admin/ingredientes`: lista ingredientes
    - `POST /admin/ingredientes/add`: cria ingrediente
    - `POST /admin/ingredientes/edit/{id}`: atualiza `pricePerKg`
    - `POST /admin/ingredientes/delete/{id}`: remove ingrediente
  - Receitas:
    - `GET /admin/receitas`: lista receitas + formulário de criação
    - `POST /admin/receitas/add`: cria receita e redireciona para o detalhe
    - `GET /admin/receitas/{id}`: detalhe da receita e inclusão de ingredientes
    - `GET /admin/receitas/{id}/custo-json`: retorna JSON `{name, cost, recommendedPrice}` com custo de produção completo (ingredientes + rateio fixo do mês atual) e preço sugerido (× 3). Usado pelo formulário de vendas para preencher campos automaticamente.
    - `POST /admin/receitas/{id}/addIngredient`: adiciona ingrediente (em gramas)
    - `POST /admin/receitas/{id}/removeIngredient/{riId}`: remove item de ingrediente
    - `POST /admin/receitas/delete/{id}`: remove receita

- **Admin (vendas + relatórios)**: `com.confeitaria.controller.SaleController`
  - `GET /admin/vendas`:
    - Filtra por período (`inicio`/`fim`) com padrão: começo do mês → hoje.
    - Calcula totais (custo, receita, lucro).
    - Monta séries de gráfico diário.
    - Busca resumos por produto e por grupo (consultas agregadas no repositório).
    - Carrega lista completa de vendas recentes e receitas para vincular na criação.
  - `POST /admin/vendas/add`: cria venda (data padrão = hoje; receita opcional via `recipeId`).
  - `POST /admin/vendas/delete/{id}`: remove venda.
  - **Auto-preenchimento no formulário:** ao selecionar uma receita, o JS faz `GET /admin/receitas/{id}/custo-json` e preenche os campos Custo e Preço de venda com os valores recomendados. O texto "Sugerido pela receita" aparece abaixo dos campos.

### Modelo e persistência (JPA)

Entidades principais (todas com `@Entity`):

- `Recipe`: receita, com lista `ingredients` (`RecipeIngredient`) e métodos de cálculo
  - `getCostTotal()`: soma custos dos ingredientes e aplica **+5%**
  - `getRecommendedPrice()`: `getCostTotal() * 3`
  - `getCostPerGram()`: `getCostTotal() / yieldGrams` (4 casas, HALF_UP)

- `Ingredient`: ingrediente com `pricePerKg`
- `RecipeIngredient`: ligação receita↔ingrediente com `quantityGrams` e custo calculado
- `Sale`: venda com custo, receita e lucro/margem calculados
- `Contact`: contato do formulário
- `ReferralLink`: link de indicação com `visits` e `conversions`
- `Faq`, `GalleryItem`, `Testimonial`: conteúdo do site, com flag `visible`
- `MonthlyExpense`: gasto mensal com campo `type`:
  - `FIXO` — recorrente (aluguel, luz, água, internet); entra no rateio por receita
  - `EVENTUAL` — pontual/inesperado (perda de ingrediente, roubo, reparo); registrado para controle mas não infla o custo de produção
- `ExpenseType`: enum `FIXO` / `EVENTUAL`

Repositórios (Spring Data):

- `SaleRepository`: consultas por período + agregações (diário, por grupo, por produto)
- `ContactRepository`: contagens agregadas por `source` e `referralCode`
- `RecipeRepository`, `IngredientRepository`, `RecipeIngredientRepository`, `ReferralLinkRepository`, `FaqRepository`, `GalleryItemRepository`, `TestimonialRepository`

## Configuração (application.properties)

Pontos importantes em `src/main/resources/application.properties`:

- **H2 em arquivo**: `jdbc:h2:file:./data/confeitaria;DB_CLOSE_ON_EXIT=FALSE`
- **DDL**: `spring.jpa.hibernate.ddl-auto=update` (atualiza schema automaticamente)
- **Uploads (novos arquivos)**: `app.upload.dir=./uploads`
- **Imagens estáticas**: `app.images.dir=../Imagens` — aponta para a pasta `Imagens/` na raiz do projeto. Altere este valor na VM se necessário.
- **Thymeleaf cache**: desativado (`spring.thymeleaf.cache=false`) para desenvolvimento

## Fluxo funcional resumido

- **Conteúdo público**
  - Admin cadastra itens de galeria e depoimentos (e alterna visibilidade).
  - A home (`/`) exibe somente itens `visible=true`.
- **Formulário de contato / pedido**
  - Visitante preenche nome, e-mail, telefone, mensagem e origem.
  - Os dados são salvos no banco com parâmetros UTM e referral capturados da sessão.
  - Após salvar, o visitante é redirecionado para `https://w.app/68oeeg` (WhatsApp da confeitaria).
- **Indicação/referral**
  - Usuário entra com `/?ref=INSTAGRAM` → incrementa visitas do código.
  - Usuário envia contato com `ref` → incrementa conversões do código.
- **Custo e preço de receita**
  - Admin cadastra ingredientes com preço por kg.
  - Admin monta receita (ingredientes em gramas).
  - O sistema calcula custo total (+5%) e preço recomendado (x3).
- **Vendas vinculadas a receitas**
  - Ao registrar uma venda, o admin seleciona a receita base.
  - Os campos Custo e Preço de venda são preenchidos automaticamente via `GET /admin/receitas/{id}/custo-json`.
  - Os valores são editáveis antes de salvar.
- **Gastos mensais**
  - Admin registra gastos **fixos** (aluguel, luz…) e **eventuais** (perda, roubo…) por mês.
  - Apenas os fixos entram no rateio por batelada (`fixedTotal ÷ unidades_estimadas`).
  - Os eventuais são visíveis no total do mês mas não afetam o custo de produção.
- **Vendas**
  - Admin registra venda com custo e receita (e opcionalmente relaciona uma receita).
  - A tela de vendas consolida totais e séries para gráficos por período.

## Possíveis erros, bugs e pontos de atenção encontrados

### Segurança (importante)

- **Credenciais configuráveis via `application.properties`**: usuário e senha do admin são lidos das propriedades `app.admin.username` e `app.admin.password` (padrão `admin` / `confeitaria123`).
  - Para produção: altere os valores no `application.properties` ou defina variáveis de ambiente (`APP_ADMIN_USERNAME`, `APP_ADMIN_PASSWORD`) — o Spring injeta automaticamente.
  - Risco residual: se o arquivo `.properties` for commitado com a senha real, ela fica exposta. O ideal é usar variáveis de ambiente em produção.

- **Uploads sem validação forte**:
  - `AdminController.saveFile()` salva qualquer arquivo enviado (não valida extensão/MIME).
  - Risco: upload de arquivos indevidos, armazenamento de conteúdo perigoso, ou uso abusivo de disco.
  - Também usa `file.getOriginalFilename()` no nome final; isso pode incluir caracteres estranhos/indesejados. Embora o arquivo seja salvo com `resolve()`, ainda é recomendável **sanitizar** o nome e/ou ignorar completamente o nome original (guardar apenas a extensão validada).

### Robustez / validação de dados

- **Falta de validação no formulário de contato e cadastros**:
  - `PublicController.submitContato()` salva `Contact` sem `@Valid` e sem validações (`@NotBlank`, `@Email`, etc.).
  - Pode permitir registros vazios/inválidos e poluir a base.

- **Campos numéricos podem ficar nulos**:
  - Ex.: `Sale.cost` e `Sale.revenue` podem ser salvos nulos dependendo do formulário, o que faz `getProfit()` virar zero.
  - Se o objetivo é obrigar preenchimento, faltam validações.

### JPA / modelagem

- **Relação `Recipe` → `RecipeIngredient`**:
  - `Recipe.ingredients` está mapeado com `cascade = ALL` e `orphanRemoval = true`, o que é correto para “composição”.
  - Porém, no `RecipeController.addRecipeIngredient()` você salva o `RecipeIngredient` sem necessariamente adicionar o item na lista `recipe.getIngredients()`; isso normalmente funciona, mas pode gerar inconsistências momentâneas na sessão/página se você depender do estado em memória (o controller redireciona, então tende a ficar ok).

- **Consultas agregadas retornam `List<Object[]>`**:
  - Funciona, mas é frágil (índices `[0]..[3]`) e pode quebrar fácil na view.
  - Melhor prática: usar projeções (interfaces DTO) para tipar os retornos.

### Código morto / qualidade

- **`ContactRepository` tem `interface BaseRepo<T> ...` não usada** e imports possivelmente sobrando (ex.: `LocalDate`, `Optional`, `Param` não são necessários ali).
  - Não quebra execução, mas é sinal de ruído e pode causar warnings.

### Comportamento funcional

- **Contagem de visitas por referral é “por pageview”**:
  - Cada refresh no `/` com `ref` incrementa `visits` (não tem deduplicação por sessão/usuário).
  - Se a intenção era visitas únicas, precisaria de estratégia (cookie/sessão/IP etc.).

## Onde olhar se algo “não funcionar”

- **Login admin**: `SecurityConfig` + template `templates/admin/login.html`
- **Rotas admin**: controllers em `com.confeitaria.controller.*`
- **Banco**: `application.properties` (URL H2) + `/h2-console`
- **Uploads (novos arquivos)**: `app.upload.dir` e `WebConfig` (mapeamento `/uploads/**`)
- **Imagens não aparecem**: verifique `app.images.dir` em `application.properties` e se a pasta `Imagens/` existe no caminho configurado
- **Auto-preenchimento de venda não funciona**: verifique se o endpoint `GET /admin/receitas/{id}/custo-json` retorna JSON válido (testar no browser com o ID de uma receita existente)

