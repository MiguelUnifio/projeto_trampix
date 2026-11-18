import json
import urllib.request
import urllib.error

BASE = "http://localhost:8080"

def cpf_valido(base):
    numeros = [int(x) for x in str(base).zfill(9)]
    soma = sum(n * peso for n, peso in zip(numeros, range(10, 1, -1)))
    d1 = 0 if soma % 11 < 2 else 11 - soma % 11
    soma = sum(n * peso for n, peso in zip(numeros + [d1], range(11, 1, -1)))
    d2 = 0 if soma % 11 < 2 else 11 - soma % 11
    return ''.join(map(str, numeros)) + str(d1) + str(d2)

def requisicao(metodo, caminho, dados=None, token=None):
    url = BASE + caminho
    corpo = None if dados is None else json.dumps(dados).encode()
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token

    req = urllib.request.Request(
        url, data=corpo, headers=headers, method=metodo
    )

    try:
        with urllib.request.urlopen(req) as resposta:
            texto = resposta.read().decode()
            return json.loads(texto) if texto else None
    except urllib.error.HTTPError as erro:
        detalhe = erro.read().decode()
        raise Exception(f"HTTP {erro.code}: {detalhe}")

def criar_usuario(nome, email, senha, numero):
    dados = {
        "nome": nome,
        "email": email,
        "senha": senha,
        "documento": cpf_valido(123456780 + numero),
        "tipoDocumento": "CPF",
        "telefone": "14999990000",
        "cep": "19900000",
        "logradouro": "Rua de Teste",
        "numero": str(numero),
        "bairro": "Centro",
        "cidade": "Ourinhos",
        "uf": "SP"
    }

    try:
        usuario = requisicao("POST", "/usuarios", dados)
        print("Usuário criado:", email)
    except Exception as erro:
        print("Cadastro não realizado; tentando login:", email)
        try:
            login = requisicao("POST", "/auth/login", {
                "email": email, "senha": senha
            })
            usuario = login["usuario"]
        except Exception:
            raise Exception(f"Não foi possível criar ou acessar {email}: {erro}")

    login = requisicao("POST", "/auth/login", {
        "email": email, "senha": senha
    })

    return usuario["id"], login["token"]

