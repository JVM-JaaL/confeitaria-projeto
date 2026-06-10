# Guia de Deploy — Quero Mais Doces Finos

> Como publicar o sistema a partir do GitHub no Railway (recomendado) ou expô-lo via Cloudflare Tunnel.  
> Última atualização: 2026-06-10

---

## Índice

1. [Pré-requisitos](#1-pré-requisitos)
2. [Preparar o repositório no GitHub](#2-preparar-o-repositório-no-github)
3. [Deploy no Railway](#3-deploy-no-railway)
4. [Domínio personalizado com Cloudflare](#4-domínio-personalizado-com-cloudflare)
5. [Alternativa: Cloudflare Tunnel (expor VM local)](#5-alternativa-cloudflare-tunnel-expor-vm-local)
6. [Atualizar o sistema depois do deploy](#6-atualizar-o-sistema-depois-do-deploy)
7. [Checklist de segurança para produção](#7-checklist-de-segurança-para-produção)

---

## Por que usar Dockerfile

Este projeto tem uma estrutura não-convencional: o `pom.xml` está dentro da subpasta `confeitaria/`, não na raiz do repositório. Além disso, a pasta `Imagens/` que contém logo e galeria fica **um nível acima** do projeto Maven. Tanto o Railway quanto o Cloudflare não conseguem detectar isso automaticamente — daí o erro "failed to build image".

A solução é um `Dockerfile` na **raiz do repositório** que cuida de tudo. Esse arquivo já está criado no projeto em `Dockerfile`.

> **Cloudflare Pages e Workers não suportam Java/Spring Boot.** O Cloudflare só entra neste guia como DNS + proxy na frente do Railway, ou como Tunnel para expor uma VM. Para o deploy em si, use Railway.

---

## 1. Pré-requisitos

| Item | Para quê |
|------|---------|
| Conta GitHub | Hospedar o código-fonte |
| Conta Railway | Hospedar a aplicação (tem plano gratuito) |
| Conta Cloudflare | Domínio personalizado ou Tunnel (opcional) |

Não precisa de Java nem Maven instalados localmente para o deploy — o Dockerfile faz o build dentro do container.

---

## 2. Preparar o repositório no GitHub

### 2.1 Verificar os arquivos na raiz

O repositório deve ter estes arquivos na raiz (já criados):

```
confeitaria-projeto-master/
├── Dockerfile          ← instrui o Railway a buildar a aplicação
├── railway.toml        ← configuração do Railway
├── .dockerignore       ← evita copiar arquivos desnecessários para o build
├── Imagens/            ← obrigatório: logo, hero e galeria
└── confeitaria/        ← projeto Maven
```

### 2.2 Verificar o .gitignore

Certifique-se que estes diretórios **não** estão sendo commitados:

```gitignore
confeitaria/data/
confeitaria/uploads/
confeitaria/target/
*.mv.db
*.trace.db
```

### 2.3 Subir o código

Execute a partir da pasta raiz do projeto:

```bash
git add .
git commit -m "add Dockerfile para deploy no Railway"
git push origin main
```

> Se o repositório ainda não existe no GitHub:
> ```bash
> git init
> git add .
> git commit -m "versão inicial"
> git branch -M main
> git remote add origin https://github.com/SEU_USUARIO/confeitaria-projeto.git
> git push -u origin main
> ```

---

## 3. Deploy no Railway

### 3.1 Criar o projeto

1. Acesse [railway.app](https://railway.app) e faça login
2. Clique em **New Project** → **Deploy from GitHub repo**
3. Autorize o Railway a acessar seu GitHub
4. Selecione o repositório `confeitaria-projeto`
5. O Railway detecta o `Dockerfile` na raiz e inicia o build automaticamente

### 3.2 Aguardar o build

O build tem dois estágios (pode levar 3–5 minutos na primeira vez):

1. **Stage 1 — Maven build**: baixa dependências e compila o JAR
2. **Stage 2 — Runtime**: cria a imagem final com o JAR e a pasta `Imagens/`

Acompanhe em tempo real na aba **Logs** do serviço.

### 3.3 Configurar variáveis de ambiente

No painel do serviço → aba **Variables**, adicione:

| Variável | Valor sugerido | Descrição |
|----------|---------------|-----------|
| `APP_ADMIN_USERNAME` | `admin` | Usuário do painel |
| `APP_ADMIN_PASSWORD` | `SuaSenhaForte123` | Senha do painel — **troque antes de publicar** |
| `SPRING_THYMELEAF_CACHE` | `true` | Melhor performance em produção |

As variáveis `APP_IMAGES_DIR` e `APP_UPLOAD_DIR` já estão configuradas no próprio `Dockerfile` (`/app/Imagens` e `/app/uploads`). Só é necessário sobrescrever se quiser apontar para outro caminho.

O Railway injeta `PORT` automaticamente. O `application.properties` já está configurado com `server.port=${PORT:8080}` para lê-la.

### 3.4 Persistência: banco de dados e uploads

O Railway tem **sistema de arquivos efêmero** — banco H2 e uploads são perdidos a cada redeploy.

**Para dados persistentes (produção):**

1. Railway → serviço → aba **Volumes** → **Add Volume**
2. Crie um volume e monte em `/app/data` (banco H2)
3. Crie outro volume e monte em `/app/uploads` (fotos enviadas pelo admin)

Após criar os volumes, adicione a variável:

| Variável | Valor |
|----------|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:h2:file:/app/data/confeitaria;DB_CLOSE_ON_EXIT=FALSE` |

> Se for só para demonstração, pode pular os volumes — o sistema sobe com os dados de exemplo do `DataInitializer` a cada restart.

### 3.5 Gerar a URL pública

1. Railway → serviço → **Settings** → **Networking** → **Generate Domain**
2. Copie a URL gerada (ex: `confeitaria-producao.up.railway.app`)
3. Acesse o site público: `https://confeitaria-producao.up.railway.app`
4. Acesse o admin: `https://confeitaria-producao.up.railway.app/admin`

---

## 4. Domínio personalizado com Cloudflare

Se você tem um domínio próprio (ex: `queromaisdocesfinos.com.br`) no Cloudflare, pode apontá-lo para o Railway. O Cloudflare atua como DNS + proxy/CDN.

### 4.1 Adicionar o domínio no Railway

1. Railway → serviço → **Settings** → **Networking** → **Custom Domain**
2. Digite: `www.queromaisdocesfinos.com.br`
3. O Railway exibe o registro CNAME para adicionar

### 4.2 Configurar o DNS no Cloudflare

1. Acesse o [Cloudflare Dashboard](https://dash.cloudflare.com)
2. Selecione o domínio → **DNS** → **Add record**

| Tipo | Nome | Destino | Proxy |
|------|------|---------|-------|
| `CNAME` | `www` | URL fornecida pelo Railway | Habilitado (nuvem laranja) |

3. Aguarde a propagação do DNS (geralmente < 5 minutos com Cloudflare)

### 4.3 HTTPS

Com o proxy Cloudflare ativo o HTTPS é gerenciado automaticamente. Configure em **SSL/TLS** → modo **Full**.

### 4.4 Redirecionar raiz para www (opcional)

Cloudflare → **Rules** → **Redirect Rules** → **Create rule**:

```
Se: hostname = queromaisdocesfinos.com.br
Ação: redirecionar para https://www.queromaisdocesfinos.com.br${uri}
Status: 301
```

---

## 5. Alternativa: Cloudflare Tunnel (expor VM local)

Se preferir rodar na sua própria VM ou servidor em vez do Railway, o Cloudflare Tunnel expõe o servidor local pela internet sem precisar abrir portas.

### 5.1 Rodar a aplicação na VM

```bash
# Clonar o repositório
git clone https://github.com/SEU_USUARIO/confeitaria-projeto.git
cd confeitaria-projeto/confeitaria

# Compilar e rodar
mvn clean package -DskipTests
java -jar target/confeitaria-1.0.0.jar \
  --app.images.dir=../Imagens \
  --app.admin.password=SuaSenhaForte
```

### 5.2 Instalar o cloudflared

```bash
# Debian/Ubuntu
curl -L https://github.com/cloudflare/cloudflared/releases/latest/download/cloudflared-linux-amd64.deb -o cloudflared.deb
sudo dpkg -i cloudflared.deb
```

### 5.3 Criar e configurar o tunnel

```bash
# Login (abre browser)
cloudflared tunnel login

# Criar o tunnel
cloudflared tunnel create confeitaria

# Anotar o ID gerado (ex: abc123-...)
```

Criar `~/.cloudflared/config.yml`:

```yaml
tunnel: abc123-SEU-TUNNEL-ID
credentials-file: /home/SEU_USUARIO/.cloudflared/abc123-SEU-TUNNEL-ID.json

ingress:
  - hostname: www.queromaisdocesfinos.com.br
    service: http://localhost:8080
  - service: http_status:404
```

### 5.4 Apontar o DNS e iniciar

```bash
# Adiciona CNAME no Cloudflare automaticamente
cloudflared tunnel route dns confeitaria www.queromaisdocesfinos.com.br

# Iniciar o tunnel
cloudflared tunnel run confeitaria
```

### 5.5 Rodar como serviço (inicialização automática)

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
WorkingDirectory=/caminho/para/confeitaria-projeto/confeitaria
ExecStart=/usr/bin/java -jar target/confeitaria-1.0.0.jar
EnvironmentFile=/etc/confeitaria.env
Restart=always

[Install]
WantedBy=multi-user.target
```

`/etc/confeitaria.env`:

```env
APP_ADMIN_PASSWORD=SuaSenhaForte
APP_IMAGES_DIR=/caminho/para/confeitaria-projeto/Imagens
APP_UPLOAD_DIR=/caminho/para/confeitaria-projeto/uploads
SPRING_THYMELEAF_CACHE=true
```

```bash
sudo systemctl enable confeitaria
sudo systemctl start confeitaria
```

---

## 6. Atualizar o sistema depois do deploy

### Railway (deploy automático)

Basta fazer push na branch `main`:

```bash
git add .
git commit -m "sua alteração"
git push origin main
# Railway detecta o push e faz redeploy automaticamente
```

### VM com Cloudflare Tunnel

```bash
cd confeitaria-projeto
git pull origin main
cd confeitaria
mvn clean package -DskipTests
sudo systemctl restart confeitaria
```

---

## 7. Checklist de segurança para produção

- [ ] Trocar `APP_ADMIN_PASSWORD` — nunca usar `confeitaria123`
- [ ] Trocar `APP_ADMIN_USERNAME` para algo não óbvio
- [ ] Definir `SPRING_THYMELEAF_CACHE=true`
- [ ] Confirmar que `data/`, `uploads/` e `*.mv.db` estão no `.gitignore`
- [ ] Não commitar `application.properties` com senhas reais — usar variáveis de ambiente
- [ ] HTTPS habilitado (Railway e Cloudflare fazem isso automaticamente)
- [ ] Adicionar volumes persistentes no Railway se os dados forem importantes

---

## Resumo

| Objetivo | Solução |
|----------|---------|
| Publicar rapidamente sem servidor próprio | Railway + Dockerfile |
| Domínio personalizado com HTTPS | Cloudflare DNS (proxy) na frente do Railway |
| Rodar na sua VM e expor pela internet | Cloudflare Tunnel + systemd |
| Atualizar após mudanças no código | `git push main` (Railway) · `git pull` + restart (VM) |
