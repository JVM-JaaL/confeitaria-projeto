# Guia de Deploy — Quero Mais Doces Finos

> Como publicar o sistema a partir do GitHub no Railway (recomendado) ou expô-lo via Cloudflare Tunnel.  
> Última atualização: 2026-06-09

---

## Índice

1. [Pré-requisitos](#1-pré-requisitos)
2. [Preparar o repositório no GitHub](#2-preparar-o-repositório-no-github)
3. [Deploy no Railway](#3-deploy-no-railway)
   - 3.1 Criar o projeto
   - 3.2 Configurar variáveis de ambiente
   - 3.3 Persistência: banco de dados e uploads
   - 3.4 Pasta Imagens/
   - 3.5 Verificar e acessar
4. [Domínio personalizado com Cloudflare](#4-domínio-personalizado-com-cloudflare)
5. [Alternativa: Cloudflare Tunnel (expor VM local)](#5-alternativa-cloudflare-tunnel-expor-vm-local)
6. [Atualizar o sistema depois do deploy](#6-atualizar-o-sistema-depois-do-deploy)
7. [Checklist de segurança para produção](#7-checklist-de-segurança-para-produção)

---

## 1. Pré-requisitos

| Item | Versão mínima | Para quê |
|------|---------------|---------|
| Java | 17 | Compilar e rodar a aplicação |
| Maven | 3.8 | Build do projeto |
| Git | qualquer | Versionar e sincronizar com GitHub |
| Conta GitHub | — | Hospedar o código-fonte |
| Conta Railway | — | Hospedar a aplicação (gratuito até certo uso) |
| Conta Cloudflare | — | Domínio personalizado ou Tunnel (opcional) |

---

## 2. Preparar o repositório no GitHub

### 2.1 Criar o repositório

1. Acesse [github.com](https://github.com) → **New repository**
2. Nome sugerido: `confeitaria-projeto`
3. Visibilidade: **Private** (recomendado, pois o código contém credenciais padrão)
4. Não inicialize com README

### 2.2 Subir o código

Execute a partir da raiz do projeto (`confeitaria-projeto-master/`):

```bash
git init
git add .
git commit -m "versão inicial"
git branch -M main
git remote add origin https://github.com/SEU_USUARIO/confeitaria-projeto.git
git push -u origin main
```

> **Importante:** certifique-se de que a pasta `Imagens/` está incluída no commit. O Railway vai clonar o repositório inteiro e precisa dessa pasta.

### 2.3 Verificar o .gitignore

Confirme que os seguintes diretórios **não** são commitados (dados gerados em runtime):

```
confeitaria/data/          ← banco H2
confeitaria/uploads/       ← imagens enviadas pelo admin
confeitaria/target/        ← build Maven
```

Se não existir um `.gitignore`, crie um na raiz:

```gitignore
confeitaria/data/
confeitaria/uploads/
confeitaria/target/
*.mv.db
*.trace.db
```

---

## 3. Deploy no Railway

O Railway detecta projetos Maven automaticamente e faz o build e deploy sem configuração adicional na maioria dos casos.

### 3.1 Criar o projeto

1. Acesse [railway.app](https://railway.app) e faça login
2. Clique em **New Project** → **Deploy from GitHub repo**
3. Autorize o Railway a acessar seu GitHub
4. Selecione o repositório `confeitaria-projeto`
5. O Railway tentará detectar o projeto automaticamente

### 3.2 Configurar o diretório raiz e o build

O projeto Maven está dentro da subpasta `confeitaria/`. É necessário informar isso ao Railway.

**Opção A — via painel Railway (mais fácil):**

1. Acesse o serviço criado → aba **Settings**
2. Em **Root Directory**, coloque: `confeitaria`
3. Salve e aguarde o redeploy

**Opção B — via arquivo `railway.toml` no repositório:**

Crie o arquivo `confeitaria-projeto-master/railway.toml`:

```toml
[build]
builder = "NIXPACKS"

[deploy]
startCommand = "java -jar target/confeitaria-*.jar"
```

E crie também `confeitaria-projeto-master/confeitaria/system.properties` para garantir o Java 17:

```properties
java.runtime.version=17
```

### 3.3 Persistência: banco de dados e uploads

O Railway usa um **sistema de arquivos efêmero** — qualquer arquivo criado em runtime (banco H2 e uploads) é perdido quando o serviço reinicia ou faz redeploy.

**Solução: volumes persistentes do Railway**

1. No painel do serviço → aba **Volumes**
2. Clique em **Add Volume**
3. Monte path: `/app/data` (para o banco H2)
4. Monte path: `/app/uploads` (para os uploads do admin) — adicione um segundo volume

Após criar os volumes, configure as variáveis de ambiente (próximo passo) para apontar os caminhos corretos.

> Se a perda de dados ao reiniciar for aceitável (ambiente de demonstração), pode pular os volumes.

### 3.4 Configurar variáveis de ambiente

No painel do serviço → aba **Variables**, adicione:

| Variável | Valor sugerido | Descrição |
|----------|---------------|-----------|
| `APP_ADMIN_USERNAME` | `admin` | Usuário do painel (troque em produção) |
| `APP_ADMIN_PASSWORD` | `SuaSenhaForte123` | Senha do painel |
| `APP_UPLOAD_DIR` | `/app/uploads` | Diretório de uploads (use o path do volume) |
| `APP_IMAGES_DIR` | `/app/Imagens` | Pasta de imagens estáticas |
| `SPRING_DATASOURCE_URL` | `jdbc:h2:file:/app/data/confeitaria;DB_CLOSE_ON_EXIT=FALSE` | Banco no volume persistente |
| `SPRING_THYMELEAF_CACHE` | `true` | Ativar cache em produção |
| `SERVER_PORT` | `8080` | Porta (Railway usa a variável `PORT` internamente) |

> **Dica:** O Railway injeta automaticamente a variável `PORT`. O Spring Boot a lê via `SERVER_PORT`. Se o app não subir na porta certa, adicione também `SERVER_PORT=${{PORT}}` nas variáveis.

### 3.5 Pasta Imagens/

A pasta `Imagens/` está na raiz do repositório. Quando o Railway define o **Root Directory** como `confeitaria`, o diretório de trabalho muda para `confeitaria/` e a pasta `Imagens/` fica em `../Imagens` — que é justamente o padrão `app.images.dir=../Imagens`.

Se você montou o volume e quer servir as imagens a partir dele, copie os arquivos para o volume ou suba as imagens pelo painel admin.

Caso queira usar as imagens do repositório, deixe `APP_IMAGES_DIR` apontando para o path absoluto onde o Railway clona o projeto (geralmente `/app/../Imagens` ou `/Imagens` — verifique nos logs de build).

### 3.6 Verificar e acessar

1. Após o deploy, vá em **Settings** → **Networking** → **Generate Domain**
2. O Railway gera uma URL tipo `confeitaria-xyz.railway.app`
3. Acesse `https://confeitaria-xyz.railway.app` — site público
4. Acesse `https://confeitaria-xyz.railway.app/admin` — painel admin

**Verificar logs:**

No painel → aba **Logs** — se aparecer algo como:

```
Started ConfeitariaApplication in 4.2 seconds
```

O deploy foi bem-sucedido.

---

## 4. Domínio personalizado com Cloudflare

Se você tem um domínio próprio (ex: `queromaisdocesfinos.com.br`) gerenciado no Cloudflare, pode apontá-lo para o Railway.

> **Nota:** O Cloudflare Pages e Workers **não suportam aplicações Java/Spring Boot** diretamente. O Cloudflare aqui atua como DNS + proxy/CDN na frente do Railway.

### 4.1 Configurar o domínio no Railway

1. Railway → serviço → **Settings** → **Networking** → **Custom Domain**
2. Digite seu domínio: `www.queromaisdocesfinos.com.br`
3. O Railway exibe um registro CNAME para adicionar no DNS

### 4.2 Configurar o DNS no Cloudflare

1. Acesse o [Cloudflare Dashboard](https://dash.cloudflare.com)
2. Selecione o domínio
3. Vá em **DNS** → **Add record**
4. Tipo: `CNAME`
5. Nome: `www`
6. Destino: o valor fornecido pelo Railway (ex: `confeitaria-xyz.up.railway.app`)
7. Proxy: **Habilitado** (nuvem laranja) — adiciona CDN e HTTPS automático

### 4.3 Redirecionar raiz para www (opcional)

No Cloudflare → **Rules** → **Redirect Rules**:

```
Se: hostname = queromaisdocesfinos.com.br
Redirecionar para: https://www.queromaisdocesfinos.com.br${uri}
Status: 301
```

Adicione também o registro A na raiz:

| Tipo | Nome | Valor | Proxy |
|------|------|-------|-------|
| `A` | `@` | `192.0.2.1` (placeholder) | Habilitado |

> O Cloudflare proxy intercepta antes de chegar ao placeholder — o redirect rule funciona corretamente.

### 4.4 HTTPS

Com o proxy Cloudflare ativo, o HTTPS é gerenciado automaticamente. Em **SSL/TLS** → selecione modo **Full** (ou **Flexible** se o Railway não tiver cert próprio, mas Full é preferível).

---

## 5. Alternativa: Cloudflare Tunnel (expor VM local)

Se preferir rodar o projeto na sua própria VM (ex: VPS, máquina local, Raspberry Pi) em vez do Railway, o **Cloudflare Tunnel** expõe o servidor local pela internet sem abrir portas no roteador.

### 5.1 Instalar o cloudflared na VM

```bash
# Debian/Ubuntu
curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb -o cloudflared.deb
sudo dpkg -i cloudflared.deb

# Ou via script
curl -fsSL https://pkg.cloudflare.com/cloudflare-main.gpg | sudo tee /usr/share/keyrings/cloudflare-main.gpg > /dev/null
```

### 5.2 Autenticar e criar o tunnel

```bash
# Faz login no Cloudflare (abre browser)
cloudflared tunnel login

# Cria o tunnel
cloudflared tunnel create confeitaria

# Anota o ID gerado, ex: abc123-...
```

### 5.3 Configurar o arquivo de tunnel

Crie `~/.cloudflared/config.yml`:

```yaml
tunnel: abc123-SEU-TUNNEL-ID
credentials-file: /home/SEU_USUARIO/.cloudflared/abc123-SEU-TUNNEL-ID.json

ingress:
  - hostname: www.queromaisdocesfinos.com.br
    service: http://localhost:8080
  - service: http_status:404
```

### 5.4 Adicionar o DNS no Cloudflare

```bash
cloudflared tunnel route dns confeitaria www.queromaisdocesfinos.com.br
```

### 5.5 Rodar o Spring Boot e o tunnel

Em dois terminais separados (ou via `systemd`):

```bash
# Terminal 1 — aplicação
cd confeitaria
mvn spring-boot:run

# Terminal 2 — tunnel
cloudflared tunnel run confeitaria
```

### 5.6 Rodar como serviço (inicialização automática)

```bash
sudo cloudflared service install
sudo systemctl enable cloudflared
sudo systemctl start cloudflared
```

Para o Spring Boot, crie `/etc/systemd/system/confeitaria.service`:

```ini
[Unit]
Description=Confeitaria Spring Boot
After=network.target

[Service]
User=SEU_USUARIO
WorkingDirectory=/caminho/para/confeitaria
ExecStart=/usr/bin/java -jar target/confeitaria-*.jar
EnvironmentFile=/etc/confeitaria.env
Restart=always

[Install]
WantedBy=multi-user.target
```

Arquivo `/etc/confeitaria.env`:

```env
APP_ADMIN_PASSWORD=SuaSenhaForte
APP_IMAGES_DIR=/caminho/para/Imagens
APP_UPLOAD_DIR=/caminho/para/uploads
SPRING_THYMELEAF_CACHE=true
```

```bash
sudo systemctl enable confeitaria
sudo systemctl start confeitaria
```

---

## 6. Atualizar o sistema depois do deploy

### Railway (deploy automático)

Por padrão o Railway faz **deploy automático** a cada push na branch `main`:

```bash
# Faça as alterações localmente
git add .
git commit -m "sua alteração"
git push origin main
# → Railway detecta o push e faz redeploy automaticamente
```

Para desativar o deploy automático: Railway → serviço → **Settings** → **Deploy** → desmarcar **Auto Deploy**.

### VM com Cloudflare Tunnel

```bash
# Puxar atualizações
cd confeitaria-projeto-master
git pull origin main

# Recompilar
cd confeitaria
mvn clean package -DskipTests

# Reiniciar o serviço
sudo systemctl restart confeitaria
```

---

## 7. Checklist de segurança para produção

Antes de expor o sistema publicamente:

- [ ] Trocar `APP_ADMIN_PASSWORD` — nunca usar `confeitaria123` em produção
- [ ] Trocar `APP_ADMIN_USERNAME` para algo não óbvio
- [ ] Definir `SPRING_THYMELEAF_CACHE=true` (performance)
- [ ] Verificar se `/h2-console` está acessível apenas com login admin
- [ ] Certificar que o `.gitignore` exclui `data/`, `uploads/` e `*.mv.db`
- [ ] Não commitar o `application.properties` com senhas reais — usar variáveis de ambiente
- [ ] Configurar HTTPS (Railway e Cloudflare fazem isso automaticamente)
- [ ] Considerar limitar o tamanho de upload (`spring.servlet.multipart.max-file-size`) conforme necessidade
- [ ] Revisar a pasta `Imagens/` antes de commitar — não incluir arquivos sensíveis

---

## Resumo rápido

| Objetivo | Solução |
|----------|---------|
| Publicar rapidamente sem servidor próprio | Railway (conecta ao GitHub, build automático) |
| Domínio personalizado com HTTPS | Railway + Cloudflare DNS como proxy |
| Rodar na sua própria VM e expor pela internet | Cloudflare Tunnel + systemd |
| Atualizar após mudanças no código | `git push` (Railway) ou `git pull` + restart (VM) |