prestadores = [
    {
        "nome": "Carlos Mendes", "email": "carlos@trampix.com",
        "sub": 1, "cat": 1, "sub2": 2,
        "bio": "Pedreiro profissional, especializado em fundações e alvenaria.",
        "titulo": "Construção de fundações",
        "descricao": "Execução de fundações, sapatas, vigas e pilares.",
        "preco": 1800, "tipo": "POR_TRABALHO",
        "titulo2": "Serviços de alvenaria",
        "descricao2": "Construção de paredes, muros e divisórias.",
        "preco2": 85, "tipo2": "POR_M2"
    },
    {
        "nome": "Roberto Almeida", "email": "roberto@trampix.com",
        "sub": 7, "cat": 2, "sub2": 8,
        "bio": "Pintor residencial e comercial.",
        "titulo": "Pintura de paredes",
        "descricao": "Pintura interna e externa com acabamento profissional.",
        "preco": 25, "tipo": "POR_M2",
        "titulo2": "Aplicação de massa corrida",
        "descricao2": "Preparação e nivelamento de paredes.",
        "preco2": 18, "tipo2": "POR_M2"
    },
    {
        "nome": "André Oliveira", "email": "andre@trampix.com",
        "sub": 12, "cat": 3, "sub2": 13,
        "bio": "Encanador com experiência em instalações hidráulicas.",
        "titulo": "Instalação hidráulica",
        "descricao": "Instalação e manutenção de tubulações de água e esgoto.",
        "preco": 90, "tipo": "POR_HORA",
        "titulo2": "Reparo de vazamentos",
        "descricao2": "Reparo de torneiras, registros, sifões e descargas.",
        "preco2": 120, "tipo2": "POR_TRABALHO"
    },
    {
        "nome": "Fernando Costa", "email": "fernando@trampix.com",
        "sub": 17, "cat": 4, "sub2": 18,
        "bio": "Eletricista especializado em instalações residenciais.",
        "titulo": "Instalação elétrica",
        "descricao": "Instalação e manutenção de fiação elétrica.",
        "preco": 100, "tipo": "POR_HORA",
        "titulo2": "Montagem de quadro elétrico",
        "descricao2": "Instalação de disjuntores e organização de quadros.",
        "preco2": 450, "tipo2": "POR_TRABALHO"
    },
    {
        "nome": "Marcos Ferreira", "email": "marcos@trampix.com",
        "sub": 22, "cat": 5, "sub2": 23,
        "bio": "Marceneiro especializado em móveis e montagem.",
        "titulo": "Reparo de móveis",
        "descricao": "Manutenção de armários, portas e gavetas.",
        "preco": 80, "tipo": "POR_HORA",
        "titulo2": "Montagem de móveis",
        "descricao2": "Montagem e instalação de móveis residenciais.",
        "preco2": 180, "tipo2": "POR_TRABALHO"
    },
    {
        "nome": "Lucas Santos", "email": "lucas@trampix.com",
        "sub": 26, "cat": 6, "sub2": 27,
        "bio": "Profissional de pequenos reparos e instalações.",
        "titulo": "Instalação de suporte de TV",
        "descricao": "Instalação de suportes, quadros e cortinas.",
        "preco": 100, "tipo": "POR_TRABALHO",
        "titulo2": "Pequenos ajustes residenciais",
        "descricao2": "Troca de puxadores, dobradiças e trincos.",
        "preco2": 70, "tipo2": "POR_TRABALHO"
    },
    {
        "nome": "Paulo Ribeiro", "email": "paulo@trampix.com",
        "sub": 30, "cat": 7, "sub2": 31,
        "bio": "Gesseiro especializado em forros e drywall.",
        "titulo": "Instalação de forro",
        "descricao": "Instalação de forros de gesso e acabamento.",
        "preco": 95, "tipo": "POR_M2",
        "titulo2": "Instalação de drywall",
        "descricao2": "Construção de paredes e divisórias em drywall.",
        "preco2": 110, "tipo2": "POR_M2"
    },
    {
        "nome": "Ricardo Lima", "email": "ricardo@trampix.com",
        "sub": 33, "cat": 8, "sub2": 34,
        "bio": "Técnico especializado em climatização.",
        "titulo": "Instalação de ar-condicionado",
        "descricao": "Instalação de equipamentos de ar-condicionado split.",
        "preco": 650, "tipo": "POR_TRABALHO",
        "titulo2": "Limpeza de ar-condicionado",
        "descricao2": "Higienização de filtros e componentes.",
        "preco2": 180, "tipo2": "POR_TRABALHO"
    },
    {
        "nome": "Eduardo Martins", "email": "eduardo@trampix.com",
        "sub": 36, "cat": 9, "sub2": 37,
        "bio": "Vidraceiro e serralheiro com experiência em instalações.",
        "titulo": "Instalação de box de vidro",
        "descricao": "Instalação de box e vidros temperados.",
        "preco": 550, "tipo": "POR_TRABALHO",
        "titulo2": "Fabricação de estruturas metálicas",
        "descricao2": "Fabricação e reparo de grades e corrimãos.",
        "preco2": 250, "tipo2": "POR_TRABALHO"
    }
]

print("\n========== CADASTRANDO PRESTADORES ==========\n")

for i, p in enumerate(prestadores, start=1):
    email = p["email"]
    senha = "TrampixTeste2026!"

    usuario_id, token = criar_usuario(
        p["nome"], email, senha, i
    )

    primeiro_servico = {
        "subcategoriaId": p["sub"],
        "categoriaId": p["cat"],
        "titulo": p["titulo"],
        "descricao": p["descricao"],
        "preco": p["preco"],
        "tipoCobranca": p["tipo"]
    }

    perfil = {
        "biografia": p["bio"],
        "raioAtendimentoKm": 30,
        "servico": primeiro_servico
    }

    try:
        requisicao(
            "POST",
            f"/usuarios/{usuario_id}/prestador",
            perfil,
            token
        )
        print("Prestador e primeiro serviço criados:", email)
    except Exception as erro:
        print("Aviso ao criar perfil:", email, erro)

    segundo_servico = {
        "subcategoriaId": p["sub2"],
        "categoriaId": p["cat"],
        "titulo": p["titulo2"],
        "descricao": p["descricao2"],
        "preco": p["preco2"],
        "tipoCobranca": p["tipo2"]
    }

    try:
        requisicao("POST", "/servicos", segundo_servico, token)
        print("Segundo serviço criado:", p["titulo2"])
    except Exception as erro:
        print("Aviso ao criar segundo serviço:", erro)

print("\n========== CADASTRANDO CLIENTES ==========\n")

clientes = [
    ("Ana Souza", "ana@trampix.com", 20),
    ("Juliana Rocha", "juliana@trampix.com", 21),
    ("Pedro Carvalho", "pedro@trampix.com", 22)
]

for nome, email, numero in clientes:
    try:
        criar_usuario(nome, email, "TrampixTeste2026!", numero)
        print("Cliente disponível:", email)
    except Exception as erro:
        print("Erro no cliente:", email, erro)

print("\n========== FINALIZADO ==========")
print("Consulte GET /servicos para verificar os serviços.")
print("Senha padrão dos usuários de teste: TrampixTeste2026!")
