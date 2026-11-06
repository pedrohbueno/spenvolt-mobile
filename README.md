# SpenVolt Mobile

App Android do **SpenVolt**: um port do `spenvolt-website` que consome a `spenvolt-api`.

- **Linguagem:** Java + layouts XML (Views)
- **Rede:** Volley, com token JWT no cabeçalho `Authorization`
- **minSdk 24 · targetSdk 36** · Android Gradle Plugin 9.1 (Android Studio Panda 2)

## Como abrir

1. Extraia o zip e, no Android Studio, use **File → Open** e escolha a pasta `spenvolt-mobile`.
2. Espere o Gradle sincronizar.
3. Suba a `spenvolt-api` na porta 8080 do seu PC.
4. Rode o app no emulador (▶).

### Endereço da API

Fica em um lugar só: `app/build.gradle.kts`

```kotlin
buildConfigField("String", "API_URL", "\"http://10.0.2.2:8080\"")
```

- **Emulador:** `10.0.2.2` é o `localhost` do seu PC. Não precisa mudar nada.
- **Celular físico:** troque pelo IP do PC na rede (ex.: `http://192.168.0.10:8080`)
  e adicione o mesmo IP em `app/src/main/res/xml/network_security_config.xml`.

## Estrutura

```
app/src/main/java/com/spenvolt/mobile/
├── api/          Chamadas HTTP (uma classe por recurso da API)
│   ├── ApiClient.java      Volley + token + tratamento de erro (401 → volta ao login)
│   ├── Json.java           Conversão JSON ↔ modelos
│   └── AuthApi, ResidenceApi, DeviceApi, MemberApi, FloorApi, HistoryApi, UserApi, MarketApi
├── model/        Classes de dados (Residence, Device, Member, Floor, Room...)
├── session/      SessionManager: token, nome, e-mail e residência escolhida
├── util/         Format (R$, kWh, datas) e Consumption (regras de consumo do site)
└── ui/
    ├── common/   BaseActivity (barra do topo + menu lateral), BarView, Views
    ├── auth/     Login e Cadastro
    ├── home/     Início
    ├── residence/ Residência (abas), Planta, Criar residência, Configurações
    ├── device/   Lista, detalhe e formulário de dispositivos
    ├── member/   Membros e formulário
    ├── economize/ Busca de ofertas nas lojas
    └── profile/  Perfil (trocar senha, sair)
```

### Como uma tela funciona

Toda tela interna estende `BaseActivity`:

```java
public class DevicesActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setScreen(R.layout.activity_devices, NAV_DEVICES); // barra + menu + layout da tela
    }

    // Chamado quando as residências chegam da API (sempre que a tela volta a aparecer)
    @Override
    protected void onResidencesLoaded(Residence selected) { ... }

    // Chamado quando o usuário troca a residência no menu lateral
    @Override
    protected void onResidenceChanged(Residence selected) { ... }
}
```

E toda chamada à API segue o mesmo formato:

```java
new DeviceApi(this).findByResidence(residenceId, new ApiCallback<List<Device>>() {
    @Override
    public void onSuccess(List<Device> devices) { /* mostra na tela */ }

    @Override
    public void onError(String message) { /* mostra o erro */ }
});
```

## Site → App

| Site (`spenvolt-website`)              | App                                   |
|----------------------------------------|---------------------------------------|
| `(auth)/login`, `(auth)/register`      | `LoginActivity`, `RegisterActivity`   |
| `(app)/page.tsx` (Início)              | `HomeActivity`                        |
| `residences/[id]`                      | `ResidenceActivity` + `FloorPlanSection` |
| `residences/new`                       | `ResidenceFormActivity`               |
| `residences/[id]/settings`             | `ResidenceSettingsActivity`           |
| `devices`, `devices/[id]`, `.../edit`  | `DevicesActivity`, `DeviceDetailActivity`, `DeviceFormActivity` |
| `members`, `members/new`               | `MembersActivity`, `MemberFormActivity` |
| `profile`                              | `ProfileActivity`                     |
| — (novo no app, do mockup)             | `EconomizeActivity` (`/market/search`) |
| `lib/consumption.ts`, `lib/members.ts` | `util/Consumption.java`               |
| `lib/format.ts`                        | `util/Format.java`                    |
| `app/theme/colors.css`                 | `res/values/colors.xml`               |

## Diferenças em relação ao site

- **Planta:** no celular as seções aparecem em uma grade (2 por linha), coloridas pelo consumo,
  em vez do desenho arquitetônico do site. Andares, vagas por tipo, adicionar/remover/renomear
  seção e colocar/tirar aparelhos funcionam igual.
- **Imagens:** a URL da imagem do aparelho e da foto do membro é salva, mas o app mostra a inicial.
  Para exibir as fotos, dá para adicionar a biblioteca Glide depois.
- **Gráfico de 6 meses** e **Alterar foto** do mockup ainda não existem na API.
