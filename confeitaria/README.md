# 🎂 Confeitaria — Sistema Web Completo

Sistema web completo para confeitaria artesanal desenvolvido com **Java 17 + Spring Boot 3.2 + H2 + Thymeleaf**.

---

## 🚀 Como Rodar

### Pré-requisitos
- Java 17+ instalado
- Maven 3.8+ instalado

### Passos
```bash
# 1. Entre na pasta do projeto
cd confeitaria

# 2. Compile e rode
mvn spring-boot:run

# 3. Acesse no navegador
# Site público:  http://localhost:8080
# Admin:         http://localhost:8080/admin
```

---

## 🔐 Credenciais do Admin

| Campo | Valor |
|-------|-------|
| Usuário | `admin` |
| Senha | `confeitaria123` |

> ⚠️ **Para mudar a senha:** edite o arquivo `SecurityConfig.java` e altere a linha:
> ```java
> .password(encoder.encode("SUA_NOVA_SENHA"))
> ```

---

## 📁 Estrutura do Projeto

```
confeitaria-projeto-master/
├── Imagens/                         ← Imagens estáticas (logo, hero, galeria)
│   ├── Logo.png
│   ├── Principal.png
│   └── WhatsApp Image *.jpeg        (6 fotos de produtos)
└── confeitaria/
    ├── src/main/java/com/confeitaria/
    │   ├── config/
    │   │   ├── SecurityConfig.java      ← Login e proteção admin
    │   │   ├── WebConfig.java           ← Handlers /uploads/** e /imagens/**
    │   │   └── DataInitializer.java     ← Dados iniciais de exemplo
    │   ├── controller/
    │   │   ├── PublicController.java    ← Páginas públicas
    │   │   ├── AdminController.java     ← Dashboard, galeria, depoimentos
    │   │   ├── RecipeController.java    ← Ingredientes, receitas e endpoint custo-json
    │   │   └── SaleController.java      ← Vendas e relatórios
    │   ├── model/                       ← Entidades JPA
    │   └── repository/                  ← Repositórios Spring Data
    ├── src/main/resources/
    │   ├── templates/
    │   │   ├── public/                  ← Site público (Thymeleaf)
    │   │   └── admin/                   ← Painel admin (Thymeleaf)
    │   ├── static/css/
    │   │   ├── style.css                ← CSS do site público (pastel)
    │   │   └── admin.css                ← CSS do admin
    │   └── application.properties
    └── pom.xml
```

---

## 🌐 Páginas Disponíveis

### Site Público
| URL | Descrição |
|-----|-----------|
| `http://localhost:8080/` | Início (hero e links para as demais páginas) |
| `http://localhost:8080/?ref=CODIGO` | Início com banner de indicação |
| `http://localhost:8080/galeria` | Galeria de fotos |
| `http://localhost:8080/depoimentos` | Depoimentos de clientes |
| `http://localhost:8080/perguntas-frequentes` | Perguntas frequentes |
| `http://localhost:8080/faq` | Alias da página de perguntas frequentes |
| `http://localhost:8080/contato` | Formulário de contato / pedido |

### Painel Admin
| URL | Descrição |
|-----|-----------|
| `/admin` | Dashboard com stats e gráficos |
| `/admin/contatos` | Todos os contatos recebidos + gráficos de origem |
| `/admin/galeria` | Gerenciar fotos e produtos |
| `/admin/depoimentos` | Gerenciar avaliações de clientes |
| `/admin/faqs` | Gerenciar perguntas frequentes |
| `/admin/links-utm` | Gerenciar links UTM e copiar URLs de campanha |
| `/admin/ingredientes` | Cadastrar ingredientes e preços por kg |
| `/admin/receitas` | Receitas com cálculo de custo automático |
| `/admin/vendas` | Registro e análise de vendas com gráficos |
| `/h2-console` | Console do banco de dados H2 |

---

## 💰 Calculadora de Custos

1. Vá em **Admin → Ingredientes** e cadastre cada ingrediente com seu preço por kg
2. Vá em **Admin → Receitas**, crie a receita e defina o rendimento
3. Clique em **Ver Detalhes** e adicione os ingredientes com a quantidade em gramas
4. O sistema calcula automaticamente:
   - **Custo bruto** dos ingredientes
   - **Custo marginal** = custo × 1,05 (+5%)
   - **Preço sugerido** = custo marginal × 3,0 (markup padrão)

### Registro de venda vinculado à receita

Ao registrar uma venda em **Admin → Vendas**, selecione a receita base no campo "Vincular à receita". Os campos **Custo** e **Preço de venda** são preenchidos automaticamente com os valores recomendados (custo de produção completo + preço × 3). Os valores são editáveis após o preenchimento automático.

---

## 📊 Links de Indicação (Referral)

Crie links personalizados no Dashboard:
- `http://localhost:8080/?ref=AMIGA`
- `http://localhost:8080/?ref=INSTAGRAM`

O sistema registra visitas e conversões (formulários enviados) por link.

### Links UTM (campanhas)

No admin, acesse **Links UTM** (`/admin/links-utm`): cadastre nome, URL base (ex.: página de contato) e parâmetros `utm_source`, `utm_medium`, `utm_campaign` (e opcionalmente `utm_term`, `utm_content`). Use **Visualizar** para copiar a URL completa.

Quando alguém entra pelo link, os parâmetros ficam na **sessão** e são reaplicados na navegação até o envio do formulário de contato; os valores são gravados no lead junto com **Referral** (`ref`), se houver.

Exemplo manual na URL:

`http://localhost:8080/contato?utm_source=instagram&utm_medium=social&utm_campaign=pascoa`

---

## 💾 Banco de Dados

O H2 salva dados em arquivo (persistente entre reinicializações):
- Arquivo: `./data/confeitaria.mv.db`
- Console: `http://localhost:8080/h2-console`
  - JDBC URL: `jdbc:h2:file:./data/confeitaria`
  - Usuário: `sa` | Senha: (vazio)

---

## 📦 Imagens

O projeto usa dois diretórios de imagens:

| Diretório | Propriedade | URL servida | Finalidade |
|-----------|-------------|-------------|------------|
| `./uploads/` | `app.upload.dir` | `/uploads/**` | Imagens enviadas via formulário admin |
| `../Imagens/` | `app.images.dir` | `/imagens/**` | Logo, hero e galeria (arquivos estáticos) |

A pasta `Imagens/` fica na **raiz do projeto** (um nível acima de `confeitaria/`).
Para a VM, ajuste `app.images.dir` em `application.properties` conforme o caminho real.

---

## ✏️ Personalização Rápida

| O que mudar | Onde mudar |
|-------------|------------|
| Nome da confeitaria | Nos arquivos HTML em `templates/public/` |
| Cores do site | `static/css/style.css` (variáveis CSS no topo) |
| Dados iniciais (FAQs, ingredientes) | `config/DataInitializer.java` |
| Credenciais admin | `config/SecurityConfig.java` |
| Markup de preço (padrão 3x) | `model/Recipe.java` método `getRecommendedPrice()` |
| Custo marginal (padrão +5%) | `model/Recipe.java` método `getCostTotal()` |
